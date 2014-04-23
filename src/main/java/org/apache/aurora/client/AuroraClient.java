package org.apache.aurora.client;

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

import java.util.HashSet;
import java.util.Set;

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
            Response res = client.killTasks(new TaskQuery().setJobKeys(jobKeys),null,SESSION);
            System.out.println(res.getMessage());
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

    public JobConfiguration makeJob(TaskConfig taskConfig,IJobKey jobKey, JobConfig jobConfig ){
        return new JobConfiguration()
                .setOwner(new Identity(jobConfig.getRole(), jobConfig.getRole()))
                .setInstanceCount(1)
                .setTaskConfig(taskConfig)
                .setKey(jobKey.newBuilder());
    }

//    private TaskConfig defaultTask(boolean production) {
//
//        Set<Constraint> constraints = new HashSet<Constraint>();
//        Constraint constraint = new Constraint();
//        TaskConstraint taskConstraint = new TaskConstraint();
//        HashSet<String> value = new HashSet<String>();
//        value.add("a") ;
//        taskConstraint.setValue(new ValueConstraint(false,value));
//        constraint.setName("rack");
//        constraint.setConstraint(taskConstraint);
//        constraints.add(constraint);
//
//        String task= "{\"priority\": 0, \"task\": {\"processes\": [{\"daemon\": false, \"name\": \"hello\", \"ephemeral\": false, \"max_failures\": 1, \"min_duration\": 5, \"cmdline\": \"\\n    while true; do\\n      echo hello world\\n      sleep 10\\n    done\\n  \", \"final\": false}], \"name\": \"hello\", \"finalization_wait\": 30, \"max_failures\": 1, \"max_concurrency\": 0, \"resources\": {\"disk\": 134217728, \"ram\": 134217728, \"cpu\": 1.0}, \"constraints\": [{\"order\": [\"hello\"]}]}, \"name\": \"hello\", \"environment\": \"prod\", \"max_task_failures\": 1, \"enable_hooks\": false, \"cluster\": \"example\", \"production\": true, \"role\": \"knagireddy\"}";
//        return new TaskConfig()
//                .setOwner(new Identity(ROLE, USER))
//                .setEnvironment("prod")
//                .setJobName(JOB_NAME)
//                .setContactEmail("smadan@paypal.com")
//                .setExecutorConfig(new ExecutorConfig("AuroraExecutor",task))
//                .setNumCpus(1)
//                .setRamMb(10)
//                .setDiskMb(10)
//                .setProduction(production)
//                .setConstraints(constraints);
//    }

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

