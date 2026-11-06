package com.fairliving.backend.user;

import de.fairliving.backend.jooq.tables.records.UserRecord;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final UserService userService;

    public CurrentUserProvider(UserService userService) {
        this.userService = userService;
    }

    public UserRecord get(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
