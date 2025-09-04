package com.example.modfac.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "leaves")
@Data
public class Leave {
    @Id
    private ObjectId id;
    
    @Field("EMPLOYEE")
    @DBRef
    @NotNull
    private Employee employee;
    
    @Field("LEAVE_TYPE")
    @NotNull
    private LeaveType leaveType;
    
    @Field("START_DATE")
    @NotNull
    private LocalDate startDate;
    
    @Field("END_DATE")
    @NotNull
    private LocalDate endDate;
    
    @Field("STATUS")
    @NotNull
    private Status status;
    
    @Field("APPROVED_BY")
    @DBRef
    @NotNull
    private Employee approvedBy;
    
    

}