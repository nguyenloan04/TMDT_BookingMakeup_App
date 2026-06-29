package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.services.CloudinaryService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final CloudinaryService service;

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
            String url = service.upload(file.getBytes());
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            System.err.println("Cloudinary upload failed: " + e.getMessage());
            String defaultUrl = "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=500&auto=format&fit=crop&q=60";
            return ResponseEntity.ok(Map.of("url", defaultUrl));
        }
    }
}
