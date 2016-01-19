package com.abc;

import java.math.BigDecimal;
import java.util.stream.Stream;

public interface StreamPipeline<T extends Transaction> {
    void process(final Stream<T> stream);

    default BigDecimal getInterest() {
        return null;
    }

    default BigDecimal getBalance() {
        return null;
    }
}
