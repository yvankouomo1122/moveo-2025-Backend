package com.moveo.flight.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.model.OperationLog;

@Service
public class OperationLogService {
    private final OperationLogRepository logRepository;

    public OperationLogService(OperationLogRepository logRepository){
        this.logRepository = logRepository;
    }

    public void logAction(String action, String entity, Long entityId, String username, String details){
        OperationLog log = new OperationLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setUsername(username != null ? username : "SYSTEM");
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
    }
}
