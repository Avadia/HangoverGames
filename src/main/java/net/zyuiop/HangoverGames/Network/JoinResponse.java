package net.zyuiop.HangoverGames.Network;

/**
 * Created by zyuiop on 26/09/14.
 */
public class JoinResponse {
    protected boolean accepted;
    protected String errorMessage;

    public JoinResponse(boolean accepted, String errorMessage) {
        this.accepted = accepted;
        this.errorMessage = errorMessage;
    }

    public JoinResponse() {
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
