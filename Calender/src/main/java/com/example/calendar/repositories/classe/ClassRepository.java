package com.example.calendar.repositories.classe;

import com.example.calendar.entities.Classe.Classe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends MongoRepository<Classe,String> {
    List<Classe> findByClassName(String className);


    Classe findByClassId(String id);
}
