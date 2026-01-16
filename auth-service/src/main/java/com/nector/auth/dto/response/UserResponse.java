package com.nector.auth.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean isActive;
}


