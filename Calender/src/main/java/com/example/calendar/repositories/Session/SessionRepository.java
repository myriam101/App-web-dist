package com.example.calendar.repositories.Session;
import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Course.Subject;
import com.example.calendar.entities.Session.TypeSession;
import com.example.calendar.entities.Session.Session;
import com.example.calendar.entities.User.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SessionRepository extends MongoRepository<Session,String> {

    List<Session> findSessionByClasseAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(
            Classe classe, LocalDate sessionDate, LocalTime sessionEndHour, LocalTime sessionStartHour);
    List<Session> findByClasseClassId(String classId);


    List<Session> findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(  User teacher, LocalDate sessionDate, LocalTime sessionStartHour, LocalTime sessionEndHour);
    List<Session> findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(User teacher, LocalDate sessionDate, LocalTime sessionStartHour, LocalTime sessionEndHour);

    List<Session> findSessionsByClasseClassIdAndSessionDateBetween(String classId, LocalDate startDate, LocalDate endDate);


    List<Session> findSessionsByClasseClassIdAndSubjectSubjectId(Classe classe, Subject subject);



    List<Session> findByTeacherId(String teacherId);



    List<Session> findSessionsByTeacherId(String id);



    List<Session> findBySessionDateAndSessionStartHourAndSessionEndHour(LocalDate currentDate, LocalTime startTime, LocalTime endTime);



    List<Session> findSessionsBySessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(LocalDate currentDate, LocalTime of, LocalTime of1);
    List<Session> findSessionsBySessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(LocalDate currentDate, LocalTime of, LocalTime of1);


    List<Session> findByTeacherIdAndSessionDate(String teacherId, LocalDate currentDate);

    List<Session> findSessionsByClasseClassIdAndSessionDateAndSessionStartHourIsGreaterThanEqualAndSessionEndHourIsLessThanEqual(Classe classe, LocalDate sessionDate, LocalTime sessionStartHour, LocalTime sessionEndHour);

    List<Session> findSessionsByClasseClassIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(Classe classe, LocalDate sessionDate, LocalTime sessionStartHour, LocalTime sessionEndHour);

    List<Session> findBySessionDateBetweenAndClasseAndTypeSession(LocalDate startOfWeek, LocalDate endOfWeek, Classe classe, TypeSession typeSession);

    List<Session> findBySessionDateAndClasseAndTypeSession(LocalDate date, String classId, TypeSession typeSession);
}
