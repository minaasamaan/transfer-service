package com.mybank.transferservice.model;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class Account{
    private UUID id;
    private double balance;
}
