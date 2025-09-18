package com.sanisidro.restaurante.core.aws.repository;

import com.sanisidro.restaurante.core.aws.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository  extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByKey(String key);
}
