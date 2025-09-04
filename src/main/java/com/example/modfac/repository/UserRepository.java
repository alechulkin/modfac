package com.example.modfac.repository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.example.modfac.model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    
    @Query("{'username': ?0}")
    Optional<User> findByUsername(String username);
    
}
