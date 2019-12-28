package com.mybank.transferservice.dto;

import lombok.*;

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
