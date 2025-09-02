package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
