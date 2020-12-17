package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.exceptions.ParameterError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;

@Contract(
        name = "UC4.Certificate"
)
public class CertificateContract extends ContractBase {

    private final CertificateContractUtil cUtil = new CertificateContractUtil();

    /**
     * Adds a certificate to the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId for the certificate to be added
     * @param certificate  certificate to be set for the given enrollmentId
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String addCertificate(final Context ctx, final String enrollmentId, final String certificate) {
        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.checkParamsAddCertificate(ctx, enrollmentId, certificate);
        } catch (ParameterError e) {
            return e.getJsonError();
        }
        return cUtil.putAndGetStringState(stub, enrollmentId, certificate);
    }

    /**
     * Updates certificate on the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId for the certificate to be updated
     * @param certificate  certificate to be set for the given enrollmentId
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String updateCertificate(final Context ctx, final String enrollmentId, final String certificate) {
        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.checkParamsUpdateCertificate(ctx, enrollmentId, certificate);
        } catch (ParameterError e) {
            return e.getJsonError();
        }
        return cUtil.putAndGetStringState(stub, enrollmentId, certificate);
    }

    /**
     * Gets certificate from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId of the certificate to be returned
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getCertificate(final Context ctx, final String enrollmentId) {
        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.checkParamsGetCertificate(ctx, enrollmentId);
        } catch (ParameterError e) {
            return e.getJsonError();
        }
        String certificate = cUtil.getStringState(stub, enrollmentId);
        return certificate;
    }
}
