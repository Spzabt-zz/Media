package com.monitor.media.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.monitor.media.domain.Message;
import com.monitor.media.domain.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonView(Views.FullMessage.class)
public class MessagePageDto {
    private List<Message> messages;
    private int currentPage;
    private int totalPages;
}
