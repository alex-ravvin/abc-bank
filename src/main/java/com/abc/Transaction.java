package com.abc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final BigDecimal _amount;
    private final LocalDateTime _dateTime;

    public Transaction(BigDecimal amount) {
        this(amount, LocalDateTime.now());
    }

    public Transaction(BigDecimal amount, LocalDateTime dateTime) {
        _amount = amount;
        _dateTime = dateTime;
    }

    public BigDecimal getAmount() {
        return _amount;
    }
    public LocalDateTime getDateTime() {
        return _dateTime;
    }
}
