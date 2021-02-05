package de.upb.cs.uc4.chaincode.model.examresult;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ExamResultEntry {

    @SerializedName("enrollmentId")
    private String enrollmentId;

    @SerializedName("grade")
    private GradeType grade;

    @SerializedName("examId")
    private String examId;

    /**
     * Get enrollmentId
     * @return enrollmentId
     **/
    @ApiModelProperty()
    public String getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(String value) {
        this.enrollmentId = value;
    }

    /**
     * Get grade
     * @return grade
     **/
    @ApiModelProperty()
    public GradeType getGrade() {
        return this.grade;
    }

    public void setGrade(GradeType value) {
        this.grade = value;
    }

    /**
     * Get examID
     * @return examID
     **/
    @ApiModelProperty()
    public String getExamId() {
        return this.examId;
    }

    public void setExamId(String value) {
        this.examId=value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExamResultEntry other = (ExamResultEntry) o;
        return Objects.equals(this.enrollmentId, other.enrollmentId)
                && Objects.equals(this.examId, other.examId)
                && Objects.equals(this.grade, other.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.enrollmentId, this.examId, this.grade);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExamResultEntry {\n");
        sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
        sb.append("    examId: ").append(toIndentedString(this.examId)).append("\n");
        sb.append("    grade: ").append(toIndentedString(this.grade)).append("\n");
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

}
