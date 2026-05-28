package com.url.shortener.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.url.shortener.models.User;
import com.url.shortener.repository.UserRepository;

import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);


}
