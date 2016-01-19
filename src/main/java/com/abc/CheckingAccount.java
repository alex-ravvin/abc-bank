package com.abc;

public class CheckingAccount extends Account {

    @Override
    public AccountType getAccountType() {
        return AccountType.CHECKING;
    }

    @Override
    public StreamPipeline<Transaction> getCalculator() {
        return new Calculator.CheckingInterest<>();
    }
}
