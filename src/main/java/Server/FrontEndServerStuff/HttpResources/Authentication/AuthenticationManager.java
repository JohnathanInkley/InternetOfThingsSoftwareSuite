package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthenticationManager {

    public static final byte[] KEY = "secret".getBytes();

    private ClientDatabaseEditor editor;

    public AuthenticationManager(ClientDatabaseEditor editor) {
        this.editor = editor;
    }

    public boolean validateUser(UsernamePasswordPair userCredentials) {
        UserEntry userEntry = editor.getUserEntry(userCredentials.username);
        return userEntry != null && userEntry.validateCredentials(userCredentials.username, userCredentials.password);
    }

    public String getJWT(String username) {
        UserEntry userEntry = editor.getUserEntry(username);
        if (userEntry == null) {
            throw new IllegalStateException("This should not be called if user has not been validated!");
        }
        String jwt = getJWTFromUser(userEntry);
        return jwt;
    }

    private String getJWTFromUser(UserEntry userEntry) {
        return Jwts.builder()
                            .setSubject("user")
                            .setHeaderParam("alg", "HS256")
                            .setHeaderParam("typ","JWT")
                            .claim("username", userEntry.getUsername())
                            .claim("client",  userEntry.getClientName())
                            .signWith(SignatureAlgorithm.HS256, KEY)
                            .compact();
    }

    public UserJson generateAuthenticationResponse(String username) {
        UserEntry user = editor.getUserEntry(username);
        UserJson response = new UserJson();
        response.client = user.getClientName();
        response.email = user.getEmail();
        response.firstName = user.getFirstName();
        response.lastName = user.getLastName();
        response.id = (int) user.getId();
        response.username = username;
        response.token = getJWTFromUser(user);
        return response;
    }

    public boolean isValidJWT(String token) {
        try {
            Jwts.parser().setSigningKey(KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UserEntry getUserEntryForJWT(String token) {
        Claims userDetails = Jwts.parser().setSigningKey(KEY).parseClaimsJws(token).getBody();
        return editor.getUserEntry(userDetails.get("username", String.class));
    }
}
