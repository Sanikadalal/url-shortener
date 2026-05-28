package com.url.shortener.security.service;

import org.springframework.stereotype.Service;
import com.url.shortener.repository.UserRepository;
import com.url.shortener.models.User;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;


    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));        
        return userRepository.save(user);
    }



}
