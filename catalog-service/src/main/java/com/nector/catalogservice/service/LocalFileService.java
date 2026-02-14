package com.nector.catalogservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileService {

	private final String uploadDir = System.getProperty("user.dir") + "/uploads/product-images";

	public String uploadFile(MultipartFile file) {

		try {
			Files.createDirectories(Paths.get(uploadDir));

			String originalFileName = file.getOriginalFilename();
			if (originalFileName == null || originalFileName.isBlank()) {
				throw new RuntimeException("File must have a name");
			}

			String fileName = UUID.randomUUID() + "-" + originalFileName;
			Path filePath = Paths.get(uploadDir, fileName);

			while (Files.exists(filePath)) {
				fileName = UUID.randomUUID() + "-" + originalFileName;
				filePath = Paths.get(uploadDir, fileName);
			}

			file.transferTo(filePath.toFile());

			return "/uploads/product-images/" + fileName;

		} catch (IOException e) {
			throw new RuntimeException("File upload failed", e);
		}
	}

	public String generateFileHash(MultipartFile file) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(file.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : hashBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate file hash", e);
		}
	}

	public void deleteFile(String imageUrl) {
		try {
			Path path = Paths.get("." + imageUrl);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete old file", e);
		}
	}

}
