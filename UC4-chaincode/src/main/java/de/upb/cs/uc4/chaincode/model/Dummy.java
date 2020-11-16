package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

public class Dummy {
    @SerializedName("matriculationId")
    private String content;

    public Dummy(String content) {
        this.content = content;
    }

    public Dummy content(String content) {
        this.content = content;
        return this;
    }

    @ApiModelProperty(value = "")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
