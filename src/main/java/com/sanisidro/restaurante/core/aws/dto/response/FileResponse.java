package com.sanisidro.restaurante.core.aws.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String folder;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime uploadDate;

    private Long size;
    private String contentType;
}