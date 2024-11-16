package com.example.calendar.entities.User;

import com.example.calendar.entities.Classe.Classe;
import com.example.calendar.entities.Leave.Leave;
import com.example.calendar.entities.specialities.Speciality;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Builder
@Document(collection ="user")
public class User {
     @Id
     private String id ;
    public String identifiant;

    private String username;


    private String email;
    private String password;
    private String telephone;
    private String genre;
    private String adresse;// Size of the file in bytes
    private String fonction;
    private String dateNaissance;
    private String aboutMe;



    private Boolean mailNotifications = true;
    //credancials
    private Boolean verified;
    private boolean enabled;

    private String verify;
    private String verificationCode;


    public User (String username, String email, String password, String telephone, String genre, String adresse) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.genre = genre;
        this.adresse = adresse;

    }



//////////////////////////Association
    @DBRef
    @JsonIgnore
    private List<Leave> leaveList;


    @JsonIgnore
    @DBRef
    private Classe classe;

    @JsonIgnore
    @DBRef
    private Speciality speciality;



    public User (String username,String email, String encode, String telephone, String dateNaissance, String genre, String adresse, String aboutMe, String verificationCode) {
              this.username = username;
              this.email = email;
              this.password = encode;
              this.telephone = telephone;
              this.dateNaissance = dateNaissance;
              this.genre = genre;
              this.adresse = adresse;
              this.aboutMe = aboutMe;
              this.verificationCode = verificationCode;
    }
}
