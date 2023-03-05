package com.monitor.media.service;

import com.monitor.media.domain.User;
import com.monitor.media.exceptions.NotFoundException;
import com.monitor.media.repo.UserDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserDetailsRepo userRepo;

    @Autowired
    public UserService(UserDetailsRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User getUserFromDb(OAuth2User user) {
        Optional<User> userFromDb = Optional.empty();

        String usrId = user.getAttribute("sub");
        if (usrId != null) {
            userFromDb = userRepo.findById(usrId);
        }

        User fromDb = null;
        if (userFromDb.isPresent()) {
            fromDb = userFromDb.get();
            return fromDb;
        } else {
            throw new NotFoundException();
        }
    }
}
