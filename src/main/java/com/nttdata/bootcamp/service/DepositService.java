package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Deposit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Interface Service
public interface DepositService {

    public Flux<Deposit> findAll();
    public Flux<Deposit> findByAccountNumber(String accountNumber);

    public Mono<Deposit> findByNumber(String number);
    public Mono<Deposit> saveDeposit(Deposit active);
    public Mono<Deposit> updateDeposit(Deposit dataActive);
    public Mono<Void> deleteDeposit(String accountNumber);

    public Flux<Deposit> findByCommission(String accountNumber);


}
