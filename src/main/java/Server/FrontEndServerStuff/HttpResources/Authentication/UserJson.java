package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;

public class UserJson {
    public int id;
    public String username;
    public String password;
    public String firstName;
    public String lastName;
    public String client;
    public String email;
    public String token;
    public String isAdmin;

    public void addNonVoidElementsToUserEntry(UserEntry entry) {
        if (id != 0)
            entry.setId(id);
        if (username != null)
            entry.setUsername(username);
        if (firstName != null)
            entry.setFirstName(firstName);
        if (lastName != null)
            entry.setLastName(lastName);
        if (client != null)
            entry.setLastName(lastName);
        if (email != null)
            entry.setEmail(email);
    }
}
