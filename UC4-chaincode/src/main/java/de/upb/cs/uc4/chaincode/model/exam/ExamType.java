package de.upb.cs.uc4.chaincode.model.exam;

import com.google.gson.annotations.SerializedName;

public enum ExamType {
    @SerializedName("Written Exam")
    WRITTEN_EXAM,
    @SerializedName("Oral Exam")
    ORAL_EXAM;
}
