package de.upb.cs.uc4.chaincode.model.examresult;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ExamResultEntry {

    @SerializedName("enrollmentId")
    private String enrollmentId;

    @SerializedName("examId")
    private String examId;

    @SerializedName("grade")
    private GradeType grade;

    /**
     * Get enrollmentId
     * @return enrollmentId
     **/
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExamResultEntry {\n");
        sb.append("    enrollmentId: ").append(this.enrollmentId).append("\n");
        sb.append("    examId: ").append(this.examId).append("\n");
        sb.append("    grade: ").append(this.grade).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
