package de.upb.cs.uc4.chaincode.helper;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class HyperledgerManager {

    public static String getTransactionName(ChaincodeStub stub){
        return stub.getFunction().split(":")[1];
    }

    public static String getEnrollmentIdFromClientId(String clientId) {
        return clientId.substring(9).split(",")[0];
    }
}
