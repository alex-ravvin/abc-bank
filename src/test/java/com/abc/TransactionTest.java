package com.abc;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class TransactionTest {
    @Test
    public void instantiationTest() {
        Transaction tran = new Transaction(new BigDecimal(1234), LocalDateTime.of(2016, 1, 1, 0, 0));
        assertNotNull(tran);
    }
    @Test
    public void instantiationAndGetters() {
        Transaction tran = new Transaction(new BigDecimal(1234));
        assertEquals(tran.getAmount().intValue(), 1234);
        assertNotNull(tran.getDateTime());
    }
}
