package com.example.modfac.repository;


import com.example.modfac.model.Leave;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LeaveRepository extends MongoRepository<Leave, ObjectId> {
}
