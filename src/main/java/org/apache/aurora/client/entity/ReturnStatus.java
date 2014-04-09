package org.apache.aurora.client.entity;

/**
 * Created by smadan on 4/9/14.
 */
public class ReturnStatus {

    private int code;
    private String status;
    private String description;

    public ReturnStatus(int code, String status){
        this.code = code;
        this.status = status;
    }

    public ReturnStatus(int code, String status, String description){
        this.code = code;
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

}
