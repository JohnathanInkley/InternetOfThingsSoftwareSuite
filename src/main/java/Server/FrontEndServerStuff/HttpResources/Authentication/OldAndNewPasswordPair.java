package Server.FrontEndServerStuff.HttpResources.Authentication;

public class OldAndNewPasswordPair {

    public String oldPassword;
    public String newPassword;

    public OldAndNewPasswordPair(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
