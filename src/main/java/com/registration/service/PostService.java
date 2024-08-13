package com.registration.service;

import com.registration.PostRequest;
import com.registration.entity.Post;
import com.registration.entity.User;
import com.registration.repo.PostRepo;
import com.registration.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepo postRepo;
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    public static final String UPLOAD_DIR = "images/";

    public Post createPost(Post post) {
        return postRepo.save(post);
    }

    public Post createPostWithImage(Post post, MultipartFile file) {
        try {
            Post savedPost = postRepo.save(post);

            if(file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, file.getBytes());

                savedPost.setImageUrl(fileName);
                postRepo.save(savedPost);
            }

            return savedPost;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Post> getAllPosts() {
        return  postRepo.findAll();
    }

    public Post getPostById(Integer id) {
        return postRepo.findById(id).orElse(null);
    }

    public List<Post> getPostByUserId(Integer userId) {
        return postRepo.findByAuthorId(userId);
    }

    public Post updatePost(Integer postId, PostRequest postRequest) {
        Post existingPost = postRepo.findById(postId).orElse(null);
        if (existingPost != null) {
            existingPost.setContent(postRequest.getContent());
            return postRepo.save(existingPost);
        }
        return null;
    }

    public boolean deletePost(Integer id) {
        if (postRepo.existsById(id)) {
            postRepo.deleteById(id);
            return true;
        }
        return false;
    }
}












