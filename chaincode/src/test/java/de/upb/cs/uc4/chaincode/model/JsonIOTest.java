package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class JsonIOTest {
    @SerializedName("name")
    private String name = null;

    @SerializedName("type")
    private String type = null;

    @SerializedName("setup")
    private List<Dummy> setup = null;

    @SerializedName("input")
    private List<Dummy> input = null;

    @SerializedName("compare")
    private List<Dummy> compare = null;

    public JsonIOTest name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(value = "")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonIOTest type(String type) {
        this.type = type;
        return this;
    }

    @ApiModelProperty(value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonIOTest setup(List<Dummy> setup) {
        this.setup = setup;
        return this;
    }

    public JsonIOTest addSetupItem(Dummy setupItem) {
        if (this.setup == null) {
            this.setup = new ArrayList<Dummy>();
        }
        this.setup.add(setupItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getSetup() {
        return setup;
    }

    public void setSetup(List<Dummy> setup) {
        this.setup = setup;
    }

    public JsonIOTest input(List<Dummy> input) {
        this.input = input;
        return this;
    }

    public JsonIOTest addInputItem(Dummy inputItem) {
        if (this.input == null) {
            this.input = new ArrayList<Dummy>();
        }
        this.input.add(inputItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getInput() {
        return input;
    }

    public void setInput(List<Dummy> input) {
        this.input = input;
    }

    public JsonIOTest compare(List<Dummy> compare) {
        this.compare = compare;
        return this;
    }

    public JsonIOTest addCompareItem(Dummy compareItem) {
        if (this.compare == null) {
            this.compare = new ArrayList<Dummy>();
        }
        this.compare.add(compareItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getCompare() {
        return compare;
    }

    public void setCompare(List<Dummy> compare) {
        this.compare = compare;
    }
}