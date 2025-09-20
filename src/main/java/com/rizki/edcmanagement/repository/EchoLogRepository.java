package com.rizki.edcmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rizki.edcmanagement.model.EchoLog;

@Repository
public interface EchoLogRepository extends JpaRepository<EchoLog, Long>, JpaSpecificationExecutor<EchoLog> {
}