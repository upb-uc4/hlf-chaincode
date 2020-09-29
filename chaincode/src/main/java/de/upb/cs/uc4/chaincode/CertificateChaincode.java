package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.util.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;

@Contract(
        name="UC4.Certificate"
)
@Default
public class CertificateChaincode implements ContractInterface {

    private CertificateContractUtil cUtil = new CertificateContractUtil();

    @Transaction()
    public void initLedger(final Context ctx) {

    }

    /**
     * Adds a certificate to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId for the certificate to be added
     * @param certificate certificate to be set for the given enrollmentId
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String addCertificate(final Context ctx, final String enrollmentId, final String certificate) {

        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getErrorForEnrollmentId(enrollmentId));
        invalidParams.addAll(getErrorForCertificate(certificate));

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String result = stub.getStringState(enrollmentId);
        if (result != null && !result.equals("")) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        return cUtil.putAndGetStringState(stub, enrollmentId, certificate);
    }

    /**
     * Updates certificate on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId for the certificate to be updated
     * @param certificate certificate to be set for the given enrollmentId
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String updateCertificate(final Context ctx, final String enrollmentId, final String certificate) {

        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getErrorForEnrollmentId(enrollmentId));
        invalidParams.addAll(getErrorForCertificate(certificate));

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String certificateOnLedger = stub.getStringState(enrollmentId);

        if (certificateOnLedger == null || certificateOnLedger.equals("")) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }

        return cUtil.putAndGetStringState(stub, enrollmentId, certificate);
    }

    /**
     * Gets certificate from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId of the certificate to be returned
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getCertificate(final Context ctx, final String enrollmentId) {

        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getErrorForEnrollmentId(enrollmentId));
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String certificate = stub.getStringState(enrollmentId);

        if (certificate == null || certificate.equals("")) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }
        return certificate;
    }

    private ArrayList<InvalidParameter> getErrorForEnrollmentId(final String enrollmentId) {
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (enrollmentId == null || enrollmentId.equals("")) {
            list.add(new InvalidParameter()
                    .name("enrollmentId")
                    .reason("ID must not be empty"));
        }
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForCertificate(final String certificate) {
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (certificate == null || certificate.equals("")) {
            list.add(new InvalidParameter()
                    .name("certificate")
                    .reason("Certificate must not be empty"));
        }
        return list;
    }
}
