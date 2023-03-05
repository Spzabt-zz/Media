package com.monitor.media.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.monitor.media.domain.Comment;
import com.monitor.media.domain.User;
import com.monitor.media.domain.Views;
import com.monitor.media.service.CommentService;
import com.monitor.media.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("comment")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping
    @JsonView(Views.FullComment.class)
    public Comment create(
            @RequestBody Comment comment,
            @AuthenticationPrincipal OAuth2User user
    ) {
        User userFromDb = userService.getUserFromDb(user);
        return commentService.create(comment, userFromDb);
    }
}
