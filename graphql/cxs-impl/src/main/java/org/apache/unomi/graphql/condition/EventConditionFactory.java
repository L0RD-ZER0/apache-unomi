/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.unomi.graphql.condition;

import graphql.schema.DataFetchingEnvironment;
import org.apache.unomi.api.conditions.Condition;
import org.apache.unomi.graphql.types.input.CDPConsentUpdateEventFilterInput;
import org.apache.unomi.graphql.types.input.CDPEventFilterInput;
import org.apache.unomi.graphql.types.input.CDPListsUpdateEventFilterInput;
import org.apache.unomi.graphql.types.input.CDPSessionEventFilterInput;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventConditionFactory extends ConditionFactory {

    private static EventConditionFactory instance;

    public static synchronized EventConditionFactory get(final DataFetchingEnvironment environment) {
        if (instance == null) {
            instance = new EventConditionFactory(environment);
        }
        return instance;
    }

    private EventConditionFactory(final DataFetchingEnvironment environment) {
        super("eventPropertyCondition", environment);
    }

    public Condition eventFilterInputCondition(CDPEventFilterInput filterInput, Date after, Date before) {
        final List<Condition> rootSubConditions = new ArrayList<>();

        if (after != null) {
            rootSubConditions.add(datePropertyCondition("timeStamp", "greaterThan", after));
        }

        if (before != null) {
            rootSubConditions.add(datePropertyCondition("timeStamp", "lessThanOrEqual", before));
        }

        if (filterInput != null) {

            if (filterInput.getCdp_timestamp_equals() != null) {
                rootSubConditions.add(datePropertyCondition("timeStamp", "equals", filterInput.getCdp_timestamp_equals()));
            }

            if (filterInput.getCdp_timestamp_gt() != null) {
                rootSubConditions.add(datePropertyCondition("timeStamp", "greaterThan", filterInput.getCdp_timestamp_gt()));
            }

            if (filterInput.getCdp_timestamp_gte() != null) {
                rootSubConditions.add(datePropertyCondition("timeStamp", "greaterThanOrEqualTo", filterInput.getCdp_timestamp_gte()));
            }

            if (filterInput.getCdp_timestamp_lt() != null) {
                rootSubConditions.add(datePropertyCondition("timeStamp", "lessThan", filterInput.getCdp_timestamp_lt()));
            }

            if (filterInput.getCdp_timestamp_lte() != null) {
                rootSubConditions.add(datePropertyCondition("timeStamp", "lessThanOrEqualTo", filterInput.getCdp_timestamp_lte()));
            }

            if (filterInput.getId_equals() != null) {
                rootSubConditions.add(propertyCondition("itemId", filterInput.getId_equals()));
            }

            if (filterInput.getCdp_clientID_equals() != null) {
                rootSubConditions.add(propertyCondition("clientId", filterInput.getCdp_clientID_equals()));
            }

            if (filterInput.getCdp_profileID_equals() != null) {
                rootSubConditions.add(propertyCondition("profileId", filterInput.getCdp_profileID_equals()));
            }

            if (filterInput.getCdp_sourceID_equals() != null) {
                rootSubConditions.add(propertyCondition("source.itemId", filterInput.getCdp_sourceID_equals()));
            }

            if (filterInput.getCdp_listsUpdateEvent() != null) {
                rootSubConditions.add(listUpdateEventCondition(filterInput.getCdp_listsUpdateEvent()));
            }

            if (filterInput.getCdp_consentUpdateEvent() != null) {
                rootSubConditions.add(createCdpConsentUpdateEventCondition(filterInput.getCdp_consentUpdateEvent()));
            }

            if (filterInput.getCdp_sessionEvent() != null) {
                rootSubConditions.add(createCdpSessionEventCondition(filterInput.getCdp_sessionEvent()));
            }

            if (filterInput.getAnd() != null && filterInput.getAnd().size() > 0) {
                rootSubConditions.add(filtersToCondition(filterInput.getAnd(), andInput -> eventFilterInputCondition(andInput, null, null), "and"));
            }

            if (filterInput.getOr() != null && filterInput.getOr().size() > 0) {
                rootSubConditions.add(filtersToCondition(filterInput.getOr(), orInput -> eventFilterInputCondition(orInput, null, null), "or"));
            }
        }

        return booleanCondition("and", rootSubConditions);
    }

    private Condition listUpdateEventCondition(CDPListsUpdateEventFilterInput cdp_listsUpdateEvent) {

        final List<Condition> rootSubConditions = new ArrayList<>();

        if (cdp_listsUpdateEvent.getJoinLists_contains() != null && !cdp_listsUpdateEvent.getJoinLists_contains().isEmpty()) {
            rootSubConditions.add(propertiesCondition("joinLists", "contains", cdp_listsUpdateEvent.getJoinLists_contains()));
        }

        if (cdp_listsUpdateEvent.getJoinLists_contains() != null && !cdp_listsUpdateEvent.getJoinLists_contains().isEmpty()) {
            rootSubConditions.add(propertiesCondition("leaveLists", "contains", cdp_listsUpdateEvent.getLeaveLists_contains()));
        }

        return booleanCondition("and", rootSubConditions);
    }

    private Condition createCdpConsentUpdateEventCondition(final CDPConsentUpdateEventFilterInput eventFilterInput) {

        final List<Condition> subConditions = new ArrayList<>();

        if (eventFilterInput.getType_equals() != null) {
            subConditions.add(propertyCondition("properties.type", eventFilterInput.getType_equals()));
        }

        if (eventFilterInput.getStatus_equals() != null) {
            subConditions.add(propertyCondition("properties.status", eventFilterInput.getStatus_equals()));
        }

        if (eventFilterInput.getLastUpdate_equals() != null) {
            subConditions.add(datePropertyCondition("properties.lastUpdate", "equals", eventFilterInput.getLastUpdate_equals()));
        }

        if (eventFilterInput.getLastUpdate_lt() != null) {
            subConditions.add(datePropertyCondition("properties.lastUpdate", "lessThan", eventFilterInput.getLastUpdate_lt()));
        }

        if (eventFilterInput.getLastUpdate_lte() != null) {
            subConditions.add(datePropertyCondition("properties.lastUpdate", "lessThanOrEqualTo", eventFilterInput.getLastUpdate_lte()));
        }

        if (eventFilterInput.getLastUpdate_gt() != null) {
            subConditions.add(datePropertyCondition("properties.lastUpdate", "greaterThan", eventFilterInput.getLastUpdate_gt()));
        }

        if (eventFilterInput.getLastUpdate_gte() != null) {
            subConditions.add(datePropertyCondition("properties.lastUpdate", "greaterThanOrEqualTo", eventFilterInput.getLastUpdate_gte()));
        }

        if (eventFilterInput.getExpiration_equals() != null) {
            subConditions.add(datePropertyCondition("properties.expiration", "equals", eventFilterInput.getExpiration_equals()));
        }

        if (eventFilterInput.getExpiration_lt() != null) {
            subConditions.add(datePropertyCondition("properties.expiration", "lessThan", eventFilterInput.getExpiration_lt()));
        }

        if (eventFilterInput.getExpiration_lte() != null) {
            subConditions.add(datePropertyCondition("properties.expiration", "lessThanOrEqualTo", eventFilterInput.getExpiration_lte()));
        }

        if (eventFilterInput.getExpiration_gt() != null) {
            subConditions.add(datePropertyCondition("properties.expiration", "greaterThan", eventFilterInput.getExpiration_gt()));
        }

        if (eventFilterInput.getExpiration_gte() != null) {
            subConditions.add(datePropertyCondition("properties.expiration", "greaterThanOrEqualTo", eventFilterInput.getExpiration_gte()));
        }

        return booleanCondition("and", subConditions);
    }

    private Condition createCdpSessionEventCondition(final CDPSessionEventFilterInput eventFilterInput) {
        final List<Condition> subConditions = new ArrayList<>();

        if (eventFilterInput.getState_equals() != null) {
            subConditions.add(propertyCondition("properties.state", eventFilterInput.getState_equals().name()));
        }

        if (eventFilterInput.getUnomi_scope_equals() != null) {
            subConditions.add(propertyCondition("properties.scope", eventFilterInput.getUnomi_scope_equals()));
        }

        if (eventFilterInput.getUnomi_sessionId_equals() != null) {
            subConditions.add(propertyCondition("properties.sessionId", eventFilterInput.getUnomi_sessionId_equals()));
        }

        return booleanCondition("and", subConditions);
    }


}
