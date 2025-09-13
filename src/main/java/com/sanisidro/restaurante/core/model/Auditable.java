package com.sanisidro.restaurante.core.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.sanisidro.restaurante.features.customers.model.Customer;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.sanisidro.restaurante.core.audit.listener.AuditableEntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class, AuditableEntityListener.class })
public class Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Transient
    private Map<String, Object> oldSnapshot;

    @PostLoad
    public void captureOldSnapshot() {
        this.oldSnapshot = AuditableEntityListener.snapshotEntity(this);
    }

}
