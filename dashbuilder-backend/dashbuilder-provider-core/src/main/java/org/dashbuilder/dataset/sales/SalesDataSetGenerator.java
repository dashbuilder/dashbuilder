package org.dashbuilder.dataset.sales;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.model.dataset.ColumnType;
import org.dashbuilder.model.dataset.DataSet;
import org.dashbuilder.model.dataset.DataSetManager;
import org.uberfire.commons.services.cdi.Startup;

/**
 * Generates a random data set containing sales opportunity records.
 */
@Startup
@ApplicationScoped
public class SalesDataSetGenerator {

    @Inject
    protected DataSetManager dataSetManager;

    protected String dataSetUUID = "sales-opportunities-dataset";
    protected int opportunitiesPerMonth = 30;
    protected int startYear = Calendar.getInstance().get(Calendar.YEAR) - 2;
    protected int endYear = Calendar.getInstance().get(Calendar.YEAR) + 2;

    @PostConstruct
    private void generateDataSet() {
        List<Opportunity> opportunities = randomOpportunities(opportunitiesPerMonth, startYear, endYear);
        DataSet dataSet = generateDataSet(dataSetUUID, opportunities);
        dataSetManager.registerDataSet(dataSet);
    }

    public DataSet generateDataSet(String uuid, List<Opportunity> opportunities) {
        DataSet dataSet = dataSetManager.createDataSet(uuid);

        dataSet.addColumn("Amount", ColumnType.NUMBER);
        dataSet.addColumn("Creation date", ColumnType.DATE);
        dataSet.addColumn("Closing date", ColumnType.DATE);
        dataSet.addColumn("Pipeline", ColumnType.LABEL);
        dataSet.addColumn("Status", ColumnType.LABEL);
        dataSet.addColumn("Customer", ColumnType.LABEL);
        dataSet.addColumn("Country", ColumnType.LABEL);
        dataSet.addColumn("Product", ColumnType.LABEL);
        dataSet.addColumn("Sales person", ColumnType.LABEL);
        dataSet.addColumn("Probability", ColumnType.LABEL);
        dataSet.addColumn("Source", ColumnType.LABEL);
        dataSet.addColumn("Expected amount", ColumnType.NUMBER);
        dataSet.addColumn("Color", ColumnType.LABEL);

        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity opp = opportunities.get(i);
            dataSet.setValueAt(i, 0, opp.amount);
            dataSet.setValueAt(i, 1, opp.amount);
            dataSet.setValueAt(i, 2, opp.creationDate);
            dataSet.setValueAt(i, 3, opp.closingDate);
            dataSet.setValueAt(i, 4, opp.pipeline);
            dataSet.setValueAt(i, 5, opp.status);
            dataSet.setValueAt(i, 6, opp.customer);
            dataSet.setValueAt(i, 7, opp.country);
            dataSet.setValueAt(i, 8, opp.product);
            dataSet.setValueAt(i, 9, opp.salesPerson);
            dataSet.setValueAt(i, 10, opp.probability);
            dataSet.setValueAt(i, 10, opp.source);
            dataSet.setValueAt(i, 10, opp.expectedAmount);
            dataSet.setValueAt(i, 10, opp.color);
        }
        return dataSet;
    }

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

    private static String CSV_SEPARATOR = ";";

    private static int START_ID_VALUES = 10000;

    private NumberFormat numberFormat = DecimalFormat.getInstance(Locale.US);

    private DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

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

    private String format(double d) {
        return numberFormat.format(d);
    }

    private String format(Date d) {
        return dateFormat.format(d);
    }

    public List<Opportunity> randomOpportunities(int opportunitiesPerMonth, int startYear, int endYear) {
        List<Opportunity> opportunities = new ArrayList<Opportunity>();
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
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
                    opportunity.expectedAmount = opportunity.amount * opportunity.probability;
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
}
