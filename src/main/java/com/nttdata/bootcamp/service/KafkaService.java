package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Deposit;
import reactor.core.publisher.Mono;

public interface KafkaService {
    Mono<Void> publish(Deposit deposit);
}
