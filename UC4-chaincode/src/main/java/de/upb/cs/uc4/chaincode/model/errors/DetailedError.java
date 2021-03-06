package de.upb.cs.uc4.chaincode.model.errors;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DetailedError
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")


public class DetailedError {
    @SerializedName("type")
    private String type = null;

    @SerializedName("title")
    private String title = null;

    @SerializedName("invalidParams")
    private List<InvalidParameter> invalidParams = null;

    public DetailedError type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DetailedError title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get title
     *
     * @return title
     **/
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DetailedError invalidParams(List<InvalidParameter> invalidParams) {
        this.invalidParams = invalidParams;
        return this;
    }

    public DetailedError addInvalidParamsItem(InvalidParameter invalidParamsItem) {
        if (this.invalidParams == null) {
            this.invalidParams = new ArrayList<InvalidParameter>();
        }
        this.invalidParams.add(invalidParamsItem);
        return this;
    }

    /**
     * Get invalidParams
     *
     * @return invalidParams
     **/
    public List<InvalidParameter> getInvalidParams() {
        return invalidParams;
    }

    public void setInvalidParams(List<InvalidParameter> invalidParams) {
        this.invalidParams = invalidParams;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DetailedError detailedError = (DetailedError) o;
        return Objects.equals(this.type, detailedError.type) &&
                Objects.equals(this.title, detailedError.title) &&
                Objects.equals(this.invalidParams, detailedError.invalidParams);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DetailedError {\n");
        sb.append("    type: ").append(type).append("\n");
        sb.append("    title: ").append(title).append("\n");
        sb.append("    invalidParams: ").append(invalidParams).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
