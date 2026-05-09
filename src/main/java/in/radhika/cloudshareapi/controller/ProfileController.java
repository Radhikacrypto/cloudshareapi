package in.radhika.cloudshareapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.radhika.cloudshareapi.dto.ProfileDTO;
import in.radhika.cloudshareapi.service.ProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor


public class ProfileController {
    
private final ProfileService profileService;

@PostMapping("/register")
public ResponseEntity<?> registerPRofile(@RequestBody ProfileDTO profileDTO){

    HttpStatus status= profileService.existsByClerkId(profileDTO.getClerkId()) ? HttpStatus.OK : HttpStatus.CREATED;

   ProfileDTO savedPRofile= profileService.createProfile((profileDTO));

   
   return ResponseEntity.status(HttpStatus.CREATED).body(savedPRofile);
}

}
