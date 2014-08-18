package org.oasis_open.wemi.context.server.persistence.elasticsearch.conditions;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.oasis_open.wemi.context.server.api.conditions.Condition;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.*;

/**
 * Created by loom on 26.06.14.
 */
public class ConditionESQueryBuilderDispatcher {

    private BundleContext bundleContext;

    public ConditionESQueryBuilderDispatcher() {
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getQuery(Condition condition) {
        return "{\"query\": " + getQueryBuilder(condition).toString() + "}";
    }

    public FilteredQueryBuilder getQueryBuilder(Condition condition) {
        return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), buildFilter(condition));
    }

    public FilterBuilder buildFilter(Condition condition) {
        Collection<ServiceReference<ESQueryBuilder>> matchingQueryBuilderReferences = null;
        try {
            matchingQueryBuilderReferences = bundleContext.getServiceReferences(ESQueryBuilder.class, condition.getConditionType().getQueryBuilderFilter());
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        // despite multiple references possible, we will only execute the first one
        for (ServiceReference<ESQueryBuilder> queryBuilderServiceReference : matchingQueryBuilderReferences) {
            ESQueryBuilder queryBuilder = bundleContext.getService(queryBuilderServiceReference);
            return queryBuilder.buildFilter(condition, this);
        }
        // if no matching
        return FilterBuilders.matchAllFilter();
    }


}