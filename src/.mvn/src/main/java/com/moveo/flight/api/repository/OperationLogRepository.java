package com.moveo.flight.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moveo.flight.api.model.OperationLog;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long>{
    
}
