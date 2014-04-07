package org.apache.aurora.client;

import org.apache.aurora.gen.*;
import org.apache.aurora.scheduler.base.JobKeys;
import org.apache.aurora.scheduler.storage.entities.IJobConfiguration;
import org.apache.aurora.scheduler.storage.entities.IJobKey;
import org.apache.aurora.scheduler.storage.entities.ILock;
import org.apache.aurora.scheduler.storage.entities.ILockKey;
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
    private static final String ROLE = "knagireddy";
    private static final String USER = "knagireddy";
    private static final String JOB_NAME = "hello_world";
    private static final IJobKey JOB_KEY = JobKeys.from(ROLE, DEFAULT_ENVIRONMENT, JOB_NAME);
    private static final SessionKey SESSION = new SessionKey();
    private static final ILockKey LOCK_KEY = ILockKey.build(LockKey.job(JOB_KEY.newBuilder()));
    private static final ILock LOCK = ILock.build(new Lock().setKey(LOCK_KEY.newBuilder()));
    private static AuroraAdmin.Client client;
    private static TTransport transport;
    private static TProtocol protocol;

//    public static void main(String args[]){
//        AuroraClient ac = new AuroraClient();
//        ac.createClient("192.168.33.6","8082");
//        ac.killJob();
//        ac.closeClient();
//    }


    public void createClient(String ip, String port){
        try {
            if (client == null) {
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

    public void killJob(){

        try {
            Response res = client.killTasks(new TaskQuery().setOwner(new Identity(ROLE, USER)).setEnvironment("devel"),null,SESSION);
            System.out.println(res.getMessage());
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public String createJob(String jobName, String environment,Integer cpu, Integer ram, Integer disk,String execConfig ){
        IJobKey jobKey= JobKeys.from(ROLE, environment, jobName);
        TaskConfig task =  makeTask(jobName,true,environment,cpu,ram,disk,execConfig);
        JobConfiguration jobConfiguration = makeJob(task,jobKey);
        String result = sendCreateJob(jobConfiguration);
        return result;
    }

    public String sendCreateJob(JobConfiguration jobConfiguration){
        try {
            IJobConfiguration job = IJobConfiguration.build(jobConfiguration);
            Response res = client.setQuota(ROLE,new ResourceAggregate().setNumCpus(3).setDiskMb(1500).setRamMb(1500), SESSION);
            System.out.println(res.getMessage());

            res = client.createJob(job.newBuilder(),null, SESSION);
            return res.getMessage();
        } catch (TException x) {
            x.printStackTrace();
            return x.getMessage();
        }
    }

    public JobConfiguration makeJob(TaskConfig taskConfig,IJobKey jobKey ){
        return new JobConfiguration()
                .setOwner(new Identity(ROLE, USER))
                .setInstanceCount(1)
                .setTaskConfig(taskConfig)
                .setKey(jobKey.newBuilder());
    }

    public JobConfiguration makeJob(){
        return makeJob(defaultTask(true), JOB_KEY);
    }

    private TaskConfig defaultTask(boolean production) {

        Set<Constraint> constraints = new HashSet<Constraint>();
        Constraint constraint = new Constraint();
        TaskConstraint taskConstraint = new TaskConstraint();
        HashSet<String> value = new HashSet<String>();
        value.add("a") ;
        taskConstraint.setValue(new ValueConstraint(false,value));
        constraint.setName("rack");
        constraint.setConstraint(taskConstraint);
        constraints.add(constraint);

        String task= "{\"priority\": 0, \"task\": {\"processes\": [{\"daemon\": false, \"name\": \"hello\", \"ephemeral\": false, \"max_failures\": 1, \"min_duration\": 5, \"cmdline\": \"\\n    while true; do\\n      echo hello world\\n      sleep 10\\n    done\\n  \", \"final\": false}], \"name\": \"hello\", \"finalization_wait\": 30, \"max_failures\": 1, \"max_concurrency\": 0, \"resources\": {\"disk\": 134217728, \"ram\": 134217728, \"cpu\": 1.0}, \"constraints\": [{\"order\": [\"hello\"]}]}, \"name\": \"hello\", \"environment\": \"prod\", \"max_task_failures\": 1, \"enable_hooks\": false, \"cluster\": \"example\", \"production\": true, \"role\": \"knagireddy\"}";
        return new TaskConfig()
                .setOwner(new Identity(ROLE, USER))
                .setEnvironment("prod")
                .setJobName(JOB_NAME)
                .setContactEmail("smadan@paypal.com")
                .setExecutorConfig(new ExecutorConfig("AuroraExecutor",task))
                .setNumCpus(1)
                .setRamMb(10)
                .setDiskMb(10)
                .setProduction(production)
                .setConstraints(constraints);
    }

    public TaskConfig makeTask(String jobName, boolean production,String environment,Integer cpu, Integer ram, Integer disk,String execConfig) {

        String task= execConfig;
        return new TaskConfig()
                .setOwner(new Identity(ROLE, USER))
                .setEnvironment(environment)
                .setJobName(jobName)
                .setContactEmail("smadan@paypal.com")
                .setExecutorConfig(new ExecutorConfig("AuroraExecutor", task))
                .setNumCpus(cpu)
                .setRamMb(ram)
                .setDiskMb(disk)
                .setProduction(production)
                .setConstraints(getConstraints(jobName));
    }

    private  Set<Constraint> getConstraints(String jobName){
        Set<Constraint> constraints = new HashSet<Constraint>();
        Constraint constraint = new Constraint();
        TaskConstraint taskConstraint = new TaskConstraint();
        HashSet<String> value = new HashSet<String>();
        constraint.setName("rack");

        if(jobName.contains("mpp")) {
            value.add("mpp");
        }else if(jobName.contains("occ")){
            value.add("occ");
        }else{
            return null;
        }

        taskConstraint.setValue(new ValueConstraint(false,value));
        constraint.setConstraint(taskConstraint);
        constraints.add(constraint);

        return constraints;


    }
}

