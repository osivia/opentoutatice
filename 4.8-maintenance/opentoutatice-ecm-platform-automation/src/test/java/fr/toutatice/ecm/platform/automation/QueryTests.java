package fr.toutatice.ecm.platform.automation;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.RecordSet;

public class QueryTests {

    private static final String host = "vm-dch-mig8";
    private static final String user = "admin";
    private static final String pwd = "osivia";

    private static final String url = "http://%s:8081/nuxeo/site/automation";

    private static final String queryNfetch = "Repository.ResultSetQuery";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final String QUERY = "select distinct ecm:uuid, dc:title, ttcs:spaceMembers/*1/login, ttcs:spaceMembers/*1/joinedDate, ttcs:spaceMembers/*1/newsPeriod, ttcs:spaceMembers/*1/nextNewsDate, ttcs:spaceMembers/*1/lastNewsDate "
            + " from Workspace where "
            + " ttcs:spaceMembers/*1/login is not null and ttcs:spaceMembers/*1/newsPeriod <> 'none' and ttcs:spaceMembers/*1/nextNewsDate >= TIMESTAMP '"
            + dateFormat.format(new Date()) + "' and ecm:mixinType != 'HiddenInNavigation' and ecm:currentLifeCycleState != 'deleted' and ecm:isVersion = 0 "
            + " order by ecm:uuid";

    public static void main(String[] args) {
        HttpAutomationClient client = null;
        Session session = null;

        try {
            client = new HttpAutomationClient(String.format(url, host));
            session = client.getSession(user, pwd);

            OperationRequest request = session.newRequest(queryNfetch).set("query", QUERY).set("currentPageIndex", "0").set("pageSize", "1000");
            RecordSet rows = (RecordSet) request.execute();

            int index = 1;
            if (!rows.isEmpty()) {
                System.out.println("=== Number of rows: " + rows.size() + "\n");

                Iterator<Map<String, Serializable>> iterator = rows.iterator();
                while (iterator.hasNext()) {
                    Map<String, Serializable> row = iterator.next();

                    if (MapUtils.isNotEmpty(row)) {
                        System.out.print(index + ")  ");
                        for (Entry<String, Serializable> entry : row.entrySet()) {
                            System.out.println(entry.getKey() + " | " + entry.getValue());
                        }
                    }
                    System.out.println("");
                    index += 1;
                }

            } else {
                System.out.println("NO result");
            }

            System.out.println("=== Ended ===");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
            if (null != client) {
                client.shutdown();
            }
        }

    }

}
