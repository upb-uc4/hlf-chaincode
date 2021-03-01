package de.upb.cs.uc4.chaincode.model.exam;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
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
    private ExamType type;
    @SerializedName("date")
    private Instant date;
    @SerializedName("ects")
    private int ects;
    @SerializedName("admittableUntil")
    private Instant admittableUntil;
    @SerializedName("droppableUntil")
    private Instant droppableUntil;

    protected static final String DELIMITER = ":";

    /**
     * Get examId
     *
     * @return examId
     **/
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
        this.examId = this.courseId + DELIMITER + this.moduleId + DELIMITER + getTypeString() + DELIMITER + getDateString();
    }

    /**
     * Get courseId
     *
     * @return courseId
     **/
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
    public ExamType getType() {
        return this.type;
    }

    public void setType(ExamType value) {
        this.type = value;
        resetExamId();
    }

    public Exam type(ExamType type) {
        this.type = type;
        return this;
    }

    public String getTypeString() {
        return GeneralHelper.enumValueAsString(this.type);
    }

    /**
     * Get date
     *
     * @return date
     **/
    public Instant getDate() {
        return this.date;
    }
    public String getDateString() {
        return GsonWrapper.toJson(this.date);
    }

    public void setDate(Instant value) {
        this.date = value;
        resetExamId();
    }

    public Exam date(Instant date) {
        this.date = date;
        return this;
    }

    /**
     * Get ects
     *
     * @return ects
     **/
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
    public Instant getAdmittableUntil() {
        return this.admittableUntil;
    }
    public String getAdmittableUntilString() {
        return GsonWrapper.toJson(this.admittableUntil);
    }

    public void setAdmittableUntil(Instant value) {
        this.admittableUntil = value;
    }

    public Exam admittableUntil(Instant admittableUntil) {
        this.admittableUntil = admittableUntil;
        return this;
    }

    /**
     * Get droppableUntil
     *
     * @return droppableUntil
     **/
    public Instant getDroppableUntil() {
        return this.droppableUntil;
    }
    public String getDroppableUntilString() {
        return GsonWrapper.toJson(this.droppableUntil);
    }

    public void setDroppableUntil(Instant value) {
        this.droppableUntil = value;
    }

    public Exam droppableUntil(Instant droppableUntil) {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Exam {\n");
        sb.append("    examId: ").append(this.examId).append("\n");
        sb.append("    courseId: ").append(this.courseId).append("\n");
        sb.append("    lecturerEnrollmentId: ").append(this.lecturerEnrollmentId).append("\n");
        sb.append("    moduleId: ").append(this.moduleId).append("\n");
        sb.append("    type: ").append(this.type).append("\n");
        sb.append("    date: ").append(this.getDateString()).append("\n");
        sb.append("    ects: ").append(this.ects).append("\n");
        sb.append("    admittableUntil: ").append(this.getAdmittableUntilString()).append("\n");
        sb.append("    droppableUntil: ").append(this.getDroppableUntilString()).append("\n");
        sb.append("}");
        return sb.toString();
    }
}