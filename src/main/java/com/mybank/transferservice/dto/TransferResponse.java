package com.mybank.transferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private UUID transactionId;
    private UUID beneficiaryTransactionId;
    private UUID correlationId;
}
