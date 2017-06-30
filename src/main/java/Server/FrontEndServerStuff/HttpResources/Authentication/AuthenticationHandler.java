package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UsernamePasswordPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/authenticate")
public class AuthenticationHandler {

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getUsernameAndPasswordFromLoginRequest(String usernameAndPassword) {
        Gson gson = new GsonBuilder().create();
        UsernamePasswordPair credentials = gson.fromJson(usernameAndPassword, UsernamePasswordPair.class);
        Response.ok();
        return "fakeToken for " + credentials.username + " and " + credentials.password;
    }

}
