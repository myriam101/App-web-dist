package com.example.calendar.entities.universities;


import com.example.calendar.entities.Programmes.Programme;
import com.example.calendar.entities.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Document(collection ="university")


public class University {
    @Id
    long id ;
    String name;
    String adress;
    String email;
    long telephone;
   @DBRef
   @JsonIgnore
   List<Programme> programmes=new ArrayList<>();
   //@DBRef
    String idUser;
    @DBRef
    List<User> users;






}
