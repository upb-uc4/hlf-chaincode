package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExaminationRegulation {
    @SerializedName("name")
    private String name = null;

    @SerializedName("active")
    private boolean active = true;

    @SerializedName("modules")
    private List<ExaminationRegulationModule> modules = null;

    public ExaminationRegulation name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExaminationRegulation active(boolean active) {
        this.active = active;
        return this;
    }

    @ApiModelProperty(value = "")
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ExaminationRegulation modules(List<ExaminationRegulationModule> modules) {
        this.modules = modules;
        return this;
    }

    public ExaminationRegulation addModuleItem(ExaminationRegulationModule module) {
        if (this.modules == null) {
            this.modules = new ArrayList<ExaminationRegulationModule>();
        }
        this.modules.add(module);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<ExaminationRegulationModule> getModules() {
        return modules;
    }

    public void setModules(List<ExaminationRegulationModule> modules) {
        this.modules = modules;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExaminationRegulation examinationRegulation = (ExaminationRegulation) o;
        return Objects.equals(this.name, examinationRegulation.name) &&
                Objects.equals(this.active, examinationRegulation.active) &&
                Objects.equals(this.modules, examinationRegulation.modules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, modules);
    }

}

