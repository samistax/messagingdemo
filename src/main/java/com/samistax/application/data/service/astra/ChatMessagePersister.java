package com.samistax.application.data.service.astra;


import com.samistax.application.data.entity.astra.TopicMessage;
import com.samistax.application.data.entity.astra.TopicMessageKey;
import com.vaadin.collaborationengine.CollaborationMessage;
import com.vaadin.collaborationengine.CollaborationMessagePersister;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.spring.annotation.SpringComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringComponent
public class ChatMessagePersister implements CollaborationMessagePersister {

    private final ChatMessageService chatService;
    private final AstraStreamingService pulsarService;

    public ChatMessagePersister(AstraStreamingService pService, ChatMessageService cService) {
        this.pulsarService = pService;
        this.chatService = cService;
    };

    @Override
    public Stream<CollaborationMessage> fetchMessages(FetchQuery query) {
        ArrayList<CollaborationMessage> messages = new ArrayList<>();
        if ( this.chatService != null ) {
            List<TopicMessage> chatMsgs = chatService.findAllMessagesSince(query.getTopicId(), query.getSince());
            if ( chatMsgs != null ) {
                chatMsgs.stream().forEach(e -> {
                    UserInfo info = new UserInfo(e.getUserId(), e.getUserName(), e.getUserImage());
                    messages.add(new CollaborationMessage(info, e.getText(), e.getKey().getTime()));
                });
            }
        }
        return messages.stream();
        /*

        if ( pulsarService != null ){
            ArrayList<Message> pulsarMsgs = pulsarService.fetchMessage(query.getTopicId(), query.getSince());
            for (Message msg:  pulsarMsgs ){
                CollaborationMessage cm = new CollaborationMessage();
                cm.setText(new String(msg.getData()));
                cm.setTime(Instant.ofEpochMilli(msg.getPublishTime()));
                String author = "Anonymous";
                if ( msg.getProducerName() != null ){
                    author = msg.getProducerName();
                }
                UserInfo user = new UserInfo(msg.getMessageId().toString(),author );
                user.setName(msg.getProducerName());
                cm.setUser(user);
                // Convert Pulsar message to collaboration engine chat message.
                messages.add(cm);
            }
        }
        return messages.stream();*/
        /*return messageService
                .findAllByTopicSince(query.getTopicId(), query.getSince())
                .map(messageEntity -> {
                    User author = messageEntity.getAuthor();
                    UserInfo userInfo = new UserInfo(author.getId(),
                            author.getName(), author.getImageUrl());

                    return new CollaborationMessage(userInfo,
                            messageEntity.getText(), messageEntity.getTime());
                });

        */
    }

    @Override
    public void persistMessage(PersistRequest request) {
        CollaborationMessage message = request.getMessage();
        if ( pulsarService != null ){
            pulsarService.sendPulsarMessage(request);
        }
        if ( chatService != null ){
            UserInfo info = message.getUser();
            TopicMessageKey key = new TopicMessageKey(request.getTopicId(), message.getTime());
            TopicMessage tm = new TopicMessage(key);
            tm.setText(message.getText());
            // Store user info
            tm.setUserId(info.getId());
            tm.setUserName(info.getName());
            tm.setUserImage(info.getImage());
            tm.setUserAbbreviation(info.getAbbreviation());
            tm.setUserColorIndex(info.getColorIndex());
            chatService.saveTopicMessage(tm);
        }
    }
}

