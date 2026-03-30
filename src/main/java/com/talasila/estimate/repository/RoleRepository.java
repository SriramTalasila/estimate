package com.talasila.estimate.repository;

import java.util.Optional;

import com.talasila.estimate.model.ERole;
import com.talasila.estimate.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByName(ERole name);
}
