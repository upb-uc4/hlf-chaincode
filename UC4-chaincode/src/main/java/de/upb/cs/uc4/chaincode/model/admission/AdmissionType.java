package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.model.exam.ExamType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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

    public static String[] possibleStringValues(){
        return Arrays.stream(ExamType.values()).map(ExamType::toString).toArray(String[]::new);
    }
}
