package com.example.calendar.entities.Session;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
@ToString
@Document(collection ="sessionAuditLog")
public class SessionAuditLog implements Serializable {
    @Id
    private String idLog;

    private String actionLog;
    private String sessionIdLog;
    private LocalDateTime timestampLog;
    private  String descriptionLog;


}
