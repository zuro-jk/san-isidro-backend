package com.sanisidro.restaurante.core.aws.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import javax.imageio.ImageIO;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sanisidro.restaurante.core.aws.dto.response.DeleteResponse;
import com.sanisidro.restaurante.core.aws.exception.AccessDeniedException;
import com.sanisidro.restaurante.core.aws.exception.FileNotFoundException;
import com.sanisidro.restaurante.core.aws.exception.FileUploadException;
import com.sanisidro.restaurante.core.aws.model.FileDetail;
import com.sanisidro.restaurante.core.aws.model.FileMetadata;
import com.sanisidro.restaurante.core.aws.repository.FileMetadataRepository;
import com.sanisidro.restaurante.core.config.FileProperties;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;
    private final FileMetadataRepository fileRepository;
    private final UserRepository userRepository;
    private final FileProperties fileProperties;

    private long getMaxFileSize() {
        String size = fileProperties.getMaxSize().toUpperCase();
        if (size.endsWith("MB"))
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        if (size.endsWith("KB"))
            return Long.parseLong(size.replace("KB", "")) * 1024;
        if (size.endsWith("B"))
            return Long.parseLong(size.replace("B", ""));
        return 5 * 1024 * 1024;
    }

    public FileMetadata uploadFile(MultipartFile file, String folder) {
        validateFolder(folder);
        validateFile(file);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            String key = s3Service.uploadFile(tempFile, folder, file.getContentType());
            String url = s3Service.getFileUrl(key);
            User currentUser = getCurrentUser();

            FileMetadata metadata = FileMetadata.builder()
                    .originalName(file.getOriginalFilename())
                    .key(key)
                    .folder(folder)
                    .url(url)
                    .uploadDate(LocalDateTime.now())
                    .uploadedBy(currentUser)
                    .build();

            Integer width = null;
            Integer height = null;
            BigDecimal duration = null;

            String checksum = calculateChecksum(new FileInputStream(tempFile));

            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image")) {
                    BufferedImage image = ImageIO.read(tempFile);
                    if (image != null) {
                        width = image.getWidth();
                        height = image.getHeight();
                    }
                } else if (contentType.startsWith("video") || contentType.startsWith("audio")) {
                    Double dur = getMediaDuration(tempFile);
                    if (dur != null) {
                        duration = BigDecimal.valueOf(dur).setScale(3, RoundingMode.HALF_UP);
                    }
                }
            }

            FileDetail detail = FileDetail.builder()
                    .contentType(contentType)
                    .size(file.getSize())
                    .width(width)
                    .height(height)
                    .duration(duration)
                    .checksum(checksum)
                    .file(metadata)
                    .build();

            metadata.setDetails(detail);

            return fileRepository.save(metadata);

        } catch (IOException e) {
            throw new FileUploadException("Error procesando el archivo: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists())
                tempFile.delete();
        }
    }

    public DeleteResponse deleteFile(Long fileId) {
        FileMetadata file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Archivo no encontrado con id: " + fileId));
        User currentUser = getCurrentUser();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !file.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No tiene permisos para eliminar este archivo");
        }

        s3Service.deleteFile(file.getKey());
        fileRepository.delete(file);

        return new DeleteResponse(file.getId(), currentUser.getId(), currentUser.getUsername());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty())
            throw new FileUploadException("El archivo está vacío");
        if (file.getSize() > getMaxFileSize())
            throw new FileUploadException(
                    "El archivo excede el tamaño máximo permitido de " + fileProperties.getMaxSize());

        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (originalName == null
                || fileProperties.getAllowedExtensions().stream().noneMatch(originalName.toLowerCase()::endsWith)) {
            throw new FileUploadException("Extensión no permitida: " + originalName);
        }

        if (contentType == null ||
                (!contentType.startsWith("image") &&
                        !contentType.startsWith("video") &&
                        !contentType.startsWith("audio") &&
                        !contentType.equals("application/pdf"))) {
            throw new FileUploadException("Tipo de archivo no permitido: " + contentType);
        }
    }

    private void validateFolder(String folder) {
        if (!fileProperties.getAllowedFolders().contains(folder))
            throw new FileUploadException("Carpeta no permitida: " + folder);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    Double getMediaDuration(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.readLine();
            }
            process.waitFor();

            if (output != null)
                return Double.parseDouble(output.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String calculateChecksum(InputStream is) throws IOException {
        try (is) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no soportado", e);
        }
    }

    public String getFileUrl(Long fileId) {
        FileMetadata file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Archivo no encontrado con id: " + fileId));
        return file.getUrl();
    }

    @Transactional
    public void deleteFileByUrl(String url) {
        if (url == null || url.isBlank())
            return;

        fileRepository.findByUrl(url).ifPresent(file -> {
            try {
                s3Service.deleteFile(file.getKey());
                fileRepository.delete(file);
            } catch (Exception e) {
                throw new RuntimeException("Error eliminando archivo de AWS: " + e.getMessage());
            }
        });
    }

}
