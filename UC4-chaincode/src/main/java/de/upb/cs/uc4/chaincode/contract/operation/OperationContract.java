package de.upb.cs.uc4.chaincode.contract.operation;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.operation.OperationData;
import de.upb.cs.uc4.chaincode.model.operation.OperationDataState;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

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
            cUtil.checkTimestamp(ctx);
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
            cUtil.checkTimestamp(ctx);
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
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
            cUtil.checkTimestamp(ctx);
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
            cUtil.checkMayParticipate(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        if(GeneralHelper.valueUnset(rejectMessage)){
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
        try {
            cUtil.checkTimestamp(ctx);
            cUtil.checkParamsGetOperations(operationIds, states);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        List<String> operationIdList = Arrays.asList(GsonWrapper.fromJson(operationIds, String[].class).clone());
        List<String> stateList = Arrays.asList(GsonWrapper.fromJson(states, String[].class).clone());

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
