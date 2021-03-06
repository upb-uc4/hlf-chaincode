package de.upb.cs.uc4.chaincode.model.errors;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * DetailedError
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")


public class GenericError {
    @SerializedName("type")
    private String type = null;

    @SerializedName("title")
    private String title = null;

    public GenericError type(String type) {
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

    public GenericError title(String title) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericError detailedError = (GenericError) o;
        return Objects.equals(this.type, detailedError.type) &&
                Objects.equals(this.title, detailedError.title);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DetailedError {\n");
        sb.append("    type: ").append(type).append("\n");
        sb.append("    title: ").append(title).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
