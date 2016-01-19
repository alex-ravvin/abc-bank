package com.abc;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.abc.Calculator.MATH_CONT;
import static com.abc.Calculator.accountTotal;

public class Customer {
    public static final DecimalFormat MONEY_FORMAT = new DecimalFormat("$#,###.00");

    private final String name;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Customer openAccount(Account account) {
        accounts.add(account);
        return this;
    }

    public int getNumberOfAccounts() {
        return accounts.size();
    }

    public BigDecimal totalInterestEarned() {
        BigDecimal total = new BigDecimal(0);
        for (Account account : accounts) {
            StreamPipeline<Transaction> balanceAndInterest = account.getCalculator();
            account.processTransactions(balanceAndInterest);
            total = total.add(balanceAndInterest.getInterest(), MATH_CONT);
        }
        return total;
    }

    public String getStatement() {
        StringBuilder statement = new StringBuilder();
        statement.append("Statement for ").append(name).append("\n");
        BigDecimal total = new BigDecimal(0);
        for (Account account : accounts) {
            statement.append("\n").append(account.getAccountType().captionString()).append("\n");

            TransactionsReport<Transaction> transactionsReport = new TransactionsReport<>();
            account.processTransactions(transactionsReport);
            StreamPipeline<Transaction> balanceAndInterest = account.getCalculator();
            account.processTransactions(balanceAndInterest);

            statement.append(transactionsReport.getReport());
            statement.append("Total: ");
            statement.append(MONEY_FORMAT.format(accountTotal(balanceAndInterest)));
            statement.append('\n');

            total = total.add(accountTotal(balanceAndInterest), MATH_CONT);
        }
        statement.append("\nTotal In All Accounts: ").append(MONEY_FORMAT.format(total));
        return statement.toString();
    }

    public static class TransactionsReport<T extends Transaction> implements StreamPipeline<T> {

        public String getReport() {
            return _builder.toString();
        }

        private StringBuilder _builder;

        @Override
        public void process(Stream<T> stream) {
            _builder = new StringBuilder();
            stream.map(transaction -> "  " + (transaction.getAmount().signum() < 0 ? "withdrawal" : "deposit")
                    + " " + MONEY_FORMAT.format(transaction.getAmount().abs()) + "\n")
                  .forEach(_builder::append);
        }
    }
}