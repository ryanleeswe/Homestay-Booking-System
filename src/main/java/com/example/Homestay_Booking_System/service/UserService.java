package com.example.Homestay_Booking_System.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;
import com.example.Homestay_Booking_System.util.error.IdInvalidException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {

        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.handleGetUserById(id);
        this.userRepository.deleteById(id);
    }

    public User handleGetUserById(long id) {
        if (id <= 0) {
            throw new IdInvalidException("ID người dùng phải lớn hơn 0");
        }
        return this.userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng với ID: " + id));
    }

    public User handleUpdateUser(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new IdInvalidException("ID người dùng không được để trống");
        }
        User currentUser = this.handleGetUserById(updatedUser.getId());
        currentUser.setName(updatedUser.getName());
        currentUser.setPassword(updatedUser.getPassword());
        currentUser.setEmail(updatedUser.getEmail());

        return this.userRepository.save(currentUser);
    }

    public User findByEmail(String username) {
        return this.userRepository.findByEmail(username);

    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public List<User> handleGetAllUsers(Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(pageable);
        return pageUser.getContent();
    }
}
