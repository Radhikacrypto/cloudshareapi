package in.radhika.cloudshareapi.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import in.radhika.cloudshareapi.document.FileMetadataDocument;
import in.radhika.cloudshareapi.document.ProfileDocument;
import in.radhika.cloudshareapi.dto.FileMetadataDTO;
import in.radhika.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {
    
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepository fileMetadataRepository;

public List<FileMetadataDTO> uploadFiles(MultipartFile files[]) throws IOException {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
        throw new RuntimeException("User not authenticated");
    }

    String clerkId = authentication.getName();

    List<FileMetadataDocument> savedFiles = new ArrayList<>();

    if (!userCreditsService.hasEnoughCredits(files.length)) {
        throw new RuntimeException(
                "Not enough credits to upload files"
        );
    }

    Path uploadPath =
            Paths.get("upload").toAbsolutePath().normalize();

    Files.createDirectories(uploadPath);

    for (MultipartFile file : files) {

        String fileName =
                UUID.randomUUID() + "." +
                StringUtils.getFilenameExtension(
                        file.getOriginalFilename()
                );

        Path targetLocation =
                uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                targetLocation,
                StandardCopyOption.REPLACE_EXISTING
        );

        FileMetadataDocument fileMetadata =
                FileMetadataDocument.builder()
                        .fileLocation(targetLocation.toString())
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .type(file.getContentType())
                        .clerkId(clerkId)   // ← changed
                        .isPublic(false)
                        .uploadedAt(LocalDateTime.now())
                        .build();

        userCreditsService.consumeCredit();

        savedFiles.add(
                fileMetadataRepository.save(fileMetadata)
        );
    }

    return savedFiles.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
}

    private FileMetadataDTO mapToDTO(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .fileLocation(fileMetadataDocument.getFileLocation())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.getIsPublic())
                .uploadedAt(fileMetadataDocument.getUploadedAt())
                .build();
    }

    public List<FileMetadataDTO> getFiles() {

                  Authentication authentication =
                          SecurityContextHolder.getContext().getAuthentication();

                  if (authentication == null) {
                      throw new RuntimeException("User not authenticated");
                  }

                  String clerkId = authentication.getName();

                  List<FileMetadataDocument> files =
                          fileMetadataRepository.findByClerkId(clerkId);

                  return files.stream()
                          .map(this::mapToDTO)
                          .collect(Collectors.toList());
    }


    public FileMetadataDTO getPublicFile(String id) {
        Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);
        if (fileOptional.isEmpty() || !fileOptional.get().getIsPublic()) {
            throw new RuntimeException("Unable to get the file");
        }

        FileMetadataDocument document = fileOptional.get();
        return mapToDTO(document);
    }

    public FileMetadataDTO getDownloadableFile(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
        return mapToDTO(file);
    }

    public void deleteFile(String id) {
        System.out.println("DELETE CONTROLLER HIT");
    try {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String clerkId = authentication.getName();

        FileMetadataDocument file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getClerkId().equals(clerkId)) {
            throw new RuntimeException("File does not belong to current user");
        }

        Path filePath = Paths.get(file.getFileLocation());
        Files.deleteIfExists(filePath);

        fileMetadataRepository.deleteById(id);

    } catch (Exception e) {
        throw new RuntimeException("Error deleting the file");
    }
}

 public FileMetadataDTO togglePublic(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        file.setIsPublic(!file.getIsPublic());
        fileMetadataRepository.save(file);
        return mapToDTO(file);
    }

}
