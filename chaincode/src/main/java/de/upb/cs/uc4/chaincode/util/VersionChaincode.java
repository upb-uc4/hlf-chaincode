package de.upb.cs.uc4.chaincode.util;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(
        name="UC4.Version"
)
@Default
public class VersionChaincode implements ContractInterface {

    private final String version = "v0.10.1";

    /**
     * Gets version of the chaincode
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getVersion(final Context ctx) {
        return version;
    }
}
