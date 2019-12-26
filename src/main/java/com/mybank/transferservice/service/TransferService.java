package com.mybank.transferservice.service;

import com.mybank.transferservice.repository.AccountRepository;
import com.mybank.transferservice.service.exception.AccountNotFoundException;
import com.mybank.transferservice.service.exception.NotEnoughBalanceException;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

@AllArgsConstructor
public class TransferService {

    private Jdbi jdbi;

    public UUID transfer(UUID fromAccount, UUID toAccount, double amount) throws AccountNotFoundException {

        return jdbi.inTransaction(handle -> {
            AccountRepository accountRepository= handle.attach(AccountRepository.class);

            //Assumptions:
            //1- There's no hard deletion for accounts
            //2- Account status (i.e. inactive accounts) is out-of-scope here!
            //Note: selects are inside the trx to reuse the opened connection..
            accountRepository.findBy(fromAccount).orElseThrow(()-> new AccountNotFoundException(fromAccount));
            accountRepository.findBy(toAccount).orElseThrow(()-> new AccountNotFoundException(toAccount));

            if(accountRepository.debit(fromAccount, amount)< 1){
                throw new NotEnoughBalanceException(fromAccount);
            }

            assert(accountRepository.credit(toAccount, amount)==1);
            return UUID.randomUUID();
        });
    }
}
