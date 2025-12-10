package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Deposit;
import com.nttdata.bootcamp.entity.enums.EventType;
import com.nttdata.bootcamp.events.DepositCreatedEventKafka;
import com.nttdata.bootcamp.events.EventKafka;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.Date;
import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {

    private final KafkaSender<String, EventKafka<?>> sender;

    @Value("${topic.deposit.name}")
    private String topicDeposit;

    public KafkaServiceImpl(KafkaSender<String, EventKafka<?>> sender) {
        this.sender = sender;
    }

    @Override
    public Mono<Void> publish(Deposit deposit) {

        DepositCreatedEventKafka event = new DepositCreatedEventKafka();
        event.setId(UUID.randomUUID().toString());
        event.setType(EventType.CREATED);
        event.setDate(new Date());
        event.setData(deposit);

        return sender.send(
                        Mono.just(
                                SenderRecord.create(topicDeposit, null, null, deposit.getDepositNumber(), event, null)
                        )
                )
                .doOnNext(result -> {
                    System.out.println("Event sent to Kafka topic: " + topicDeposit);
                })
                .then(); // Mono<Void>
    }
}
