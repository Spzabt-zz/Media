package com.monitor.media.service;

import com.monitor.media.domain.GoogleUserInfo;
import com.monitor.media.domain.User;
import com.monitor.media.repo.UserDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomOdicUserService extends OidcUserService {
    private UserDetailsRepo userDetailsRepo;

    @Autowired
    public CustomOdicUserService(UserDetailsRepo userDetailsRepo) {
        this.userDetailsRepo = userDetailsRepo;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(oidcUser.getAttributes());

        User user = userDetailsRepo.findById(googleUserInfo.getId()).orElseGet(() -> {
           User newUser = new User();

            newUser.setId(googleUserInfo.getId());
            newUser.setName(googleUserInfo.getName());
            newUser.setEmail(googleUserInfo.getEmail());
            newUser.setGender(googleUserInfo.getGender());
            newUser.setLocale(googleUserInfo.getLocale());
            newUser.setUserpic(googleUserInfo.getUserpic());

           return newUser;
        });

        user.setLastVisit(LocalDateTime.now());
        userDetailsRepo.save(user);

        return oidcUser;
    }
}
