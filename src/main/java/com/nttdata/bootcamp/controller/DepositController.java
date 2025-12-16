package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.Deposit;
import com.nttdata.bootcamp.entity.dto.DepositDto;
import com.nttdata.bootcamp.service.DepositService;
import com.nttdata.bootcamp.util.Constant;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/deposit")
public class DepositController{

    private static final Logger LOGGER = LoggerFactory.getLogger(DepositController.class);

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    // ====================================================================================
    // GET ALL DEPOSITS
    // ====================================================================================
    @GetMapping("/findAllDeposits")
    public Flux<Deposit> findAllDeposits() {
        return depositService.findAll()
                .doOnNext(d -> LOGGER.info("Registered deposit: {}", d));
    }

    // ====================================================================================
    // GET ALL DEPOSITS BY ACCOUNT NUMBER
    // ====================================================================================
    @GetMapping("/findAllDepositsByAccountNumber/{accountNumber}")
    public Flux<Deposit> findAllDepositsByAccountNumber(@PathVariable String accountNumber) {
        return depositService.findByAccountNumber(accountNumber)
                .doOnNext(d -> LOGGER.info("Deposit for {} : {}", accountNumber, d));
    }

    // ====================================================================================
    // GET DEPOSIT BY DEPOSIT NUMBER
    // ====================================================================================
    @CircuitBreaker(name = "deposits", fallbackMethod = "fallbackDeposit")
    @GetMapping("/findByDepositNumber/{numberDeposits}")
    public Mono<Deposit> findByDepositNumber(@PathVariable String numberDeposits) {
        LOGGER.info("Searching deposit by number: {}", numberDeposits);
        return depositService.findByNumber(numberDeposits);
    }

    // ====================================================================================
    // SAVE DEPOSIT (FULLY REACTIVE)
    // ====================================================================================
    @CircuitBreaker(name = "deposits", fallbackMethod = "fallbackDeposit")
    @PostMapping("/saveDeposits")
    public Mono<Deposit> saveDeposits(@RequestBody DepositDto dto) {

        // Step 1: Count current transactions reactively
        return getCountDeposits(dto.getAccountNumber())
                .flatMap(count -> {
                    Deposit deposit = new Deposit();
                    deposit.setDepositNumber(dto.getDepositNumber());
                    deposit.setAccountNumber(dto.getAccountNumber());
                    deposit.setDni(dto.getDni());
                    deposit.setAmount(dto.getAmount());
                    deposit.setTypeAccount(Constant.TYPE_ACCOUNT);
                    deposit.setStatus(Constant.STATUS_ACTIVE);
                    deposit.setCreationDate(new Date());
                    deposit.setModificationDate(new Date());
                    deposit.setCommission(
                            count > Constant.COUNT_TRANSACTION
                                    ? Constant.COMISSION
                                    : 0.00
                    );
                    LOGGER.info("Saving deposit: {}", deposit);
                    return depositService.saveDeposit(deposit);
                });
    }

    // ====================================================================================
    // UPDATE DEPOSIT
    // ====================================================================================
    @CircuitBreaker(name = "deposits", fallbackMethod = "fallbackDeposit")
    @PutMapping("/updateDeposit/{numberTransaction}")
    public Mono<Deposit> updateDeposit(@PathVariable String numberTransaction,
                                       @Valid @RequestBody Deposit dataDeposit) {

        // Reactive entity update
        dataDeposit.setDepositNumber(numberTransaction);
        dataDeposit.setModificationDate(new Date());
        LOGGER.info("Updating deposit {}", numberTransaction);
        return depositService.updateDeposit(dataDeposit);
    }

    // ====================================================================================
    // DELETE DEPOSIT
    // ====================================================================================
    @CircuitBreaker(name = "deposits", fallbackMethod = "fallbackVoid")
    @DeleteMapping("/deleteDeposits/{numberTransaction}")
    public Mono<Void> deleteDeposits(@PathVariable String numberTransaction) {
        LOGGER.info("Deleting deposit {}", numberTransaction);
        return depositService.deleteDeposit(numberTransaction);
    }

    // ====================================================================================
    // GET ALL COMMISSIONS BY ACCOUNT NUMBER
    // ====================================================================================
    @GetMapping("/getCommissionsDeposit/{accountNumber}")
    public Flux<Deposit> getCommissionsDeposit(@PathVariable String accountNumber) {
        return depositService.findByCommission(accountNumber)
                .doOnNext(c ->
                        LOGGER.info("Commission for {} : {}", accountNumber, c));
    }

    // ====================================================================================
    // GET COUNT OF DEPOSITS FOR AN ACCOUNT (REACTIVE)
    // ====================================================================================
    @GetMapping("/getCountTransaction/{accountNumber}")
    public Mono<Long> getCountDeposits(@PathVariable String accountNumber) {
        return depositService.findByAccountNumber(accountNumber).count();
    }

    // ====================================================================================
    // CIRCUIT BREAKER FALLBACK METHODS
    // ====================================================================================
    private Mono<Deposit> fallbackDeposit(Exception e) {
        LOGGER.error("Fallback error (Deposit): {}", e.getMessage());
        return Mono.just(new Deposit());
    }

    private Mono<Void> fallbackVoid(Exception e) {
        LOGGER.error("Fallback error (Void): {}", e.getMessage());
        return Mono.empty();
    }
}
