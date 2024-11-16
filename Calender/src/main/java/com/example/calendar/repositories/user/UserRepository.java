package com.example.calendar.repositories.user;

import com.example.calendar.entities.User.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    // In your UserRepository interface
    Optional<User> findByEmail(String email);

    @Query(value = "{'verify' : ?0}")
    User getUserCD(String code);

    @Query(value = "{'email' : ?0}")
    User getUserByUsername(String username);


    List<User> findAll();

    @Query(value = "{$or: [{'email': ?0}, {'email2': ?0}]}")
    Optional<User> findByEmailorEmail2(String email);

    User findByTelephone(String telephone);

    @Query(value = "{'roles.id' : ?0}")
    List<User> findByRoleNot(String role);


    @Query(value = "{'admin' : ?0}")
    List<User> findByAdmin(String admin);

    @Query(value = "{'verificationCode' : ?0}")
    User findByVerificationCode(String verificationCode);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);


    //for the add methode of the module Leave management
    @Query(value = "{'roles.id' : ?0}")
    long countByRole(String role);

    @Query(value = "{'offers.id' : ?0}")
    public User getUserByOffersId(String Id);

    @Query(value = "{'offers.id' : ?0}")
    public User findByOffersId(String Id);

   @Query(value = "{'roles.id' : ?0}")
    List<User> findByRole(String role);


}
