package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.user.CreateUserRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateProfileRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.ChangePasswordRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateUserAdminRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateServiceOwnerProfileRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto;
import com.example.tmdt_bookingmakeup_app.dto.response.user.ServiceOwnerProfileDto;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.models.user.ServiceOwner;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ServiceOwnerRepository;
import com.example.tmdt_bookingmakeup_app.security.PasswordEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ServiceOwnerRepository serviceOwnerRepository;

    @Autowired
    public UserService(UserRepository userRepository, ServiceOwnerRepository serviceOwnerRepository) {
        this.userRepository = userRepository;
        this.serviceOwnerRepository = serviceOwnerRepository;
    }

    public UserDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateUserProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.address() != null) {
            user.setAddress(String.valueOf(request.address()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!PasswordEncryption.checkPassword(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        
        user.setPassword(PasswordEncryption.hashPassword(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserStatus(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public UserDto updateUserRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        User existUserByEmail = userRepository.findByEmail(request.email());
        if (existUserByEmail != null) {
            throw new RuntimeException("Email already exists");
        }
        User existUserByUsername = userRepository.findByUsername(request.username());
        if (existUserByUsername != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(PasswordEncryption.hashPassword(request.password()));
        user.setDisplayName(request.displayName());
        user.setPhone(request.phone());
        user.setGender(request.gender());
        user.setRole(request.role() != null ? request.role() : UserRole.USER);
        user.setActive(true);
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setTotalPoints(0);

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto updateUserAdmin(UUID userId, UpdateUserAdminRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.address() != null) {
            user.setAddress(String.valueOf(request.address()));
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setVerified(user.isVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setAddress(user.getAddress());
        dto.setTotalPoints(user.getTotalPoints());
        return dto;
    }

    public ServiceOwnerProfileDto getServiceOwnerProfile(UUID userId) {
        ServiceOwner so = serviceOwnerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ServiceOwner profile not found for user: " + userId));
        return mapToSoDto(so);
    }

    @Transactional
    public ServiceOwnerProfileDto updateServiceOwnerProfile(UUID userId, UpdateServiceOwnerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        ServiceOwner so = serviceOwnerRepository.findById(userId).orElse(null);
        if (so == null) {
            so = new ServiceOwner();
            so.setUser(user);
            so.setVerificationStatus("pending");
        }

        if (request.bio() != null) so.setBio(request.bio());
        if (request.experienceYears() != null) so.setExperienceYears(request.experienceYears());
        if (request.showcaseType() != null) so.setShowcaseType(request.showcaseType());
        if (request.identityFront() != null) so.setIdentityFront(request.identityFront());
        if (request.identityBack() != null) so.setIdentityBack(request.identityBack());

        ServiceOwner saved = serviceOwnerRepository.save(so);
        return mapToSoDto(saved);
    }

    private ServiceOwnerProfileDto mapToSoDto(ServiceOwner so) {
        return ServiceOwnerProfileDto.builder()
                .userId(so.getUserId())
                .bio(so.getBio())
                .experienceYears(so.getExperienceYears())
                .showcaseType(so.getShowcaseType())
                .identityFront(so.getIdentityFront())
                .identityBack(so.getIdentityBack())
                .verificationStatus(so.getVerificationStatus())
                .build();
    }
}
