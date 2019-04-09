package fr.toutatice.ecm.platform.automation;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.RecordSet;


public class QueryTests {

    private static final String URL = "http://vm-dch-dev:8081/nuxeo/site/automation";
    private static final String USER = "admin";
    private static final String PWD = "osivia44";

    private static final String QUERY_N_FETCH = "ResultSet.PaginatedQuery";

    private static final String TEST_QUERY_1 = "select distinct ecm:uuid, dc:title, ttcs:spaceMembers/*1/login, ttcs:spaceMembers/*1/joinedDate, ttcs:spaceMembers/*1/newsPeriod, ttcs:spaceMembers/*1/nextNewsDate, ttcs:spaceMembers/*1/lastNewsDate from Workspace"
            + "where  ttcs:spaceMembers/*1/login is not null and ttcs:spaceMembers/*1/newsPeriod != 'none'  and ecm:mixinType != 'HiddenInNavigation' and ecm:currentLifeCycleState != 'deleted' and ecm:isVersion = 0 order by ecm:uuid ";
    private static final String TEST_QUERY_2 = "select count(ttcs:spaceMembers/*1/login) from Workspace where  ttcs:spaceMembers/*1/login is not null and ttcs:spaceMembers/*1/newsPeriod != 'none' and ecm:mixinType != 'HiddenInNavigation' and ecm:currentLifeCycleState != 'deleted' and ecm:isVersion = 0";

    private static final String[] TEST_QUERIES = {TEST_QUERY_1, TEST_QUERY_2};

    public static void main(String[] args) {

        Session session = null;
        try {
            HttpAutomationClient client = new HttpAutomationClient(URL);
            session = client.getSession(USER, PWD);

            // for (String qry : TEST_QUERIES) {
            // queryNFetch(session, qry);
            // }

            queryNFetch(session, TEST_QUERY_2);

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

    }

    /**
     * @param session
     * @throws Exception
     */
    private static void queryNFetch(Session session, String query) throws Exception {
        System.out.println(query);

        OperationRequest request = session.newRequest(QUERY_N_FETCH).set("query", query);

        long b = System.currentTimeMillis();
        RecordSet res = (RecordSet) session.execute(request);

        long e = System.currentTimeMillis();

        for (Map<String, Serializable> entry : res) {
            Set<String> keySet = entry.keySet();
            for (String key : keySet) {
                System.out.println(key + " : " + entry.get(key));
            }
        }

        System.out.println(String.valueOf(e - b) + " ms");
        System.out.println("\n");
    }

}
