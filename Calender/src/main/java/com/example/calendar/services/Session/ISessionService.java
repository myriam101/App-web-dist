package com.example.calendar.services.Session;


import com.example.calendar.entities.Session.Session;

import java.time.LocalDate;
import java.util.List;

public interface ISessionService {

   public List<Session> getSessionsForWeekAndClass(String classId, LocalDate startDate, LocalDate endDate);

    public void deleteSession(String sessionId);
    public List<Session> getSessionsByTeacherId(String teacherId);
}
