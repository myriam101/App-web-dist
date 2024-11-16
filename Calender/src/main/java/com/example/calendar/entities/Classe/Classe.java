package com.example.calendar.entities.Classe;

import com.example.calendar.entities.User.User;
import com.example.calendar.entities.specialities.Speciality;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
@Document(collection = "Class")
public class Classe implements Serializable {
    @Id
    private String classId;

    private String className;
    private Integer nbStudent;
    private String conditionAdmission;
    private String objectif;

    @JsonIgnore
    @DBRef
    private Speciality speciality;

}