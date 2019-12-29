package com.mybank.transferservice.service;

import com.mybank.transferservice.AbstractIntegrationTest;
import com.mybank.transferservice.exception.AccountNotFoundException;
import com.mybank.transferservice.exception.NotEnoughBalanceException;
import com.mybank.transferservice.model.Account;
import com.mybank.transferservice.vo.TransferVo;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Account fromAccount = createAccount(100);
        Account toAccount = createAccount(50);

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
        Account fromAccount = createAccount(100);

        //when
        assertThrows(AccountNotFoundException.class, () -> testee.transfer(fromAccount.getId(), UUID.randomUUID(), 24.99));
        assertThrows(AccountNotFoundException.class, () -> testee.transfer(UUID.randomUUID(), fromAccount.getId(), 24.99));

        //then
        verifyAccountBalance(fromAccount.getId(), 100);
    }

    @Test
    public void shouldHandleInsufficientBalance() {

        //given
        Account fromAccount = createAccount(24.98);
        Account toAccount = createAccount(50);

        //when
        assertThrows(NotEnoughBalanceException.class, () -> testee.transfer(fromAccount.getId(), toAccount.getId(), 24.99));

        //then
        verifyAccountBalance(fromAccount.getId(), 24.98);
        verifyAccountBalance(toAccount.getId(), 50);
    }

    @Test
    public void shouldStayConsistentWhileExecutingConcurrentCalls(){

        //given
        final Account fromAccount = createAccount(1275);
        final Account toAccount = createAccount(1275);

        double expectedBalance = fromAccount.getBalance() + toAccount.getBalance();

        IntStream.rangeClosed(1, 50).parallel().forEach(number -> {

            if(number%2==0){
                testee.transfer(fromAccount.getId(), toAccount.getId(), number);
            }else{
                testee.transfer(toAccount.getId(), fromAccount.getId(), number);
            }
        });

        Account refreshedFromAccount = getAccount(fromAccount.getId()).get();
        Account refreshedToAccount = getAccount(toAccount.getId()).get();

        double actualBalance = refreshedFromAccount.getBalance() + refreshedToAccount.getBalance();

        assertEquals(expectedBalance, actualBalance);
    }
}
