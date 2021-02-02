package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExamResult {

    @SerializedName("examResultEntries")
    private List<ExamResultEntry> examResultEntries = new ArrayList<>();

    /**
     * Get examResultEntries
     * @return examResultEntries
     **/
    @ApiModelProperty()
    public List<ExamResultEntry> getExamResultEntries() {
        return this.examResultEntries;
    }

    public void setExamResultEntries(List<ExamResultEntry> value) {
        this.examResultEntries = value;
    }


    public ExamResult addExamResultEntriesItem(ExamResultEntry examResultEntryItem) {
        if (this.examResultEntries == null) {
            this.examResultEntries = new ArrayList<ExamResultEntry>();
        }
        if (!this.examResultEntries.contains(examResultEntryItem)) {
            this.examResultEntries.add(examResultEntryItem);
        }
        return this;
    }

    public ExamResult addExamResultEntriesItems(List<ExamResultEntry> examResultEntryItems){
        for (ExamResultEntry examResultEntryItem : examResultEntryItems){
            this.addExamResultEntriesItem(examResultEntryItem);
        }
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExamResult other = (ExamResult) o;
        return Objects.equals(this.examResultEntries, other.examResultEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.examResultEntries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExamResult {\n");
        sb.append("    examResultEntries: ").append(toIndentedString(this.examResultEntries)).append("\n");
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
