package com.example.calendar.entities.Session;

import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Course.Subject;
import com.example.calendar.entities.User.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
@ToString
@Document(collection ="session")
public class Session implements Serializable {
    @Id
    String idSession;
    TypeSession typeSession;
    LocalDate sessionDate;
    String linkLiveS;
    LocalTime sessionStartHour;
    LocalTime sessionEndHour;


    @DBRef

    private Classe classe;

    @DBRef
    private Subject subject;

    @DBRef
    private User teacher;


}
