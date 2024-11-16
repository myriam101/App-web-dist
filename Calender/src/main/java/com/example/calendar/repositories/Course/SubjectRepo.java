package com.example.calendar.repositories.Course;

import com.example.calendar.entities.Course.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepo extends MongoRepository<Subject, String> {


    List<Subject> findByIsPublishedTrue();

}
