package com.nttdata.bootcamp.events;

import com.nttdata.bootcamp.entity.Deposit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepositCreatedEventKafka extends EventKafka<Deposit> {

}
