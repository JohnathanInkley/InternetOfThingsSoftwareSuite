package Server.FrontEndServerStuff.HttpResources.Data;

import Server.AccountManagement.UserEntry;
import Server.DataAnalysisStuff.DatabaseEntryStatsToolbox;
import Server.DatabaseStuff.*;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationFilter;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManager;
import Server.FrontEndServerStuff.HttpResources.Authentication.Secured;
import Server.PhysicalLocationStuff.SensorLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("")
public class GetSiteData {

    private static ClientDatabaseEditor editor;
    private static Database timeSeriesDatabase;
    private static AuthenticationManager authenticationManager;
    private static DatabaseEntryStatsToolbox statsToolbox;
    private static Gson gson;

    public static void setClientDatabaseEditor(ClientDatabaseEditor clientDatabaseEditor, Database timeSeriesDatabase) {
        editor = clientDatabaseEditor;
        GetSiteData.timeSeriesDatabase = timeSeriesDatabase;
        authenticationManager = new AuthenticationManager(editor);
        AuthenticationFilter.setAuthenticationManager(authenticationManager);
        statsToolbox = new DatabaseEntryStatsToolbox();
        gson = new GsonBuilder().create();
    }


    @GET
    @Path("/api/sites/{siteName}/data/getDataLabels")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDataLabelsForDataProducedAtSite(@PathParam("siteName") String siteName, @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());

        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            String listAsJson = getLabelListJson(user.getClientName(), siteName);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(listAsJson)
                    .build();
        }

    }

    private String getLabelListJson(String clientName, String siteName) {
        List<String> sensorIPs = getSensorIPsAtSite(clientName, siteName);
        SensorLabelMapGenerator labelMapGenerator = new SensorLabelMapGenerator(timeSeriesDatabase);
        Set<String> listAsJson = labelMapGenerator.getLabelsForSite(sensorIPs, clientName, siteName);
        return gson.toJson(listAsJson);
    }

    private List<String> getSensorIPsAtSite(String clientName, String siteName) {
        List<SensorLocation> sensorsForClientSite = editor.getSensorsForClientSite(clientName, siteName);
        List<String> sensorIPs = new ArrayList<>();
        for (SensorLocation sensor : sensorsForClientSite) {
            sensorIPs.add(sensor.getName());
        }
        return sensorIPs;
    }

    @GET
    @Path("/api/sites/{siteName}/sensors")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorIPsForSite(@PathParam("siteName") String siteName, @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());

        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            List<String> sensorIPsAtSite = getSensorIPsAtSite(user.getClientName(), siteName);
            String listAsJson = gson.toJson(sensorIPsAtSite);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(listAsJson)
                    .build();
        }
    }

    @GET
    @Path("/api/sites/{siteName}/sensors/{IP}/labels")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLabelsForSensor(@PathParam("siteName") String siteName, @PathParam("IP") String IP, @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());

        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            String clientName = user.getClientName();
            List<String> sensorIPsAtSite = getSensorIPsAtSite(clientName, siteName);

            if (sensorIPsAtSite.contains(IP)) {
                return getResponseForValidIP(IP, siteName, clientName);
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.TEXT_PLAIN)
                        .entity("No sensor with IP " + IP + " at site " + siteName)
                        .build();
            }
        }
    }

    private Response getResponseForValidIP(String IP, String siteName, String clientName) {
        SensorLabelMapGenerator labelMapGenerator = new SensorLabelMapGenerator(timeSeriesDatabase);
        List<String> labels = labelMapGenerator.getLabels(IP, clientName, siteName);
        String labelsAsJson = gson.toJson(labels);
        return Response.status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(labelsAsJson)
                .build();
    }


    @GET
    @Path("/api/sites/{siteName}/data/getSensorsAndData")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorIPsAndLabels(@PathParam("siteName") String siteName, @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());
        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            SensorLabelMapGenerator labelMapGenerator = new SensorLabelMapGenerator(timeSeriesDatabase);
            String clientName = user.getClientName();
            List<String> sensorIPsAtSite = getSensorIPsAtSite(clientName, siteName);
            Map<String, List<String>> mapOfIPsToLabels = labelMapGenerator.getLabelMap(sensorIPsAtSite, clientName, siteName);
            String mapAsJson = gson.toJson(mapOfIPsToLabels);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(mapAsJson)
                    .build();
        }
    }

    @GET
    @Path("/api/sites/{siteName}/sensors/{sensorIP}/{label}/from/{startDate}/{startTime}/until/{endDate}/{endTime}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorDataBetweenTimes(@PathParam("siteName") String siteName,
                                              @PathParam("sensorIP") String IP,
                                              @PathParam("label") String dataLabel,
                                              @PathParam("startDate") String startDate,
                                              @PathParam("startTime") String startTime,
                                              @PathParam("endDate") String endDate,
                                              @PathParam("endTime") String endTime,
                                              @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());
        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            String siteTableIdentifier = user.getClientName() + "." + siteName;
            String start = startDate + " " + startTime;
            String end = endDate + " " + endTime;
            DatabaseEntrySet sensorEntries = timeSeriesDatabase.getSensorEntriesBetween(siteTableIdentifier, IP, start, end);
            String resultJson = makeJsonFromEntries(IP, dataLabel, sensorEntries);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(resultJson)
                    .build();
        }
    }

    private String makeJsonFromEntries(String IP, String dataLabel, DatabaseEntrySet sensorEntries) {
        List<DataValuesWithMetaData> results = new ArrayList<>();
        for (int i = 0; i < sensorEntries.size(); i++) {
            DatabaseEntry entry = sensorEntries.get(i);
            DataValuesWithMetaData datum = new DataValuesWithMetaData(entry.getTimestamp(), IP, dataLabel, (Double) entry.get(dataLabel));
            results.add(datum);
        }
        return gson.toJson(results);
    }

    @GET
    @Path("/api/sites/{siteName}/sensors/{sensorIP}/{label}/{operation}/{intervalMS}/modulo/{mod}/from/{startDate}/{startTime}/until/{endDate}/{endTime}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatsForSensorDataBetweenTimesModulo(@PathParam("siteName") String siteName,
                                                            @PathParam("sensorIP") String IP,
                                                            @PathParam("label") String dataLabel,
                                                            @PathParam("intervalMS") int intervalMS,
                                                            @PathParam("mod") int mod,
                                                            @PathParam("operation") String operation,
                                                            @PathParam("startDate") String startDate,
                                                            @PathParam("startTime") String startTime,
                                                            @PathParam("endDate") String endDate,
                                                            @PathParam("endTime") String endTime,
                                                            @Context SecurityContext context) {
        UserEntry user = editor.getUserEntry(context.getUserPrincipal().getName());
        if (!user.getSitePermissions().contains(siteName)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } else {
            String siteTableIdentifier = user.getClientName() + "." + siteName;
            String start = startDate + " " + startTime;
            String end = endDate + " " + endTime;
            DatabaseEntrySet sensorEntries = timeSeriesDatabase.getSensorEntriesBetween(siteTableIdentifier, IP, start, end);
            ArrayList<AbstractMap.SimpleEntry<String, Double>> statsData = null;
                if (operation.equals("meanEvery")) {
                    statsData = statsToolbox.getMeanForIntervalsModulo(sensorEntries, dataLabel, intervalMS, mod);
                } else if (operation.equals("sdEvery")) {
                    statsData = statsToolbox.getSdForIntervalsModulo(sensorEntries, dataLabel, intervalMS, mod);
                }
            String resultJson = makeJsonFromMeans(IP, dataLabel, statsData);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(resultJson)
                    .build();
        }
    }

    private String makeJsonFromMeans(String ip, String dataLabel, ArrayList<AbstractMap.SimpleEntry<String, Double>> statsData) {
        List<DataValuesWithMetaData> results = new ArrayList<>();
        for (int i = 0; i < statsData.size(); i++) {
            AbstractMap.SimpleEntry<String, Double> entry = statsData.get(i);
            Double value = entry.getValue();
            if (value.isNaN()) {
                DataValuesWithMetaData datum = new DataValuesWithMetaData(entry.getKey(), ip, dataLabel, 0.0);
                results.add(datum);
            } else {
                DataValuesWithMetaData datum = new DataValuesWithMetaData(entry.getKey(), ip, dataLabel, value);
                results.add(datum);
            }
        }
        return gson.toJson(results);
    }


}
