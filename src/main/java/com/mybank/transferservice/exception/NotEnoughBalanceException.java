package com.mybank.transferservice.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.UUID;

public class NotEnoughBalanceException extends ClientErrorException {
    public NotEnoughBalanceException(UUID id) {
        super(String.format("Not enough balance in account with id: %s", id), Response.Status.PRECONDITION_FAILED);
    }
}
