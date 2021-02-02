package de.upb.cs.uc4.chaincode.model.admission;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class AbstractAdmission {
    protected static final String DELIMITER = ":";

    @SerializedName("admissionId")
    protected String admissionId;

    @SerializedName("enrollmentId")
    protected String enrollmentId;

    @SerializedName("timestamp")
    protected LocalDateTime timestamp;

    @SerializedName("type")
    protected AdmissionType type;

    /**
     * Get admissionId
     *
     * @return admissionId
     **/
    @ApiModelProperty()
    public String getAdmissionId() {
        return this.admissionId;
    }

    public void resetAdmissionId() {}

    @ApiModelProperty()
    public String getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
        resetAdmissionId();
    }

    @ApiModelProperty()
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @ApiModelProperty()
    public AdmissionType getType() {
        return this.type;
    }

    public void setType(AdmissionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractAdmission other = (AbstractAdmission) o;
        return Objects.equals(this.admissionId, other.admissionId)
                && Objects.equals(this.enrollmentId, other.enrollmentId)
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.admissionId, this.enrollmentId, this.timestamp, this.type);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Admission {\n");
        sb.append("    admissionId: ").append(toIndentedString(this.admissionId)).append("\n");
        sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
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
}

