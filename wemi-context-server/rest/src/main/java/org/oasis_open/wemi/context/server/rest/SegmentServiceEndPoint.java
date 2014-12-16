package org.oasis_open.wemi.context.server.rest;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.oasis_open.wemi.context.server.api.Metadata;
import org.oasis_open.wemi.context.server.api.PartialList;
import org.oasis_open.wemi.context.server.api.segments.Segment;
import org.oasis_open.wemi.context.server.api.User;
import org.oasis_open.wemi.context.server.api.services.SegmentService;
import org.oasis_open.wemi.context.server.api.segments.SegmentsAndScores;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Created by loom on 26.04.14.
 */
@WebService
@Produces(MediaType.APPLICATION_JSON)
@CrossOriginResourceSharing(
        allowAllOrigins = true,
        allowCredentials = true
)
public class SegmentServiceEndPoint {

    SegmentService segmentService;

    public SegmentServiceEndPoint() {
        System.out.println("Initializing segment service endpoint...");
    }

    @WebMethod(exclude=true)
    public void setSegmentService(SegmentService segmentService) {
        this.segmentService = segmentService;
    }

    @GET
    @Path("/{scope}/{segmentID}/match")
    public PartialList<User> getMatchingIndividuals(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("size") @DefaultValue("50") int size, @QueryParam("sort") String sortBy) {
        return segmentService.getMatchingIndividuals(scope, segmentId, offset, size, sortBy);
    }

    @GET
    @Path("/{scope}/{segmentID}/count")
    public long getMatchingIndividualsCount(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId) {
        return segmentService.getMatchingIndividualsCount(scope, segmentId);
    }

    @GET
    @Path("/{scope}/{segmentID}/match/{user}")
    public Boolean isUserInSegment(@PathParam("user") User user, @PathParam("scope") String scope, @PathParam("segmentID") String segmentId) {
        return segmentService.isUserInSegment(user, scope, segmentId);
    }

    @GET
    @Path("/")
    public Set<Metadata> getSegmentMetadatas() {
        return segmentService.getSegmentMetadatas();
    }

    @GET
    @Path("/{scope}")
    public Set<Metadata> getSegmentMetadatas(@PathParam("scope") String scope) {
        return segmentService.getSegmentMetadatas(scope);
    }

    @GET
    @Path("/{scope}/{segmentID}")
    public Segment getSegmentDefinition(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId) {
        return segmentService.getSegmentDefinition(scope, segmentId);
    }

    @POST
    @Path("/{scope}/{segmentID}")
    public void setSegmentDefinition(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId, Segment segment) {
        segmentService.setSegmentDefinition(segment);
    }

    @PUT
    @Path("/{scope}/{segmentID}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void createSegmentDefinition(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId, @FormParam("segmentName") String segmentName, @FormParam("segmentDescription") String segmentDescription) {
        segmentService.createSegmentDefinition(scope, segmentId, segmentName, segmentDescription);
    }

    @DELETE
    @Path("/{scope}/{segmentID}")
    public void removeSegmentDefinition(@PathParam("scope") String scope, @PathParam("segmentID") String segmentId) {
        segmentService.removeSegmentDefinition(scope, segmentId);
    }

}
