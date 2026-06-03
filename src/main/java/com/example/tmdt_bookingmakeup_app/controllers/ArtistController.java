package com.example.tmdt_bookingmakeup_app.controllers;


import com.example.tmdt_bookingmakeup_app.dto.request.artist.ArtistRequestDTO;
import com.example.tmdt_bookingmakeup_app.dto.response.artist.ArtistResponseDTO;
import com.example.tmdt_bookingmakeup_app.services.ArtistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/artists")
public class ArtistController {

    private final ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("/my-artists")
    public ResponseEntity<?> getMyArtists(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID soId = UUID.fromString(rawUserId);
            List<ArtistResponseDTO> artists = artistService.getArtistsByOwnerId(soId);
            return ResponseEntity.ok(artists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArtistById(@PathVariable UUID id) {
        try {
            ArtistResponseDTO artist = artistService.getArtistById(id);
            return ResponseEntity.ok(artist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createArtist(@RequestBody ArtistRequestDTO requestDTO, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            UUID soId = UUID.fromString(rawUserId);
            ArtistResponseDTO created = artistService.createArtist(soId, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
