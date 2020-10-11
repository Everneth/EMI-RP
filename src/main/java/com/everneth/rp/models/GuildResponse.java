package com.everneth.rp.models;

public class GuildResponse {
    private String message;
    private boolean successfulAction;

    public GuildResponse()
    {
        this.message = "No action completed.";
        this.successfulAction = false;
    }

    public GuildResponse(String message, boolean success)
    {
        this.message = message;
        this.successfulAction = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccessfulAction() {
        return successfulAction;
    }

    public void setSuccessfulAction(boolean successfulAction) {
        this.successfulAction = successfulAction;
    }
}
