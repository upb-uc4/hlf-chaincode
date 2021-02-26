package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public enum AdmissionType {
    @SerializedName("Course")
    COURSE,
    @SerializedName("Exam")
    EXAM;

    public Type valueToType() {
        switch (this) {
            case COURSE:
                return CourseAdmission.class;
            case EXAM:
                return ExamAdmission.class;
            default:
                return AbstractAdmission.class;
        }
    }
}
