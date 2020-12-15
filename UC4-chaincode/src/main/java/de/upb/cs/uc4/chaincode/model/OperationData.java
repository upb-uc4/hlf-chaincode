package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Objects;

public class OperationData {
    @SerializedName("operationId")
    private String operationId = null;
    @SerializedName("transactionInfo")
    private TransactionInfo transactionInfo = null;
    @SerializedName("state")
    private OperationDataState state = OperationDataState.PENDING;
    @SerializedName("reason")
    private String reason;
    @SerializedName("existingApprovals")
    private ApprovalList existingApprovals = null;
    @SerializedName("missingApprovals")
    private ApprovalList missingApprovals = null;

    public OperationData operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    @ApiModelProperty(value = "")
    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public OperationData existingApprovals(ApprovalList existingApprovals) {
        this.existingApprovals = existingApprovals;
        return this;
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public OperationData transactionInfo(TransactionInfo transactionInfo)  {
        this.transactionInfo = transactionInfo;
        return this;
    }

    public OperationData state(OperationDataState state)  {
        this.state = state;
        return this;
    }

    public OperationData reason(String reason)  {
        this.reason = reason;
        return this;
    }

    public OperationDataState getState() {
        return state;
    }

    public void setState(OperationDataState state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ApprovalList getExistingApprovals() {
        return existingApprovals;
    }

    public void setExistingApprovals(ApprovalList existingApprovals) {
        this.existingApprovals = existingApprovals;
    }

    public OperationData missingApprovals(ApprovalList missingApprovals) {
        this.missingApprovals = missingApprovals;
        return this;
    }

    public ApprovalList getMissingApprovals() {
        return missingApprovals;
    }

    public void setMissingApprovals(ApprovalList missingApprovals) {
        this.missingApprovals = missingApprovals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationData operationData = (OperationData) o;
        return Objects.equals(this.operationId, operationData.operationId) &&
                Objects.equals(this.transactionInfo, operationData.transactionInfo) &&
                Objects.equals(this.state, operationData.state) &&
                Objects.equals(this.reason, operationData.reason) &&
                Objects.equals(this.existingApprovals, operationData.existingApprovals) &&
                Objects.equals(this.missingApprovals, operationData.missingApprovals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, transactionInfo, state, reason, existingApprovals, missingApprovals);
    }

}

