package org.apache.aurora.client.entity;

/**
 * Created by smadan on 4/8/14.
 */
public class JobConfig {
    private String aSchedulerAddr;
    private String aSchedulerPort;
    private String jobName;
    private String environment;
    private String cpu;
    private String ram;
    private String disk;
    private String execConfig;
    private String constraintName;
    private String constraintValue;
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getConstraintValue() {
        return constraintValue;
    }

    public void setConstraintValue(String constraintValue) {
        this.constraintValue = constraintValue;
    }

    public String getaSchedulerAddr() {
        return aSchedulerAddr;
    }

    public void setaSchedulerAddr(String aSchedulerAddr) {
        this.aSchedulerAddr = aSchedulerAddr;
    }

    public String getaSchedulerPort() {
        return aSchedulerPort;
    }

    public void setaSchedulerPort(String aSchedulerPort) {
        this.aSchedulerPort = aSchedulerPort;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public String getExecConfig() {
        return execConfig;
    }

    public void setExecConfig(String execConfig) {
        this.execConfig = execConfig;
    }

}
