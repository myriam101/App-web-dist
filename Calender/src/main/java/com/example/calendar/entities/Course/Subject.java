package com.example.calendar.entities.Course;

import com.example.calendar.entities.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection ="subjects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Subject {
    @Id
    private String subjectId;
    private String titleSubject;
    private String descriptionSubject;
    private Long nbrHours;
    @CreatedDate
    private Date createdDate;
    private Date updatedDate;
    private Boolean isPublished;


    //////liste de teacher eli i9ariw fel matiere hedhi//////
    @DBRef
    @JsonIgnore
    private List<User> teachers = new ArrayList<>();
    ///////////////////////////////////////////////////////

}
