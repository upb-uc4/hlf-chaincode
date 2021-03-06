package de.upb.cs.uc4.chaincode.contract.certificate;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;


@Contract(
        name = CertificateContract.contractName
)
public class CertificateContract extends ContractBase {

    private final CertificateContractUtil cUtil = new CertificateContractUtil();

    public final static String contractName = "UC4.Certificate";
    public final static String transactionNameAddCertificate = "addCertificate";
    public final static String transactionNameGetCertificate = "getCertificate";
    public final static String transactionNameUpdateCertificate = "updateCertificate";

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
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final  String[] args = new String[]{enrollmentId, certificate};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
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
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{enrollmentId, certificate};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
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
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{enrollmentId};
        ChaincodeStub stub = ctx.getStub();

        try {
            cUtil.validateTransaction(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return cUtil.getStringState(stub, enrollmentId);
    }
}
