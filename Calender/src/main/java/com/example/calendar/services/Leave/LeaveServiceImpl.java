package com.example.calendar.services.Leave;
import com.example.calendar.entities.Leave.Leave;
import com.example.calendar.entities.Leave.LeaveType;
import com.example.calendar.entities.Leave.StatusLeave;
import com.example.calendar.entities.Leave.TimeLeave;
import com.example.calendar.entities.Session.Session;
import com.example.calendar.entities.Session.TypeSession;
import com.example.calendar.entities.User.User;
import com.example.calendar.repositories.Leave.LeaveRepository;
import com.example.calendar.repositories.Session.SessionRepository;
import com.example.calendar.repositories.user.UserRepository;
import com.example.calendar.services.Session.SessionServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.text.DateFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class LeaveServiceImpl implements ILeaveService {
    @Autowired
    private MongoTemplate mongoTemplate;
    LeaveRepository leaveRepository;
    UserRepository userRepository;
    SessionRepository sessionRepository;
    private SessionServiceImpl sessionService;



    public long getTotalTimeTakenByInstructor(String instructorId) {
        List<Leave> leaves = leaveRepository.findLeavesByInstructorId(instructorId);

        long totalHoursTaken = 0;
        for (Leave leave : leaves) {
            if (leave != null && leave.getStatusLeave() != StatusLeave.Refused) {
                totalHoursTaken += leave.getNumberHoursLeave();
            }
        }
        return totalHoursTaken;
    }

   /* public int calculdaysleft(String instructorId) {
        // max allowed days for paid leave
        int maxAllowedDays = 30;

        // calculate total days taken by the instructor
        int totalDaysTaken = getTotalDaysTakenByInstructor(instructorId);

        // calculate remaining days
        int remainingDays = maxAllowedDays - totalDaysTaken;

        return remainingDays;
    }*/
    @Override
    public int[] calculDaysLeft(String instructorId) {
        int maxAllowedHours = 720;

        long totalHoursTaken = getTotalTimeTakenByInstructor(instructorId);

        int remainingHours = maxAllowedHours - (int) totalHoursTaken;

        int remainingDays = remainingHours / 24;
        int remainingHoursInDay = remainingHours % 24;

        return new int[]{remainingDays, remainingHoursInDay};
    }

    //condition acceptation automatique
   /* public boolean shouldAcceptLeave(Leave leave) {
        Date leaveStartDate = leave.getLeaveStartDate();
        Date leaveEndDate = leave.getLeaveEndDate();

        List<Leave> acceptedByDaysLeaves = leaveRepository.findLeavesByStatusLeaveAndTimeLeave(StatusLeave.Accepted, TimeLeave.byDays);

        Set<String> uniqueInstructorIds = new HashSet<>();
        for (Leave acceptedLeave : acceptedByDaysLeaves) {
            Date acceptedLeaveStartDate = acceptedLeave.getLeaveStartDate();
            Date acceptedLeaveEndDate = acceptedLeave.getLeaveEndDate();

            // Check if the leave overlaps with the current accepted leave
            if ((leaveStartDate.before(acceptedLeaveEndDate) || leaveStartDate.equals(acceptedLeaveEndDate)) &&
                    (leaveEndDate.after(acceptedLeaveStartDate) || leaveEndDate.equals(acceptedLeaveStartDate))) {
                uniqueInstructorIds.add(acceptedLeave.getInstructor().getId());
            }
        }

        return true;


    }*/

public  Leave emergencyAdd(Leave leave,String instructorId){

    User instructor = userRepository.findById(instructorId).orElse(null);
    if (instructor != null) {


        // number of days leave
        long diffInMillies = Math.abs(leave.getLeaveEndDate().getTime() - leave.getLeaveStartDate().getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        leave.setNumberDaysLeave((int) diffInDays);

        // days to hours
        long numberHoursLeave = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        leave.setNumberHoursLeave(numberHoursLeave);

        int[] remainingTime = calculDaysLeft(instructorId);
        int remainingDays = remainingTime[0];
        int remainingHours = remainingTime[1];
        int totalRemainingHours = remainingDays * 24 + remainingHours;

            leave.setInstructor(instructor);
            leave.setStatusLeave(StatusLeave.Pending);
            leave.setTimeLeave(TimeLeave.byDays);
            leave.setLeaveType(LeaveType.Emergency);
            return leaveRepository.save(leave);

    }else return null;
}
    @Override
    public Leave addLeave(Leave leave, String instructorId) {


        User instructor = userRepository.findById(instructorId).orElse(null);
        if (instructor != null) {


            // number of days leave
            long diffInMillies = Math.abs(leave.getLeaveEndDate().getTime() - leave.getLeaveStartDate().getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            leave.setNumberDaysLeave((int) diffInDays);

            // days to hours
            long numberHoursLeave = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            leave.setNumberHoursLeave(numberHoursLeave);

            int[] remainingTime = calculDaysLeft(instructorId);
            int remainingDays = remainingTime[0];
            int remainingHours = remainingTime[1];
            int totalRemainingHours = remainingDays * 24 + remainingHours;

            if (totalRemainingHours == 0) {
                return null;
            } else if (numberHoursLeave > totalRemainingHours) {
                throw new RuntimeException("Error: The number of days being added exceeds the remaining days.");
            } else {
            leave.setInstructor(instructor);
            leave.setTimeLeave(TimeLeave.byDays);

                // Check if the leave should be accepted
               /* if (shouldAcceptLeave(leave)) {
                    leave.setStatusLeave(StatusLeave.Accepted);
                } else {}*/
                    leave.setStatusLeave(StatusLeave.Pending);

            return leaveRepository.save(leave);
        }

    }else {
            return null;
        }
   }

    public Leave addLeaveForSpecificHours(Leave leave, String instructorId) {
        User instructor = userRepository.findById(instructorId).orElse(null);
        if (instructor != null) {


            Duration duration = Duration.between(leave.getLeaveStartHour(), leave.getLeaveEndHour());
            long totalHours = duration.toHours();
            leave.setNumberHoursLeave(totalHours);

            // Calculate number of days and remaining hours before adding
            int[] remainingTimeBeforeAdd = calculDaysLeft(instructorId);
            int remainingDaysBeforeAdd = remainingTimeBeforeAdd[0];
            int remainingHoursBeforeAdd = remainingTimeBeforeAdd[1];
            int totalRemainingHoursBeforeAdd = remainingDaysBeforeAdd * 24 + remainingHoursBeforeAdd;

            // Calculate number of days and remaining hours after adding
            long numberDaysLeave = totalHours / 24;
            int remainingHoursAfterAdd = (int) (totalHours % 24);
            int remainingDaysAfterAdd = remainingDaysBeforeAdd;
            if (remainingHoursAfterAdd > remainingHoursBeforeAdd) {
                remainingDaysAfterAdd--;
                remainingHoursAfterAdd = remainingHoursAfterAdd + 24;
            }

            if (remainingDaysAfterAdd < 0) {
                throw new RuntimeException("Error: The number of hours being added exceeds the remaining hours.");
            } else {
                leave.setInstructor(instructor);
                leave.setStatusLeave(StatusLeave.Pending);
                leave.setTimeLeave(TimeLeave.byHours);
                leave.setNumberDaysLeave((int) numberDaysLeave);
                leave.setNumberHoursLeave(totalHours);

                /////////////////////////////////// to cancel sessions//////////////////////////////
                LocalDate localDate = leave.getLeaveStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String id = leave.getInstructor().getId();

                List<Session> instructorSessions = sessionRepository.findSessionsByTeacherId(id);

                for (Session session : instructorSessions) {
                    if (session.getSessionDate().equals(localDate) &&
                            session.getSessionStartHour().equals(leave.getLeaveStartHour()) &&
                            session.getSessionEndHour().equals(leave.getLeaveEndHour())) {
                        session.setTypeSession(TypeSession.Canceled);
                        sessionRepository.save(session);
                        sessionService.rescheduleSessionForNextWeek(session.getIdSession()); // Call the method from SessionService

                    }
                }

                return leaveRepository.save(leave);


            }
        } else {
            return null;
        }
    }






    @Override
    public Leave updateLeave(Leave leave, String leaveId, String instructorId) {
        Leave existingLeave = leaveRepository.findById(leaveId).orElse(null);

        if (existingLeave != null && existingLeave.getInstructor().getId().equals(instructorId)) {
            // Get the existing leave's number of days and convert it to hours
            int existingDays = existingLeave.getNumberDaysLeave();
            long existingHours = existingDays * 24;

            int[] remainingTimeBeforeUpdate = calculDaysLeft(instructorId);
            int remainingDaysBeforeUpdate = remainingTimeBeforeUpdate[0];
            int remainingHoursBeforeUpdate = remainingTimeBeforeUpdate[1];
            long totalRemainingHoursBeforeUpdate = remainingDaysBeforeUpdate * 24 + remainingHoursBeforeUpdate;

            // number of days leave
            long diffInMillies = Math.abs(leave.getLeaveEndDate().getTime() - leave.getLeaveStartDate().getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            // days to hours
            long numberHoursLeave = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            int[] remainingTimeAfterUpdate = calculDaysLeft(instructorId);
            int remainingDaysAfterUpdate = remainingTimeAfterUpdate[0];
            int remainingHoursAfterUpdate = remainingTimeAfterUpdate[1];
            long totalRemainingHoursAfterUpdate = remainingDaysAfterUpdate * 24 + remainingHoursAfterUpdate;

            long remainingHours = totalRemainingHoursBeforeUpdate - numberHoursLeave + existingHours;

            if (remainingHours <= 0) {
                throw new IllegalArgumentException("Leave cannot be updated. Exceeds remaining hours or days limit.");
            } else {
               // boolean shouldAccept = shouldAcceptLeave(leave);

                // Set the status based on the shouldAccept
              //  existingLeave.setStatusLeave(shouldAccept ? StatusLeave.Accepted : StatusLeave.Pending);

                existingLeave.setStatusLeave(StatusLeave.Pending);
                existingLeave.setLeaveType(leave.getLeaveType());
                existingLeave.setLeaveStartDate(leave.getLeaveStartDate());
                existingLeave.setLeaveEndDate(leave.getLeaveEndDate());
                existingLeave.setLeaveDescription(leave.getLeaveDescription());
                existingLeave.setTimeLeave(TimeLeave.byDays);
                existingLeave.setNumberHoursLeave(numberHoursLeave);
                existingLeave.setNumberDaysLeave((int) diffInDays);

                return leaveRepository.save(existingLeave);
            }
        } else {
            throw new IllegalArgumentException("Leave update failed.");
        }
    }


    public Leave updateLeavebyHours(Leave leave, String leaveId, String instructorId) {
        Leave existingLeave = leaveRepository.findById(leaveId).orElse(null);

        if (existingLeave != null && existingLeave.getInstructor().getId().equals(instructorId)) {
            // Get the existing leave's number of hours.
            long existingHours = existingLeave.getNumberHoursLeave();

            int[] remainingTimeBeforeUpdate = calculDaysLeft(instructorId);
            int remainingDaysBeforeUpdate = remainingTimeBeforeUpdate[0];
            int remainingHoursBeforeUpdate = remainingTimeBeforeUpdate[1];
            Duration duration = Duration.between(leave.getLeaveStartHour(), leave.getLeaveEndHour());
            long totalHours = duration.toHours();

            // Calculate remaining hours before update
            long totRemainingHoursBeforeUpdate = remainingDaysBeforeUpdate * 24 + remainingHoursBeforeUpdate;

            // Calculate total remaining hours after update
            long totalRemainingHoursAfterUpdate = totRemainingHoursBeforeUpdate + existingHours- totalHours;

            if (totalRemainingHoursAfterUpdate < 0) {
                throw new RuntimeException("Leave cannot be updated. Exceeds remaining hours or days limit.");
            } else {
                // Check if the total remaining hours after update is less than remaining hours before update

                    existingLeave.setLeaveType(leave.getLeaveType());
                    existingLeave.setLeaveStartDate(leave.getLeaveStartDate());
                    existingLeave.setLeaveDescription(leave.getLeaveDescription());
                    existingLeave.setStatusLeave(StatusLeave.Pending);
                    existingLeave.setNumberHoursLeave(totalHours);
                    existingLeave.setTimeLeave(TimeLeave.byHours);
                    existingLeave.setLeaveStartHour(leave.getLeaveStartHour());
                    existingLeave.setLeaveEndHour(leave.getLeaveEndHour());

                    // Save the updated leave object
                    return leaveRepository.save(existingLeave);
            }
        } else {
            throw new RuntimeException("Leave update failed.");
        }
    }






    @Override
    public void deleteLeave(String  leaveId,String instructorId) {
        Optional<Leave> leaveOptional = leaveRepository.findById(leaveId);
        if (leaveOptional.isPresent()) {
            Leave leave = leaveOptional.get();
            if (leave.getInstructor().getId().equals(instructorId)) {
                leaveRepository.delete(leave);
            } else {
                throw new IllegalStateException("Instructor is not authorized to delete this leave.");
            }
        } else {
            throw new IllegalArgumentException("Leave with ID " + leaveId + " not found.");
        }

    }

    public int getNumberOfPeopleTakingLeaveToday() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime(); // Start of today

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.SECOND, -1); // Move to end of today
        Date endDate = calendar.getTime(); // End of today

        List<Leave> leavesToday = leaveRepository.findLeavesByLeaveStartDateBeforeAndLeaveEndDateAfter(startDate, endDate);

        return leavesToday.size();
    }

    //to show on the admin dashboard
    @Override
    public List<Leave> allLeave() {

        return leaveRepository.findAll();

    }
    public List<Leave> allLeaveByHours() {

       return leaveRepository.findLeavesByTimeLeave(TimeLeave.byHours);
    }
    public List<Leave> allLeaveByDays() {

        return leaveRepository.findLeavesByTimeLeave(TimeLeave.byDays);

    }


    @Override
    public int NbrPeopLeaveToday() {
        int numberOfPeople = getNumberOfPeopleTakingLeaveToday();
        return numberOfPeople;
    }


    @Override
    public List<Leave> leaveBystatus(String status)
    {
       if (status != null) {
            return leaveRepository.findByStatusLeave(status);
        } else {
            return leaveRepository.findAll();
        }
    }
    @Override
    public Leave acceptLeave(String leaveId) {
        Leave leave = leaveRepository.findById(leaveId).orElse(null);

        if (leave != null) {
            leave.setStatusLeave(StatusLeave.Accepted);

            return leaveRepository.save(leave);
        } else {
            return null;
        }
    }

    @Override
    public Leave refuseLeave(String leaveId) {
        Leave leave = leaveRepository.findById(leaveId).orElse(null);

        if (leave != null) {
            leave.setStatusLeave(StatusLeave.Refused);

            return leaveRepository.save(leave);
        } else {
            return null;
        }    }
    @Override
    public int countPendingLeaves() {
        return leaveRepository.countByStatusLeave(StatusLeave.Pending);
    }

    @Override
    public List<Leave> getLeaveByInstructorId(String instructorId) {
        return leaveRepository.findLeavesByInstructorId(instructorId);
    }
    // Method to get leaves by instructor ID and time leave type
    public List<Leave> getLeaveInsByHours(String instructorId) {
        return leaveRepository.findLeavesByInstructorIdAndTimeLeave(instructorId, TimeLeave.byHours);
    }
    public List<Leave> getLeaveInsByDays(String instructorId) {
        return leaveRepository.findLeavesByInstructorIdAndTimeLeave(instructorId, TimeLeave.byDays);
    }
    public Map<LeaveType, Integer> calculateLeaveTypePercentage() {
        List<Leave> allLeaves = leaveRepository.findAll();
        Map<LeaveType, Integer> leaveTypeCounts = new HashMap<>();

        for (Leave leave : allLeaves) {
            LeaveType leaveType = leave.getLeaveType();
            leaveTypeCounts.put(leaveType, leaveTypeCounts.getOrDefault(leaveType, 0) + 1);
        }

        // Calculate total leave count
        int totalLeaveCount = allLeaves.size();

        // Calculate percentage for each leave type
        Map<LeaveType, Integer> leaveTypePercentage = new HashMap<>();
        for (Map.Entry<LeaveType, Integer> entry : leaveTypeCounts.entrySet()) {
            int percentage = (int) Math.round(((double) entry.getValue() / totalLeaveCount) * 100);
            leaveTypePercentage.put(entry.getKey(), percentage);
        }

        return leaveTypePercentage;
    }
    public Map<String, Integer> getPercentageLeavesByMonth() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project()
                        .andExpression("month(leaveStartDate)").as("month"),
                Aggregation.group("month").count().as("count")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "leave", Map.class);

        // map to store the count for each month
        Map<Integer, Integer> monthCountMap = new HashMap<>();
        int totalLeaves = 0; // total leaves across all months

        for (Map monthCount : results.getMappedResults()) {
            int month = (int) monthCount.get("_id");
            int count = (int) monthCount.get("count");
            monthCountMap.put(month, count);
            totalLeaves += count; // accumulate total leaves
        }

        // map to store the percentage for each month
        Map<String, Integer> percentageLeavesByMonth = new HashMap<>();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] monthNames = dfs.getMonths();

        for (int i = 1; i <= 12; i++) {
            int count = monthCountMap.getOrDefault(i, 0); // Get the count for the month, default to 0 if not present
            if (totalLeaves == 0) {
                percentageLeavesByMonth.put(monthNames[i - 1], 0); // Handle division by zero
            } else {
                double percentage = ((double) count / totalLeaves) * 100;
                int roundedPercentage = (int) Math.round(percentage);
                percentageLeavesByMonth.put(monthNames[i - 1], roundedPercentage);
            }
        }

        return percentageLeavesByMonth;
    }

    @Override

    public String getInstructorDetailsForLeave(String leaveId) {
        Leave leave = leaveRepository.findById(leaveId).orElse(null);
        ObjectMapper objectMapper = new ObjectMapper();

        if (leave != null && leave.getInstructor() != null) {
            User instructor = leave.getInstructor();
            Map<String, String> instructorDetails = new HashMap<>();
            instructorDetails.put("leaveId", leaveId);
            instructorDetails.put("id",instructor.getId());
            instructorDetails.put("firstName", instructor.getUsername ());
            instructorDetails.put("lastName", instructor.getUsername());
            instructorDetails.put("username", instructor.getUsername());
            instructorDetails.put("email", instructor.getEmail());

            try {
                return objectMapper.writeValueAsString(instructorDetails);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "{\"error\": \"Error serializing instructor details\"}";
            }
        } else {
            return "{\"error\": \"No instructor assigned for Leave " + leaveId + "\"}";
        }
    }

}
