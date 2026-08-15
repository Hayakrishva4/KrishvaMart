package com.krishva.krishvamart.dto;
import com.krishva.krishvamart.model.User;
import java.time.LocalDateTime;
 public class UserResponseDTO 
  {
    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final LocalDateTime createdAt;
    public UserResponseDTO(Long id, String name, String email, String role, LocalDateTime createdAt) 
     {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
     }
    public static UserResponseDTO from(User user) 
     {
        return new UserResponseDTO(
         user.getId(),
         user.getName(),
         user.getEmail(),
         user.getRole().name(),
         user.getCreatedAt());
     }
    public Long getId() 
    {
        return id;
    }
    public String getName() 
    {
        return name;
    }
    public String getEmail() 
    {
        return email;
    }
    public String getRole() 
    {
        return role;
    }
    public LocalDateTime getCreatedAt() 
    {
        return createdAt;
    }
  }