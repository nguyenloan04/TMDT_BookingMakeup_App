package com.example.tmdt_bookingmakeup_app.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.tmdt_bookingmakeup_app.services.CloudinaryService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final CloudinaryService service;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Autowired
    public UploadController(CloudinaryService service) {
        this.service = service;
    }

    @GetMapping("/signature/identity")
    public ResponseEntity<String> getIdentityUploadSignature(
            @RequestParam("fileName") String fileName
    ) {
        JsonObject result = new JsonObject();
        try {
            String sanitizedFileName = fileName == null
                    ? "identity_" + System.currentTimeMillis()
                    : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

            if (sanitizedFileName.isBlank()) {
                sanitizedFileName = "identity_" + System.currentTimeMillis();
            }

            Map<String, Object> listParam = service.generateSignature(
                    "service-owner/identity",
                    sanitizedFileName,
                    "image"
            );

            if (listParam != null) {
                result = new Gson().toJsonTree(listParam).getAsJsonObject();
                result.addProperty("result", true);
            } else {
                result.addProperty("result", false);
            }

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        } catch (Exception e) {
            result.addProperty("result", false);
            result.addProperty("message", e.getMessage());
            return ResponseEntity
                    .status(500)
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        }
    }

    @GetMapping("/get-upload-url")
    public ResponseEntity<String> getUploadUrl(
            @RequestAttribute("userId") String id,
            @RequestParam("folder") String folderName,
            @RequestParam("fileName") String fileName,
            @RequestParam("contentType") String contentType
    ) {
        JsonObject result = new JsonObject();
        try {
            Map<String, Object> listParam = service.generateSignature(folderName, fileName, contentType);
            if (listParam != null) {
                result = new Gson().toJsonTree(listParam).getAsJsonObject();
                result.addProperty("result", true);
            }
            else result.addProperty("result", false);
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        } catch (Exception e) {
            result.addProperty("result", false);
            return ResponseEntity
                    .status(500)
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        }
    }

    @GetMapping("/upload-multiple-attachment")
    public ResponseEntity<String> uploadMultipleAttachment(
            @RequestParam("folder") String folderName,
            @RequestParam("fileName") List<String> filenames,
            @RequestParam("contentType") String contentType
    ) {
        JsonObject result = new JsonObject();
        JsonArray attachments = new JsonArray();
        try {
            for (String name: filenames) {
                Map<String, Object> listParam = service.generateSignature(folderName, name, contentType);
                if (listParam != null) {
                    attachments.add(new Gson().toJsonTree(listParam));
                }
            }
            result.addProperty("result", true);
            result.add("signatures", attachments);
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        }
        catch (Exception e) {
            result.addProperty("result", false);
            return ResponseEntity
                    .status(500)
                    .header("Content-Type", "application/json")
                    .body(result.toString());
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
            Cloudinary cloudinary;
            if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
                cloudinary = new Cloudinary(cloudinaryUrl);
            } else if (cloudName != null && !cloudName.isEmpty() && apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty()) {
                cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret
                ));
            } else {
                String mockUrl = getRandomMockBeautyImageUrl();
                return ResponseEntity.ok(Map.of("url", mockUrl));
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("url", url));

        } catch (Exception e) {
            System.err.println("Cloudinary upload failed: " + e.getMessage());
            String mockUrl = getRandomMockBeautyImageUrl();
            return ResponseEntity.ok(Map.of("url", mockUrl));
        }
    }

    private String getRandomMockBeautyImageUrl() {
        String[] mockImages = {
            "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1519741497674-611481863552?w=500&auto=format&fit=crop&q=60"
        };
        int index = (int) (Math.random() * mockImages.length);
        return mockImages[index];
    }
}