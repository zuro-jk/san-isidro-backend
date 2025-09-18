package com.sanisidro.restaurante.core.aws.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "file_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentType;
    private Long size;

    private Integer width;
    private Integer height;

    @Column(precision = 10, scale = 3)
    private BigDecimal duration;

    private String checksum;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    private FileMetadata file;


}
