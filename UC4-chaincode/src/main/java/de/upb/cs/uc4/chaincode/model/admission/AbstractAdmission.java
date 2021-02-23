package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.model.exam.ExamType;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.Instant;
import java.util.ArrayList;
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
    @ApiModelProperty()
    public String getAdmissionId() {
        return this.admissionId;
    }

    public abstract void resetAdmissionId();

    @ApiModelProperty()
    public String getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
        resetAdmissionId();
    }

    @ApiModelProperty()
    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @ApiModelProperty()
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
    public int hashCode() {
        return Objects.hash(this.admissionId, this.enrollmentId, this.timestamp, this.type);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(toIndentedString(this.admissionId)).append("\n");
        sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(this.timestamp)).append("\n");
        sb.append("    type: ").append(toIndentedString(this.type)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public ArrayList<InvalidParameter> getParameterErrors() {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();

        if (cUtil.valueUnset(this.enrollmentId)) {
            invalidParams.add(cUtil.getEmptyEnrollmentIdParam(cUtil.getErrorPrefix() + "."));
        }
        if (cUtil.valueUnset(this.type)) {
            invalidParams.add(cUtil.getInvalidEnumValue(cUtil.getErrorPrefix() + ".type", AdmissionType.possibleStringValues()));
        }

        return invalidParams;
    }

    public ArrayList<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();
        if (!cUtil.checkStudentMatriculated(stub, this)) {
            invalidParameters.add(cUtil.getStudentNotMatriculatedParam("enrollmentId"));
        }
        return invalidParameters;
    }

    public abstract void ensureIsDroppable(ChaincodeStub stub);
}

