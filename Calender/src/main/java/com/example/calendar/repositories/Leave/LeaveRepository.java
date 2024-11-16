package com.example.calendar.repositories.Leave;
import com.example.calendar.entities.Leave.Leave;
import com.example.calendar.entities.Leave.StatusLeave;
import com.example.calendar.entities.Leave.TimeLeave;
import com.example.calendar.entities.User.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface LeaveRepository extends MongoRepository<Leave,String> {
    List<Leave> findLeavesByInstructorId(String  instructorId);
    List<Leave> findLeavesByLeaveStartDateBeforeAndLeaveEndDateAfter (Date startDate, Date endDate);


    List<Leave> findByStatusLeave(String status);

    int countByStatusLeave(StatusLeave Pending);
    List<Leave> findLeavesByTimeLeave(TimeLeave timeLeave);
    List<Leave> findLeavesByInstructorIdAndTimeLeave(String  instructorId,TimeLeave timeLeave);




    List<Leave> findByInstructorAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(User popo, LocalDate sessionDate, LocalDate sessionDate1);

}
