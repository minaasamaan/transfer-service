package com.mybank.transferservice.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private UUID beneficiaryId;
    private double amount;
}
