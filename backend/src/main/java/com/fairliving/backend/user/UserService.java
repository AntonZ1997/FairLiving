package com.fairliving.backend.user;

import com.fairliving.backend.exception.EmailAlreadyUsedException;
import com.fairliving.backend.exception.UserNotFoundException;
import de.fairliving.backend.jooq.tables.records.UserRecord;

import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserRecord createUser(String email, String passwordHash, String userName) {
        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        return userRepository.insert(email, passwordHash, userName);
    }

    public UserRecord getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }
}
