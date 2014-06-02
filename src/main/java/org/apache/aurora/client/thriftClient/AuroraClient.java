package org.apache.aurora.client.thriftClient;

import org.apache.aurora.client.entity.JobConfig;
import org.apache.aurora.gen.*;
import org.apache.aurora.scheduler.base.JobKeys;
import org.apache.aurora.scheduler.storage.entities.IJobConfiguration;
import org.apache.aurora.scheduler.storage.entities.IJobKey;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.*;

import static org.apache.aurora.gen.apiConstants.DEFAULT_ENVIRONMENT;

/**
 * Created by smadan on 3/27/14.
 */
public class AuroraClient{
    private static final String JOB_NAME = "hello_world";
    private static final SessionKey SESSION = new SessionKey();
    private static AuroraAdmin.Client client;
    private static TTransport transport;
    private static TProtocol protocol;


    public void createClient(String ip, String port){
        try {
            if (client == null) {
                System.out.println("Opening Connection to IP="+ip+":"+port);
                transport = new TSocket(ip, Integer.parseInt(port));
                transport.open();

                protocol = new TBinaryProtocol(transport);
                client = new AuroraAdmin.Client(protocol);

            }
        }catch(TException e){
                e.printStackTrace();
        }
    }

    public void closeClient(){
        transport.close();
        client=null;
    }

    public String killJob(JobConfig jobConfig){

        try {
            IJobKey jobKey= JobKeys.from(jobConfig.getRole(), jobConfig.getEnvironment(), jobConfig.getJobName());
            Set<JobKey> jobKeys = new HashSet<JobKey>();
            jobKeys.add(jobKey.newBuilder());
            TaskQuery taskQuery = new TaskQuery();
            taskQuery.setJobName(jobConfig.getJobName());
            taskQuery.setEnvironment(jobConfig.getEnvironment());
            taskQuery.setOwner(new Identity(jobConfig.getRole(),jobConfig.getRole()));
            Response res = client.killTasks(taskQuery,null,SESSION);
            System.out.println(res.toString());
            return res.getMessage();
        } catch (TException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }

    public String createJob(JobConfig jobConfig){
        IJobKey jobKey= JobKeys.from(jobConfig.getRole(), jobConfig.getEnvironment(), jobConfig.getJobName());
        TaskConfig task =  makeTask(jobConfig);
        JobConfiguration jobConfiguration = makeJob(task,jobKey,jobConfig);
        System.out.println(jobConfiguration.toString());
        String result = sendCreateJob(jobConfiguration, jobConfig.getRole());
        return result;
    }

    public String sendCreateJob(JobConfiguration jobConfiguration, String role){
        try {
            IJobConfiguration job = IJobConfiguration.build(jobConfiguration);
            Response res = client.setQuota(role,new ResourceAggregate().setNumCpus(400).setDiskMb(700000).setRamMb(1200000), SESSION);
            System.out.println(res.getMessage());

            res = client.createJob(job.newBuilder(),null, SESSION);
            return res.getMessage();
        } catch (TException x) {
            x.printStackTrace();
            return x.getMessage();
        }
    }

    public String getStatus(JobConfig jobConfig){
        try {
             IJobKey jobKey= JobKeys.from(jobConfig.getRole(), jobConfig.getEnvironment(), jobConfig.getJobName());
             TaskQuery taskQuery = new TaskQuery();
             taskQuery.setJobName(jobConfig.getJobName());
             taskQuery.setEnvironment(jobConfig.getEnvironment());
             Set<JobKey> jobKeys = new HashSet<JobKey>();
             taskQuery.setOwner(new Identity(jobConfig.getRole(),jobConfig.getRole()));
             //jobKeys.add(jobKey.newBuilder());
             taskQuery.setJobKeys(jobKeys);
             Response res =  client.getTasksStatus(taskQuery);
             List<ScheduledTask> tasks = res.getResult().getScheduleStatusResult().getTasks();
             ScheduledTask lastTask = tasks.get(tasks.size()-1);
             return lastTask.getStatus().toString();
        } catch (TException x) {
            x.printStackTrace();
            return x.getMessage();
        }
    }

    public JobConfiguration makeJob(TaskConfig taskConfig,IJobKey jobKey, JobConfig jobConfig ){
        return new JobConfiguration()
                .setOwner(new Identity(jobConfig.getRole(), jobConfig.getRole()))
                .setInstanceCount(1)
                .setTaskConfig(taskConfig)
                .setKey(jobKey.newBuilder());
    }

    public TaskConfig makeTask(JobConfig jobConfig){
        String task= jobConfig.getExecConfig();
        return new TaskConfig()
                .setOwner(new Identity(jobConfig.getRole(), jobConfig.getRole()))
                .setEnvironment(jobConfig.getEnvironment())
                .setJobName(jobConfig.getJobName())
                .setContactEmail("smadan@paypal.com")
                .setExecutorConfig(new ExecutorConfig("AuroraExecutor", task))
                .setNumCpus(Integer.parseInt(jobConfig.getCpu()))
                .setRamMb(Integer.parseInt(jobConfig.getRam()))
                .setDiskMb(Integer.parseInt(jobConfig.getDisk()))
                .setProduction(true)
                .setConstraints(getConstraints(jobConfig));
    }

    private  Set<Constraint> getConstraints(JobConfig jobConfig){
        Set<Constraint> constraints = new HashSet<Constraint>();
        Constraint constraint = new Constraint();
        TaskConstraint taskConstraint = new TaskConstraint();
        HashSet<String> value = new HashSet<String>();
        if(!jobConfig.getConstraintName().isEmpty()) {
            constraint.setName(jobConfig.getConstraintName());
            value.add(jobConfig.getConstraintValue());
        }else{
            return null;
        }

        taskConstraint.setValue(new ValueConstraint(false,value));
        constraint.setConstraint(taskConstraint);
        constraints.add(constraint);
        return constraints;
    }
}

