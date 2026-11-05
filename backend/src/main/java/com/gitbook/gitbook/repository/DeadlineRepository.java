package com.gitbook.gitbook.repository;

import com.gitbook.gitbook.models.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeadlineRepository extends JpaRepository<Deadline, UUID> {
}
