package com.abc;

public class SavingsAccount extends Account {

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public StreamPipeline<Transaction> getCalculator() {
        return new Calculator.SavingsInterest<>();
    }
}
