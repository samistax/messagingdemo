package com.samistax.application.data.service.astra;

import com.samistax.application.data.entity.astra.cdc.BookCDC;
import com.vaadin.collaborationengine.*;
import com.vaadin.flow.component.Component;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.common.schema.KeyValueEncodingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class AstraStreamingService {

    @Value("${pulsar.data-topic-url:persistent://<tenant name>/astracdc/<data topic name>}")
    private String PULSAR_DATA_TOPIC_URL;
    @Value("${pulsar.service.url:}")
    private String SERVICE_URL;
    @Value("${pulsar.service.token:}")
    private String PULSAR_TOKEN;

    PulsarClient client;
    Consumer consumer;

    public final static String SUBSCRIPTION_NAME = "cdc-app-subscription";

    private boolean pulserConsumerEnabled = false;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AstraStreamingService( ) {}

    private CassandraOperations cassandraTemplate;


    @Bean
    public PulsarClient createPulsarClient() {

        try {
           this.client =  PulsarClient.builder()
                    .serviceUrl(SERVICE_URL)
                    .authentication(AuthenticationFactory.token(PULSAR_TOKEN))
                    .build();
            // Subscribe to topic to listen on messages.
            if ( this.consumer == null  ) {
                subscribeConsumer(SUBSCRIPTION_NAME);
            }
        }catch (PulsarClientException pce) {
            logger.debug("Pulsar Consumer exception", pce);
        }
        return this.client;
    }

    public void subscribeConsumer(String subscriptionName){
        // Create consumer on a topic with a subscription
        if ( client != null ) {
            try {
                this.consumer = client.newConsumer(
                                Schema.KeyValue(
                                        Schema.AVRO(BookCDC.Key.class),
                                        Schema.AVRO(BookCDC.Value.class),
                                KeyValueEncodingType.SEPARATED))
                        .consumerName("astra-cdc-demo-app")
                        .subscriptionType(SubscriptionType.Exclusive)
                        .topic(PULSAR_DATA_TOPIC_URL)
                        .subscriptionName(subscriptionName)
                        .subscribe();
            } catch (PulsarClientException e) {
                logger.debug("createPulsarConsumer threw an exception", e);
            }
        }
    }
    public void closePulsarConsumer() throws PulsarClientException{
        // Create consumer on a topic with a subscription
        if ( consumer != null) {
            pulserConsumerEnabled = false;
            this.consumer.close();
            this.consumer = null;
        }
    }
    public long pausePulsarConsumer()  {
        pulserConsumerEnabled = false;
        if ( consumer != null ) {
            consumer.pause();
            return consumer.getLastDisconnectedTimestamp();
        }
        return 0;
    }
    public long resumePulsarConsumer() {
        pulserConsumerEnabled = false;
        if ( consumer != null ) {
            consumer.resume();
            return consumer.getLastDisconnectedTimestamp();
        }
        return 0;
    }
    public void unsubscribePulsarConsumer() throws PulsarClientException {
        pulserConsumerEnabled = false;
        if ( consumer != null ) {
            this.consumer.unsubscribe();
            //  this.consumer.close();
            this.consumer = null;
        }
    }


    private String pulsarMsgToString(Message msg) {
        String formattedMessage  = null;
        if ( msg != null && msg.getKey() != null ) {
            KeyValue<BookCDC.Key,BookCDC.Value> key = (KeyValue)msg.getValue();

            if ( formattedMessage == null ) {
                formattedMessage = key.getKey().toString();
                if ( key.getValue() != null ) {
                    formattedMessage = formattedMessage
                            + System.lineSeparator() + "Updated properties:"
                            + System.lineSeparator() + key.getValue().toString();
                }
            }
            logger.debug("pulsarMsgToString msg.getReaderSchema(): " + msg.getReaderSchema());
            logger.debug("pulsarMsgToString msg.getKey(): " + msg.getKey());
            logger.debug("pulsarMsgToString msg.getValue(): " + msg.getValue());
        }
        return formattedMessage;
    }

    public void stopAsynchConsumer()  {
        pulserConsumerEnabled = false;
    }
    public void startAsynchConsumer(CollaborationEngine collaborationEngine, String topicId )  {

        // Initiate new asynchronous pulsar msg listener if not already running
        if ( ! pulserConsumerEnabled && this.consumer != null ) {


            UserInfo cdcBotUser = new UserInfo("astra-cdc", "Astra CDC", "https://plugins.jetbrains.com/files/17013/169775/icon/pluginIcon.svg");
            ConnectionContext context = collaborationEngine.getSystemContext();
            MessageManager messageManager = new MessageManager(context, cdcBotUser, topicId, collaborationEngine);

            new Thread(()  -> { // Message Retrieval Thread

                logger.debug("STARTING PULSAR CONSUMER THREAD: " + Thread.currentThread().getId());
                CompletableFuture<Message> future;
                pulserConsumerEnabled = true;

                while ( pulserConsumerEnabled && (future = consumer.receiveAsync()) != null) {

                    try {
                        Message msg = future.get();
                        String pulsarMsg = pulsarMsgToString(msg);
                        if ( pulsarMsg != null ){

                            //MessageManager cdcMsgmanager = new MessageManager(manager, cdcBotUser,topicId );
                            CollaborationMessage cdcEventMsg = new CollaborationMessage(cdcBotUser, pulsarMsg, Instant.now());
                            messageManager.submit(cdcEventMsg);
                        }
                        // Acknowledge message is received.
                        consumer.acknowledgeAsync(msg);
                    } catch (Exception e) {
                        logger.debug("Exception processing Pulsar message", e);
                    }
                }
                // Close messageManager, thread about to close
                messageManager.close();
                logger.debug("LEAVING PULSAR CONSUMER THREAD: " + Thread.currentThread().getId());
            }).start();
        }
    }

    public void sendPulsarMessage(CollaborationMessagePersister.PersistRequest req)  {
        if (client != null) {
            try {
                // Create producer on a topic
                Producer<byte[]> producer = client.newProducer()
                        //.topic(PULSAR_TENANT+"/"+PULSAR_NAMESPACE+"/"+PULSAR_TOPIC)
                        .topic(PULSAR_DATA_TOPIC_URL)
                        .producerName(req.getMessage().getUser().getName())
                        .create();

                // Send a message to the topic
                producer.send(req.getMessage().getText().getBytes());

                //Close the producer
                producer.close();
            } catch (PulsarClientException pce) {
                logger.debug("Exception while Pulsar sendMessage", pce);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Closing Pulsar connections");
        if ( consumer != null && consumer.isConnected() ) {
            try {
                consumer.unsubscribe();
                consumer.close();
                if (! client.isClosed() ) {
                    client.close();
                }
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
