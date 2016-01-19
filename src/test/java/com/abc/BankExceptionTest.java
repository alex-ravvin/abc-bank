package com.abc;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BankExceptionTest {
    @Test
    public void instantiationTest() {
        BankException ex1 = new BankException();
        assertNotNull(ex1);

        BankException ex2 = new BankException("Testing BankException.");
        assertNotNull(ex2);

        BankException ex3 = new BankException("Testing BankException.", new Throwable());
        assertNotNull(ex3);

        BankException ex4 = new BankException(new Throwable());
        assertNotNull(ex4);

    }
}
