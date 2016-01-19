package com.abc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    private final List<Customer> customers = new ArrayList<>();

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public String customerSummary() {
        StringBuilder summary = new StringBuilder("Customer Summary");
        for (Customer customer : customers) {
            summary.append("\n - ").append(customer.getName()).append(" (").append(customer.getNumberOfAccounts());
            summary.append(" account").append(pluralitySuffix(customer.getNumberOfAccounts()));
            summary.append(")");
        }
        return summary.toString();
    }

    public BigDecimal totalInterestPaid() {
        BigDecimal total = new BigDecimal(0);
        for(Customer customer: customers)
            total = total.add(customer.totalInterestEarned());
        return total;
    }

    // Helps create correct plural
    private static String pluralitySuffix(int count) {
        return count == 1 ? "" : "s";
    }
}