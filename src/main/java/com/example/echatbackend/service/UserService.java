package com.example.echatbackend.service;

import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends BaseService<User, Integer, UserRepository> {

    public User findUserById(int id) {
        Optional<User> user = baseRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }
        return user.get();
    }

    public User findUserByName(String username) {
        return baseRepository.findByUserName(username);
    }

    public List<User> searchUserByName(String keyword, int offset, int limit) {
        Specification<User> userSpecification = (Specification<User>) (root, criteriaQuery, cb) -> cb.like(root.get("userName"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("userName"));
        return baseRepository.findAll(userSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }

    public List<User> searchUserByNickname(String keyword, int offset, int limit) {
        Specification<User> userSpecification = (Specification<User>) (root, criteriaQuery, cb) -> cb.like(root.get("nickname"), "%" + keyword + "%");
        Sort sort = Sort.by(Sort.Order.asc("nickname"));
        return baseRepository.findAll(userSpecification, PageRequest.of(offset, limit, sort)).getContent();
    }
}
