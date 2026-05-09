package in.radhika.cloudshareapi.service;

import org.springframework.stereotype.Service;

import in.radhika.cloudshareapi.document.UserCredits;
import in.radhika.cloudshareapi.repository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Service
@RequiredArgsConstructor

public class UserCreditsService {

    private final UserCreditsRepository userCreditsRepository;
        private final ProfileService profileService;
   
     public UserCredits createInitialCredits(String clerkId) {
        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();


        UserCredits saved = userCreditsRepository.save(userCredits);

    System.out.println("Saved in Mongo = " + saved);

    return saved;
    }

 public UserCredits getUserCredits(String clerkId) {
        return userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));
    }


    public UserCredits getUserCredits() {
    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
        throw new RuntimeException("User not authenticated");
    }

    String clerkId = authentication.getName();

    return getUserCredits(clerkId);
}

    public Boolean hasEnoughCredits(int requiredCredits) {
        UserCredits userCredits = getUserCredits();
        return userCredits.getCredits() >= requiredCredits;
    }


    public UserCredits consumeCredit() {
        UserCredits userCredits = getUserCredits();

        if (userCredits.getCredits() <= 0) {
            return null;
        }

        userCredits.setCredits(userCredits.getCredits() - 1);
        return userCreditsRepository.save(userCredits);
    }

    public UserCredits addCredits(String clerkId, Integer creditsToAdd, String plan) {
        UserCredits userCredits = userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));

        userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
        userCredits.setPlan(plan);
        return userCreditsRepository.save(userCredits);
    }

}
