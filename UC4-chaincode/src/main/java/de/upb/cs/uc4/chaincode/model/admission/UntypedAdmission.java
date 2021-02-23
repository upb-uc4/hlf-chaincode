package de.upb.cs.uc4.chaincode.model.admission;

import org.hyperledger.fabric.shim.ChaincodeStub;

public class UntypedAdmission extends AbstractAdmission {

    public void resetAdmissionId() {
        this.admissionId = this.enrollmentId;
    }

    @Override
    public void ensureIsDroppable(ChaincodeStub stub) {

    }
}

