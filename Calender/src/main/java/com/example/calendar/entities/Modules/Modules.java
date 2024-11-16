package com.example.calendar.entities.Modules;

import com.example.calendar.entities.Course.Subject;
import com.example.calendar.entities.specialities.Speciality;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "modules")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class Modules {
    @Id
    String idModule;
    String nameModule;

    String description;
    int nbCredit;
    @DBRef
    Speciality speciality;

    @DBRef
    List<Subject> subjectList;






}
