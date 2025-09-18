package com.sanisidro.restaurante.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {
    private List<String> allowedExtensions;
    private List<String> allowedFolders;
    private String maxSize;
}
