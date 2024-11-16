package com.example.calendar.entities.Programmes;

import com.example.calendar.entities.specialities.Speciality;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Document(collection ="Programme")

public class Programme {
    @Id
    String idProgramme;
    String nomProgramme;
    String description;
    @DBRef
    List<Speciality> specialities=new ArrayList<>();
   // @DBRef
    //University university;
}
