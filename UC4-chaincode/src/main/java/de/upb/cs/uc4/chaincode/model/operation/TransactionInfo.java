package de.upb.cs.uc4.chaincode.model.operation;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class TransactionInfo {

    @SerializedName("contractName")
    private String contractName = null;
    @SerializedName("transactionName")
    private String transactionName = null;
    @SerializedName("parameters")
    private String parameters = null;

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public TransactionInfo contractName(String contractName) {
        this.contractName = contractName;
        return this;
    }

    public TransactionInfo transactionName(String transactionName) {
        this.transactionName = transactionName;
        return this;
    }

    public TransactionInfo parameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionInfo transactionInfo = (TransactionInfo) o;
        return Objects.equals(this.contractName, transactionInfo.contractName) &&
                Objects.equals(this.transactionName, transactionInfo.transactionName) &&
                Objects.equals(this.parameters, transactionInfo.parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransactionInfo {\n");
        sb.append("    contractName: ").append(contractName).append("\n");
        sb.append("    transactionName: ").append(transactionName).append("\n");
        sb.append("    parameters: ").append(parameters).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
