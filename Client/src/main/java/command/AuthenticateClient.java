package command;

import data.Account;
import handler.ClientHandler;

public class AuthenticateClient implements ClientCommand {
    /**
     * Sets the client account to the one received from the server
     * @param param Account object received from the server
     * @return Account object received from the server
     */
    @Override
    public Object execute(Object param) {
        if (param instanceof Account) {
            ClientHandler.setAccount((Account) param);
        }
        return param;
    }
}
