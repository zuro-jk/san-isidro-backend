package com.sanisidro.restaurante.features.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.products.model.ComboProductItem;

public interface ComboProductItemRepository extends JpaRepository<ComboProductItem, Long> {

}
