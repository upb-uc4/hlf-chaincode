package de.upb.cs.uc4.chaincode.contract.operation;

import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "UC4.OperationData"
)
public class OperationContract extends ContractBase {

    private final OperationContractUtil cUtil = new OperationContractUtil();

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
        // check if the user trying to approve is not allowed to approve the operation
        if(!operationData.getMissingApprovals().getUsers().contains(initiator)){
            return GsonWrapper.toJson(cUtil.getApprovalDeniedError());
        }
        // approve
        try {
            operationData = cUtil.approveOperation(ctx, operationData);
        } catch (MissingTransactionError missingTransactionError) {
            return missingTransactionError.getJsonError();
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
        // approve
        try {
            operationData = cUtil.approveOperation(ctx, operationData);
        } catch (MissingTransactionError missingTransactionError) {
            return missingTransactionError.getJsonError();
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
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        //check whether transaction still PENDING
        if(operationState != OperationDataState.PENDING){
            return GsonWrapper.toJson(cUtil.getRejectionImpossibleError());
        }


        // reject
        operationData.state(OperationDataState.REJECTED).reason(cUtil.getUserRejectionMessage(rejectMessage));

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

        List<String> operationIdList = new ArrayList<>();
        if(!cUtil.valueUnset(operationIds)) {
            try {
                operationIdList = GsonWrapper.fromJson(operationIds, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("operationIds")))).getJsonError();
            }
        }
        List<String> stateList = new ArrayList<>();
        if(!cUtil.valueUnset(states)) {
            try {
                stateList = GsonWrapper.fromJson(states, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("states")))).getJsonError();
            }
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
