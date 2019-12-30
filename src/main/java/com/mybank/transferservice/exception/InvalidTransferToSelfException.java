package com.mybank.transferservice.exception;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.UUID;

public class InvalidTransferToSelfException extends ClientErrorException {
    public InvalidTransferToSelfException(UUID id) {
        super(String.format("Can't transfer to self for account with id: %s", id), Response.Status.CONFLICT);
    }
}
