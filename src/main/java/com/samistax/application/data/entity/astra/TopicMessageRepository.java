package com.samistax.application.data.entity.astra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.time.Instant;
import java.util.List;

public interface TopicMessageRepository extends CassandraRepository<TopicMessage,TopicMessageKey> {
    List<TopicMessage> findByKeyTopic(final String topic);

    List<TopicMessage> findByKeyTopicAndKeyTimeAfter(final String key, final Instant timestamp);
    List<TopicMessage> findByKeyTopicAndKeyTime(final String key, final Instant timestamp);

    // @Query("SELECT t FROM Todo t WHERE t.title = 'title'")
    // List<TopicMessage> findByKeyTopicAndKeyTimeAfter(final String key, final Instant timestamp);

}