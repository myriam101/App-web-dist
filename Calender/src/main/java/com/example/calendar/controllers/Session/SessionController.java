package com.example.calendar.controllers.Session;
import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Course.Subject;
import com.example.calendar.entities.Session.Session;
import com.example.calendar.entities.Session.SessionAuditLog;
import com.example.calendar.entities.User.User;
import com.example.calendar.services.Session.SessionServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calendar")
//@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class SessionController {
    @Autowired
    SessionServiceImpl sessionService;

   @PostMapping("/admin/session/create")
   @PreAuthorize("hasAuthority('admin')")
   public ResponseEntity<?> createSessionForClassWithSubject(
           @RequestParam String teacherId,
           @RequestParam String classId,
           @RequestParam String subjectId,
           @RequestBody Session session) {
       try {
           Session createdSession = sessionService.createSessionForClassWithSubject(teacherId,classId, subjectId, session);
           return ResponseEntity.ok(createdSession); // Return the created session
       } catch (RuntimeException e) {
           String errorMessage = e.getMessage(); // Get the error message from the exception
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(errorMessage); // Return the error message in the response body
       }}

    ////to get sujbects of a specified class///
    @GetMapping("/session/subjects/{idclass}")
    public ResponseEntity<List<Subject>> getSubjectsForClassByName(@PathVariable String idclass) {
        try {
            List<Subject> subjects = sessionService.getSubjectsForClassByName(idclass);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or you can return an appropriate error message
        }
    }


    /*@PutMapping("/{universityId}/users")
    public ResponseEntity<Void> assignUsersToUniversity(@PathVariable String universityId, @RequestBody List<String> userIds) {
        sessionService.assignUsersToUniversity(userIds, universityId);
        return ResponseEntity.ok().build();
    }*/
    @GetMapping("/session/allClasses")
    public ResponseEntity<List<Classe>> getAllclasses() {
        try {
            List<Classe> classes = sessionService.classes();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or you can return an appropriate error message
        }
    }

    @GetMapping("/user/session/sessions/{classId}")
    @PreAuthorize("hasAuthority('user')")

    public ResponseEntity<List<Session>> getSessionsByClassId(@PathVariable String classId) {
        List<Session> sessions = sessionService.getSessionsByClassId(classId);
        if (sessions.isEmpty()) {
            return ResponseEntity.noContent().build(); // Or any other appropriate response
        }
        return ResponseEntity.ok(sessions);
    }

    /*@PostMapping("/{subjectIds}/assign/subjects")
    public ResponseEntity<String> assignSubjectsToUser(@PathVariable String subjectIds, @RequestBody List<String> userId) {
        try {
            sessionService.assignTeacherToSubject( subjectIds,userId);
            return ResponseEntity.ok("Subjects assigned successfully to the user.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to assign subjects to the user.");
        }
    }*/

    // Endpoint to fetch users affected by a specified subject
    /*@GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<User>> getUsersBySubjectId(@PathVariable String subjectId) {
            List<User> users = sessionService.getUsersBySubjectId(subjectId);
        return ResponseEntity.ok(users);
    }*/
    @DeleteMapping("/admin/session/delete/{sessionId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        try {
            sessionService.deleteSession(sessionId);
            return ResponseEntity.ok("Session deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting session");
        }
    }
    @PutMapping("/user/session/cancel/{sessionId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<?> cancelSession(@PathVariable String sessionId) {
        try {
            Session canceledSession = sessionService.cancelSession(sessionId);
            return ResponseEntity.ok(canceledSession);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error canceling session");
        }
    }
    @GetMapping("/session/getNbrHremaining/{classId}/{subjectId}")
    public long getRemainingHoursForSubject(@PathVariable String classId, @PathVariable String subjectId) {
        return sessionService.getRemainingHoursForSubject(classId, subjectId);
    }
    @GetMapping("/session/sessionsTeacher/{teacherId}")
    public ResponseEntity<List<Session>> getSessionsByTeacherId(@PathVariable String teacherId) {
        List<Session> sessions = sessionService.getSessionsByTeacherId(teacherId);
        return ResponseEntity.ok(sessions);
    }
    @PutMapping("/session/updateSession/{sessionId}")
    public ResponseEntity<?> updateSession(@PathVariable String sessionId,

                                           @RequestBody Session updatedSession) {
        try {
            Session updated = sessionService.updateSession(sessionId,updatedSession);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/session/sessions/{sessionId}/reschedule")
    public ResponseEntity<?> rescheduleSessionForNextWeekk(@PathVariable String sessionId) {
        Session newSession = sessionService.rescheduleSessionForNextWeek(sessionId);
        if (newSession != null) {
            return ResponseEntity.ok(newSession); // Return the newly created session
        } else {
            return ResponseEntity.notFound().build(); // Session not found or not canceled
        }
    }

    @PostMapping("/admin/session/sessions/{classId}/auto/{weekStartDate}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> generateSessionsForClassAndWeekk(@PathVariable String classId, @PathVariable LocalDate weekStartDate) {
        List<Session> newSessions = sessionService.generateSessionsForClassAndWeek(classId,weekStartDate);
        if (newSessions != null) {
            return ResponseEntity.ok(newSessions); // Return the newly created session
        } else {
            return ResponseEntity.notFound().build(); // Session not found or not canceled
        }
    }
    @GetMapping("/admin/session/Log")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<SessionAuditLog>> getAllSessionAuditLogs() {
        List<SessionAuditLog> sessionAuditLogs = sessionService.getAllSessionAuditLogs();
        return ResponseEntity.ok(sessionAuditLogs);
    }
    @GetMapping("/admin/session/percentage-by-month/{teacherId}")
    @PreAuthorize("hasAuthority('admin')")
    public Map<String, Integer> getPercentageCatchUpByMonthForTeacher(@PathVariable String teacherId) {
        return sessionService.getPercentageCatchUpByMonthForTeacher(teacherId);
    }
    @GetMapping("/session/users/teachers")
    public ResponseEntity<List<User>> getTeachers() {
        try {
            List<User> users = sessionService.listAllTeachers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Or you can return an appropriate error message
        }    }
    @GetMapping("/session/teacher/{teacherId}/session-status")
    public ResponseEntity<String> getTeacherSessionStatus(@PathVariable String teacherId) {
        String sessionStatus = sessionService.getTeacherSessionStatus(teacherId);
        return ResponseEntity.ok(sessionStatus);
    }
    @PostMapping("/admin/session/teacher/exam-sessions/{classId}/{weekStartDate}/{examDuration}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Session>> generateExamSessionsForTeacher(@PathVariable String classId,
                                                                        @PathVariable LocalDate weekStartDate,
                                                                        @PathVariable Duration examDuration) {
        try {
            List<Session> generatedSessions = sessionService.generateExamSessionsForClassAndWeek(classId, weekStartDate, examDuration);
            return ResponseEntity.ok(generatedSessions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/user/session/this-week/{classId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<String> checkExamsThisWeekForClass(@PathVariable String classId) {
        String reminderMessage = sessionService.checkExamsThisWeekForClass(classId);
        return new ResponseEntity<>(reminderMessage, HttpStatus.OK);
    }




}
