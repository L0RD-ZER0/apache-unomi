package org.oasis_open.wemi.context.server.rest;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.oasis_open.wemi.context.server.api.Metadata;
import org.oasis_open.wemi.context.server.api.conditions.Condition;
import org.oasis_open.wemi.context.server.api.rules.Rule;
import org.oasis_open.wemi.context.server.api.services.RulesService;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Created by loom on 10.08.14.
 */
@WebService
@Produces(MediaType.APPLICATION_JSON)
@CrossOriginResourceSharing(
        allowAllOrigins = true,
        allowCredentials = true
)
public class RulesServiceEndPoint {

    RulesService rulesService;

    public RulesServiceEndPoint() {
        System.out.println("Initializing rule service endpoint...");
    }

    @WebMethod(exclude=true)
    public void setRulesService(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @GET
    @Path("/")
    public Set<Metadata> getRuleMetadatas() {
        return rulesService.getRuleMetadatas();
    }

    @GET
    @Path("/{scope}")
    public Set<Metadata> getRuleMetadatas(@PathParam("scope") String scope) {
        return rulesService.getRuleMetadatas(scope);
    }

    @GET
    @Path("/{scope}/{ruleId}")
    public Rule getRule(@PathParam("scope") String scope, @PathParam("ruleId") String ruleId) {
        return rulesService.getRule(scope, ruleId);
    }

    @POST
    @Path("/{scope}/{ruleId}")
    public void setRule(@PathParam("scope") String scope, @PathParam("ruleId") String ruleId, Rule rule) {
        rulesService.setRule(rule);
    }

    @PUT
    @Path("/{scope}/{ruleId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void createRule(@PathParam("scope") String scope, @PathParam("ruleId") String ruleId, @FormParam("ruleName") String name, @FormParam("ruleDescription") String description) {
        rulesService.createRule(scope, ruleId, name, description);

    }

    @DELETE
    @Path("/{scope}/{ruleId}")
    public void removeRule(@PathParam("scope") String scope, @PathParam("ruleId") String ruleId) {
        rulesService.removeRule(scope, ruleId);
    }

}
