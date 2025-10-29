package com.sanisidro.restaurante.core.aws.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.core.aws.model.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByKey(String key);

    Optional<FileMetadata> findByUrl(String url);
}
