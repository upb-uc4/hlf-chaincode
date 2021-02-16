package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

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
        return Objects.equals(this.courseId, other.courseId)
                && Objects.equals(this.moduleId, other.moduleId)
                && super.equals(o);
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
        ArrayList<InvalidParameter> invalidParams = super.getParameterErrors();

        if (cUtil.valueUnset(this.getCourseId())) {
            invalidParams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".courseId"));
        }
        if (cUtil.valueUnset(this.getModuleId())) {
            invalidParams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".moduleId"));
        }

        return invalidParams;
    }

    @Override
    public ArrayList<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        ArrayList<InvalidParameter> invalidParameters = super.getSemanticErrors(stub);

        if (!cUtil.checkModuleAvailable(stub, this)) {
            invalidParameters.add(cUtil.getInvalidModuleAvailable("moduleId"));
        }
        return invalidParameters;
    }

    @Override
    public void ensureIsDroppable(ChaincodeStub stub) {}
}

