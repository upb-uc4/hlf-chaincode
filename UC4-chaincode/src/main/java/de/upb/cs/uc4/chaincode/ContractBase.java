package de.upb.cs.uc4.chaincode;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Transaction;


public abstract class ContractBase implements ContractInterface {
    protected String contractName = "UC4.ContractBase";
    private final String version = "v0.12.3";

    /**
     * Gets version of the chaincode
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getVersion(final Context ctx) {
        return version;
    }

    public String getContractName() {
        return contractName;
    }
}
