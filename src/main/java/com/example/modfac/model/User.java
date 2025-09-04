package com.example.modfac.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;

@Document(collection = "users")
@Data
public class User {
    @Id
    private ObjectId id;
    
    @Field(name = "USERNAME")
    @Size(max = 50)
    @Indexed(unique = true)
    @NotBlank
    private String username;
    
    @Field(name = "PASSWORD")
    @Size(max = 100)
    @NotBlank
    private String password;
    
    @Field(name = "ROLE")
    @NotNull
    private Role role;
}
