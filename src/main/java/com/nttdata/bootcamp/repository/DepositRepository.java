package com.nttdata.bootcamp.repository;

import com.nttdata.bootcamp.entity.Deposit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Mongodb Repository
public interface DepositRepository extends ReactiveCrudRepository<Deposit, String> {
    Mono<Deposit> findByDepositNumber(String depositNumber);

    Flux<Deposit> findByAccountNumber(String accountNumber);

    Flux<Deposit> findByCommissionGreaterThan(double commission);
}
