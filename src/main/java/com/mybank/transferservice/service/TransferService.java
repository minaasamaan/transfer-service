package com.mybank.transferservice.service;

import com.mybank.transferservice.exception.AccountNotFoundException;
import com.mybank.transferservice.exception.NotEnoughBalanceException;
import com.mybank.transferservice.model.JournalEntry;
import com.mybank.transferservice.repository.AccountRepository;
import com.mybank.transferservice.repository.JournalEntryRepository;
import com.mybank.transferservice.vo.TransferVo;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mybank.transferservice.util.Validators.validateOrThrow;

@AllArgsConstructor
public class TransferService {

    private static final Logger logger = Logger.getLogger(TransferService.class.getName());

    private Jdbi jdbi;

    /**
     * Executes money transfer in an atomic transaction,
     *
     * @param fromAccount
     * @param toAccount
     * @param amount
     * @return
     * @throws AccountNotFoundException
     * @throws NotEnoughBalanceException
     * @throws IllegalArgumentException
     */
    public TransferVo transfer(UUID fromAccount, UUID toAccount, double amount) {

        logger.log(Level.INFO, String.format("Starting transfer of amount %s from account: %s to account %s", amount, fromAccount, toAccount));

        Objects.requireNonNull(fromAccount);
        Objects.requireNonNull(toAccount);

        validateOrThrow(() -> amount >= 1, new IllegalArgumentException("Amount must be >=1"));
        validateOrThrow(() -> !fromAccount.equals(toAccount), new IllegalArgumentException("Can't transfer money to self!"));

        TransferVo transferVo = doTransfer(fromAccount, toAccount, amount);

        logger.log(Level.INFO, String.format("Transfer transaction of amount %s from account: %s to account %s has been committed", amount, fromAccount, toAccount));

        return transferVo;
    }

    private TransferVo doTransfer(UUID fromAccount, UUID toAccount, double amount) {

        return jdbi.inTransaction(handle -> {
            AccountRepository accountRepository = handle.attach(AccountRepository.class);
            JournalEntryRepository journalRepository = handle.attach(JournalEntryRepository.class);

            List<UUID> ids = accountRepository.lockAndGet(fromAccount, toAccount);// locking here to avoid deadlocks in case of both accounts transferring money at the same time!

            validateAccountsExistOrThrowException(ids, fromAccount, toAccount);

            validateOrThrow(() -> accountRepository.debit(fromAccount, amount) == 1, new NotEnoughBalanceException(fromAccount));

            assert (accountRepository.credit(toAccount, amount) == 1);

            UUID correlationId = UUID.randomUUID();

            String transferDescription = String.format("Transfer transaction with correlationId: %s of amount: %s from account: %s to account: %s"
                    , correlationId, amount, fromAccount, toAccount);

            Instant createdAt = Instant.now();

            JournalEntry debitJournalEntry = JournalEntry.builder()
                    .id(UUID.randomUUID())
                    .accountId(fromAccount)
                    .correlationId(correlationId)
                    .amount(-amount)
                    .description(transferDescription)
                    .createdAt(createdAt)
                    .build();

            JournalEntry creditJournalEntry = JournalEntry.builder()
                    .id(UUID.randomUUID())
                    .accountId(toAccount)
                    .correlationId(correlationId)
                    .amount(amount)
                    .description(transferDescription)
                    .createdAt(createdAt)
                    .build();

            journalRepository.create(debitJournalEntry);
            journalRepository.create(creditJournalEntry);

            return TransferVo.builder()
                    .debitTransactionId(debitJournalEntry.getId())
                    .creditTransactionId(creditJournalEntry.getId())
                    .correlationId(correlationId)
                    .createdAt(createdAt)
                    .build();
        });
    }

    private void validateAccountsExistOrThrowException(List<UUID> ids, UUID fromAccount, UUID toAccount) {
        if (ids.size() < 2) {
            if (!ids.contains(fromAccount)) {
                throw new AccountNotFoundException(fromAccount);
            } else {
                throw new AccountNotFoundException(toAccount);
            }
        }
    }
}
