package com.abc;

import com.sun.istack.internal.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

import static com.abc.Calculator.MATH_CONT;

public abstract class Account {

    public abstract AccountType getAccountType();
    public abstract StreamPipeline<Transaction> getCalculator();

    private final UUID _uuid = UUID.randomUUID();
    private final List<Transaction> _transactions = new ArrayList<>();
    private final StampedLock _lock = new StampedLock();

    protected boolean processWithOptimisticLock(StreamPipeline<Transaction> pipeline) {
        long stamp = _lock.tryOptimisticRead();
        if (stamp == 0L)
            return false;
        pipeline.process(_transactions.stream());
        return _lock.validate(stamp);
    }

    protected void processWithReadLock(StreamPipeline<Transaction> pipeline) {
        long stamp = _lock.readLock();
        try {
            pipeline.process(_transactions.stream());
        } finally {
            _lock.unlock(stamp);
        }
    }

    protected void processTransactions(StreamPipeline<Transaction> pipeline) {
        if (! processWithOptimisticLock(pipeline))
            processWithReadLock(pipeline);
    }

    public void deposit(BigDecimal amount) throws BankException {
        if (amount.signum() <= 0)
            throw new IllegalArgumentException("Deposit amount(" + amount.toString() + ") must be greater than zero.");
        addTransaction(new Transaction(amount), false);
    }

    public void withdraw(BigDecimal amount) throws BankException {
        if (amount.signum() <= 0)
            throw new IllegalArgumentException("Withdraw amount(" + amount.toString() + ") must be greater than zero.");
        addTransaction(new Transaction(amount.negate()), true);
    }

    /**
     * transfers funds from this account to anotherAccount
     * @param amount amount to transfer if there are sufficient funds
     * @param anotherAccount account to transfer to
     * @throws BankException
     */
    public void transfer(BigDecimal amount, Account anotherAccount) throws BankException {
        if (amount.signum() <= 0)
            throw new IllegalArgumentException("Transfer amount(" + amount.toString() + ") must be greater than zero.");

        OrderedWriteLock orderedWriteLock = new OrderedWriteLock(this, anotherAccount);
        if (orderedWriteLock.writeLockInOrder()) {
            try {
                validateAvailableFunds(amount);
                _transactions.add(new Transaction(amount.negate()));
                anotherAccount._transactions.add(new Transaction(amount));
            } finally {
                orderedWriteLock.writeUnLockInReverseOrder();
            }
        }
    }

    private void validateAvailableFunds(BigDecimal amount) throws BankException {
        StreamPipeline<Transaction> calculator = getCalculator();
        calculator.process(_transactions.stream());
        BigDecimal availableFunds = calculator.getBalance().add(calculator.getInterest(), MATH_CONT);
        if (availableFunds.compareTo(amount) < 0)
            throw new BankException("Insufficient Funds");
    }

    private void addTransaction(Transaction transaction, boolean validateFundsFlag) throws BankException {
        long stamp = _lock.writeLock();
        try {
            if (validateFundsFlag)
                validateAvailableFunds(transaction.getAmount().abs());
            _transactions.add(transaction);
        } finally {
            _lock.unlock(stamp);
        }
    }

    // this is quick demo of the idea, kept private...
    // One way of improving:
    // - inherit from StampedLock to add unique ID
    // - add ordered container to hold those new locks
    // - sort locks to establish locking order
    // - add unlock policy
    private static class OrderedWriteLock {
        private StampedLock _lockOne;
        private StampedLock _lockTwo;
        private long _stampOne;
        private long _stampTwo;
        private volatile boolean _isLocked = false;

        private OrderedWriteLock(@NotNull Account account1, @NotNull Account account2)
                throws BankException {
            int comparison = account1._uuid.compareTo(account2._uuid);
            if (comparison == 0)
                throw new BankException("Non-unique UUID("+account1._uuid+") generated for "
                        +account1.getAccountType().captionString()+" and for "
                        +account2.getAccountType().captionString());
            if (comparison < 0) {
                _lockOne = account1._lock;
                _lockTwo = account2._lock;
            } else {
                _lockOne = account2._lock;
                _lockTwo = account1._lock;
            }
        }
        private boolean writeLockInOrder() {
            if (! _isLocked) {
                _stampOne = _lockOne.writeLock();
                _stampTwo = _lockTwo.writeLock();
                _isLocked = true;
                return true;
            }
            return false;
        }
        private boolean writeUnLockInReverseOrder() {
            if (_isLocked) {
                _lockTwo.unlock(_stampTwo);
                _lockOne.unlock(_stampOne);
                _isLocked = false;
                return true;
            }
            return false;
        }
    }
}
