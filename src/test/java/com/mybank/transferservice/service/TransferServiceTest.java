package com.mybank.transferservice.service;

import com.mybank.transferservice.AbstractIntegrationTest;
import com.mybank.transferservice.model.Account;
import com.mybank.transferservice.repository.AccountRepository;
import com.mybank.transferservice.service.exception.AccountNotFoundException;
import com.mybank.transferservice.service.exception.NotEnoughBalanceException;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransferServiceTest extends AbstractIntegrationTest {

    private TransferService testee;
    private AccountRepository accountRepository;

    @Override
    protected void doBeforeEach(Jdbi jdbi) {
        //empty implementation
    }

    @Override
    protected void doBeforeAll(Jdbi jdbi) {
        testee= new TransferService(jdbi);
        accountRepository= jdbi.onDemand(AccountRepository.class);
    }

    @Test
    public void transferWorks() {
        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(100).build();
        Account account2= Account.builder().id(UUID.randomUUID()).balance(50).build();

        accountRepository.create(account1);
        accountRepository.create(account2);

        //when
        UUID trxId= testee.transfer(account1.getId(), account2.getId(), 24.99);

        //then
        assertEquals(accountRepository.findBy(account1.getId()).get().getBalance(),75.01);
        assertEquals(accountRepository.findBy(account2.getId()).get().getBalance(),74.99);
    }

    @Test
    public void shouldHandleAccountNotFound() {
        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(100).build();

        accountRepository.create(account1);

        //when
        assertThrows(AccountNotFoundException.class, ()-> testee.transfer(account1.getId(), UUID.randomUUID(), 24.99));
        assertThrows(AccountNotFoundException.class, ()-> testee.transfer(UUID.randomUUID(), account1.getId(), 24.99));

        //then
        assertEquals(accountRepository.findBy(account1.getId()).get().getBalance(),100);
    }

    @Test
    public void shouldHandleInsufficientBalance() {
        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(24.98).build();
        Account account2= Account.builder().id(UUID.randomUUID()).balance(50).build();

        accountRepository.create(account1);
        accountRepository.create(account2);

        //when
        assertThrows(NotEnoughBalanceException.class, ()-> testee.transfer(account1.getId(), account2.getId(), 24.99));

        //then
        assertEquals(accountRepository.findBy(account1.getId()).get().getBalance(),24.98);
        assertEquals(accountRepository.findBy(account2.getId()).get().getBalance(),50);
    }
}
