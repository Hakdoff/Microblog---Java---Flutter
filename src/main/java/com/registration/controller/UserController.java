package com.registration.controller;

import com.registration.JwtUtils;
import com.registration.entity.User;
import com.registration.repo.UserRepo;
import com.registration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepo repo;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    private ResponseEntity<String>  registerUser(@RequestBody User user) {
        String msg = service.saveUser(user);
        if (msg.equals("Email already exists")) {
            return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
        } else if (msg.equals("Error registering user")) {
            return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(msg, HttpStatus.OK);
    }

    @PostMapping("/login")
    private ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        boolean isValid = service.validateUser(user);
        if (isValid) {
            String token = jwtUtils.generateToken(user.getEmail());
            User authenticatedUser = repo.findByEmail(user.getEmail());
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", authenticatedUser.getId());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/users")
    private ResponseEntity<List<User>> getAllUsers() {
        List<User> users = service.getAllUser();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/uploadProfilePicture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("userId") Integer userId, @RequestParam("file")MultipartFile file) {
        String url = service.uploadProfilePicture(userId, file);
        if (url == null) {
            return new ResponseEntity<>("Error uploading", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping("/profilePictures/{userId}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable Integer userId) {
        try {
            User user = repo.findById(userId).orElse(null);
            if (user == null || user.getProfilePictureUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(UserService.UPLOAD_DIR).resolve(user.getProfilePictureUrl()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
