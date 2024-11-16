package com.example.calendar.repositories.universities;

import com.example.calendar.entities.universities.University;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRepository extends MongoRepository<University,String> {
    University findByName(String name);
    University findByIdUser(String idUser);

}
