package com.abc;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BankTest {
    private Account _checkingAccount;
    private Account _savingsAccount;

    @Before
    public void createTestCheckingAccount()throws NoSuchFieldException, IllegalAccessException {
        LocalDateTime aYearAgo = LocalDateTime.now().minusDays(Calculator.DAYS_IN_YEAR.longValue());
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(new BigDecimal(1000), aYearAgo));
        _checkingAccount = new CheckingAccount();

        // depositing $1,000 AsOf 1 year ago
        Class accountClass = Account.class;
        Field field = accountClass.getDeclaredField("_transactions");
        field.setAccessible(true);
        field.set(_checkingAccount, transactions);
    }

    @Before
    public void createTestSavingsAccount()throws NoSuchFieldException, IllegalAccessException {
        LocalDateTime aYearAgo = LocalDateTime.now().minusDays(Calculator.DAYS_IN_YEAR.longValue());
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(new BigDecimal(1500), aYearAgo));
        _savingsAccount = new SavingsAccount();

        // depositing $1,500 AsOf 1 year ago
        Class accountClass = Account.class;
        Field field = accountClass.getDeclaredField("_transactions");
        field.setAccessible(true);
        field.set(_savingsAccount, transactions);
    }

    @Test
    public void customerSummary() {
        Bank bank = new Bank();
        Customer john = new Customer("John");
        john.openAccount(new CheckingAccount());
        bank.addCustomer(john);

        assertEquals("Customer Summary\n - John (1 account)", bank.customerSummary());
    }

    @Test
    public void checkingAccountInterestTest() {
        Bank bank = new Bank();
        Customer bill = new Customer("Bill").openAccount(_checkingAccount);
        bank.addCustomer(bill);

        // ((1.0 + 0.001/365.0) ** 365) * 1000 - 1000 = 1.000498795
        String interestPayed = bank.totalInterestPaid().toPlainString().substring(0, 11);
        assertTrue(interestPayed.equals("1.000498795"));
    }

    @Test
    public void savingsAccountInterestTest() {
        Bank bank = new Bank();
        Customer bill = new Customer("Bill").openAccount(_savingsAccount);
        bank.addCustomer(bill);

        // ((1.0 + 0.001/365.0) ** 365) * 1000 - 1000 +
        // ((1.0 + 0.002/365.0) ** 365) * 500 - 500
        // 2.001496717
        String interestPayed = bank.totalInterestPaid().toPlainString().substring(0, 11);
        assertTrue(interestPayed.equals("2.001496717"));
    }
}
