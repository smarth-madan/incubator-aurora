package org.apache.aurora.client.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.aurora.client.AuroraClient;

/**
 * Created by smadan on 4/2/14.
 */
@Path("/auroraclient")
public class AuroraClientWebApp {
    //private static AuroraClient auroraClient = new AuroraClient();

    @GET()
    public String hello() {
        return "Hello";
    }

    @POST()
    @Path("/createjob")
    public String createJob(@QueryParam("aSchedulerAddr") String aSchedulerAddr,
                            @QueryParam("aSchedulerPort") String aSchedulerPort,
                            @QueryParam("jobName") String jobName,
                            @QueryParam("environment") String environment,
                            @QueryParam("cpu") String cpu,
                            @QueryParam("ram") String ram,
                            @QueryParam("disk") String disk,
                            @QueryParam("execConfig") String execConfig) {
        AuroraClient ac = new AuroraClient();
        ac.createClient(aSchedulerAddr,aSchedulerPort);
        String ret = ac.createJob(jobName,environment,Integer.parseInt(cpu),Integer.parseInt(ram),Integer.parseInt(disk),execConfig);
        ac.closeClient();
        return ret;
    }
}
