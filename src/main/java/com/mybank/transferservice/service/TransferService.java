package com.mybank.transferservice.service;

import com.mybank.transferservice.exception.AccountNotFoundException;
import com.mybank.transferservice.exception.NotEnoughBalanceException;
import com.mybank.transferservice.model.JournalEntry;
import com.mybank.transferservice.repository.AccountRepository;
import com.mybank.transferservice.repository.JournalEntryRepository;
import com.mybank.transferservice.vo.TransferVo;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class TransferService {

    private static final Logger logger = Logger.getLogger(TransferService.class.getName());

    private Jdbi jdbi;

    /**
     * @param fromAccount
     * @param toAccount
     * @param amount
     * @return
     * @throws AccountNotFoundException
     * @throws NotEnoughBalanceException
     */
    public TransferVo transfer(UUID fromAccount, UUID toAccount, double amount) throws AccountNotFoundException, NotEnoughBalanceException {

        logger.log(Level.INFO, String.format("Starting transfer of amount %s from account: %s to account %s", amount, fromAccount, toAccount));

        return jdbi.inTransaction(handle -> {
            AccountRepository accountRepository = handle.attach(AccountRepository.class);
            JournalEntryRepository journalRepository = handle.attach(JournalEntryRepository.class);

            List<UUID> ids= accountRepository.lockAndGet(fromAccount, toAccount);

            validateOrThrowException(ids, fromAccount, toAccount);

            if (accountRepository.debit(fromAccount, amount) < 1) {
                throw new NotEnoughBalanceException(fromAccount);
            }

            assert (accountRepository.credit(toAccount, amount) == 1);

            UUID correlationId = UUID.randomUUID();

            String transferDescription = String.format("Transfer transaction with correlationId: %s of amount: %s from account: %s to account: %s"
                    ,correlationId, amount, fromAccount, toAccount);

            JournalEntry debitJournalEntry = JournalEntry.builder()
                    .id(UUID.randomUUID())
                    .accountId(fromAccount)
                    .correlationId(correlationId)
                    .amount(-amount)
                    .description(transferDescription)
                    .build();

            JournalEntry creditJournalEntry = JournalEntry.builder()
                    .id(UUID.randomUUID())
                    .accountId(toAccount)
                    .correlationId(correlationId)
                    .amount(amount)
                    .description(transferDescription)
                    .build();

            journalRepository.create(debitJournalEntry);
            journalRepository.create(creditJournalEntry);

            return TransferVo.builder()
                    .debitTransactionId(debitJournalEntry.getId())
                    .creditTransactionId(creditJournalEntry.getId())
                    .correlationId(correlationId)
                    .build();
        });
    }

    private void validateOrThrowException(List<UUID> ids, UUID fromAccount, UUID toAccount) {
        if(ids.size()<2){
            if(!ids.contains(fromAccount)){
                throw new AccountNotFoundException(fromAccount);
            }else{
                throw new AccountNotFoundException(toAccount);
            }
        }
    }
}
