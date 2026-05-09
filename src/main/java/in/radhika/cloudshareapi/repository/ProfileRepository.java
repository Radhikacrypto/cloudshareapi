package in.radhika.cloudshareapi.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.radhika.cloudshareapi.document.ProfileDocument;

public interface ProfileRepository extends MongoRepository<ProfileDocument, String>{
    
    Optional<ProfileDocument> findByEmail(String email);
    Optional<ProfileDocument> findByClerkId(String clerkId);

    Boolean existsByClerkId(String clerkId);

}
