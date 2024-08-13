package com.registration.repo;

import com.registration.entity.Post;
import com.registration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Integer> {
    List<Post> findByAuthorId(Integer userId);
}
