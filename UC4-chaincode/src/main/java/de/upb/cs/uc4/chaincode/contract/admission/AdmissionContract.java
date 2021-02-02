package de.upb.cs.uc4.chaincode.contract.admission;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.model.admission.AbstractAdmission;
import de.upb.cs.uc4.chaincode.model.admission.CourseAdmission;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Collections;
import java.util.List;

@Contract(
        name = AdmissionContract.contractName
)
public class AdmissionContract extends ContractBase {
    private final AdmissionContractUtil cUtil = new AdmissionContractUtil();

    public final static String contractName = "UC4.Admission";
    public final static String transactionNameAddAdmission = "addAdmission";
    public final static String transactionNameDropAdmission = "dropAdmission";
    public final static String transactionNameGetAdmissions = "getAdmissions";

    /**
     * Adds MatriculationData to the ledger.
     *
     * @param ctx           transaction context providing access to ChaincodeStub etc.
     * @param admissionJson json representation of new Admission to add.
     * @return newAdmission on success, serialized error on failure
     */
    @Transaction()
    public String addAdmission(final Context ctx, String admissionJson) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsAddAdmission(ctx, Collections.singletonList(admissionJson));
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        AbstractAdmission newAdmission = GsonWrapper.fromJson(admissionJson, AbstractAdmission.class);
        newAdmission.resetAdmissionId();

        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, new String[]{admissionJson});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, new String[]{admissionJson});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        // TODO: can we create a composite key of all inputs to improve reading performance for get...forUser/Module/Course
        return cUtil.putAndGetStringState(stub, newAdmission.getAdmissionId(), GsonWrapper.toJson(newAdmission));
    }

    /**
     * Drops an existing admission from the ledger
     *
     * @param ctx         transaction context providing access to ChaincodeStub etc.
     * @param admissionId identifier of admission to drop
     * @return empty string on success, serialized error on failure
     */
    @Transaction()
    public String dropAdmission(final Context ctx, String admissionId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsDropAdmission(ctx, Collections.singletonList(admissionId));
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, new String[]{admissionId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        // perform delete
        try {
            cUtil.delState(stub, admissionId);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, new String[]{admissionId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return "";
    }

    /**
     * Gets AdmissionList from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollment to find admissions for
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getAdmissions(final Context ctx, final String enrollmentId, final String courseId, final String moduleId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, new String[]{enrollmentId, courseId, moduleId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        List<CourseAdmission> admissions = cUtil.getAdmissions(stub, enrollmentId, courseId, moduleId);
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, new String[]{enrollmentId, courseId, moduleId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(admissions);
    }
}
