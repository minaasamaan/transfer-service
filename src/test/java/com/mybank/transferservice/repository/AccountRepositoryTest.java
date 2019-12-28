package com.mybank.transferservice.repository;

import com.mybank.transferservice.AbstractIntegrationTest;
import com.mybank.transferservice.model.Account;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountRepositoryTest extends AbstractIntegrationTest {

    private AccountRepository testee;

    @Override
    protected void doBeforeAll(Jdbi jdbi) {
        testee = jdbi.onDemand(AccountRepository.class);
    }

    @Test
    public void debitWorks() {

        //given
        Account account = createAccount(UUID.randomUUID(), 100);

        //when
        int rowsUpdated = testee.debit(account.getId(), 24.99);

        //then
        assertEquals(1, rowsUpdated);
        verifyAccountBalance(account.getId(), 75.01);
    }

    @Test
    public void shouldNotDebit_ifNotEnoughBalance() {

        //given
        Account account = createAccount(UUID.randomUUID(), 100);

        //when
        int rowsUpdated = testee.debit(account.getId(), 124.99);

        //then
        assertEquals(0, rowsUpdated);
        verifyAccountBalance(account.getId(), 100);
    }

    @Test
    public void creditWorks() {

        //given
        Account account = createAccount(UUID.randomUUID(), 100);

        //when
        int rowsUpdated = testee.credit(account.getId(), 24.99);

        //then
        assertEquals(1, rowsUpdated);
        verifyAccountBalance(account.getId(), 124.99);
    }
}
