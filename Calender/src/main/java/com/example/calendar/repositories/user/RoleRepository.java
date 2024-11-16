package com.example.calendar.repositories.user;


import com.example.calendar.entities.User.ERole;
import com.example.calendar.entities.User.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}