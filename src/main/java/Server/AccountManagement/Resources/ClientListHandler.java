package Server.AccountManagement.Resources;

import Server.AccountManagement.UserChoiceHandler;

import java.util.List;

public class ClientListHandler implements UserChoiceHandler {
    @Override
    public void processUserChoice(List<String> linesOfTextForChoice) {
        System.out.println(linesOfTextForChoice.get(0));
        System.out.println();
    }
}
