package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class CourseAdmission extends AbstractAdmission {

    public CourseAdmission () {
        type = AdmissionType.COURSE;
    }

    @SerializedName("courseId")
    protected String courseId;

    @SerializedName("moduleId")
    protected String moduleId;

    @ApiModelProperty()
    public String getAdmissionId() {
        return this.admissionId;
    }

    public void resetAdmissionId() {
        this.admissionId = this.enrollmentId + DELIMITER + this.courseId;
    }

    @ApiModelProperty()
    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
        resetAdmissionId();
    }

    @ApiModelProperty()
    public String getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
        resetAdmissionId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CourseAdmission other = (CourseAdmission) o;
        return Objects.equals(this.admissionId, other.admissionId)
                && Objects.equals(this.enrollmentId, other.enrollmentId)
                && Objects.equals(this.courseId, other.courseId)
                && Objects.equals(this.moduleId, other.moduleId)
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.admissionId, this.enrollmentId, this.courseId, this.moduleId, this.timestamp, type);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(toIndentedString(this.admissionId)).append("\n");
        sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
        sb.append("    courseId: ").append(toIndentedString(this.courseId)).append("\n");
        sb.append("    moduleId: ").append(toIndentedString(this.moduleId)).append("\n");
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

    @Override
    public ArrayList<InvalidParameter> getParameterErrors() {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if (cUtil.valueUnset(this.getEnrollmentId())) {
            invalidparams.add(cUtil.getEmptyEnrollmentIdParam(cUtil.getErrorPrefix() + "."));
        }
        if (cUtil.valueUnset(this.getCourseId())) {
            invalidparams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".courseId"));
        }
        if (cUtil.valueUnset(this.getModuleId())) {
            invalidparams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".moduleId"));
        }
        if (cUtil.valueUnset(this.getTimestamp())) {
            invalidparams.add(cUtil.getInvalidTimestampParam());
        }

        return invalidparams;
    }

    @Override
    public ArrayList<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();

        if (!cUtil.checkModuleAvailable(stub, this)) {
            invalidParameters.add(cUtil.getInvalidModuleAvailable("enrollmentId"));
            invalidParameters.add(cUtil.getInvalidModuleAvailable("moduleId"));
        }

        return invalidParameters;
    }
}

