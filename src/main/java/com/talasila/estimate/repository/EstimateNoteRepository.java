package com.talasila.estimate.repository;

import com.talasila.estimate.model.EstimateNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstimateNoteRepository extends JpaRepository<EstimateNote, Long> {
}