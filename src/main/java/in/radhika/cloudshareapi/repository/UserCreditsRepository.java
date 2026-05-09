package in.radhika.cloudshareapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.radhika.cloudshareapi.document.UserCredits;

import java.util.Optional;

public interface UserCreditsRepository extends MongoRepository<UserCredits, String> {

    Optional<UserCredits> findByClerkId(String clerkId);
}

