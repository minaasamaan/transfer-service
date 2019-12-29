package com.mybank.transferservice;

import com.mybank.transferservice.dto.TransferRequest;
import com.mybank.transferservice.dto.TransferResponse;
import com.mybank.transferservice.model.Account;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ApplicationE2ETest extends AbstractIntegrationTest {

    @Test
    public void shouldTransferMoneySuccessfully() {

        //given
        Account fromAccount = createAccount(100);
        Account toAccount = createAccount(50);

        TransferRequest request = TransferRequest.builder().beneficiaryId(toAccount.getId()).amount(24.99).build();

        //when
        Response response = newRequest()
                .path(String.format("accounts/%s/transfers", fromAccount.getId()))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        TransferResponse transferResponse = response.readEntity(TransferResponse.class);

        //then
        assertEquals(200, response.getStatus());

        verifyAccountBalance(fromAccount.getId(), 75.01);
        verifyAccountBalance(toAccount.getId(), 74.99);

        verifyJournalEntry(transferResponse.getTransactionId(), fromAccount.getId(), transferResponse.getCorrelationId(), -24.99);
        verifyJournalEntry(transferResponse.getBeneficiaryTransactionId(), toAccount.getId(), transferResponse.getCorrelationId(), 24.99);
    }
}
