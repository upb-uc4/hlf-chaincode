package de.upb.cs.uc4.chaincode.contract.operation;

import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

        String clientId = cUtil.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        initiator = cUtil.valueUnset(initiator) ? clientId : initiator;

        OperationData operationData;
        OperationDataState operationState;
        try {
            operationData = cUtil.getOrInitializeOperationData(ctx, initiator, contractName, transactionName, params);
            operationState = operationData.getState();
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }
        // check whether transaction still PENDING
        if(operationState != OperationDataState.PENDING){
            return GsonWrapper.toJson(cUtil.getApprovalImpossibleError());
        }

        try {if(!cUtil.mayParticipateInOperation(ctx, operationData)) {
            return GsonWrapper.toJson(cUtil.getApprovalDeniedError());
        }
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        if(!operationData.getMissingApprovals().getUsers().contains(initiator)){
        //    return GsonWrapper.toJson(cUtil.getApprovalDeniedError());
        }
        // approve
        try {
            operationData = cUtil.approveOperation(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        // store
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
        OperationDataState operationState;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
            operationState = operationData.getState();
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        //check whether transaction still PENDING
        if(operationState != OperationDataState.PENDING){
            return GsonWrapper.toJson(cUtil.getApprovalImpossibleError());
        }
        try {if(!cUtil.mayParticipateInOperation(ctx, operationData)) {
            return GsonWrapper.toJson(cUtil.getApprovalDeniedError());
        }
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        // approve
        try {
            operationData = cUtil.approveOperation(ctx, operationData);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        // store
        return cUtil.putAndGetStringState(ctx.getStub(), operationData.getOperationId(), GsonWrapper.toJson(operationData));
    }

    @Transaction
    public String rejectOperation(final Context ctx, final String operationId, final String rejectMessage) {
        OperationData operationData;
        OperationDataState operationState;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
            operationState = operationData.getState();
            if(!cUtil.mayParticipateInOperation(ctx, operationData)) {
                return GsonWrapper.toJson(cUtil.getRejectionDeniedError());
            }
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        //check whether transaction still PENDING
        if(operationState != OperationDataState.PENDING){
            return GsonWrapper.toJson(cUtil.getRejectionImpossibleError());
        }
        // reject
        if(cUtil.valueUnset(rejectMessage)){
            return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getEmptyInvalidParameter("rejectMessage")))).getJsonError();
        }
        operationData.state(OperationDataState.REJECTED).reason(rejectMessage);

        // store
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
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        List<String> operationIdList = null;
        try {
            operationIdList = GsonWrapper.fromJson(operationIds, listType);
        } catch (Exception e) {
            invalidParams.add(cUtil.getUnparsableParam("operationIds"));
        }
        List<String> stateList = null;
        try {
            stateList = GsonWrapper.fromJson(states, listType);
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
        return GsonWrapper.toJson(operations);
    }
}
