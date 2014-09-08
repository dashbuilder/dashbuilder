/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.backend;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetManager;

import static org.dashbuilder.shared.sales.SalesConstants.*;

/**
 * Generates a random data set containing sales opportunity records.
 */
@ApplicationScoped
public class SalesDataSetGenerator {

    @Inject DataSetManager dataSetManager;

    private static String[] DIC_PIPELINE = {"EARLY", "STANDBY", "ADVANCED"};

    private static String[] DIC_STATUS = {"CONTACTED", "STANDBY", "DEMO", "SHORT LISTED",
                                            "LOST", "WIN", "VERBAL COMMITMENT", "QUALIFIED"};

    private static String[] DIC_COUNTRIES = {"United States", "China", "Japan", "Germany", "France", "United Kingdom",
                                            "Brazil", "Italy", "India", "Canada", "Russia", "Spain", "Australia",
                                            "Mexico", "South Korea", "Netherlands", "Turkey", "Indonesia", "Switzerland",
                                            "Poland", "Belgium", "Sweden", "Saudi Arabia", "Norway"};

    private static String[] DIC_PRODUCT = {"PRODUCT 1", "PRODUCT 2", "PRODUCT 3", "PRODUCT 4", "PRODUCT 5", "PRODUCT 6",
                                            "PRODUCT 7", "PRODUCT 8", "PRODUCT 8", "PRODUCT 10", "PRODUCT 11"};

    private static String[] DIC_SALES_PERSON = {"Roxie Foraker", "Jamie Gilbeau", "Nita Marling", "Darryl Innes",
                                                "Julio Burdge", "Neva Hunger", "Kathrine Janas", "Jerri Preble"};

    private static String[] DIC_CUSTOMER = {"Company 1", "Company 2", "Company 3", "Company 3", "Company 4",
                                            "Company 5", "Company 6", "Company 7", "Company 8", "Company 9"};

    private static String[] DIC_SOURCE = {"Customer", "Reference", "Personal contact", "Partner",
                                        "Website", "Lead generation", "Event"};

    private static double MAX_AMOUNT = 15000;

    private static double MIN_AMOUNT = 8000;

    private static double AVG_CLOSING_DAYS = 90;

    private Random random = new Random(System.currentTimeMillis());

    public static class Opportunity {
        public String pipeline;
        public String status;
        public String country;
        public String product;
        public String customer;
        public String salesPerson;
        double amount;
        double probability;
        double expectedAmount;
        public Date creationDate;
        public Date closingDate;
        public String source;
        public String color;
    }

    private Date buildDate(int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, random.nextInt(28)); // No sales on 29, 30 and 31 ;-)
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1); // Some genius thought that the first month is 0
        c.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
        c.set(Calendar.MINUTE, random.nextInt(60));
        return c.getTime();
    }

    private Date addDates(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    private String randomValue(String[] dic) {
        return dic[random.nextInt(dic.length)];
    }

    public List<Opportunity> randomOpportunities(int opportunitiesPerMonth, int startYear, int endYear) {
        List<Opportunity> opportunities = new ArrayList<Opportunity>();
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 0; month < 12; month++) {
                for (int i = 0; i < opportunitiesPerMonth; i++) {
                    Opportunity opportunity = new Opportunity();
                    opportunity.amount = MIN_AMOUNT + random.nextDouble() * (MAX_AMOUNT - MIN_AMOUNT);
                    opportunity.creationDate = buildDate(month, year);
                    opportunity.closingDate = addDates(opportunity.creationDate, (int) (AVG_CLOSING_DAYS + random.nextDouble() * AVG_CLOSING_DAYS * 0.5));
                    opportunity.pipeline = randomValue(DIC_PIPELINE);
                    opportunity.status = randomValue(DIC_STATUS);
                    opportunity.country = randomValue(DIC_COUNTRIES);
                    opportunity.customer = randomValue(DIC_CUSTOMER);
                    opportunity.product = randomValue(DIC_PRODUCT);
                    opportunity.salesPerson = randomValue(DIC_SALES_PERSON);
                    opportunity.probability = random.nextDouble() * 100.0;
                    opportunity.expectedAmount = opportunity.amount * (1 + (random.nextDouble() * ((month*i)%10)/10));
                    opportunity.source = randomValue(DIC_SOURCE);
                    if (opportunity.probability < 25) opportunity.color = "RED";
                    else if (opportunity.probability < 50) opportunity.color = "GREY";
                    else if (opportunity.probability < 75) opportunity.color = "YELLOW";
                    else opportunity.color = "GREEN";
                    opportunities.add(opportunity);
                }
            }
        }
        return opportunities;
    }

    public DataSet generateDataSet(String uuid, int opportunitiesPerMonth, int startYear, int endYear) {
        List<Opportunity> opportunities = randomOpportunities(opportunitiesPerMonth, startYear, endYear);
        DataSet dataSet = dataSetManager.createDataSet(uuid);

        dataSet.addColumn(AMOUNT, ColumnType.NUMBER);
        dataSet.addColumn(CREATION_DATE, ColumnType.DATE);
        dataSet.addColumn(CLOSING_DATE, ColumnType.DATE);
        dataSet.addColumn(PIPELINE, ColumnType.LABEL);
        dataSet.addColumn(STATUS, ColumnType.LABEL);
        dataSet.addColumn(CUSTOMER, ColumnType.LABEL);
        dataSet.addColumn(COUNTRY, ColumnType.LABEL);
        dataSet.addColumn(PRODUCT, ColumnType.LABEL);
        dataSet.addColumn(SALES_PERSON, ColumnType.LABEL);
        dataSet.addColumn(PROBABILITY, ColumnType.LABEL);
        dataSet.addColumn(SOURCE, ColumnType.LABEL);
        dataSet.addColumn(EXPECTED_AMOUNT, ColumnType.NUMBER);
        dataSet.addColumn(COLOR, ColumnType.LABEL);

        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity opp = opportunities.get(i);
            dataSet.setValueAt(i, 0, opp.amount);
            dataSet.setValueAt(i, 1, opp.creationDate);
            dataSet.setValueAt(i, 2, opp.closingDate);
            dataSet.setValueAt(i, 3, opp.pipeline);
            dataSet.setValueAt(i, 4, opp.status);
            dataSet.setValueAt(i, 5, opp.customer);
            dataSet.setValueAt(i, 6, opp.country);
            dataSet.setValueAt(i, 7, opp.product);
            dataSet.setValueAt(i, 8, opp.salesPerson);
            dataSet.setValueAt(i, 9, opp.probability);
            dataSet.setValueAt(i, 10, opp.source);
            dataSet.setValueAt(i, 11, opp.expectedAmount);
            dataSet.setValueAt(i, 12, opp.color);
        }
        return dataSet;
    }
}
