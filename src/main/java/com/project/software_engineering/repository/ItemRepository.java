package com.project.software_engineering.repository;

import com.project.software_engineering.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
