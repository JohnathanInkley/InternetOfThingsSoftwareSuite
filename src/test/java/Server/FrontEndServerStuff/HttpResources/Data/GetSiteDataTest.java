package Server.FrontEndServerStuff.HttpResources.Data;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.DatabaseStuff.Database;
import Server.DatabaseStuff.DatabaseEntry;
import Server.FrontEndServerStuff.FrontEndServer;
import Server.FrontEndServerStuff.HttpsClientMaker;
import Server.PhysicalLocationStuff.SensorLocation;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManagerTest.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class GetSiteDataTest {

    private static List<String> DATA_LABELS = new ArrayList<>(Arrays.asList("temp", "opt"));
    private static String SITE_NAME = "site1";

    private static String serverUrl;
    private static String databaseURL;
    private static FrontEndServer underTest;
    private static ClientDatabaseEditor editor;
    private static UserEntry user;
    private static Gson gson;
    private static Database tsDatabase;
    private static Database clientDatabase;

    @BeforeClass
    public static void setUpClass() throws IOException {
        serverUrl = "https://localhost:8081";
        databaseURL = "http://localhost:8086/";
        gson = new GsonBuilder().create();
        setupDatabase();
        GetSiteData.setClientDatabaseEditor(editor, tsDatabase);
        underTest = new FrontEndServer(serverUrl);
        underTest.runServer();
    }

    @AfterClass
    public static void clearUpClass() {
        underTest.stopServer();
        tsDatabase.deleteDatabase("ts");
        clientDatabase.deleteDatabase("clients");
    }


    @Test
    public void shouldBeAbleToGetDataLabelsForSite() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getLabels = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/data/getDataLabels");
        getLabels.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getLabels);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type setOfStringsType = new TypeToken<Set<String>>() {}.getType();
        Set<String> dataLabels = gson.fromJson(new String(messageBytes), setOfStringsType);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertTrue(DATA_LABELS.containsAll(dataLabels));
    }

    @Test
    public void shouldNotBeAbleToGetDataLabelsIfNoPermission() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getLabels = new HttpGet(serverUrl + "/api/sites/BAD_SITE/data/getDataLabels");
        getLabels.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getLabels);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldBeAbleToGetSensorIPsForSite() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getSensorIPs = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/sensors/");
        getSensorIPs.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getSensorIPs);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type listOfStringsType = new TypeToken<List<String>>() {}.getType();
        List<String> sensorIPs = gson.fromJson(new String(messageBytes), listOfStringsType);

        assertTrue(sensorIPs.contains("123"));
        assertTrue(sensorIPs.contains("345"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldNotBeAbleToGetIPsIfNoPermission() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getSensorIPs = new HttpGet(serverUrl + "/api/sites/BAD_SITE/sensors");
        getSensorIPs.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getSensorIPs);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldBeAbleToGetLabelsForGivenValidIP() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getLabelsForSensor = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/sensors/123/labels");
        getLabelsForSensor.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getLabelsForSensor);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type listOfStringsType = new TypeToken<List<String>>() {}.getType();
        List<String> listOfLabels = gson.fromJson(new String(messageBytes), listOfStringsType);

        assertEquals(DATA_LABELS.get(0), listOfLabels.get(0));
        assertEquals(1, listOfLabels.size());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldGet404IfSiteValidButInvalidIP() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getLabelsForInvalidSensor = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/sensors/BAD_SENSOR/labels");
        getLabelsForInvalidSensor.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getLabelsForInvalidSensor);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldGet401IfNoPermissionForSite() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getLabelsForInvalidSensor = new HttpGet(serverUrl + "/api/sites/BAD_SITE/sensors/BAD_SENSOR/labels");
        getLabelsForInvalidSensor.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getLabelsForInvalidSensor);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldBeAbleToGetCombinedListOfIPsWithLabelsForEach() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getMapOfIPsToLabels = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/data/getSensorsAndData");
        getMapOfIPsToLabels.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getMapOfIPsToLabels);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type mapOfListsType = new TypeToken<Map<String, List<String>>>() {}.getType();
        Map<String, List<String>> mapOfIPsToLabels = gson.fromJson(new String(messageBytes), mapOfListsType);

        assertEquals("temp", mapOfIPsToLabels.get("123").get(0));
        assertEquals("opt", mapOfIPsToLabels.get("345").get(0));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldGet401IfNoAccessToSite() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getBadSiteData = new HttpGet(serverUrl + "/api/sites/BAD_SITE/data/getSensorsAndData");
        getBadSiteData.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getBadSiteData);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldBeAbleToGetDataForSensorAtAGivenSite() throws IOException {
        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getSensorData = new HttpGet(serverUrl + "/api/sites/" + SITE_NAME + "/sensors/123/temp/from/2000-01-01/01:00:00/until/2000-01-01/01:00:08");
        getSensorData.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getSensorData);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type listOfDataType = new TypeToken<List<DataValuesWithMetaData>>() {}.getType();
        List<DataValuesWithMetaData> listOfData = gson.fromJson(new String(messageBytes), listOfDataType);

        List<DataValuesWithMetaData> listShouldBe = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            DataValuesWithMetaData data  = new DataValuesWithMetaData("2000-01-01 00:00:0" + (2*i) + ".000", "123", "temp", 30.0 + 2*i);
            listShouldBe.add(data);
        }

        System.out.println(listOfData);
        System.out.println(listShouldBe);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertTrue(listShouldBe.containsAll(listOfData));

    }

    private static void setupDatabase() throws IOException {
        tsDatabase = new Database("ts", databaseURL);
        makeDatabaseEntries();

        clientDatabase = new Database("clients", databaseURL);
        editor = new ClientDatabaseEditor(clientDatabase);
        editor.createNewClient(CLIENT);
        editor.addSiteForClient(CLIENT, SITE_NAME);
        editor.addSensorToClientSite(CLIENT, SITE_NAME, new SensorLocation("123",0.1,0.2));
        editor.addSensorToClientSite(CLIENT, SITE_NAME, new SensorLocation("345",0.2,0.3));
        editor.generateNewUsersForClient(CLIENT, 1, "src/testOutputFile.txt");
        String userDetails = Files.readAllLines(Paths.get("src/testOutputFile.txt"), Charset.defaultCharset()).get(0);
        UsernamePasswordPair credentials = new UsernamePasswordPair(userDetails.split("\\s+")[1], userDetails.split("\\s+")[3]);
        user = editor.getUserEntry(credentials.username);
        user.setUsername(VALID_USERNAME);
        user.giveSitePermission(SITE_NAME);
        editor.addUserEntry(user);

        Files.delete(Paths.get("src/testOutputFile.txt"));
    }

    private static void makeDatabaseEntries() {
        for (int i = 0; i < 9; i++) {
            DatabaseEntry entry = new DatabaseEntry();
            entry.add("DeviceCollection", CLIENT + "." + SITE_NAME);
            entry.setTimestamp("2000-01-01 00:00:0" + i + ".000");
            if (i % 2 == 0) {
                entry.add(DATA_LABELS.get(0), 30.0 + i);
                entry.add("IP", "123");
            } else {
                entry.add(DATA_LABELS.get(1), 5 + i);
                entry.add("IP", "345");
            }
            tsDatabase.addEntry(entry);
        }
    }
}