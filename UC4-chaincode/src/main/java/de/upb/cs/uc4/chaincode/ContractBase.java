package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.util.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class ContractBase implements ContractInterface {

    private final String version = "v0.11.5";

    /**
     * Gets version of the chaincode
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String getVersion(final Context ctx) {
        return version;
    }

    @Transaction()
    public String resetLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        if (!ctx.getClientIdentity().getAttributeValue("hf.Type").equals("admin")) {
            return "FAILURE";
        }
        List<ContractUtil> cUtils = Arrays.asList(
                new ApprovalContractUtil(),
                new CertificateContractUtil(),
                new ExaminationRegulationContractUtil(),
                new MatriculationDataContractUtil());
        for (ContractUtil cUtil: cUtils) {
            for (KeyValue keyValue: cUtil.getAllRawStates(stub)) {
                stub.delState(keyValue.getKey());
            }
        }
        return "";
    }
}
