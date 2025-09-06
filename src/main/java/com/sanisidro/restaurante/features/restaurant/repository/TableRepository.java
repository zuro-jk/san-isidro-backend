package com.sanisidro.restaurante.features.restaurant.repository;

import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
}
