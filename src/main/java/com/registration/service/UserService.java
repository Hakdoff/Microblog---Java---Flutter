package com.registration.service;

import com.registration.PasswordUtils;
import com.registration.entity.User;
import com.registration.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@Service
public class UserService{

    @Autowired
    private UserRepo repo;

    public static final String UPLOAD_DIR = "profile-pictures/";

    public String saveUser(User user) {
        User existingUser = repo.findByEmail(user.getEmail());
        if (existingUser != null) {
            return "Email already exists";
        }
        try{
            String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            repo.save(user);
            return "User registered Successfully";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Error registering user";
        }

    }

    public User getUserByEmail(String email) {
        return repo.findByEmail(email);
    }

    public boolean validateUser(User user) {
        User foundUser = repo.findByEmail(user.getEmail());
        if (foundUser !=null) {
            try {
                String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
                return hashedPassword.equals(foundUser.getPassword());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public User getUserById(Integer id) {
        return repo.findById(id).orElse(null);
    }

    public List<User> getAllUser() {
        return  repo.findAll();
    }

    public String uploadProfilePicture(Integer userId, MultipartFile file) {
        try {
            User user = repo.findById(userId).orElse(null);
            if (user == null) {
                return null;
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            user.setProfilePictureUrl(fileName);
            repo.save(user);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
