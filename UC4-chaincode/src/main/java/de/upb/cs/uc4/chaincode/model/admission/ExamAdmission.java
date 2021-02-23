package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.contract.exam.ExamContractUtil;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExamAdmission extends AbstractAdmission {

    public ExamAdmission () {
        type = AdmissionType.EXAM;
    }

    @SerializedName("examId")
    private String examId;

    public void resetAdmissionId() {
        this.admissionId = this.enrollmentId + DELIMITER + this.examId;
    }

    @ApiModelProperty()
    public String getExamId() {
        return this.examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
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
        ExamAdmission other = (ExamAdmission) o;
        return Objects.equals(this.examId, other.examId)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.admissionId, this.enrollmentId, this.examId, this.timestamp);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(toIndentedString(this.admissionId)).append("\n");
        sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
        sb.append("    courseId: ").append(toIndentedString(this.examId)).append("\n");
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
    public List<InvalidParameter> getParameterErrors() {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParams = super.getParameterErrors();

        if (GeneralHelper.valueUnset(this.examId)) {
            invalidParams.add(cUtil.getEmptyInvalidParameter(cUtil.getErrorPrefix() + ".examId"));
        }

        return invalidParams;
    }

    public List<InvalidParameter> getSemanticErrors(ChaincodeStub stub) {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        List<InvalidParameter> invalidParameters = super.getSemanticErrors(stub);

        if (!cUtil.checkExamExists(stub, this)) {
            invalidParameters.add(cUtil.getAdmissionExamNotExistsParam());
        }
        if (!cUtil.checkExamAvailableForStudent(stub, this)) {
            invalidParameters.add(cUtil.getAdmissionExamNotAvailableParam("enrollmentId"));
            invalidParameters.add(cUtil.getAdmissionExamNotAvailableParam("examId"));
        }
        if (!cUtil.checkExamAdmissionNotAlreadyExists(stub, this)) {
            invalidParameters.add(cUtil.getAdmissionAlreadyExistsParam("enrollmentId"));
            invalidParameters.add(cUtil.getAdmissionAlreadyExistsParam("examId"));
        }
        if (!cUtil.checkCourseAdmissionExists(stub, this)) {
            invalidParameters.add(cUtil.getCourseAdmissionNotExistsParam("enrollmentId"));
            invalidParameters.add(cUtil.getCourseAdmissionNotExistsParam("examId"));
        }
        if (!cUtil.checkExamAdmittable(stub, this)) {
            invalidParameters.add(cUtil.getAdmissionNotPossibleParam());
        }

        return invalidParameters;
    }

    @Override
    public void ensureIsDroppable(ChaincodeStub stub) {
        // TODO check if corresponding exam is droppable (otherwise throw error)
    }
}

