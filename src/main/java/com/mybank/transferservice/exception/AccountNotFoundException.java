package com.mybank.transferservice.exception;

import javax.ws.rs.NotFoundException;
import java.util.UUID;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(UUID id) {
        super(String.format("No accounts exist with id: %s", id));
    }
}
