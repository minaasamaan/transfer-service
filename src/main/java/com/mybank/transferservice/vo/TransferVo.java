package com.mybank.transferservice.vo;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class TransferVo {
    private UUID debitTransactionId;
    private UUID creditTransactionId;
    private UUID correlationId;
}
