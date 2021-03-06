package de.upb.cs.uc4.chaincode.contract;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Transaction;

public abstract class ContractBase implements ContractInterface {
    protected String contractName = "UC4.ContractBase";
    public final static String transactionNameGetVersion = "getVersion";

    /**
     * Gets version of the chaincode
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getVersion(final Context ctx) {
        return "1.0.0";

        /*
        URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();

        Properties properties = new Properties();
        properties.load(cl.findResource("project.properties").openStream());
        String v1 = properties.getProperty("rootVersion");

        return v1;*/
    }
}
