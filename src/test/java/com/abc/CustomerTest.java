package com.abc;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class CustomerTest {

    @Test //Test customer statement generation
    public void applicationTest() throws BankException {

        CheckingAccount checkingAccount = new CheckingAccount();
        SavingsAccount  savingsAccount =  new SavingsAccount();

        com.abc.Customer henry = new Customer("Henry").openAccount(checkingAccount).openAccount(savingsAccount);

        checkingAccount.deposit(new BigDecimal(100));
        savingsAccount.deposit(new BigDecimal(4000));
        savingsAccount.withdraw(new BigDecimal(200));

        assertEquals("Statement for Henry\n" +
                "\n" +
                "Checking Account\n" +
                "  deposit $100.00\n" +
                "Total: $100.00\n" +
                "\n" +
                "Savings Account\n" +
                "  deposit $4,000.00\n" +
                "  withdrawal $200.00\n" +
                "Total: $3,800.00\n" +
                "\n" +
                "Total In All Accounts: $3,900.00", henry.getStatement());
    }

    @Test
    public void oneAccountTest(){
        Customer oscar = new Customer("Oscar").openAccount(new SavingsAccount());
        assertEquals(1, oscar.getNumberOfAccounts());
    }

    @Test
    public void twoAccountsTest(){
        Customer oscar = new Customer("Oscar")
                .openAccount(new SavingsAccount());
        oscar.openAccount(new CheckingAccount());
        assertEquals(2, oscar.getNumberOfAccounts());
    }
}
