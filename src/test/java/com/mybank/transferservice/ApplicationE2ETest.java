package com.mybank.transferservice;

import com.mybank.transferservice.dto.TransferRequest;
import com.mybank.transferservice.dto.TransferResponse;
import com.mybank.transferservice.model.Account;
import com.mybank.transferservice.repository.AccountRepository;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ApplicationE2ETest extends AbstractIntegrationTest {

    private AccountRepository accountRepository;

    @Override
    protected void doBeforeEach(Jdbi jdbi) {

    }

    @Override
    protected void doBeforeAll(Jdbi jdbi) {
        accountRepository = jdbi.onDemand(AccountRepository.class);
    }

    @Test
    public void shouldTransferMoneySuccessfully() {

        //given
        Account account1 = Account.builder().id(UUID.randomUUID()).balance(100).build();
        Account account2 = Account.builder().id(UUID.randomUUID()).balance(50).build();

        accountRepository.create(account1);
        accountRepository.create(account2);

        TransferRequest request = TransferRequest.builder().beneficiaryId(account2.getId()).amount(24.99).build();
        //when
        Response response = newRequest()
                .path(String.format("accounts/%s/transfers", account1.getId()))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        TransferResponse transferResponse = response.readEntity(TransferResponse.class);

        //then
        assertEquals(200, response.getStatus());
        assertEquals(accountRepository.findBy(account1.getId()).get().getBalance(), 75.01);
        assertEquals(accountRepository.findBy(account2.getId()).get().getBalance(), 74.99);
    }
}
