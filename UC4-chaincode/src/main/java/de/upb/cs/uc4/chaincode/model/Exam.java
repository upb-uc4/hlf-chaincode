package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exam {

    @SerializedName("examId")
    private String examId;
    @SerializedName("courseId")
    private String courseId;
    @SerializedName("lecturerEnrollmentId")
    private String lecturerEnrollmentId;
    @SerializedName("moduleId")
    private String moduleId;
    @SerializedName("type")
    private String type;
    @SerializedName("date")
    private LocalDateTime date;
    @SerializedName("ects")
    private int ects;
    @SerializedName("admittableUntil")
    private LocalDateTime admittableUntil;
    @SerializedName("droppableUntil")
    private LocalDateTime droppableUntil;

    protected static final String DELIMITER = ":";

    /**
     * Get examId
     *
     * @return examId
     **/
    @ApiModelProperty()
    public String getExamId() {
        return this.examId;
    }

    public void setExamId(String value) {
        this.examId = value;
    }

    public Exam examId(String examId) {
        this.examId = examId;
        return this;
    }

    public void resetExamId() {
        this.examId = this.courseId + DELIMITER + this.moduleId + DELIMITER + this.type + DELIMITER + this.date;
    }

    /**
     * Get courseId
     *
     * @return courseId
     **/
    @ApiModelProperty()
    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String value) {
        this.courseId = value;
        resetExamId();
    }

    public Exam courseId(String courseId) {
        this.courseId = courseId;
        return this;
    }

    /**
     * Get lecturerEnrollmentId
     *
     * @return lecturerEnrollmentId
     **/
    @ApiModelProperty()
    public String getLecturerEnrollmentId() {
        return this.lecturerEnrollmentId;
    }

    public void setLecturerEnrollmentId(String value) {
        this.lecturerEnrollmentId = value;
    }

    public Exam lecturerEnrollmentId(String lecturerEnrollmentId) {
        this.lecturerEnrollmentId = lecturerEnrollmentId;
        return this;
    }

    /**
     * Get moduleId
     *
     * @return moduleId
     **/
    @ApiModelProperty()
    public String getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(String value) {
        this.moduleId = value;
        resetExamId();
    }

    public Exam moduleId(String moduleId) {
        this.moduleId = moduleId;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @ApiModelProperty()
    public String getType() {
        return this.type;
    }

    public void setType(String value) {
        this.type = value;
        resetExamId();
    }

    public Exam type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get date
     *
     * @return date
     **/
    @ApiModelProperty()
    public LocalDateTime getDate() {
        return this.date;
    }

    public void setDate(LocalDateTime value) {
        this.date = value;
        resetExamId();
    }

    public Exam date(LocalDateTime date) {
        this.date = date;
        return this;
    }

    /**
     * Get ects
     *
     * @return ects
     **/
    @ApiModelProperty()
    public int getEcts() {
        return this.ects;
    }

    public void setEcts(int value) {
        this.ects = value;
    }

    public Exam ects(int ects) {
        this.ects = ects;
        return this;
    }

    /**
     * Get admittableUntil
     *
     * @return admittableUntil
     **/
    @ApiModelProperty()
    public LocalDateTime getAdmittableUntil() {
        return this.admittableUntil;
    }

    public void setAdmittableUntil(LocalDateTime value) {
        this.admittableUntil = value;
    }

    public Exam admittableUntil(LocalDateTime admittableUntil) {
        this.admittableUntil = admittableUntil;
        return this;
    }

    /**
     * Get droppableUntil
     *
     * @return droppableUntil
     **/
    @ApiModelProperty()
    public LocalDateTime getDroppableUntil() {
        return this.droppableUntil;
    }

    public void setDroppableUntil(LocalDateTime value) {
        this.droppableUntil = value;
    }

    public Exam droppableUntil(LocalDateTime droppableUntil) {
        this.droppableUntil = droppableUntil;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Exam exam = (Exam) o;
        return Objects.equals(this.examId, exam.examId) && Objects.equals(this.courseId, exam.courseId) &&
                Objects.equals(this.lecturerEnrollmentId, exam.lecturerEnrollmentId) &&
                Objects.equals(this.moduleId, exam.moduleId) &&
                Objects.equals(this.type, exam.type) &&
                Objects.equals(this.date, exam.date) &&
                Objects.equals(this.ects, exam.ects)&&
                Objects.equals(this.admittableUntil, exam.admittableUntil) &&
                Objects.equals(this.droppableUntil, exam.droppableUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.examId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Exam {\n");
        sb.append("    examId: ").append(toIndentedString(this.examId)).append("\n");
        sb.append("    courseId: ").append(toIndentedString(this.courseId)).append("\n");
        sb.append("    lecturerEnrollmentId: ").append(toIndentedString(this.lecturerEnrollmentId)).append("\n");
        sb.append("    moduleId: ").append(toIndentedString(this.moduleId)).append("\n");
        sb.append("    type: ").append(toIndentedString(this.type)).append("\n");
        sb.append("    date: ").append(toIndentedString(this.date)).append("\n");
        sb.append("    ects: ").append(toIndentedString(this.ects)).append("\n");
        sb.append("    admittableUntil: ").append(toIndentedString(this.admittableUntil)).append("\n");
        sb.append("    droppableUntil: ").append(toIndentedString(this.droppableUntil)).append("\n");
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