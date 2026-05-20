package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setSkillCoinBalance(0);

        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithDetails(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName,
                              String bio, String city) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (city != null) {
            user.setCity(city);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User addSkillCanOffer(Long userId, String skill) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!user.getSkillsCanOffer().contains(skill)) {
            user.getSkillsCanOffer().add(skill);
            userRepository.save(user);
        }
        return user;
    }

    @Transactional
    public User addSkillNeeded(Long userId, String skill) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!user.getSkillsNeeded().contains(skill)) {
            user.getSkillsNeeded().add(skill);
            userRepository.save(user);
        }
        return user;
    }
}
