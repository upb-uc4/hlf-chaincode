package de.upb.cs.uc4.chaincode.model.errors;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * InvalidParameter
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")


public class InvalidParameter {
    @SerializedName("name")
    private String name = null;

    @SerializedName("reason")
    private String reason = null;

    public InvalidParameter name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InvalidParameter reason(String reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Get reason
     *
     * @return reason
     **/
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvalidParameter invalidParameter = (InvalidParameter) o;
        return Objects.equals(this.name, invalidParameter.name) &&
                Objects.equals(this.reason, invalidParameter.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reason);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InvalidParameter {\n");
        sb.append("    name: ").append(name).append("\n");
        sb.append("    reason: ").append(reason).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
