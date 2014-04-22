package org.apache.aurora.client.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.aurora.client.AuroraClient;
import org.apache.aurora.client.entity.JobConfig;
import org.apache.aurora.client.entity.ReturnStatus;

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

//    @POST()
//    @Path("/createjob")
//    public String createJob(@QueryParam("aSchedulerAddr") String aSchedulerAddr,
//                            @QueryParam("aSchedulerPort") String aSchedulerPort,
//                            @QueryParam("jobName") String jobName,
//                            @QueryParam("environment") String environment,
//                            @QueryParam("cpu") String cpu,
//                            @QueryParam("ram") String ram,
//                            @QueryParam("disk") String disk,
//                            @QueryParam("execConfig") String execConfig) {
//        AuroraClient ac = new AuroraClient();
//        ac.createClient(aSchedulerAddr,aSchedulerPort);
//        String ret = ac.createJob(jobName,environment,Integer.parseInt(cpu),Integer.parseInt(ram),Integer.parseInt(disk),execConfig);
//        ac.closeClient();
//        return ret;
//    }

    @POST()
    @Path("/createjob")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createJob(JobConfig jobConfig,
                              @Context HttpServletRequest request,
                              @Context HttpServletResponse response) {
        AuroraClient ac = new AuroraClient();
        ac.createClient(jobConfig.getaSchedulerAddr(),jobConfig.getaSchedulerPort());
        String ret = ac.createJob(jobConfig);
        ReturnStatus returnStatus = new ReturnStatus(0,ret);
        ac.closeClient();
        return Response.status(Response.Status.CREATED)
                .entity(returnStatus).type(MediaType.APPLICATION_JSON)
                .build();
    }
}
