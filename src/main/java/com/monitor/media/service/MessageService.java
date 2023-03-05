package com.monitor.media.service;

import com.monitor.media.domain.Message;
import com.monitor.media.domain.User;
import com.monitor.media.domain.UserSubscription;
import com.monitor.media.domain.Views;
import com.monitor.media.dto.EventType;
import com.monitor.media.dto.MessagePageDto;
import com.monitor.media.dto.MetaDto;
import com.monitor.media.dto.ObjectType;
import com.monitor.media.repo.MessageRepo;
import com.monitor.media.repo.UserSubscriptionRepo;
import com.monitor.media.util.Pipeline;
import com.monitor.media.util.WsSender;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private static String URL_PATTERN = "https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static String IMAGE_PATTERN = "\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMAGE_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

    private final MessageRepo messageRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final BiConsumer<EventType, Message> wsSender;
    private final UserService userService;

    @Autowired
    public MessageService(
            MessageRepo messageRepo,
            UserSubscriptionRepo userSubscriptionRepo,
            WsSender wsSender,
            UserService userService) {
        this.messageRepo = messageRepo;
        this.userSubscriptionRepo = userSubscriptionRepo;
        this.wsSender = wsSender.getSender(ObjectType.MESSAGE, Views.FullMessage.class);
        this.userService = userService;
    }

    private void fillMeta(Message message) throws IOException {
        String text = message.getText();
        Matcher matcher = URL_REGEX.matcher(text);

        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());

            matcher = IMAGE_REGEX.matcher(url);

            message.setLink(url);

            if (matcher.find()) {
                message.setLinkCover(url);
            } else if (!url.contains("youtu")) {
                MetaDto meta = getMeta(url);

                message.setLinkCover(meta.getCover());
                message.setLinkTitle(meta.getTitle());
                message.setLinkDescription(meta.getDescription());
            }
        }
    }

    private MetaDto getMeta(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent("Chrome")
                .header("Accept", "text/html")
                .header("Accept-Encoding", "gzip,deflate")
                .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                .header("Connection", "keep-alive")
                .ignoreContentType(true)
                .get();
        Elements title = document.select("meta[name$=title],meta[property$=title]");
        Elements description = document.select("meta[name$=description],meta[property$=description]");
        Elements cover = document.select("meta[name$=image],meta[property$=image]");

        String coverUrl = cover.attr("content");
        String newCoverUrl = "";
        if (cover.size() != 0 && !coverUrl.contains(url.substring(0, 5))) {
            String regex = "^.+?[^\\/:](?=[?\\/]|$)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            String baseUrl = "";

            if (matcher.find()) {
                baseUrl = matcher.group();
                newCoverUrl = baseUrl + coverUrl;
            }

            return new MetaDto(
                    getContent(title.first()),
                    getContent(description.first()),
                    newCoverUrl
            );
        }

        return new MetaDto(
                getContent(title.first()),
                getContent(description.first()),
                getContent(cover.first())
        );
    }

    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }

    private void test() {
        StanfordCoreNLP stanfordCoreNLP = Pipeline.getPipeline();
        CoreDocument coreDocument = new CoreDocument("I'm very angry");
        stanfordCoreNLP.annotate(coreDocument);
        List<CoreSentence> sentences = coreDocument.sentences();
        for (CoreSentence sentence : sentences) {
            String sentiment = sentence.sentiment();
            System.out.println(sentiment + "\t" + sentence);
        }
    }

    public void delete(Message message) {
        messageRepo.delete(message);
        wsSender.accept(EventType.REMOVE, message);
    }

    public Message update(Message messageFromDb, Message message, OAuth2User user) throws IOException {
        messageFromDb.setText(message.getText());
        fillMeta(messageFromDb);

        User userFromDb = userService.getUserFromDb(user);
        messageFromDb.setAuthor(userFromDb);
        Message updatedMessage = messageRepo.save(messageFromDb);

        wsSender.accept(EventType.UPDATE, updatedMessage);

        return updatedMessage;
    }

    public Message create(Message message, OAuth2User user) throws IOException {
        message.setCreationDate(LocalDateTime.now());
        fillMeta(message);

        User userFromDb = userService.getUserFromDb(user);
        message.setAuthor(userFromDb);
        Message updatedMessage = messageRepo.save(message);

        wsSender.accept(EventType.CREATE, updatedMessage);

        return updatedMessage;
    }

    public List<Long> getAllIds(Pageable pageable) {
        return messageRepo.getAllIds(pageable);
    }

    public MessagePageDto findForUser(List<Long> ids, Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepo.findBySubscriber(user)
                .stream()
                .filter(UserSubscription::isActive)
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        List<Message> allMessages = messageRepo.findAllMessages(ids);
        List<Message> userMessages = messageRepo.findMessagesByUser(channels, allMessages);
        final int start = 0;
        //final int start = (int)pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int min = Math.min((start + pageSize), userMessages.size());
        final int end = min;
        List<Message> messageSubList = userMessages.subList(start, end);
        final Page<Message> page = new PageImpl<>(messageSubList, pageable, userMessages.size());

        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages());
    }

    /*public MessagePageDto findAll(Pageable pageable) {
        Page<Message> page = messageRepo.findAll(pageable);
        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages());
    }*/
}
