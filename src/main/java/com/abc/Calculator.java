package com.abc;

import com.sun.istack.internal.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Calculator {

    public static final MathContext MATH_CONT = MathContext.DECIMAL128;
    // 1 + rate/365
    public static BigDecimal dailyInterestRate(@NotNull BigDecimal annualRate) {
        return annualRate.divide(DAYS_IN_YEAR, MATH_CONT)
                .add(new BigDecimal(1), MATH_CONT);
    }

    public static final BigDecimal DAYS_IN_YEAR = new BigDecimal(365);

    private static final BigDecimal CHECKING_ANNUAL_RATE = new BigDecimal("0.001");
    private static final BigDecimal CHECKING_DAILY_INTEREST_RATE = dailyInterestRate(CHECKING_ANNUAL_RATE);

    private static final BigDecimal SAVINGS_ANNUAL_RATE_ONE = new BigDecimal("0.001");
    private static final BigDecimal SAVINGS_DAILY_INTEREST_RATE_ONE = dailyInterestRate(SAVINGS_ANNUAL_RATE_ONE);
    private static final BigDecimal SAVINGS_ANNUAL_RATE_TWO = new BigDecimal("0.002");
    private static final BigDecimal SAVINGS_DAILY_INTEREST_RATE_TWO = dailyInterestRate(SAVINGS_ANNUAL_RATE_TWO);
    private static final BigDecimal SAVINGS_LIMIT = new BigDecimal(1000);

    public static class CheckingInterest<T extends Transaction> extends SimpleInterest<T> {

        @Override
        public void process(Stream<T> stream) {
            stream = appendDummyTransaction(stream);
            stream.forEach(transaction -> {
                BigDecimal interestPlusBalance = _interest.add(_balance, MATH_CONT);
                BigDecimal accruedInterest = accruedInterest(interestPlusBalance, CHECKING_DAILY_INTEREST_RATE,
                        _prev.getDateTime(), transaction.getDateTime());
                iterateToNextTransaction(accruedInterest, transaction);
            });
        }
    }

    public static class SavingsInterest<T extends Transaction> extends SimpleInterest<T> {

        @Override
        public void process(Stream<T> stream) {
            stream = appendDummyTransaction(stream);
            stream.forEach(transaction -> {
                BigDecimal interestPlusBalance = _interest.add(_balance, MATH_CONT);
                BigDecimal accruedInterest;
                if (interestPlusBalance.compareTo(SAVINGS_LIMIT) <= 0) {
                    accruedInterest = accruedInterest(interestPlusBalance, SAVINGS_DAILY_INTEREST_RATE_ONE,
                            _prev.getDateTime(), transaction.getDateTime());
                }
                else {
                    accruedInterest = accruedInterest(SAVINGS_LIMIT, SAVINGS_DAILY_INTEREST_RATE_ONE,
                            _prev.getDateTime(), transaction.getDateTime());
                    interestPlusBalance = interestPlusBalance.subtract(SAVINGS_LIMIT, MATH_CONT);
                    accruedInterest = accruedInterest.add(
                            accruedInterest(interestPlusBalance, SAVINGS_DAILY_INTEREST_RATE_TWO,
                                    _prev.getDateTime(), transaction.getDateTime()),
                            MATH_CONT);

                }
                iterateToNextTransaction(accruedInterest, transaction);
            });
        }
    }

    public static abstract class SimpleInterest<T extends Transaction> implements StreamPipeline<T> {

        @Override
        public abstract void process(Stream<T> stream);

        protected BigDecimal _interest = new BigDecimal(0);
        protected BigDecimal _balance  = new BigDecimal(0);
        protected Transaction _prev = new Transaction(new BigDecimal(0), LocalDateTime.of(1970, 1, 1, 0, 0));

        protected Stream<T> appendDummyTransaction(Stream<T> stream) {
            ArrayList<T> dummyList = new ArrayList<>();
            dummyList.add((T)new Transaction(new BigDecimal(0), LocalDateTime.now()));
            return Stream.concat(stream, dummyList.stream());
        }

        protected void iterateToNextTransaction(BigDecimal accruedInterest, Transaction transaction) {
            _interest = _interest.add(accruedInterest, MATH_CONT);
            _balance = _balance.add(transaction.getAmount(), MATH_CONT);
            _prev = transaction;
        }

        @Override
        public BigDecimal getInterest() {
            return _interest;
        }

        @Override
        public BigDecimal getBalance() {
            return _balance;
        }

    }

    public static <T extends Transaction> BigDecimal accountTotal(StreamPipeline<T> pipeline) {
        return pipeline.getBalance().add(pipeline.getInterest(), MATH_CONT);
    }

    public static BigDecimal accruedInterest(@NotNull BigDecimal amount, BigDecimal dailyInterest,
                                             @NotNull LocalDateTime start, @NotNull LocalDateTime end) {
        long accrualLength =
                Calculator.accrualDateDifference(start, end);
        BigDecimal interestRate = dailyInterest.pow((int) accrualLength, MATH_CONT);
        BigDecimal futureValue = amount.multiply(interestRate, MATH_CONT);
        return futureValue.subtract(amount, MATH_CONT);
    }

    public static long accrualDateDifference(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {
        LocalDateTime accrualStart = beginningOfNextDay(start);
        LocalDateTime accrualEnd   = beginningOfNextDay(end);
        Duration duration = Duration.between(accrualStart, accrualEnd);
        long daysBetween = duration.toDays();
        if (daysBetween < 0L)
            throw new IllegalArgumentException(
                    "Accrual start date("+accrualStart+") must be before accrual end date("+accrualEnd+").");
        return daysBetween;
    }

    public static LocalDateTime beginningOfNextDay(@NotNull LocalDateTime dateTime) {
        LocalDateTime nextDay = dateTime.plusDays(1L);
        return LocalDateTime.of(nextDay.getYear(), nextDay.getMonth(), nextDay.getDayOfMonth(), 0, 0);
    }
}
