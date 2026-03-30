package com.talasila.estimate.repository;

import com.talasila.estimate.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndBusinessId(String name, Long businessId);

    List<Category> findByBusinessId(Long businessId);
}