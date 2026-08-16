package com.pocket_plant.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final Path uploadDirectory =
            Path.of("uploads")
                    .toAbsolutePath()
                    .normalize();

    @PostConstruct
    public void initialize() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public String save(MultipartFile image)
            throws IOException {

        validate(image);

        String filename =
                UUID.randomUUID()
                        + extensionOf(image.getContentType());

        Path destination =
                uploadDirectory
                        .resolve(filename)
                        .normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "잘못된 파일 경로입니다."
            );
        }

        try (InputStream inputStream =
                     image.getInputStream()) {

            Files.copy(
                    inputStream,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return filename;
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    private void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "업로드할 이미지가 없습니다."
            );
        }

        if (image.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "이미지는 10MB 이하여야 합니다."
            );
        }

        String contentType = image.getContentType();

        if (
                contentType == null
                        || !ALLOWED_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )
        ) {
            throw new IllegalArgumentException(
                    "JPEG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}