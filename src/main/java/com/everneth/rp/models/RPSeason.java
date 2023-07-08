package com.everneth.rp.models;

import javax.swing.*;

public class RPSeason {
    private int id;
    private String name;
    private int seasonTypeId;
    private int seasonThemeId;
    private boolean allowGuilds;
    private String dateCreated;
    private String dateStarted;
    private String dateEnded;
    private int resultId;

    public RPSeason () {}

    public ActionResponse createSeason()
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse editSeason(T t)
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse removeSeason(T t)
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse startSeason(T t)
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse endSeason(T t)
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse pauseSeason(T t)
    {
        return new ActionResponse("", false);
    }

    public ActionResponse getAllSeasons()
    {
        return new ActionResponse("", false);
    }

    public <T> ActionResponse joinSeason(T t)
    {
        return new ActionResponse("", false);

    }

    public <T, U> ActionResponse joinSeason(T t, U u)
    {
        return new ActionResponse("", false);

    }

}
