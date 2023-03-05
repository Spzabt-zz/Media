package com.monitor.media.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.monitor.media.domain.Message;
import com.monitor.media.domain.User;
import com.monitor.media.domain.Views;
import com.monitor.media.dto.MessagePageDto;
import com.monitor.media.repo.UserDetailsRepo;
import com.monitor.media.service.MessageService;
import com.monitor.media.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("message")
public class MessageController {
    public static final int MESSAGES_PER_PAGE = 5;

    private final MessageService messageService;
    private final UserService userService;
    private final UserDetailsRepo userDetailsRepo;

    @Autowired
    public MessageController(MessageService messageService, UserService userService, UserDetailsRepo userDetailsRepo) {
        this.messageService = messageService;
        this.userService = userService;
        this.userDetailsRepo = userDetailsRepo;
    }

    @GetMapping
    @JsonView(Views.FullMessage.class)
    public MessagePageDto list(
            @AuthenticationPrincipal OAuth2User user,
            @PageableDefault(size = MESSAGES_PER_PAGE, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User usrFromDb = userService.getUserFromDb(user);
        User usrFromDbById = userDetailsRepo.findById(usrFromDb.getId()).get();

        List<Long> ids = messageService.getAllIds(pageable);
        return messageService.findForUser(ids, pageable, usrFromDbById);
    }

    @GetMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message getOne(@PathVariable("id") Message message) {
        return message;
    }

    @PostMapping
    @JsonView(Views.FullMessage.class)
    public Message create(
            @RequestBody Message message,
            @AuthenticationPrincipal OAuth2User user
    ) throws IOException {
        return messageService.create(message, user);
    }

    @PutMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message update(@RequestBody Message message,
                          @PathVariable("id") Message messageFromDb,
                          @AuthenticationPrincipal OAuth2User user
    ) throws IOException {
        return messageService.update(messageFromDb, message, user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message) {
        messageService.delete(message);
    }
}
