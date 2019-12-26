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
    protected void doBeforeEach(Jdbi jdbi) {
        //empty implementation
    }

    @Override
    protected void doBeforeAll(Jdbi jdbi){
        testee = jdbi.onDemand(AccountRepository.class);
    }

    @Test
    public void debitWorks() {

        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(100d).build();

        testee.create(account1);

        //when
        int rowsUpdated= testee.debit(account1.getId(), 24.99);

        //then
        assertEquals(1, rowsUpdated);

        assertEquals(testee.findBy(account1.getId()).get().getBalance(),75.01);
    }

    @Test
    public void shouldNotDebit_ifNotEnoughBalance() {

        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(100d).build();

        testee.create(account1);

        //when
        int rowsUpdated= testee.debit(account1.getId(), 124.99);

        //then
        assertEquals(0, rowsUpdated);

        assertEquals(testee.findBy(account1.getId()).get().getBalance(),100d);
    }

    @Test
    public void creditWorks() {

        //given
        Account account1= Account.builder().id(UUID.randomUUID()).balance(100d).build();

        testee.create(account1);

        //when
        int rowsUpdated= testee.credit(account1.getId(), 24.99);

        //then
        assertEquals(1, rowsUpdated);

        assertEquals(testee.findBy(account1.getId()).get().getBalance(),124.99);
    }
}
