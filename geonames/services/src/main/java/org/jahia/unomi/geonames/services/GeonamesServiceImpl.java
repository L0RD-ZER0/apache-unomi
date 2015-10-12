package org.jahia.unomi.geonames.services;


import org.apache.commons.lang3.StringUtils;
import org.oasis_open.contextserver.api.PartialList;
import org.oasis_open.contextserver.api.conditions.Condition;
import org.oasis_open.contextserver.api.services.DefinitionsService;
import org.oasis_open.contextserver.persistence.spi.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GeonamesServiceImpl implements GeonamesService {
    private static final Logger logger = LoggerFactory.getLogger(GeonamesServiceImpl.class.getName());

    public static final String GEOCODING_MAX_DISTANCE = "100km";

    private DefinitionsService definitionsService;
    private PersistenceService persistenceService;

    private String pathToGeonamesDatabase;
    private Boolean forceDbImport;

    public void setForceDbImport(Boolean forceDbImport) {
        this.forceDbImport = forceDbImport;
    }

    public void setDefinitionsService(DefinitionsService definitionsService) {
        this.definitionsService = definitionsService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setPathToGeonamesDatabase(String pathToGeonamesDatabase) {
        this.pathToGeonamesDatabase = pathToGeonamesDatabase;
    }

    public void start() {
        importDatabase();
    }

    public void stop() {
    }

    public void importDatabase() {
        if (!persistenceService.createIndex("geonames")) {
            if (forceDbImport) {
                persistenceService.removeIndex("geonames");
                persistenceService.createIndex("geonames");
                logger.info("Geonames index removed and recreated");
            } else if (persistenceService.getAllItemsCount(GeonameEntry.ITEM_TYPE) > 0) {
                return;
            }
        } else {
            logger.info("Geonames index created");
        }

        if (pathToGeonamesDatabase == null) {
            logger.info("No geonames DB provided");
            return;
        }
        final File f = new File(pathToGeonamesDatabase);
        if (f.exists()) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(f));
                        ZipEntry zipEntry = zipInputStream.getNextEntry();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream, "UTF-8"));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String line;
                        logger.info("Starting to import geonames database ...");
                        while ((line = reader.readLine()) != null) {
                            String[] values = line.split("\t");

                            if (FEATURES_CLASSES.contains(values[6])) {
                                GeonameEntry geonameEntry = new GeonameEntry(values[0], values[1], values[2],
                                        StringUtils.isEmpty(values[4]) ? null : Double.parseDouble(values[4]),
                                        StringUtils.isEmpty(values[5]) ? null : Double.parseDouble(values[5]),
                                        values[6], values[7], values[8],
                                        Arrays.asList(values[9].split(",")),
                                        values[10], values[11], values[12], values[13],
                                        StringUtils.isEmpty(values[14]) ? null : Integer.parseInt(values[14]),
                                        StringUtils.isEmpty(values[15]) ? null : Integer.parseInt(values[15]),
                                        values[16], values[17],
                                        sdf.parse(values[18]));

                                persistenceService.save(geonameEntry);
                            }
                        }
                        logger.info("Geonames database imported");
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }, 5000);
        }
    }

    public List<GeonameEntry> getHierarchy(String itemId) {
        return getHierarchy(persistenceService.load(itemId, GeonameEntry.class));
    }

    public List<GeonameEntry> getHierarchy(GeonameEntry entry) {
        List<GeonameEntry> entries = new ArrayList<>();
        entries.add(entry);

        List<Condition> l = new ArrayList<>();
        Condition andCondition = new Condition();
        andCondition.setConditionType(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        andCondition.setParameter("subConditions", l);

        Condition featureCodeCondition = new Condition();
        featureCodeCondition.setConditionType(definitionsService.getConditionType("sessionPropertyCondition"));
        featureCodeCondition.setParameter("propertyName", "featureCode");
        featureCodeCondition.setParameter("comparisonOperator", "in");
        l.add(featureCodeCondition);

        PartialList<GeonameEntry> country = buildHierarchy(andCondition, featureCodeCondition, "countryCode", entry.getCountryCode(), COUNTRY_FEATURE_CODES, 0, 1);

        if (!StringUtils.isEmpty(entry.getAdmin1Code())) {
            PartialList<GeonameEntry> adm1 = buildHierarchy(andCondition, featureCodeCondition, "admin1Code", entry.getAdmin1Code(), ADM1_FEATURE_CODES, 0, 1);

            if (!StringUtils.isEmpty(entry.getAdmin2Code())) {
                PartialList<GeonameEntry> adm2 = buildHierarchy(andCondition, featureCodeCondition, "admin2Code", entry.getAdmin2Code(), ADM2_FEATURE_CODES, 0, 1);

                if (!adm2.getList().isEmpty()) {
                    entries.add(adm2.get(0));
                }
            }
            if (!adm1.getList().isEmpty()) {
                entries.add(adm1.get(0));
            }

        }

        if (!country.getList().isEmpty()) {
            entries.add(country.get(0));
        }
        return entries;
    }

    private PartialList<GeonameEntry> buildHierarchy(Condition andCondition, Condition featureCodeCondition, String featurePropertyName, String featureValue, List<String> featuresCode, int offset, int size) {
        featureCodeCondition.setParameter("propertyValues", featuresCode);

        List<Condition> l = (List<Condition>) andCondition.getParameter("subConditions");
        Condition condition = getPropertyCondition(featurePropertyName, "propertyValue", featureValue, "equals");
        l.add(condition);

        return persistenceService.query(andCondition, null, GeonameEntry.class, offset, size);
    }

    public List<GeonameEntry> reverseGeoCode(String lat, String lon) {
        List<Condition> l = new ArrayList<Condition>();
        Condition andCondition = new Condition();
        andCondition.setConditionType(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        andCondition.setParameter("subConditions", l);


        Condition geoLocation = new Condition();
        geoLocation.setConditionType(definitionsService.getConditionType("geoLocationByPointSessionCondition"));
        geoLocation.setParameter("type", "circle");
        geoLocation.setParameter("circleLatitude", Double.parseDouble(lat));
        geoLocation.setParameter("circleLongitude", Double.parseDouble(lon));
        geoLocation.setParameter("distance", GEOCODING_MAX_DISTANCE);
        l.add(geoLocation);

        l.add(getPropertyCondition("featureCode", "propertyValues", CITIES_FEATURE_CODES, "in"));

        PartialList<GeonameEntry> list = persistenceService.query(andCondition, "geo:location:" + lat + ":" + lon, GeonameEntry.class, 0, 1);
        if (!list.getList().isEmpty()) {
            return getHierarchy(list.getList().get(0));
        }
        return Collections.emptyList();
    }


    public PartialList<GeonameEntry> getChildrenEntries(List<String> items, int offset, int size) {
        Condition andCondition = getItemsInChildrenQuery(items, GeonamesService.CITIES_FEATURE_CODES);
        Condition featureCodeCondition = ((List<Condition>) andCondition.getParameter("subConditions")).get(0);
        int level = items.size();

        featureCodeCondition.setParameter("propertyValues", ORDERED_FEATURES.get(level));
        PartialList<GeonameEntry> r = persistenceService.query(andCondition, null, GeonameEntry.class, offset, size);
        while (r.size() == 0 && level < ORDERED_FEATURES.size() - 1) {
            level++;
            featureCodeCondition.setParameter("propertyValues", ORDERED_FEATURES.get(level));
            r = persistenceService.query(andCondition, null, GeonameEntry.class, offset, size);
        }
        return r;
    }

    public PartialList<GeonameEntry> getChildrenCities(List<String> items, int offset, int size) {
        return persistenceService.query(getItemsInChildrenQuery(items, GeonamesService.CITIES_FEATURE_CODES), null, GeonameEntry.class, offset, size);
    }

    private Condition getItemsInChildrenQuery(List<String> items, List<String> featureCodes) {
        List<Condition> l = new ArrayList<Condition>();
        Condition andCondition = new Condition();
        andCondition.setConditionType(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        andCondition.setParameter("subConditions", l);

        Condition featureCodeCondition = getPropertyCondition("featureCode", "propertyValues", featureCodes, "in");
        l.add(featureCodeCondition);

        if (items.size() > 0) {
            l.add(getPropertyCondition("countryCode", "propertyValue", items.get(0), "equals"));
        }
        if (items.size() > 1) {
            l.add(getPropertyCondition("admin1Code", "propertyValue", items.get(1), "equals"));
        }
        if (items.size() > 2) {
            l.add(getPropertyCondition("admin2Code", "propertyValue", items.get(2), "equals"));
        }
        return andCondition;
    }

    public List<GeonameEntry> getCapitalEntries(String itemId) {
        GeonameEntry entry = persistenceService.load(itemId, GeonameEntry.class);
        List<String> featureCodes;

        List<Condition> l = new ArrayList<Condition>();
        Condition andCondition = new Condition();
        andCondition.setConditionType(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        andCondition.setParameter("subConditions", l);

        l.add(getPropertyCondition("countryCode", "propertyValue", entry.getCountryCode(), "equals"));

        if (COUNTRY_FEATURE_CODES.contains(entry.getFeatureCode())) {
            featureCodes = Arrays.asList("PPLC");
        } else if (ADM1_FEATURE_CODES.contains(entry.getFeatureCode())) {
            featureCodes = Arrays.asList("PPLA", "PPLC");
            l.add(getPropertyCondition("admin1Code", "propertyValue", entry.getAdmin1Code(), "equals"));
        } else if (ADM2_FEATURE_CODES.contains(entry.getFeatureCode())) {
            featureCodes = Arrays.asList("PPLA2", "PPLA", "PPLC");
            l.add(getPropertyCondition("admin1Code", "propertyValue", entry.getAdmin1Code(), "equals"));
            l.add(getPropertyCondition("admin2Code", "propertyValue", entry.getAdmin2Code(), "equals"));
        } else {
            return Collections.emptyList();
        }

        Condition featureCodeCondition = new Condition();
        featureCodeCondition.setConditionType(definitionsService.getConditionType("sessionPropertyCondition"));
        featureCodeCondition.setParameter("propertyName", "featureCode");
        featureCodeCondition.setParameter("propertyValues", featureCodes);
        featureCodeCondition.setParameter("comparisonOperator", "in");
        l.add(featureCodeCondition);
        List<GeonameEntry> entries = persistenceService.query(andCondition, null, GeonameEntry.class);
        if (entries.size() == 0) {
            featureCodeCondition.setParameter("propertyValues", CITIES_FEATURE_CODES);
            entries = persistenceService.query(andCondition, "population:desc", GeonameEntry.class, 0, 1).getList();
        }
        if (entries.size() > 0) {
            return getHierarchy(entries.get(0));
        }
        return Collections.emptyList();
    }

    private Condition getPropertyCondition(String name, String propertyValueField, Object value, String operator) {
        Condition condition = new Condition();
        condition.setConditionType(definitionsService.getConditionType("sessionPropertyCondition"));
        condition.setParameter("propertyName", name);
        condition.setParameter(propertyValueField, value);
        condition.setParameter("comparisonOperator", operator);
        return condition;
    }


}
