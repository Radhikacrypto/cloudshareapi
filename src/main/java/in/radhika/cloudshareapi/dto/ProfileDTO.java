package in.radhika.cloudshareapi.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProfileDTO {
    private String id;
    private String clerkId;
   
    private String email;
    private String firstName;
    private String lastName;
    private Integer credits;
    private String photoUrl;
    
    private Instant createdAt;
    
}
