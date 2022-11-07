package com.samistax.application.data.service;

import com.samistax.application.data.Role;
import com.samistax.application.data.entity.User;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> get(UUID id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public User register(User entity) throws UserServiceException{
        // Ensure username is not reserved
        User existingUser = repository.findByUsername(entity.getUsername());
        if ( existingUser != null ) {
            throw new UserServiceException("Username already exist.");
        }
        // if not user exist proceed and save User entity to repository
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    /**
     * Utility Exception class that we can use in the frontend to show that
     * something went wrong during save.
     */
    public static class UserServiceException extends Exception {
        public UserServiceException(String msg) {
            super(msg);
        }
    }
}
