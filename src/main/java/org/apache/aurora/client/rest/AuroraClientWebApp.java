package org.apache.aurora.client.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.aurora.client.thriftClient.AuroraClient;
import org.apache.aurora.client.entity.JobConfig;
import org.apache.aurora.client.entity.ReturnStatus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import com.twitter.common.base.Closure;
import com.twitter.common.stats.Stats;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Created by smadan on 4/2/14.
 */
@Path("/auroraclient")
public class AuroraClientWebApp {
    //private static AuroraClient auroraClient = new AuroraClient();
    private static final Logger LOG = Logger.getLogger(AuroraClientWebApp.class.getName());
    private static final AtomicLong PINGS = Stats.exportLong("pings");
    @VisibleForTesting
    static final int DEFAULT_TTL = 60;

    private final Closure<String> client;

    @Inject
    AuroraClientWebApp(Closure<String> client) {
        this.client = Preconditions.checkNotNull(client);
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

    @POST()
    @Path("/killjob")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response killJob(JobConfig jobConfig,
                              @Context HttpServletRequest request,
                              @Context HttpServletResponse response) {
        AuroraClient ac = new AuroraClient();
        ac.createClient(jobConfig.getaSchedulerAddr(),jobConfig.getaSchedulerPort());
        String ret = ac.killJob(jobConfig);
        ReturnStatus returnStatus = new ReturnStatus(0,ret);
        ac.closeClient();
        return Response.status(Response.Status.CREATED)
                .entity(returnStatus).type(MediaType.APPLICATION_JSON)
                .build();

    }

    @GET
    @Path("/{message}")
    public String incoming(@PathParam("message") String message) {
        return incoming(message, DEFAULT_TTL);
    }

    /**
     * Services an incoming ping request.
     */
    @GET
    @Path("/{message}/{ttl}")
    public String incoming(
            @PathParam("message") final String message,
            @PathParam("ttl") int ttl) {

        LOG.info("Got ping, ttl=" + ttl);
        PINGS.incrementAndGet();
        if (ttl > 1) {
            client.execute("/ping/" + message + "/" + (ttl - 1));
        }
        return "pong\n";
    }
}
