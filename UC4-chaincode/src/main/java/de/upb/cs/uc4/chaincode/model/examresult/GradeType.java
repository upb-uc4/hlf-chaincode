package de.upb.cs.uc4.chaincode.model.examresult;

import com.google.gson.annotations.SerializedName;

public enum GradeType {
    @SerializedName("1.0")
    ONE_ZERO,
    @SerializedName("1.3")
    ONE_THREE,
    @SerializedName("1.7")
    ONE_SEVEN,
    @SerializedName("2.0")
    TWO_ZERO,
    @SerializedName("2.3")
    TWO_THREE,
    @SerializedName("2.7")
    TWO_SEVEN,
    @SerializedName("3.0")
    THREE_ZERO,
    @SerializedName("3.3")
    THREE_THREE,
    @SerializedName("3.7")
    THREE_SEVEN,
    @SerializedName("4.0")
    FOUR_ZERO,
    @SerializedName("5.0")
    FIVE_ZERO
}
