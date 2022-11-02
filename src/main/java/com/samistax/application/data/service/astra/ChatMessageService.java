package com.samistax.application.data.service.astra;

import com.samistax.application.data.entity.astra.TopicMessage;
import com.samistax.application.data.entity.astra.TopicMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ChatMessageService {

    private TopicMessageRepository repository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ChatMessageService(@Autowired TopicMessageRepository repository) {
        this.repository = repository;
    }

    private void printRequestDuration(String methodName, long startTime) {
        logger.info(methodName+" request (ms): " + (System.currentTimeMillis() - startTime));
    }

    public int countAllMessages() {
        return (int) repository.count();
    }

    public List<TopicMessage> findAllMessages() {
        long startTime = System.currentTimeMillis();
        List<TopicMessage> response = repository.findAll();
        printRequestDuration("findAll: ",startTime);
        return response;
    }

    public List<TopicMessage> findAllMessages(String topic) {
        long startTime = System.currentTimeMillis();
        List<TopicMessage> response = repository.findByKeyTopic(topic);
        printRequestDuration("findAllMessages("+topic+"): ",startTime);
        return response;
    }
    public List<TopicMessage> findAllMessagesSince(String topic, Instant timestamp) {
        long startTime = System.currentTimeMillis();
        List<TopicMessage> test = repository.findByKeyTopicAndKeyTime(topic, timestamp);
        List<TopicMessage> response = repository.findByKeyTopicAndKeyTimeAfter(topic, timestamp);
        printRequestDuration("findAllMessagesSince("+topic+"): ", startTime);
        // workaround for JPA not having equal or after convention
        response.addAll(test);
        return response;
    }

    // Start of Voy<gePrediction methods
    public TopicMessage saveTopicMessage(TopicMessage entity) {
        long startTime = System.currentTimeMillis();
        TopicMessage response =  repository.save(entity);
        printRequestDuration("saveTopicMessage: ",startTime);
        return response;
    }
}
