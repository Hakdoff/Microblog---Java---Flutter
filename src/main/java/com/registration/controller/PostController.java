package com.registration.controller;

import com.registration.PostRequest;
import com.registration.entity.Post;
import com.registration.entity.User;
import com.registration.repo.PostRepo;
import com.registration.service.PostService;
import com.registration.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserService userService;

//    @PostMapping("/post")
//    public ResponseEntity<Post> createPost(@RequestBody PostRequest postRequest) {
//        if (postRequest.getUserId() == null || postRequest.getContent() == null || postRequest.getContent().isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//        User user = userService.getUserById(postRequest.getUserId());
//        if (user == null) {
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//
//        Post post = new Post();
//        post.setContent(postRequest.getContent());
//        post.setAuthor(user);
//
//        try {
//            Post savedPost = postService.createPost(post);
//            return ResponseEntity.ok(savedPost);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/post")
    public ResponseEntity<Post> createPost(@RequestParam("userId") Integer userId,
                                           @RequestParam("content") String content,
                                           @RequestParam(value = "file", required = false) MultipartFile file) {
        if (userId == null || content == null || content.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Post post = new Post();
        post.setContent(content);
        post.setAuthor(user);

        try {
            Post savedPost = postService.createPostWithImage(post, file);
            if(savedPost == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getPosts")
    private ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/postByUser/{userId}")
    public ResponseEntity<List<Post>> getPostByUserId(@PathVariable Integer userId) {
        List<Post> posts = postService.getPostByUserId(userId);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<Resource> getPostById(@PathVariable Integer id) {
//        Post post = postService.getPostById(id);
//        if(post == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return ResponseEntity.ok(post);
        try {
            Post post = postRepo.findById(id).orElse(null);
            if (post == null || post.getImageUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(PostService.UPLOAD_DIR).resolve(post.getImageUrl()).normalize();
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

    @PutMapping("/post/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Integer id, @RequestBody PostRequest postRequest) {
        if (postRequest.getContent() == null || postRequest.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Post updatedPost = postService.updatePost(id, postRequest);
        if (updatedPost == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        boolean isDeleted = postService.deletePost(id);
        if(isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PostMapping("post/imageUrl")
//    public ResponseEntity<String> uploadProfilePicture(@RequestParam("postId") Integer postId, @RequestParam("file")MultipartFile file) {
//        String url = postService.imageUrl(postId, file);
//        if(url == null) {
//            return new ResponseEntity<>("Error uploading", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        return new ResponseEntity<>(url, HttpStatus.OK);
//    }
//
//    @GetMapping("/post/imageUrl/{postId}")
//    public ResponseEntity<Resource> getImageUrl(@PathVariable Integer postId) {
//        try {
//            Post post = postRepo.findById(postId).orElse(null);
//            if (post == null || post.getImageUrl() == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Path filePath = Paths.get(PostService.UPLOAD_DIR).resolve(post.getImageUrl()).normalize();
//            Resource resource = new UrlResource(filePath.toUri());
//
//            if (resource.exists() && resource.isReadable()) {
//                return ResponseEntity.ok()
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                        .body(resource);
//            }  else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
}
