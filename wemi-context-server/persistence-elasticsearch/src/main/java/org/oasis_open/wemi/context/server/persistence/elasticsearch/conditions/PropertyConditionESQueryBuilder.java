package org.oasis_open.wemi.context.server.persistence.elasticsearch.conditions;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.oasis_open.wemi.context.server.api.conditions.Condition;

/**
* Created by toto on 27/06/14.
*/
public class PropertyConditionESQueryBuilder implements ESQueryBuilder {

    public PropertyConditionESQueryBuilder() {
    }

    public FilterBuilder buildFilter(Condition condition, ConditionESQueryBuilderDispatcher dispatcher) {
        String op = (String) condition.getParameterValues().get("comparisonOperator");
        String name = (String) condition.getParameterValues().get("propertyName");
        String value = (String) condition.getParameterValues().get("propertyValue");
        if (op.equals("equals")) {
            return FilterBuilders.termFilter(name, value);
        } else if (op.equals("greaterThan")) {
            return FilterBuilders.rangeFilter(name).gt(Integer.parseInt(value));
        } else if (op.equals("greaterThanOrEqualTo")) {
            return FilterBuilders.rangeFilter(name).gte(Integer.parseInt(value));
        } else if (op.equals("lessThan")) {
            return FilterBuilders.rangeFilter(name).lt(Integer.parseInt(value));
        } else if (op.equals("lessThanOrEqualTo")) {
            return FilterBuilders.rangeFilter(name).lte(Integer.parseInt(value));
        } else if (op.equals("exists")) {
            return FilterBuilders.existsFilter(name);
        } else if (op.equals("contains")) {
            return FilterBuilders.termFilter(name, value);
        } else if (op.equals("startsWith")) {
            return FilterBuilders.prefixFilter(name, value);
        } else if (op.equals("endsWith")) {
            return FilterBuilders.regexpFilter(name, ".*" + value);
        } else if (op.equals("matchesRegex")) {
            return FilterBuilders.regexpFilter(name, value);
        }
        return null;
    }
}