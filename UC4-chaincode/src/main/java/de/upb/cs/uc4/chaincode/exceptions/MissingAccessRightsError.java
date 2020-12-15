package de.upb.cs.uc4.chaincode.exceptions;

public class MissingAccessRightsError extends RuntimeException {
    private final String contractName;
    private final String transactionName;

    public MissingAccessRightsError(String contractName, String transactionName) {
        this.contractName = contractName;
        this.transactionName = transactionName;
    }

    @Override
    public String toString() {
        return "No access rights found for transaction " + contractName + ":" + transactionName;
    }
}
