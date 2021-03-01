package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;
import java.util.Objects;

public class CourseAdmission extends AbstractAdmission {

    public CourseAdmission () {
        type = AdmissionType.COURSE;
    }

    @SerializedName("courseId")
    protected String courseId;

    @SerializedName("moduleId")
    protected String moduleId;

    public String getAdmissionId() {
        return this.admissionId;
    }

    public void resetAdmissionId() {
        this.admissionId = this.enrollmentId + DELIMITER + this.courseId;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
        resetAdmissionId();
    }

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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(this.admissionId).append("\n");
        sb.append("    enrollmentId: ").append(this.enrollmentId).append("\n");
        sb.append("    courseId: ").append(this.courseId).append("\n");
        sb.append("    moduleId: ").append(this.moduleId).append("\n");
        sb.append("    timestamp: ").append(this.timestamp).append("\n");
        sb.append("    type: ").append(this.type).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List<InvalidParameter> getParameterErrors() {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParams = super.getParameterErrors();

        if (GeneralHelper.valueUnset(this.getCourseId())) {
            invalidParams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".courseId"));
        }
        if (GeneralHelper.valueUnset(this.getModuleId())) {
            invalidParams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".moduleId"));
        }

        return invalidParams;
    }

    @Override
    public List<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParameters = super.getSemanticErrors(stub);

        if (!cUtil.checkModuleAvailable(stub, this)) {
            invalidParameters.add(cUtil.getInvalidModuleAvailable("moduleId"));
        }
        return invalidParameters;
    }

    @Override
    public void ensureIsDroppable(ChaincodeStub stub) {
        // TODO: overwrite
    }
}

