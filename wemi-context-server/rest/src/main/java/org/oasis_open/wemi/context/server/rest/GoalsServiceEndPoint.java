package org.oasis_open.wemi.context.server.rest;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.oasis_open.wemi.context.server.api.Metadata;
import org.oasis_open.wemi.context.server.api.conditions.Condition;
import org.oasis_open.wemi.context.server.api.goals.Goal;
import org.oasis_open.wemi.context.server.api.goals.GoalReport;
import org.oasis_open.wemi.context.server.api.services.GoalsService;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@WebService
@Produces(MediaType.APPLICATION_JSON)
@CrossOriginResourceSharing(
        allowAllOrigins = true,
        allowCredentials = true
)
public class GoalsServiceEndPoint {

    GoalsService goalsService;

    @WebMethod(exclude = true)
    public void setGoalsService(GoalsService goalsService) {
        this.goalsService = goalsService;
    }

    @GET
    @Path("/")
    public Set<Metadata> getGoalMetadatas() {
        return goalsService.getGoalMetadatas();
    }

    @GET
    @Path("/{scope}/")
    public Set<Metadata> getGoalMetadatas(@PathParam("scope") String scope) {
        return goalsService.getGoalMetadatas(scope);
    }

    @GET
    @Path("/{scope}/{goalId}")
    public Goal getGoal(@PathParam("scope") String scope, @PathParam("goalId") String goalId) {
        return goalsService.getGoal(scope, goalId);
    }

    @POST
    @Path("/{scope}/{goalId}")
    public void setGoal(@PathParam("scope") String scope, @PathParam("goalId") String goalId, Goal goal) {
        goalsService.setGoal(goal);
    }

    @PUT
    @Path("/{scope}/{goalId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void createGoal(@PathParam("scope") String scope, @PathParam("goalId") String goalId, @FormParam("goalName") String name, @FormParam("goalDescription") String description) {
        goalsService.createGoal(scope, goalId, name, description);
    }

    @DELETE
    @Path("/{scope}/{goalId}")
    public void removeGoal(@PathParam("scope") String scope, @PathParam("goalId") String goalId) {
        goalsService.removeGoal(scope, goalId);
    }

    @GET
    @Path("/{scope}/{goalID}/report")
    public GoalReport getGoalReport(@PathParam("scope") String scope, @PathParam("goalID") String goalId) {
        return goalsService.getGoalReport(scope, goalId);
    }

    @GET
    @Path("/{scope}/{goalID}/report/{split}")
    public GoalReport getGoalReport(@PathParam("scope") String scope, @PathParam("goalID") String goalId, @PathParam("split") String split) {
        return goalsService.getGoalReport(scope, goalId, split);
    }

    @POST
    @Path("/{scope}/{goalID}/conditionalReport/{split}")
    public GoalReport getGoalReport(@PathParam("scope") String scope, @PathParam("goalID") String goalId, @PathParam("split") String split, Condition condition) {
        return goalsService.getGoalReport(scope, goalId, split, condition);
    }

}
