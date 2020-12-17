package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.exceptions.ParameterError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;

public class CertificateContractUtil extends ContractUtil {

    public CertificateContractUtil() {
        keyPrefix = "certificate";
        thing = "certificate";
        identifier = "enrollmentId";
    }

    private ArrayList<InvalidParameter> getErrorForEnrollmentId(final String enrollmentId) {
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (enrollmentId == null || enrollmentId.equals("")) {
            list.add(getEmptyEnrollmentIdParam());
        }
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForCertificate(final String certificate) {
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (certificate == null || certificate.equals("")) {
            list.add(getEmptyInvalidParameter("certificate"));
        }
        return list;
    }

    public void checkParamsAddCertificate(Context ctx, String enrollmentId, String certificate) throws ParameterError {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getErrorForEnrollmentId(enrollmentId));
        invalidParams.addAll(getErrorForCertificate(certificate));

        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

        String result = getStringState(stub, enrollmentId);
        if (result != null && !result.equals("")) {
            throw new ParameterError(GsonWrapper.toJson(getConflictError()));
        }
    }

    public void checkParamsUpdateCertificate(Context ctx, String enrollmentId, String certificate) throws ParameterError {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getErrorForEnrollmentId(enrollmentId));
        invalidParams.addAll(getErrorForCertificate(certificate));

        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

        String certificateOnLedger = getStringState(stub, enrollmentId);

        if (certificateOnLedger == null || certificateOnLedger.equals("")) {
            throw new ParameterError(GsonWrapper.toJson(getNotFoundError()));
        }
    }

    public void checkParamsGetCertificate(Context ctx, String enrollmentId) throws ParameterError {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>(getErrorForEnrollmentId(enrollmentId));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

        String certificate = getStringState(stub, enrollmentId);

        if (certificate == null || certificate.equals("")) {
            throw new ParameterError(GsonWrapper.toJson(getNotFoundError()));
        }
    }
}
