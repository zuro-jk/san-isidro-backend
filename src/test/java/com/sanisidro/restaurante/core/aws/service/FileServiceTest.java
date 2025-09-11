package com.sanisidro.restaurante.core.aws.service;


import com.sanisidro.restaurante.core.aws.repository.FileMetadataRepository;
import com.sanisidro.restaurante.core.config.FileProperties;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;


class FileServiceTest {

    private FileService fileService;

    @BeforeEach
    void setUp() {
        S3Service s3Service = Mockito.mock(S3Service.class);
        FileMetadataRepository fileRepository = Mockito.mock(FileMetadataRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        FileProperties fileProperties = new FileProperties();
        fileProperties.setAllowedExtensions(java.util.List.of(".mp4"));
        fileProperties.setAllowedFolders(java.util.List.of("test"));
        fileProperties.setMaxSize("10MB");

        fileService = new FileService(s3Service, fileRepository, userRepository, fileProperties);
    }

    @Test
    void testGetMediaDuration() throws Exception {
        try (FileInputStream fis = new FileInputStream("src/test/resources/test-video.mp4")) {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "test-video.mp4",
                    "video/mp4",
                    fis
            );

            // Crear archivo temporal
            File tempFile = File.createTempFile("test-video-", ".mp4");
            tempFile.deleteOnExit(); // para borrar al terminar
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(mockFile.getBytes());
            }

            Double duration = fileService.getMediaDuration(tempFile);

            assertNotNull(duration, "La duración no debería ser nula");
            assertTrue(duration > 0, "La duración debería ser mayor a 0");
            System.out.println("Duración del video: " + duration + " segundos");
        }
    }

}