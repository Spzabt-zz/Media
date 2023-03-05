package com.monitor.media.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.monitor.media.domain.User;
import com.monitor.media.domain.UserSubscription;
import com.monitor.media.domain.Views;
import com.monitor.media.service.ProfileService;
import com.monitor.media.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("{id}")
    @JsonView(Views.FullProfile.class)
    public User get(@PathVariable("id") User user) {
        return user;
    }

    @PostMapping("change-subscription/{channelId}")
    @JsonView(Views.FullProfile.class)
    public User changeSubscription(
            @AuthenticationPrincipal OAuth2User userFromOAuth,
            @PathVariable("channelId") User channel
    ) {
        User subscriber = userService.getUserFromDb(userFromOAuth);
        if (subscriber.equals(channel)) {
            return channel;
        } else {
            return profileService.changeSubscription(channel, subscriber);
        }
    }

    @GetMapping("get-subscribers/{channelId}")
    @JsonView(Views.IdName.class)
    public List<UserSubscription> subscribers(
            @PathVariable("channelId") User channel
    ) {
        return profileService.getSubscribers(channel);
    }

    @PostMapping("change-status/{subscriberId}")
    @JsonView(Views.IdName.class)
    public UserSubscription changeSubscriptionStatus(
            @AuthenticationPrincipal OAuth2User channel,
            @PathVariable("subscriberId") User subscriber
    ) {
        User channelFromDb = userService.getUserFromDb(channel);
        return profileService.changeSubscriptionStatus(channelFromDb, subscriber);
    }
}
