This repo is forked from Apache Aurora to develoop a REST interface for scheduling jobs along with a simple UI. Please refer to documentation of Aurora at https://github.com/apache/incubator-aurora before using this app. 


The current list of features included in this repo are :

* Schedule a simple sequential job.
* Schedule a job with advance features.
* Send a kill job command to the scheduler. 
* Get status of a Job ( Work in progress ).


To run the UI, clone the repo and run jettyRun task from gradle :

$ ./gradlew jettyRun

The UI can be accessed at : http://<HOSTNAME>:8080/incubator-aurora/pages/home.html

The jobs scheduled are stored in the browser's indexedDB, hence everytime you use a different browser or delete all the browser data, all previous jobs listed on the home page will get removed. If you do not wish to use the web UI, you could also use the REST interface to schedule jobs. Following are the interface with sample payloads :

1. Create Job:
    URL : http://localhost:8080/incubator-aurora/rest/auroraclient/createjob
    method : POST
    Header : "Content-Type:application/json"
    Sample Payload(JSON) :
        {
          "aSchedulerAddr":"192.168.33.7",
          "aSchedulerPort":"8082",
          "jobName":"job_test1",
          "environment":"devel",
          "cpu":"1",
          "ram":"256",
          "disk":"500",
          "execConfig":"{\"priority\":0,\"task\":{\"processes\":[{\"name\":\"p1\",\"cmdline\":\"echo \\\"hello\\\"\",\"daemon\":\"false\",\"ephemeral\":\"false\",\"max_failures\":\"1\",\"min_duration\":\"5\",\"final\":\"false\"},{\"name\":\"p2\",\"cmdline\":\"echo \\\"world\\\"\",\"daemon\":\"false\",\"ephemeral\":\"false\",\"max_failures\":\"1\",\"min_duration\":\"5\",\"final\":\"false\"},{\"name\":\"p3\",\"cmdline\":\"echo \\\"dsaodsf\\\"\",\"daemon\":\"false\",\"ephemeral\":\"false\",\"max_failures\":\"1\",\"min_duration\":\"5\",\"final\":\"false\"}],\"name\":\"p1\",\"finalization_wait\":\"30\",\"max_failures\":\"1\",\"max_concurrency\":\"0\",\"resources\":{\"disk\":\"500\",\"ram\":\"256\",\"cpu\":\"1\"},\"constraints\":[{\"order\":[\"p3\",\"p1\",\"p2\"]}]},\"name\":\"job1\",\"environment\":\"devel\",\"max_task_failures\":\"1\",\"enable_hooks\":\"false\",\"cluster\":\"example\",\"production\":\"false\",\"role\":\"vagrant\"}",
          "constraintName":"",
          "constraintValue":"",
          "role":"vagrant"
        }
    
    Sample Response :
        {
            "code": 0,
            "status": "1 new tasks pending for job smadan/devel/job_test1",
            "description": null
        }

2. Kill Job:
    URL : http://localhost:8080/incubator-aurora/rest/auroraclient/killjob
    method : POST
    Sample payload : 
        {
          "aSchedulerAddr":"stage2p1137.qa.paypal.com",
          "aSchedulerPort":"8082",
          "jobName":"pool_back_occ",
          "environment":"staging",
          "role":"root"
        }
    
    Sample Response : 
        {
            "code": 0,
            "status": "No tasks found for query: TaskQuery(owner:Identity(role:smadan, user:smadan), environment:staging, jobName:_occ, taskIds:null, statuses:null, slaveHost:null, instanceIds:null)",
            "description": null
        }

