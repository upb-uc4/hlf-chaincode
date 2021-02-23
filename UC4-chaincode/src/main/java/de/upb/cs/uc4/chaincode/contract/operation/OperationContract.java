package de.upb.cs.uc4.chaincode.contract.operation;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Contract(
        name = OperationContract.contractName
)
public class OperationContract extends ContractBase {

    private final OperationContractUtil cUtil = new OperationContractUtil();

    public final static String contractName = "UC4.OperationData";
    public final static String transactionNameInitiateOperation = "initiateOperation";
    public final static String transactionNameApproveOperation = "approveOperation";
    public final static String transactionNameRejectOperation = "rejectOperation";
    public final static String transactionNameGetOperations = "getOperations";

    /**
     * Submits a draft to the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String initiateOperation(final Context ctx, String initiator, final String contractName, final String transactionName, final String params) {
        try {
            ValidationManager.validateParams(ctx, contractName, transactionName, params);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        OperationData operationData;
        try {
            operationData = cUtil.getOrInitializeOperationData(ctx, initiator, contractName, transactionName, params);
        } catch (ValidationError e) {
            return e.getJsonError();
        }

        try {
            cUtil.approveOperation(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        return cUtil.putAndGetStringState(ctx.getStub(), operationData.getOperationId(), GsonWrapper.toJson(operationData));
    }

    /**
     * Submits a draft to the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String approveOperation(final Context ctx, String operationId) {
        OperationData operationData;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        try {
            cUtil.approveOperation(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        return cUtil.putAndGetStringState(ctx.getStub(), operationData.getOperationId(), GsonWrapper.toJson(operationData));
    }

    @Transaction

    public String rejectOperation(final Context ctx, final String operationId, final String rejectMessage) {
        OperationData operationData;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        try {
            cUtil.checkMayParticipate(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        if(cUtil.valueUnset(rejectMessage)){
            return  GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getEmptyInvalidParameter("rejectMessage")));
        }
        operationData.state(OperationDataState.REJECTED).reason(rejectMessage);
        return cUtil.putAndGetStringState(ctx.getStub(), operationId, GsonWrapper.toJson(operationData));
    }

    @Transaction()
    public String getOperations(
            final Context ctx,
            final String operationIds,
            final String existingEnrollmentId,
            final String missingEnrollmentId,
            final String initiatorEnrollmentId,
            final String involvedEnrollmentId,
            final String states) {

        List<InvalidParameter> invalidParams = new ArrayList<>();
        List<String> operationIdList = null;
        try {
            operationIdList = Arrays.asList(GsonWrapper.fromJson(operationIds, String[].class).clone());
        } catch (Exception e) {
            invalidParams.add(cUtil.getUnparsableParam("operationIds"));
        }
        List<String> stateList = null;
        try {
            stateList = Arrays.asList(GsonWrapper.fromJson(states, String[].class).clone());
        } catch (Exception e) {
            invalidParams.add(cUtil.getUnparsableParam("states"));
        }
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        List<OperationData> operations = cUtil.getOperations(
                        ctx.getStub(),
                        operationIdList,
                        existingEnrollmentId,
                        missingEnrollmentId,
                        initiatorEnrollmentId,
                        involvedEnrollmentId,
                        stateList);
        return GsonWrapper.toJson(operations.toArray());
    }
}
