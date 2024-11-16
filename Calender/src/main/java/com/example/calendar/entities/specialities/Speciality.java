package com.example.calendar.entities.specialities;

import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Modules.Modules;
import com.example.calendar.entities.Programmes.Programme;
import com.example.calendar.entities.User.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "speciality")
public class Speciality  {
    @Id
    public String id;
    public String name;
    public String description;
    @DBRef
    List<Modules> modules=new ArrayList<>();
    @DBRef
    Programme programme;
    

    @DBRef
    private List<User> studentSet;

    @DBRef
    private List<Classe> classes;


}
