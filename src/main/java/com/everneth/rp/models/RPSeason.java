package com.everneth.rp.models;

import co.aikar.idb.DB;
import com.everneth.rp.RP;

import javax.swing.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    // Fallback empty ctor
    public RPSeason () {}

    // Season create ctor
    public RPSeason (String name, int typeId, int themeId, boolean allowGuilds)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.name = name;
        this.seasonTypeId = typeId;
        this.seasonThemeId = themeId;
        this.allowGuilds = allowGuilds;
        this.dateCreated = format.format(now);
        this.dateStarted = null;
        this.dateEnded = null;
        this.resultId = 1;
    }


    public ActionResponse createSeason()
    {
        ActionResponse response = new ActionResponse();
        long seasonId = 0;
        try {
            seasonId = DB.executeInsert("INSERT INTO seasons (season_name, season_type_id, season_theme_id, " +
                            "allow_guilds, date_created, date_started, date_ended, season_result_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ",
                    this.getName(),
                    this.getSeasonTypeId(),
                    this.getSeasonThemeId(),
                    this.isAllowGuilds(),
                    this.getDateCreated(),
                    null,
                    null,
                    1);
            response.setSuccessfulAction(true);
            response.setMessage("Season creation successful!");
        }
        catch (SQLException e)
        {
            RP.getPlugin().getLogger().warning(e.getMessage());
            response.setSuccessfulAction(false);
            response.setMessage("Season creation failed. Please check logs as this may be a SQL error.");
        }
        return response;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeasonTypeId() {
        return seasonTypeId;
    }

    public void setSeasonTypeId(int seasonTypeId) {
        this.seasonTypeId = seasonTypeId;
    }

    public int getSeasonThemeId() {
        return seasonThemeId;
    }

    public void setSeasonThemeId(int seasonThemeId) {
        this.seasonThemeId = seasonThemeId;
    }

    public boolean isAllowGuilds() {
        return allowGuilds;
    }

    public void setAllowGuilds(boolean allowGuilds) {
        this.allowGuilds = allowGuilds;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(String dateEnded) {
        this.dateEnded = dateEnded;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }
}
