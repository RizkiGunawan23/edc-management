package com.rizki.edcmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rizki.edcmanagement.model.TerminalEDC;

@Repository
public interface TerminalEDCRepository extends JpaRepository<TerminalEDC, String> {
    boolean existsByIpAddress(String ipAddress);
}