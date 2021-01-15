package de.upb.cs.uc4.chaincode.helper;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class HyperledgerManager {
    public static String getTransactionName(ChaincodeStub stub){
        return stub.getFunction().split(":")[1];
    }
}
