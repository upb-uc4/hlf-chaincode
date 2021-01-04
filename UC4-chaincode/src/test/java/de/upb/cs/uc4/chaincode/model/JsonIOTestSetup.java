package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import de.upb.cs.uc4.chaincode.util.*;
import io.swagger.annotations.ApiModelProperty;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MatriculationData
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")


public class JsonIOTestSetup {

    @SerializedName("admissionContract")
    private List<Dummy> admissionContract = null;

    public JsonIOTestSetup admissionContract(List<Dummy> admissionContract) {
        this.admissionContract = admissionContract;
        return this;
    }

    public JsonIOTestSetup addAdmissionContractItem(Dummy admissionContractItem) {
        if (this.admissionContract == null) {
            this.admissionContract = new ArrayList<Dummy>();
        }
        this.admissionContract.add(admissionContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getAdmissionContract() {
        return admissionContract;
    }

    public void setAdmissionContract(List<Dummy> admissionContract) {
        this.admissionContract = admissionContract;
    }

    @SerializedName("operationContract")
    private List<Dummy> operationContract = null;

    public JsonIOTestSetup operationContract(List<Dummy> operationContract) {
        this.operationContract = operationContract;
        return this;
    }

    public JsonIOTestSetup addOperationContractItem(Dummy operationContractItem) {
        if (this.operationContract == null) {
            this.operationContract = new ArrayList<Dummy>();
        }
        this.operationContract.add(operationContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getOperationContract() {
        return operationContract;
    }

    public void setOperationContract(List<Dummy> operationContract) {
        this.operationContract = operationContract;
    }

    @SerializedName("certificateContract")
    private List<Dummy> certificateContract = null;

    public JsonIOTestSetup certificateContract(List<Dummy> certificateContract) {
        this.certificateContract = certificateContract;
        return this;
    }

    public JsonIOTestSetup addCertificateContractItem(Dummy certificateContractItem) {
        if (this.certificateContract == null) {
            this.certificateContract = new ArrayList<Dummy>();
        }
        this.certificateContract.add(certificateContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getCertificateContract() {
        return certificateContract;
    }

    public void setCertificateContract(List<Dummy> certificateContract) {
        this.certificateContract = certificateContract;
    }

    @SerializedName("examinationRegulationContract")
    private List<Dummy> examinationRegulationContract = null;

    public JsonIOTestSetup examinationRegulationContract(List<Dummy> examinationRegulationContract) {
        this.examinationRegulationContract = examinationRegulationContract;
        return this;
    }

    public JsonIOTestSetup addExaminationRegulationContractItem(Dummy examinationRegulationContractItem) {
        if (this.examinationRegulationContract == null) {
            this.examinationRegulationContract = new ArrayList<Dummy>();
        }
        this.examinationRegulationContract.add(examinationRegulationContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getExaminationRegulationContract() {
        return examinationRegulationContract;
    }

    public void setExaminationRegulationContract(List<Dummy> examinationRegulationContract) {
        this.examinationRegulationContract = examinationRegulationContract;
    }

    @SerializedName("groupContract")
    private List<Dummy> groupContract = null;

    public JsonIOTestSetup groupContract(List<Dummy> groupContract) {
        this.groupContract = groupContract;
        return this;
    }

    public JsonIOTestSetup addGroupContractItem(Dummy groupContractItem) {
        if (this.groupContract == null) {
            this.groupContract = new ArrayList<Dummy>();
        }
        this.groupContract.add(groupContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getGroupContract() {
        return groupContract;
    }

    public void setGroupContract(List<Dummy> groupContract) {
        this.groupContract = groupContract;
    }

    @SerializedName("matriculationDataContract")
    private List<Dummy> matriculationDataContract = null;

    public JsonIOTestSetup matriculationDataContract(List<Dummy> matriculationDataContract) {
        this.matriculationDataContract = matriculationDataContract;
        return this;
    }

    public JsonIOTestSetup addMatriculationDataContractItem(Dummy matriculationDataContractItem) {
        if (this.matriculationDataContract == null) {
            this.matriculationDataContract = new ArrayList<Dummy>();
        }
        this.matriculationDataContract.add(matriculationDataContractItem);
        return this;
    }

    @ApiModelProperty(value = "")
    public List<Dummy> getMatriculationDataContract() {
        return matriculationDataContract;
    }

    public void setMatriculationDataContract(List<Dummy> matriculationDataContract) {
        this.matriculationDataContract = matriculationDataContract;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonIOTestSetup matriculationData = (JsonIOTestSetup) o;
        return Objects.equals(this.operationContract, matriculationData.operationContract) &&
                Objects.equals(this.certificateContract, matriculationData.certificateContract) &&
                Objects.equals(this.examinationRegulationContract, matriculationData.examinationRegulationContract) &&
                Objects.equals(this.matriculationDataContract, matriculationData.matriculationDataContract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationContract, certificateContract, examinationRegulationContract, matriculationDataContract);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JsonIOTestSetup {\n");

        sb.append("    operationContract: ").append(toIndentedString(operationContract)).append("\n");
        sb.append("    certificateContract: ").append(toIndentedString(certificateContract)).append("\n");
        sb.append("    examinationRegulationContract: ").append(toIndentedString(examinationRegulationContract)).append("\n");
        sb.append("    matriculationDataContract: ").append(toIndentedString(matriculationDataContract)).append("\n");
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

    public void prepareStub(ChaincodeStub stub) {
        ContractUtil cUtil = new OperationContractUtil();
        List<String> setup = TestUtil.toStringList(this.operationContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }

        cUtil = new CertificateContractUtil();
        setup = TestUtil.toStringList(this.certificateContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }

        cUtil = new ExaminationRegulationContractUtil();
        setup = TestUtil.toStringList(this.examinationRegulationContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }

        cUtil = new MatriculationDataContractUtil();
        setup = TestUtil.toStringList(this.matriculationDataContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }

        cUtil = new AdmissionContractUtil();
        setup = TestUtil.toStringList(this.admissionContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }

        cUtil = new GroupContractUtil();
        setup = TestUtil.toStringList(this.groupContract);
        for (int i = 0; i < setup.size(); i += 2) {
            cUtil.putAndGetStringState(stub, setup.get(i).toString(), setup.get(i + 1));
        }
    }

}

