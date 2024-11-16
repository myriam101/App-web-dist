package com.example.calendar.repositories.Session;


import com.example.calendar.entities.Session.SessionAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionAuditLogRepository  extends MongoRepository<SessionAuditLog,String> {
}
