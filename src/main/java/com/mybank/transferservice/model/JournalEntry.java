package com.mybank.transferservice.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class JournalEntry {
    private UUID id;
    private UUID accountId;
    private UUID correlationId;
    private double amount;
    private String description;
    private Instant createdAt;
}
