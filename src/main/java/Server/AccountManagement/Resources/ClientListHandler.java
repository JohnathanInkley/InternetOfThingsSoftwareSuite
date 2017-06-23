package Server.AccountManagement.Resources;

import Server.AccountManagement.UserChoiceHandler;

public class ClientListHandler implements UserChoiceHandler {
    @Override
    public void processUserChoice() {
        System.out.println("dummy choice processing");
        System.out.println();
    }
}
