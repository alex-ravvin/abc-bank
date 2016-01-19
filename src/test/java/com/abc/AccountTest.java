package com.abc;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AccountTest {

    private Account _checking;
    private Account _savings;

    @Before
    public void createTestCheckingAccount() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(new BigDecimal(100), LocalDateTime.of(2015, 1, 5, 10, 37)));
        transactions.add(new Transaction(new BigDecimal(100), LocalDateTime.of(2015, 7, 5, 18, 12)));
        _checking = new CheckingAccount();

        Class checkingAccountClass = Account.class;
        try {
            Field field = checkingAccountClass.getDeclaredField("_transactions");
            field.setAccessible(true);
            field.set(_checking, transactions);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Before
    public void createTestSavingsAccount() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(new BigDecimal(2000), LocalDateTime.of(2015, 1, 5, 10, 37)));
        transactions.add(new Transaction(new BigDecimal(1000), LocalDateTime.of(2015, 7, 5, 18, 12)));
        _savings = new SavingsAccount();

        Class checkingAccountClass = Account.class;
        try {
            Field field = checkingAccountClass.getDeclaredField("_transactions");
            field.setAccessible(true);
            field.set(_savings, transactions);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void checkingAccountBalanceAndInterestCalculationTest() {
        Calculator.SimpleInterest<Transaction> checkingCalculator = new Calculator.CheckingInterest<>();
        assertTrue(_checking.processWithOptimisticLock(checkingCalculator));
        assertEquals(checkingCalculator.getBalance().intValue(), 200);
        assertNotNull(checkingCalculator.getInterest());

        Calculator.SimpleInterest<Transaction> checkingCalc = new Calculator.CheckingInterest<>();
        _checking.processWithReadLock(checkingCalc);
        assertEquals(checkingCalc.getBalance().intValue(), 200);
        assertNotNull(checkingCalc.getInterest());
    }

    @Test
    public void savingsAccountBalanceAndInterestCalculationTest() {
        Calculator.SimpleInterest<Transaction> savingsCalculator = new Calculator.SavingsInterest<>();
        assertTrue(_savings.processWithOptimisticLock(savingsCalculator));
        assertEquals(savingsCalculator.getBalance().intValue(), 3000);
        assertNotNull(savingsCalculator.getInterest());
    }

    @Test
    public void depositWithdrawTransferTest() throws BankException {
        _checking.deposit(new BigDecimal(500));
        Calculator.SimpleInterest<Transaction> calc1 = new Calculator.CheckingInterest<>();
        assertTrue(_checking.processWithOptimisticLock(calc1));
        assertEquals(calc1.getBalance().intValue(), 700);

        _savings.withdraw(new BigDecimal(500));
        Calculator.SimpleInterest<Transaction> calc2 = new Calculator.SavingsInterest<>();
        assertTrue(_savings.processWithOptimisticLock(calc2));
        assertEquals(calc2.getBalance().intValue(), 2500);

        _savings.transfer(new BigDecimal(400), _checking);
        Calculator.SimpleInterest<Transaction> calc3 = new Calculator.SavingsInterest<>();
        assertTrue(_savings.processWithOptimisticLock(calc3));
        assertEquals(calc3.getBalance().intValue(), 2100);
        Calculator.SimpleInterest<Transaction> calc4 = new Calculator.CheckingInterest<>();
        assertTrue(_checking.processWithOptimisticLock(calc4));
        assertEquals(calc4.getBalance().intValue(), 1100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void depositInvalidAmountTest() throws BankException {
        _checking.deposit(new BigDecimal(-500));
    }

    @Test(expected=IllegalArgumentException.class)
    public void withdrawInvalidAmountTest() throws BankException {
        _checking.withdraw(new BigDecimal(-500));
    }

    @Test(expected=IllegalArgumentException.class)
    public void transferInvalidAmountTest() throws BankException {
        _savings.transfer(new BigDecimal(-400), _checking);
    }

    @Test(expected=BankException.class)
    public void transferToSameAccountTest() throws BankException {
        _savings.transfer(new BigDecimal(400), _savings);
    }

    @Test(expected=BankException.class)
    public void withdrawTooMuchTest() throws BankException {
        _checking.withdraw(new BigDecimal(200000000000L));
    }

    @Test
    public void accountTypeTest() {
        assertEquals(_checking.getAccountType().toString().compareTo("Checking"), 0);
        assertEquals(_checking.getAccountType().captionString().compareTo("Checking Account"), 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void dateDiffTest() {
        LocalDateTime day1 = LocalDateTime.of(2016, 1, 3, 0, 0);
        LocalDateTime day2 = LocalDateTime.of(2016, 1, 7, 0, 0);
        long daysBetween = Calculator.accrualDateDifference(day1, day2);
        assertEquals(daysBetween, 4L);
        Calculator.accrualDateDifference(day2, day1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void dateDiffExceptionTest() {
        LocalDateTime day1 = LocalDateTime.of(2016, 1, 3, 0, 0);
        LocalDateTime day2 = LocalDateTime.of(2016, 1, 7, 0, 0);
        Calculator.accrualDateDifference(day2, day1);
    }
}
