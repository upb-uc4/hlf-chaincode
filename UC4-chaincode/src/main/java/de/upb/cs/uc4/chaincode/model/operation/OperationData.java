package de.upb.cs.uc4.chaincode.model.operation;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.util.Objects;

public class OperationData {

    @SerializedName("operationId")
    private String operationId = null;
    @SerializedName("transactionInfo")
    private TransactionInfo transactionInfo = null;
    @SerializedName("initiator")
    private String initiator = null;
    @SerializedName("initiatedTimestamp")
    private Instant initiatedTimestamp = null;
    @SerializedName("lastModifiedTimestamp")
    private Instant lastModifiedTimestamp = null;
    @SerializedName("state")
    private OperationDataState state = OperationDataState.PENDING;
    @SerializedName("reason")
    private String reason;
    @SerializedName("existingApprovals")
    private ApprovalList existingApprovals = new ApprovalList();
    @SerializedName("missingApprovals")
    private ApprovalList missingApprovals = new ApprovalList();

    public OperationData operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public OperationData initiator(String initiator) {
        this.initiator = initiator;
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Instant getInitiatedTimestamp() {
        return initiatedTimestamp;
    }

    public void setInitiatedTimestamp(Instant initiatedTimestamp) {
        this.initiatedTimestamp = initiatedTimestamp;
    }

    public OperationData initiatedTimestamp(Instant initiatedTimestamp) {
        this.initiatedTimestamp = initiatedTimestamp;
        return this;
    }

    public Instant getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(Instant lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public OperationData lastModifiedTimestamp(Instant lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
        return this;
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public OperationData transactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
        return this;
    }

    public OperationDataState getState() {
        return state;
    }

    public void setState(OperationDataState state) {
        this.state = state;
    }

    public OperationData state(OperationDataState state) {
        this.state = state;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OperationData reason(String reason) {
        this.reason = reason;
        return this;
    }

    public ApprovalList getExistingApprovals() {
        if (existingApprovals == null) {
            existingApprovals = new ApprovalList();
        }
        return existingApprovals;
    }

    public void setExistingApprovals(ApprovalList existingApprovals) {
        this.existingApprovals = existingApprovals;
    }

    public OperationData existingApprovals(ApprovalList existingApprovals) {
        this.existingApprovals = existingApprovals;
        return this;
    }

    public ApprovalList getMissingApprovals() {
        return missingApprovals;
    }

    public void setMissingApprovals(ApprovalList missingApprovals) {
        this.missingApprovals = missingApprovals;
    }

    public OperationData missingApprovals(ApprovalList missingApprovals) {
        this.missingApprovals = missingApprovals;
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
        OperationData operationData = (OperationData) o;
        return Objects.equals(this.operationId, operationData.operationId) &&
                Objects.equals(this.transactionInfo, operationData.transactionInfo) &&
                Objects.equals(this.state, operationData.state) &&
                Objects.equals(this.reason, operationData.reason) &&
                Objects.equals(this.existingApprovals, operationData.existingApprovals) &&
                Objects.equals(this.missingApprovals, operationData.missingApprovals);
    }
}
