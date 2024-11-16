package com.example.calendar.entities.specialities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "Niveauspeciality")

public class NiveauSpec {
    @Id
    String id;
    String Name;

}
