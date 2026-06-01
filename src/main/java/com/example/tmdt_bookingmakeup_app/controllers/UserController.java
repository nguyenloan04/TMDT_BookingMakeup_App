package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.dto.request.user.CreateUserRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateProfileRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateRoleRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateStatusRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.UpdateUserAdminRequest;
import com.example.tmdt_bookingmakeup_app.dto.request.user.ChangePasswordRequest;
import com.example.tmdt_bookingmakeup_app.dto.response.user.UserDto;
import com.example.tmdt_bookingmakeup_app.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Get Current User Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID userId = UUID.fromString(rawUserId);
            UserDto profile = userService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 2. Update Current User Profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID userId = UUID.fromString(rawUserId);
            UserDto updated = userService.updateUserProfile(userId, updateProfileRequest);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 2.5 Change Password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest requestParams, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID userId = UUID.fromString(rawUserId);
            userService.changePassword(userId, requestParams);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 3. Get Public Profile of Any User
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            UserDto profile = userService.getUserById(id);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
        }
    }

    // 4. Admin API: List All Users
    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 5. Admin API: Update User Active Status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable UUID id, @RequestBody UpdateStatusRequest updateStatusRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            UserDto updated = userService.updateUserStatus(id, updateStatusRequest.active());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 6. Admin API: Update User Role
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest updateRoleRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            UserDto updated = userService.updateUserRole(id, updateRoleRequest.role());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 7. Admin API: Create User
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            UserDto created = userService.createUser(createUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 8. Admin API: Update User Details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserAdmin(@PathVariable UUID id, @RequestBody UpdateUserAdminRequest updateUserAdminRequest, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            UserDto updated = userService.updateUserAdmin(id, updateUserAdminRequest);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 9. Admin API: Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        String rawUserId = (String) request.getAttribute("userId");
        if (rawUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing User Session");
        }
        try {
            UUID requesterId = UUID.fromString(rawUserId);
            UserDto requester = userService.getUserProfile(requesterId);
            if (requester.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only Admins can perform this action");
            }
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
