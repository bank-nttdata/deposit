//package com.nttdata.bootcamp.service.impl;
//
//import com.nttdata.bootcamp.entity.Deposit;
//import com.nttdata.bootcamp.repository.DepositRepository;
//import com.nttdata.bootcamp.service.DepositService;
//import com.nttdata.bootcamp.service.KafkaService;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@Service
//public class DepositServiceImpl implements DepositService {
//
//    private final DepositRepository depositRepository;
//    private final KafkaService kafkaService;
//
//    public DepositServiceImpl(DepositRepository depositRepository,
//                              KafkaService kafkaService) {
//        this.depositRepository = depositRepository;
//        this.kafkaService = kafkaService;
//    }
//
//    @Override
//    public Flux<Deposit> findAll() {
//        return depositRepository.findAll();
//    }
//
//    @Override
//    public Flux<Deposit> findByAccountNumber(String accountNumber) {
//        return depositRepository
//                .findAll()
//                .filter(x -> accountNumber.equals(x.getAccountNumber()));
//    }
//
//    @Override
//    public Mono<Deposit> findByNumber(String number) {
//        return depositRepository
//                .findAll()
//                .filter(x -> number.equals(x.getDepositNumber()))
//                .next();
//    }
//
//    @Override
//    public Mono<Deposit> saveDeposit(Deposit dataDeposit) {
//
//        return findByNumber(dataDeposit.getDepositNumber())
//                // If the deposit exists → throw error
//                .flatMap(existing ->
//                        Mono.<Deposit>error(new RuntimeException(
//                                "Deposit number " + dataDeposit.getDepositNumber() + " already exists"
//                        ))
//                )
//                // If not exists → save and publish to Kafka
//                .switchIfEmpty(
//                        depositRepository.save(dataDeposit)
//                                .flatMap(saved ->
//                                        kafkaService.publish(saved)
//                                                .thenReturn(saved)
//                                )
//                );
//    }
//
//    @Override
//    public Mono<Deposit> updateDeposit(Deposit dataDeposit) {
//
//        return findByNumber(dataDeposit.getDepositNumber())
//                .switchIfEmpty(
//                        Mono.error(new RuntimeException(
//                                "Deposit " + dataDeposit.getDepositNumber() + " does not exist"
//                        ))
//                )
//                .flatMap(original -> {
//                    // preserve immutable fields
//                    dataDeposit.setDni(original.getDni());
//                    dataDeposit.setAmount(original.getAmount());
//                    dataDeposit.setCreationDate(original.getCreationDate());
//                    return depositRepository.save(dataDeposit);
//                });
//    }
//
//    @Override
//    public Mono<Void> deleteDeposit(String number) {
//
//        return findByNumber(number)
//                .switchIfEmpty(
//                        Mono.error(new RuntimeException(
//                                "Deposit number " + number + " does not exist"
//                        ))
//                )
//                .flatMap(depositRepository::delete);
//    }
//
//    @Override
//    public Flux<Deposit> findByCommission(String accountNumber) {
//        return depositRepository
//                .findAll()
//                .filter(x -> x.getCommission() > 0);
//    }
//}

package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Deposit;
import com.nttdata.bootcamp.repository.DepositRepository;
import com.nttdata.bootcamp.service.DepositService;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DepositServiceImpl implements DepositService {

    private final DepositRepository depositRepository;
    private final KafkaService kafkaService;

    public DepositServiceImpl(DepositRepository depositRepository,
                              KafkaService kafkaService) {
        this.depositRepository = depositRepository;
        this.kafkaService = kafkaService;
    }

    // -------------------------------
    // LISTAR TODOS
    // -------------------------------
    @Override
    public Flux<Deposit> findAll() {
        return depositRepository.findAll();
    }

    // -------------------------------
    // BUSCAR POR CUENTA
    // -------------------------------
    @Override
    public Flux<Deposit> findByAccountNumber(String accountNumber) {
        return depositRepository.findByAccountNumber(accountNumber);
    }

    // -------------------------------
    // BUSCAR POR NÚMERO DE DEPÓSITO
    // -------------------------------
    @Override
    public Mono<Deposit> findByNumber(String number) {
        return depositRepository.findByDepositNumber(number);
    }

    // -------------------------------
    // CREAR DEPÓSITO
    // -------------------------------
    @Override
    public Mono<Deposit> saveDeposit(Deposit dataDeposit) {

        return findByNumber(dataDeposit.getDepositNumber())
                .flatMap(existing ->
                        Mono.<Deposit>error(
                                new RuntimeException("Deposit number " +
                                        dataDeposit.getDepositNumber() + " already exists")
                        )
                )
                .switchIfEmpty(
                        depositRepository.save(dataDeposit)
                                .flatMap(saved ->
                                        kafkaService.publish(saved).thenReturn(saved)
                                )
                );
    }

    // -------------------------------
    // ACTUALIZAR DEPÓSITO
    // -------------------------------
    @Override
    public Mono<Deposit> updateDeposit(Deposit dataDeposit) {

        return findByNumber(dataDeposit.getDepositNumber())
                .switchIfEmpty(
                        Mono.error(new RuntimeException(
                                "Deposit " + dataDeposit.getDepositNumber() + " does not exist"
                        ))
                )
                .flatMap(original -> {
                    // Mantener campos que no deben cambiar
                    dataDeposit.setDni(original.getDni());
                    dataDeposit.setAmount(original.getAmount());
                    dataDeposit.setCreationDate(original.getCreationDate());

                    return depositRepository.save(dataDeposit);
                });
    }

    // -------------------------------
    // ELIMINAR DEPÓSITO
    // -------------------------------
    @Override
    public Mono<Void> deleteDeposit(String number) {

        return findByNumber(number)
                .switchIfEmpty(
                        Mono.error(new RuntimeException(
                                "Deposit number " + number + " does not exist"
                        ))
                )
                .flatMap(depositRepository::delete);
    }

    // -------------------------------
    // BUSCAR POR COMISIÓN > 0
    // -------------------------------
    @Override
    public Flux<Deposit> findByCommission(String accountNumber) {
        return depositRepository
                .findByCommissionGreaterThan(0)
                .filter(x -> x.getAccountNumber().equals(accountNumber));
    }
}
