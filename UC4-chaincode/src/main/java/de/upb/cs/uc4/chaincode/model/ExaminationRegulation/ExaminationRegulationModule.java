package de.upb.cs.uc4.chaincode.model.ExaminationRegulation;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ExaminationRegulationModule {

    @SerializedName("id")
    private String id = null;

    @SerializedName("name")
    private String name = null;

    public ExaminationRegulationModule id(String id) {
        this.id = id;
        return this;
    }

    public ExaminationRegulationModule name(String name) {
        this.name = name;
        return this;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExaminationRegulationModule module = (ExaminationRegulationModule) o;
        return Objects.equals(this.id, module.id) &&
                Objects.equals(this.name, module.name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Module {\n");
        sb.append("    id: ").append(id).append("\n");
        sb.append("    name: ").append(name).append("\n");
        sb.append("}");
        return sb.toString();
    }
}

