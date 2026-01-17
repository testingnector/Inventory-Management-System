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

    private UUID id;
    private UUID userId;
    private UUID permissionId;
    private Boolean allowed;
    private Boolean active;
    private LocalDateTime assignedAt;

}
