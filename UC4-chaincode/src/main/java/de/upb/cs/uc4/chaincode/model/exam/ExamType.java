package de.upb.cs.uc4.chaincode.model.exam;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public enum ExamType {
    @SerializedName("Written Exam")
    WRITTEN_EXAM ("Written Exam"),
    @SerializedName("Oral Exam")
    ORAL_EXAM ("Oral Exam");

    private final String beautifulValue;

    ExamType(String pBeautifulValue) {
        beautifulValue = pBeautifulValue;
    }

    @Override
    public String toString() {
        return this.beautifulValue;
    }

    public static String[] possibleStringValues(){
        return Arrays.stream(ExamType.values()).map(ExamType::toString).toArray(String[]::new);
    }
}
