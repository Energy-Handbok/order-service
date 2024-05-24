package com.khaphp.common.dto.noti;

import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NotificationDTOcreate {
    private String userId;
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;
    @Size(min = 3, max = 255, message = "description must be between 3 and 255 characters")
    private String description;
}
