package com.example.calendar.services.Session;
import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Course.Subject;
import com.example.calendar.entities.Leave.Leave;
import com.example.calendar.entities.Leave.StatusLeave;
import com.example.calendar.entities.Modules.Modules;
import com.example.calendar.entities.Session.Session;
import com.example.calendar.entities.Session.SessionAuditLog;
import com.example.calendar.entities.Session.TypeSession;
import com.example.calendar.entities.User.ERole;
import com.example.calendar.entities.User.Role;
import com.example.calendar.entities.User.User;
import com.example.calendar.entities.specialities.Speciality;
import com.example.calendar.repositories.Course.SubjectRepo;
import com.example.calendar.repositories.Leave.LeaveRepository;
import com.example.calendar.repositories.Session.SessionAuditLogRepository;
import com.example.calendar.repositories.Session.SessionRepository;
import com.example.calendar.repositories.classe.ClassRepository;
import com.example.calendar.repositories.universities.UniversityRepository;
import com.example.calendar.repositories.user.RoleRepository;
import com.example.calendar.repositories.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class SessionServiceImpl implements ISessionService {

    SessionRepository sessionRepository;
    ClassRepository ur;
    UniversityRepository universityRepository;
    UserRepository userRepository;
    SubjectRepo subjectRepo;
    LeaveRepository leaveRepository;
    SessionAuditLogRepository sessionAuditLogRepository;

    RoleRepository roleRepository;

    // Method to log session actions
    private void logAction(String action, String sessionId, String description) {
        SessionAuditLog logEntry = new SessionAuditLog();
        logEntry.setActionLog(action);
        logEntry.setSessionIdLog(sessionId);
        logEntry.setDescriptionLog(description);
        logEntry.setTimestampLog(LocalDateTime.now());
        sessionAuditLogRepository.save(logEntry);
    }

    public Session createSessionForClassWithSubject(String teacherId, String classId, String subjectId, Session session) {
        // Retrieve the class by its ID
        Classe classe = ur.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        User popo = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        // Retrieve the speciality of the class
        Speciality speciality = classe.getSpeciality();

        // Get the modules associated with the speciality
        List<Modules> modules = speciality.getModules();

        // Iterate through the modules to find the subjects
        Subject selectedSubject = null;
        for (Modules module : modules) {
            List<Subject> subjects = module.getSubjectList();
            for (Subject subject : subjects) {
                if (subject.getSubjectId().equals(subjectId)) {
                    selectedSubject = subject;
                    break;
                }
            }
            if (selectedSubject != null) {
                break;
            }
        }

        if (selectedSubject == null) {
            throw new RuntimeException("Subject not found in the class's speciality modules.");
        }


        // Check if there are existing sessions for the same class, day, and time range
        List<Session> existingClassSessions = sessionRepository.findSessionsByClasseClassIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(
                classe, session.getSessionDate(), session.getSessionStartHour(), session.getSessionEndHour());

// Check if any sessions exist in the result
        if (!existingClassSessions.isEmpty()) {
            throw new RuntimeException("The class " + classe.getClassName() + " already has a session scheduled at the same time on the same day.");
        }


        // Get existing sessions of the subject for the specified class
        List<Session> existingSessions = sessionRepository.findSessionsByClasseClassIdAndSubjectSubjectId(classe, selectedSubject);

        if (session.getTypeSession() != TypeSession.Exam) {
            // Calculate the total duration of existing sessions for the specified class and subject
            Duration totalDuration = Duration.ZERO;
            for (Session existingSession : existingSessions) {
                if (existingSession.getTypeSession() != TypeSession.Exam && existingSession.getTypeSession() != TypeSession.Canceled) {
                    Duration sessionDuration = Duration.between(existingSession.getSessionStartHour(), existingSession.getSessionEndHour());
                    totalDuration = totalDuration.plus(sessionDuration);
                }
            }


            // Calculate the duration of the new session being added
            Duration newSessionDuration = Duration.between(session.getSessionStartHour(), session.getSessionEndHour());

            // Calculate the remaining hours for the subject
            long remainingHoursBefore = selectedSubject.getNbrHours() - totalDuration.toHours();
            long remainingHoursAfter = selectedSubject.getNbrHours() - totalDuration.toHours() - newSessionDuration.toHours();


            // Check if there are remaining hours for the subject
            if (remainingHoursBefore <= 0) {
                throw new RuntimeException("There are no remaining hours for the subject" + selectedSubject.getTitleSubject() + ".");
            } else if (remainingHoursAfter < 0) {
                throw new RuntimeException("you should add no more than " + remainingHoursBefore + "hours for the subject " + selectedSubject.getTitleSubject());
            }
        }
        // Check if there are existing sessions for the same teacher, day, and time range
        List<Session> existingTeacherSession = sessionRepository.findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(popo, session.getSessionDate(), session.getSessionStartHour(), session.getSessionEndHour());

        if (!existingTeacherSession.isEmpty()) {
            throw new RuntimeException("That teacher " + popo.getUsername() + "already has a session scheduled for those hours.");
        }

        // Check if the teacher has a leave on the session date
        List<Leave> leaves = leaveRepository.findByInstructorAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(popo, session.getSessionDate(), session.getSessionDate());
        List<Leave> acceptedLeaves = leaves.stream().filter(leave -> leave.getStatusLeave() == StatusLeave.Accepted).collect(Collectors.toList());

        if (!acceptedLeaves.isEmpty()) {
            for (Leave leave : acceptedLeaves) {
                LocalDate leaveStartDate = leave.getLeaveStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate leaveEndDate = leave.getLeaveEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (session.getSessionDate().isEqual(leaveStartDate) || session.getSessionDate().isEqual(leaveEndDate) || (session.getSessionDate().isAfter(leaveStartDate) && session.getSessionDate().isBefore(leaveEndDate))) {
                    throw new RuntimeException("Cannot add session for teacher " + popo.getUsername() + ". They have a leave from " + leaveStartDate + " to " + leaveEndDate + ".");
                }
            }

        }


        // Set the class and subject for the session
        session.setClasse(classe);
        session.setSubject(selectedSubject);
        session.setTeacher(popo);
        // Save the session details for the audit log description
        String sessionDescription = "Class: " + session.getClasse().getClassName() + ", type: " + session.getTypeSession() + ", Date: "
                + session.getSessionDate() + ", Start Time: " + session.getSessionStartHour() + ", End Time: "
                + session.getSessionEndHour() + ", Link: " + session.getLinkLiveS() + ", Teacher: " + session.getTeacher().getUsername();

        Session ses = sessionRepository.save(session);
        // Log the action
        logAction("Added", ses.getIdSession(), sessionDescription);

        // Save the session and return the saved session
        return ses;
    }

    public List<Subject> getSubjectsForClassByName(String idclass) {
        // Retrieve the class by its name
        Classe classe = ur.findById(idclass).orElseThrow(null);

        // Retrieve the speciality associated with the class
        Speciality speciality = classe.getSpeciality();

        // Get the modules associated with the speciality
        List<Modules> modules = speciality.getModules();

        // Extract and collect all subjects from the modules
        List<Subject> subjects = modules.stream()
                .flatMap(module -> module.getSubjectList().stream())
                .collect(Collectors.toList());

        return subjects;
    }

    public List<Session> getSessionsByClassId(String classId) {
        // Use the session repository to find sessions by class ID
        return sessionRepository.findByClasseClassId(classId);
    }

    public List<Classe> classes() {

        return ur.findAll();
    }

   /* public void assignUsersToUniversity(List<String> userIds, String universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        // Get the existing users assigned to the university
        List<User> existingUsers = university.getUsers();

        // Find and add new users to the list of existing users
        List<User> newUsers = userRepository.findAllById(userIds);
        existingUsers.addAll(newUsers);

        // Set the updated list of users to the university
        university.setUsers(existingUsers);

        // Save the updated university entity
        universityRepository.save(university);
    }*/

    /*public void assignTeacherToSubject(String subjectId, List<String> userId) {
        // Retrieve the user from the database
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get the existing subjects of the user
        List<User> existingteachers = subject.getTeachers();

        // Load the new subjects based on their IDs
        List<User> newteachers = userRepository.findAllById(userId);

        // Add only the new subjects that are not already associated with the user
        for (User newteacher : newteachers) {
            boolean alreadyExists = existingteachers.stream()
                    .anyMatch(user -> user.getId().equals(newteacher.getId()));
            if (!alreadyExists) {
                existingteachers.add(newteacher);
            }
        }

        // Save the changes
        subjectRepo.save(subject);
    }
*/
    // Method to fetch users affected by a specified subject
   /* public List<User> getUsersBySubjectId(String subjectId) {
        List<User> users = new ArrayList<>();

        // Find the subject by ID
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + subjectId));

        // Get the list of users associated with the subject
        if (subject != null) {
            users = subject.getTeachers();
        }

        return users;
    }*/

    @Override
    public List<Session> getSessionsForWeekAndClass(String classId, LocalDate startDate, LocalDate endDate) {
        // Retrieve sessions for the specified class and date range
        List<Session> sessions = sessionRepository.findSessionsByClasseClassIdAndSessionDateBetween(classId, startDate, endDate);
        return sessions;
    }

    @Override
    public void deleteSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + sessionId));
        // Save the session details for the audit log description
        String sessionDescription = "Session ID: " + session.getIdSession() + ",for the class :" + session.getClasse().getClassName() + ", type :" + session.getTypeSession() + ", Date: "
                + session.getSessionDate() + ", Start Time: " + session.getSessionStartHour() + ", End Time: "
                + session.getSessionEndHour() + ", Link: " + session.getLinkLiveS() + ", Teacher: " + session.getTeacher().getUsername();

        sessionRepository.delete(session);
        // Log the action with session details in the description
        logAction("Deleted", sessionId, sessionDescription);
    }

    public Session cancelSession(String sessionId) {
        // Retrieve the session by its ID
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Change the session type to "Canceled"
        session.setTypeSession(TypeSession.Canceled);

        // Save the updated session
        return sessionRepository.save(session);
    }

    public long getRemainingHoursForSubject(String classId, String subjectId) {
        Classe classe = ur.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        long totalHours = subject.getNbrHours();

        List<Session> sessions = sessionRepository.findSessionsByClasseClassIdAndSubjectSubjectId(classe, subject);

        // Calculate the total duration of regular sessions
        Duration totalDuration = Duration.ZERO;
        for (Session existingSession : sessions) {
            if (existingSession.getTypeSession() != TypeSession.Exam && existingSession.getTypeSession() != TypeSession.Canceled) {
                Duration sessionDuration = Duration.between(existingSession.getSessionStartHour(), existingSession.getSessionEndHour());
                totalDuration = totalDuration.plus(sessionDuration);
            }
        }

        // Calculate remaining hours
        long remainingHours = totalHours - totalDuration.toHours();
        return Math.max(0, remainingHours); // Ensure remaining hours are non-negative
    }

    @Override
    public List<Session> getSessionsByTeacherId(String teacherId) {
        return sessionRepository.findByTeacherId(teacherId);
    }
    

    public Session updateSession(String sessionId, Session updatedSession) {
        Session existingSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Classe classe = existingSession.getClasse();
        User teacher = existingSession.getTeacher();



        // Check if there are existing sessions for the same class, day, and time range
        List<Session> existingClassSessions = sessionRepository.findSessionsByClasseClassIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(
                classe, updatedSession.getSessionDate(), updatedSession.getSessionStartHour(), updatedSession.getSessionEndHour());

        List<Session> existingClassSessions2 = sessionRepository.findSessionsByClasseClassIdAndSessionDateAndSessionStartHourIsGreaterThanEqualAndSessionEndHourIsLessThanEqual(
                classe, updatedSession.getSessionDate(), updatedSession.getSessionStartHour(), updatedSession.getSessionEndHour());

// Check if any sessions exist in the result
        if (!existingClassSessions.isEmpty()||!existingClassSessions2.isEmpty()) {
            throw new RuntimeException("The class " + classe.getClassName() + " already has a session scheduled at the same time on the same day.");
        }
        // Check if there are existing sessions for the same teacher, day, and time range
        List<Session> existingTeacherSession = sessionRepository.findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(teacher, updatedSession.getSessionDate(), updatedSession.getSessionStartHour(), updatedSession.getSessionEndHour());

        if (!existingTeacherSession.isEmpty()) {
            throw new RuntimeException("That teacher " + teacher.getUsername() + "already has a session scheduled for those hours.");
        }

        List<Leave> leaves = leaveRepository.findByInstructorAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(teacher, updatedSession.getSessionDate(), updatedSession.getSessionDate());
        List<Leave> acceptedLeaves = leaves.stream().filter(leave -> leave.getStatusLeave() == StatusLeave.Accepted).collect(Collectors.toList());
        if (!acceptedLeaves.isEmpty()) {
            for (Leave leave : acceptedLeaves) {
                LocalDate leaveStartDate = leave.getLeaveStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate leaveEndDate = leave.getLeaveEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (updatedSession.getSessionDate().isEqual(leaveStartDate) || updatedSession.getSessionDate().isEqual(leaveEndDate) || (updatedSession.getSessionDate().isAfter(leaveStartDate) && updatedSession.getSessionDate().isBefore(leaveEndDate))) {
                    throw new RuntimeException("Cannot update session for teacher " + teacher.getUsername() + ". They have a leave from " + leaveStartDate + " to " + leaveEndDate + ".");
                }
            }
        }


        // Update session details
        existingSession.setSessionDate(updatedSession.getSessionDate());
        existingSession.setSessionStartHour(updatedSession.getSessionStartHour());
        existingSession.setSessionEndHour(updatedSession.getSessionEndHour());
        existingSession.setLinkLiveS(updatedSession.getLinkLiveS());
        existingSession.setTypeSession(updatedSession.getTypeSession());

        // Save and return the updated session
        return sessionRepository.save(existingSession);
    }

    public Session rescheduleSessionForNextWeek(String sessionId) {
        // Retrieve the session by its ID
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null && session.getTypeSession().equals(TypeSession.Canceled)) {

            User teacher = userRepository.findById(session.getTeacher().getId()).orElseThrow(null);
            Classe classe = ur.findById(session.getClasse().getClassId()).orElseThrow(null);
            Subject subject = subjectRepo.findById(session.getSubject().getSubjectId()).orElseThrow(null);


            LocalDate sessionDate = session.getSessionDate();

            LocalDate nextMonday = sessionDate.plusDays(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

            LocalDate currentDate = nextMonday;
            while (true) {
                if (isTimeSlotAvailable2(currentDate, LocalTime.of(9, 0), LocalTime.of(12, 0))) {

                    return createCatchUpSession(currentDate, LocalTime.of(9, 0), LocalTime.of(12, 0), session, teacher, classe, subject);
                }

                if (isTimeSlotAvailable2(currentDate, LocalTime.of(13, 0), LocalTime.of(16, 0))) {

                    return createCatchUpSession(currentDate, LocalTime.of(13, 0), LocalTime.of(16, 0), session, teacher, classe, subject);
                }

                // Move to the next day
                currentDate = currentDate.plusDays(1);
            }
        }

        return null; // Return null if session not found or not canceled
    }

    private boolean isTimeSlotAvailable(LocalDate currentDate, LocalTime startTime, LocalTime endTime) {
        // Check if there are any sessions occupying the given time slot on the given date
        List<Session> sessions = sessionRepository.findBySessionDateAndSessionStartHourAndSessionEndHour(currentDate, startTime, endTime);
        return sessions.isEmpty();
    }


    private Session createCatchUpSession(LocalDate sessionDate, LocalTime newSessionStartTime, LocalTime newSessionEndTime, Session originalSession, User teacher, Classe classe, Subject subject) {
        // Create a new session with Catchup type
        Session newSession = new Session();
        newSession.setSessionDate(sessionDate);
        newSession.setTypeSession(TypeSession.CatchUp);
        newSession.setSessionStartHour(newSessionStartTime);
        newSession.setSessionEndHour(newSessionEndTime);
        newSession.setTeacher(teacher);
        newSession.setClasse(classe);
        newSession.setSubject(subject);

        return sessionRepository.save(newSession);
    }

    ////////////auto

    public List<Session> generateSessionsForClassAndWeek(String classId, LocalDate weekStartDate) {
        // Retrieve the class by its ID
        Classe classe = ur.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Retrieve the speciality of the class
        Speciality speciality = classe.getSpeciality();

        // Get the modules associated with the speciality
        List<Modules> modules = speciality.getModules();

        // Initialize list to hold generated sessions
        List<Session> generatedSessions = new ArrayList<>();

        // Initialize map to keep track of scheduled subjects for each day of the week
        Map<LocalDate, Set<Subject>> scheduledSubjectsPerDay = new HashMap<>();

        // Loop through the days of the week
        LocalDate currentDate = weekStartDate;
        while (currentDate.isBefore(weekStartDate.plusWeeks(1))) {
            // Exclude Sundays
            if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Initialize morning slot availability
            boolean morningSlotAvailable = true;
            // Initialize afternoon slot availability
            boolean afternoonSlotAvailable = true;

            // Loop through modules
            for (Modules module : modules) {
                // Loop through subjects in the module
                for (Subject subject : module.getSubjectList()) {
                    // Check if the subject has already been added for this week
                    if (isSubjectAlreadyScheduledForWeek(subject, scheduledSubjectsPerDay)) {
                        continue; // Skip this subject if it's already scheduled for the week
                    }

                    LocalTime sessionStartTime;
                    LocalTime sessionEndTime;

                    if (morningSlotAvailable) {
                        sessionStartTime = LocalTime.of(9, 0); // 9:00 AM
                        sessionEndTime = LocalTime.of(12, 0); // 12:00 PM
                    } else if (afternoonSlotAvailable) {
                        sessionStartTime = LocalTime.of(13, 0); // 1:00 PM
                        sessionEndTime = LocalTime.of(16, 0); // 4:00 PM
                    } else {
                        // If both slots are filled, move to the next day
                        morningSlotAvailable = true;
                        afternoonSlotAvailable = true;
                        break;
                    }

                    // Check if the time slot is available
                    if (isTimeSlotAvailable2(currentDate, sessionStartTime, sessionEndTime)) {
                        // Find available teacher and create session
                        Set<User> teacherSet = new HashSet<>(subject.getTeachers());
                        User availableTeacher = findAvailableTeacher(teacherSet, currentDate);
                        if (availableTeacher != null) {
                            generatedSessions.add(createSession(currentDate, sessionStartTime, sessionEndTime, classe, subject, availableTeacher));
                            scheduledSubjectsPerDay.computeIfAbsent(currentDate, k -> new HashSet<>()).add(subject);
                            // Update slot availability
                            if (morningSlotAvailable && sessionStartTime.equals(LocalTime.of(9, 0))) {
                                morningSlotAvailable = false;
                            } else if (afternoonSlotAvailable && sessionStartTime.equals(LocalTime.of(13, 0))) {
                                afternoonSlotAvailable = false;
                            }
                        }
                    }
                }
            }

            // Move to the next day
            currentDate = currentDate.plusDays(1);
        }

// Save the generated sessions
        return sessionRepository.saveAll(generatedSessions);
    }

    private boolean isTimeSlotAvailable2(LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Check for existing sessions within the specified time slot
        List<Session> existingSessions = sessionRepository.findSessionsBySessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(date, startTime, endTime);
        return existingSessions.isEmpty();
    }

    private boolean isSubjectAlreadyScheduledForWeek(Subject subject, Map<LocalDate, Set<Subject>> scheduledSubjectsPerDay) {
        // Check if the subject has already been scheduled for the week
        return scheduledSubjectsPerDay.values().stream().anyMatch(subjects -> subjects.contains(subject));
    }

    private User findAvailableTeacher(Set<User> teachers, LocalDate date) {
        // Check for leaves for each teacher associated with the subject
        for (User teacher : teachers) {
            // Check if the teacher has a leave on the session date
            List<Leave> leaves = leaveRepository.findByInstructorAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(teacher, date, date);
            if (!leaves.isEmpty()) {
                continue; // Skip this teacher if they have a leave
            }

            // Check for existing sessions for the same teacher, day, and time range
            List<Session> existingTeacherSessions = sessionRepository.findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsGreaterThanEqual(teacher, date, LocalTime.of(9, 0), LocalTime.of(16, 0));
            if (existingTeacherSessions.isEmpty()) {
                return teacher;
            }
        }
        return null;
    }


    private Session createSession(LocalDate sessionDate, LocalTime sessionStartTime, LocalTime sessionEndTime, Classe classe, Subject subject, User teacher) {
        Session session = new Session();
        session.setSessionDate(sessionDate);
        session.setSessionStartHour(sessionStartTime);
        session.setSessionEndHour(sessionEndTime);
        session.setClasse(classe);
        session.setSubject(subject);
        session.setTeacher(teacher);
        session.setTypeSession(TypeSession.Fixed);

        return session;
    }


    public List<SessionAuditLog> getAllSessionAuditLogs() {
        return sessionAuditLogRepository.findAll();
    }

    public Map<String, Integer> getPercentageCatchUpByMonthForTeacher(String teacherId) {
        List<Session> allSessionsForTeacher = sessionRepository.findByTeacherId(teacherId);

        Map<Month, Long> allSessionsByMonth = new HashMap<>();
        for (Month month : Month.values()) {
            allSessionsByMonth.put(month, 0L);
        }

        // Count the number of sessions in each month
        allSessionsForTeacher.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSessionDate().getMonth(),
                        Collectors.counting()
                ))
                .forEach(allSessionsByMonth::put);

        List<Session> catchUpSessionsForTeacher = allSessionsForTeacher.stream()
                .filter(session -> session.getTypeSession() == TypeSession.CatchUp)
                .collect(Collectors.toList());

        Map<Month, Long> catchUpSessionsByMonth = new HashMap<>();
        for (Month month : Month.values()) {
            catchUpSessionsByMonth.put(month, 0L);
        }

        catchUpSessionsForTeacher.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSessionDate().getMonth(),
                        Collectors.counting()
                ))
                .forEach(catchUpSessionsByMonth::put);

        Map<String, Integer> percentageCatchUpByMonth = new HashMap<>();
        allSessionsByMonth.forEach((month, count) -> {
            long catchUpSessionsCount = catchUpSessionsByMonth.getOrDefault(month, 0L); // Get count of catch-up sessions for the month
            if (count == 0) {
                percentageCatchUpByMonth.put(month.toString(), 0); // Set percentage to 0 if there are no sessions in the month
            } else {
                int percentage = (int) Math.round((double) catchUpSessionsCount / count * 100);
                percentageCatchUpByMonth.put(month.toString(), percentage);
            }
        });

        return percentageCatchUpByMonth;
    }

    //TO LIST THE TEACHERS
    public List<User> listAllTeachers() {
        Optional<Role> roleOptional = roleRepository.findByName(ERole.ROLE_TEACHER);
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            return userRepository.findByRole(role.getId());
        } else {
            return Collections.emptyList();
        }
    }

    public String getTeacherSessionStatus(String teacherId) {
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        LocalDate currentDate = LocalDate.now();
        List<Session> sessions = sessionRepository.findByTeacherIdAndSessionDate(teacherId, currentDate);

        // Check if the teacher has a session scheduled at the current time
        for (Session session : sessions) {
            LocalTime sessionStartTime = session.getSessionStartHour();
            LocalTime sessionEndTime = session.getSessionEndHour();
            if (currentTime.isAfter(sessionStartTime) && currentTime.isBefore(sessionEndTime)) {
                return "In session right now with the class " + session.getClasse().getClassName();
            }
        }

        Long timeUntilNextSession = null;
        for (Session session : sessions) {
            LocalTime sessionStartTime = session.getSessionStartHour();
            if (currentTime.isBefore(sessionStartTime)) {
                timeUntilNextSession = Duration.between(currentTime, sessionStartTime).toMinutes();
                break;
            }
        }

        if (timeUntilNextSession != null) {
            return timeUntilNextSession + " minutes until next session";
        } else {
            return "No sessions scheduled ahead for today ";
        }
    }

    private boolean isSubjectAlreadyScheduledForWeekExam(Subject subject, Map<LocalDate, Set<Session>> scheduledSessionsPerDay, LocalDate weekStartDate) {
        for (Set<Session> sessions : scheduledSessionsPerDay.values()) {
            for (Session session : sessions) {
                if (session.getSubject().equals(subject) && session.getTypeSession() == TypeSession.Exam &&
                        isSameWeek(session.getSessionDate(), weekStartDate)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSameWeek(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2.minusDays(1)) && date1.isBefore(date2.plusWeeks(1));
    }

    public List<Session> generateExamSessionsForClassAndWeek(String classId, LocalDate weekStartDate, Duration examDuration) {
        // Retrieve the class by its ID
        Classe classe = ur.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Retrieve the speciality of the class
        Speciality speciality = classe.getSpeciality();

        // Get the modules associated with the speciality
        List<Modules> modules = speciality.getModules();

        // Initialize list to hold generated sessions
        List<Session> generatedSessions = new ArrayList<>();

        // Initialize map to keep track of scheduled sessions for each day of the week
        Map<LocalDate, Set<Session>> scheduledSessionsPerDay = new HashMap<>();

        // Loop through the days of the week
        LocalDate currentDate = weekStartDate;
        while (currentDate.isBefore(weekStartDate.plusWeeks(1))) {
            // Exclude Sundays
            if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Initialize morning slot availability
            boolean morningSlotAvailable = true;
            // Initialize afternoon slot availability
            boolean afternoonSlotAvailable = true;

            // Loop through modules
            for (Modules module : modules) {
                // Loop through subjects in the module
                for (Subject subject : module.getSubjectList()) {
                    // Check if the subject already has an exam session scheduled for the week
                    if (isSubjectAlreadyScheduledForWeekExam(subject, scheduledSessionsPerDay, weekStartDate)) {
                        continue; // Skip this subject if it already has an exam session scheduled for the week
                    }

                    LocalTime sessionStartTime;
                    LocalTime sessionEndTime;

                    if (morningSlotAvailable) {
                        sessionStartTime = LocalTime.of(9, 0); // 9:00 AM
                        sessionEndTime = sessionStartTime.plus(examDuration);
                    } else if (afternoonSlotAvailable) {
                        sessionStartTime = LocalTime.of(13, 0); // 1:00 PM
                        sessionEndTime = sessionStartTime.plus(examDuration);
                    } else {
                        // If both slots are filled, move to the next day
                        morningSlotAvailable = true;
                        afternoonSlotAvailable = true;
                        break;
                    }

                    // Check if the time slot is available
                    if (isTimeSlotAvailable2(currentDate, sessionStartTime, sessionEndTime)) {
                        // Find available teacher and create session
                        Set<User> teacherSet = new HashSet<>(subject.getTeachers());
                        User availableTeacher = findAvailableTeacherExam(teacherSet, currentDate);
                        if (availableTeacher != null) {
                            Session session = createSessionExam(currentDate, sessionStartTime, sessionEndTime, classe, subject, availableTeacher);
                            generatedSessions.add(session);
                            scheduledSessionsPerDay.computeIfAbsent(currentDate, k -> new HashSet<>()).add(session);
                            // Update slot availability
                            if (morningSlotAvailable && sessionStartTime.equals(LocalTime.of(9, 0))) {
                                morningSlotAvailable = false;
                            } else if (afternoonSlotAvailable && sessionStartTime.equals(LocalTime.of(13, 0))) {
                                afternoonSlotAvailable = false;
                            }
                        }
                    }
                }
            }

            // Move to the next day
            currentDate = currentDate.plusDays(1);
        }

        // Save the generated sessions
        return sessionRepository.saveAll(generatedSessions);
    }


    private Session createSessionExam(LocalDate sessionDate, LocalTime sessionStartTime, LocalTime sessionEndTime, Classe classe, Subject subject, User teacher) {
        Session session = new Session();
        session.setSessionDate(sessionDate);
        session.setSessionStartHour(sessionStartTime);
        session.setSessionEndHour(sessionEndTime);
        session.setClasse(classe);
        session.setSubject(subject);
        session.setTeacher(teacher);
        session.setTypeSession(TypeSession.Exam);

        return session;
    }
    private User findAvailableTeacherExam(Set<User> teachers, LocalDate date) {
        // Check for leaves for each teacher associated with the subject
        for (User teacher : teachers) {
            // Check if the teacher has a leave on the session date
            List<Leave> leaves = leaveRepository.findByInstructorAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(teacher, date, date);
            if (!leaves.isEmpty()) {
                continue; // Skip this teacher if they have a leave
            }

            // Check for existing sessions for the same teacher, day, and time range
            List<Session> existingTeacherSessions = sessionRepository.findSessionsByTeacherIdAndSessionDateAndSessionStartHourIsLessThanEqualAndSessionEndHourIsLessThanEqual(teacher, date, LocalTime.of(9, 0), LocalTime.of(16, 0));
            if (existingTeacherSessions.isEmpty()) {
                return teacher; // Return this teacher if available
            }
        }
        return null; // Return null if no available teacher found
    }
    public String checkExamsThisWeekForClass(String classId) {
        // Retrieve the class by its ID
        Classe classe = ur.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the start date of the current week (Monday)
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Calculate the end date of the current week (Sunday)
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Initialize counter for the number of exams
        int numberOfExams = 0;

        // Iterate over the days of the week to count the exams
        for (LocalDate date = startOfWeek; date.isBefore(endOfWeek.plusDays(1)); date = date.plusDays(1)) {
            // Query the database to check for exam sessions for the given class and date
            List<Session> examSessions = sessionRepository.findBySessionDateAndClasseAndTypeSession(date, classId, TypeSession.Exam);

            // Increment the number of exams by the count of exam sessions for the current day
            numberOfExams += examSessions.size();
        }

        // Construct the reminder message
        String startDate = startOfWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String reminderMessage = String.format("Reminder for class %s: You have %d exam(s) this week starting from %s.", classe.getClassName(), numberOfExams, startDate);

        // Return the reminder message
        return reminderMessage;
    }


}
