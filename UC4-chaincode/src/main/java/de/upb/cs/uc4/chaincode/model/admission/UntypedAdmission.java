package de.upb.cs.uc4.chaincode.model.admission;

public class UntypedAdmission extends AbstractAdmission {

    public void resetAdmissionId() {
        this.admissionId = this.enrollmentId;
    }
}

