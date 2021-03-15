package de.upb.cs.uc4.chaincode.contract.admission;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.admission.AbstractAdmission;
import de.upb.cs.uc4.chaincode.model.admission.CourseAdmission;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.admission.ExamAdmission;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Arrays;
import java.util.List;

@Contract(
        name = AdmissionContract.contractName
)
public class AdmissionContract extends ContractBase {
    private final AdmissionContractUtil cUtil = new AdmissionContractUtil();

    public final static String contractName = "UC4.Admission";
    public final static String transactionNameAddAdmission = "addAdmission";
    public final static String transactionNameDropAdmission = "dropAdmission";
    public final static String transactionNameGetCourseAdmissions = "getCourseAdmissions";
    public final static String transactionNameGetExamAdmissions = "getExamAdmissions";

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
        final String[] args = new String[]{admissionJson};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        AbstractAdmission newAdmission = GsonWrapper.fromJson(admissionJson, AbstractAdmission.class);
        newAdmission.setTimestamp(ctx.getStub().getTxTimestamp());
        newAdmission.resetAdmissionId();
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
        ChaincodeStub stub = ctx.getStub();
        String transactionName = HyperledgerManager.getTransactionName(stub);
        final String[] args = new String[]{admissionId};

        try {
            cUtil.validateTransaction(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        try {
            cUtil.delState(stub, admissionId);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        return "";
    }

    /**
     * Gets CourseAdmissionList from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollment to filter admissions by
     * @param courseId     courseId to filter admissions by
     * @param moduleId     moduleId to filter admissions by
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getCourseAdmissions(final Context ctx, final String enrollmentId, final String courseId, final String moduleId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{enrollmentId, courseId, moduleId};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        List<CourseAdmission> admissions = cUtil.getCourseAdmissions(stub, enrollmentId, courseId, moduleId);
        return GsonWrapper.toJson(admissions);
    }

    /**
     * Gets ExamAdmissionList from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param admissionIds admissionIds to filter admissions by
     * @param enrollmentId enrollment to filter admissions by
     * @param examIds examIds to filter admissions by
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getExamAdmissions(final Context ctx, final String admissionIds, final String enrollmentId, final String examIds) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{admissionIds, enrollmentId, examIds};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        List<String> admissionIdList = Arrays.asList(GsonWrapper.fromJson(admissionIds, String[].class).clone());
        List<String> examIdList = Arrays.asList(GsonWrapper.fromJson(examIds, String[].class).clone());
        List<ExamAdmission> admissions = cUtil.getExamAdmissions(stub, admissionIdList, enrollmentId, examIdList);
        return GsonWrapper.toJson(admissions);
    }
}
