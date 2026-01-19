package com.nector.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionResponse{

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    
    private UUID permissionId;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String moduleName;

    private Boolean allowed;
    private Boolean active;
    private LocalDateTime assignedAt;

}
