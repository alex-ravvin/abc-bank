package com.abc;

public enum AccountType {

    CHECKING("Checking"),
    SAVINGS("Savings"),
    MAXI_SAVINGS("Maxi Savings");

    private static final String entityName = "Account";

    private final String type;

    AccountType(String accType) {
        type = accType;
    }

    @Override
    public String toString() {
        return type;
    }

    public String captionString() {
        return type + " " + entityName;
    }
}
