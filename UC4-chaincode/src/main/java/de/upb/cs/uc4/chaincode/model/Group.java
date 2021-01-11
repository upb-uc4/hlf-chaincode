package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("userList")
    private List<String> userList = new ArrayList<String>();

    /**
     * Get groupId
     *
     * @return groupId
     **/
    @ApiModelProperty()
    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Get userList
     *
     * @return userList
     **/
    @ApiModelProperty()
    public List<String> getUserList() {
        return this.userList;
    }

    public void resetUserList() {
        this.userList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group other = (Group) o;
        return Objects.equals(this.groupId, other.groupId) && Objects.equals(this.userList, other.userList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.groupId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Group {\n");
        sb.append("    groupId: ").append(toIndentedString(this.groupId)).append("\n");
        sb.append("    userList: ").append(toIndentedString(this.userList)).append("\n");
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