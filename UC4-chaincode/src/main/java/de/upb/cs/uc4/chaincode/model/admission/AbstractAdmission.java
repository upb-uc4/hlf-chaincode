package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.shim.ChaincodeStub;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAdmission {
    protected static final String DELIMITER = ":";

    @SerializedName("admissionId")
    protected String admissionId;

    @SerializedName("enrollmentId")
    protected String enrollmentId;

    @SerializedName("timestamp")
    protected Instant timestamp;

    @SerializedName("type")
    protected AdmissionType type;

    /**
     * Get admissionId
     *
     * @return admissionId
     **/
    public String getAdmissionId() {
        return this.admissionId;
    }

    public abstract void resetAdmissionId();

    public String getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
        resetAdmissionId();
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AdmissionType getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractAdmission other = (AbstractAdmission) o;
        return Objects.equals(this.admissionId, other.admissionId)
                && Objects.equals(this.enrollmentId, other.enrollmentId)
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.type, other.type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(this.admissionId).append("\n");
        sb.append("    enrollmentId: ").append(this.enrollmentId).append("\n");
        sb.append("    timestamp: ").append(this.timestamp).append("\n");
        sb.append("    type: ").append(this.type).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public List<InvalidParameter> getParameterErrors() {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParams = new ArrayList<>();

        if (GeneralHelper.valueUnset(this.enrollmentId)) {
            invalidParams.add(cUtil.getEmptyEnrollmentIdParam(cUtil.getErrorPrefix() + "."));
        }
        if (GeneralHelper.valueUnset(this.type)) {
            invalidParams.add(cUtil.getInvalidEnumValue(cUtil.getErrorPrefix() + ".type", AdmissionType.class));
        }

        return invalidParams;
    }

    public List<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParameters = new ArrayList<>();
        if (!cUtil.checkStudentMatriculated(stub, this)) {
            invalidParameters.add(cUtil.getStudentNotMatriculatedParam("enrollmentId"));
        }
        return invalidParameters;
    }

    public void ensureIsDroppable(ChaincodeStub stub) {
        throw new NotImplementedException();
    }
}

