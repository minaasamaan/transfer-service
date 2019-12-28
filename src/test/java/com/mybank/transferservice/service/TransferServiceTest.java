package com.mybank.transferservice.service;

import com.mybank.transferservice.AbstractIntegrationTest;
import com.mybank.transferservice.exception.AccountNotFoundException;
import com.mybank.transferservice.exception.NotEnoughBalanceException;
import com.mybank.transferservice.model.Account;
import com.mybank.transferservice.vo.TransferVo;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferServiceTest extends AbstractIntegrationTest {

    private TransferService testee;

    @Override
    protected void doBeforeAll(Jdbi jdbi) {
        testee = new TransferService(jdbi);
    }

    @Test
    public void transferWorks() {

        //given
        Account fromAccount = createAccount(UUID.randomUUID(), 100);
        Account toAccount = createAccount(UUID.randomUUID(), 50);

        //when
        TransferVo actual = testee.transfer(fromAccount.getId(), toAccount.getId(), 24.99);

        //then
        verifyAccountBalance(fromAccount.getId(), 75.01);
        verifyAccountBalance(toAccount.getId(), 74.99);

        verifyJournalEntry(actual.getDebitTransactionId(), fromAccount.getId(), actual.getCorrelationId(), -24.99);
        verifyJournalEntry(actual.getCreditTransactionId(), toAccount.getId(), actual.getCorrelationId(), 24.99);
    }

    @Test
    public void shouldHandleAccountNotFound() {

        //given
        Account fromAccount = createAccount(UUID.randomUUID(), 100);

        //when
        assertThrows(AccountNotFoundException.class, () -> testee.transfer(fromAccount.getId(), UUID.randomUUID(), 24.99));
        assertThrows(AccountNotFoundException.class, () -> testee.transfer(UUID.randomUUID(), fromAccount.getId(), 24.99));

        //then
        verifyAccountBalance(fromAccount.getId(), 100);
    }

    @Test
    public void shouldHandleInsufficientBalance() {

        //given
        Account fromAccount = createAccount(UUID.randomUUID(), 24.98);
        Account toAccount = createAccount(UUID.randomUUID(), 50);

        //when
        assertThrows(NotEnoughBalanceException.class, () -> testee.transfer(fromAccount.getId(), toAccount.getId(), 24.99));

        //then
        verifyAccountBalance(fromAccount.getId(), 24.98);
        verifyAccountBalance(toAccount.getId(), 50);
    }
}
