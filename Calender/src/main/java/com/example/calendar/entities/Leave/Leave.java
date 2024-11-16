package com.example.calendar.entities.Leave;

import com.example.calendar.entities.User.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
@Document(collection ="leave")
@ToString
public class Leave implements Serializable {
    @Id
    String leaveId;
    LeaveType leaveType;
    Integer numberDaysLeave;
    Date leaveStartDate;
    Date leaveEndDate;
    String leaveDescription;
    String leaveHistory;
    StatusLeave statusLeave;
    LocalTime leaveStartHour;
    LocalTime leaveEndHour;
    long numberHoursLeave;
    TimeLeave timeLeave;


    @DBRef
    private User instructor;


}
