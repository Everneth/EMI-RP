package com.everneth.rp.models;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import com.everneth.rp.RP;

import javax.swing.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RPSeason {
    private int id;
    private String name;
    private int seasonTypeId;
    private int seasonThemeId;
    private int allowGuilds;
    private String dateCreated;
    private String dateStarted;
    private String dateEnded;
    private int resultId;

    // Fallback empty ctor
    public RPSeason () {
        this.id = 0;
    }

    // Season create ctor
    public RPSeason (String name, int typeId, int themeId, int allowGuilds)
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

    public RPSeason(int id, String seasonName, int seasonTypeId, int seasonThemeId, int allowGuilds, String dateCreated,
                    String dateStarted, String dateEnded, int seasonResultId)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.id = id;
        this.name = seasonName;
        this.seasonTypeId = seasonTypeId;
        this.seasonThemeId = seasonThemeId;
        this.allowGuilds = allowGuilds;
        this.dateCreated = format.format(dateCreated);
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
                    this.getAllowGuilds(),
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

    public ActionResponse removeSeason(boolean override)
    {
        if(isSeasonCurrentlyActive())
        {
            if (this.getDateStarted() != null && !override)
                return new ActionResponse("[WARN] Season is in progress! If you still wish to remove it, please perform the command with optional override parameter.", false);
            else if (this.getDateStarted() != null && override)
            {
                DB.executeUpdateAsync("DELETE FROM seasons WHERE id = ?", this.getId());
                return new ActionResponse("Season " + this.getName() + " has been removed!", true);
            }
        }
        else
        {
            DB.executeUpdateAsync("DELETE FROM seasons WHERE id = ?", this.getId());
            return new ActionResponse("Season " + this.getName() + " has been removed!", true);
        }
        return new ActionResponse("Error occurred when deleting Season " + this.getName() + ". Please check the logs.", false);
    }

    public ActionResponse startSeason()
    {
        if(!isSeasonCurrentlyActive()) {
            if (this.getId() == 0)
                return new ActionResponse("No season found to start!", false);
            if (this.getDateStarted() != null)
                return new ActionResponse("Season already started!", false);
            else {
                Date now = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DB.executeUpdateAsync("UPDATE seasons SET date_started = ?", format.format(now));
                return new ActionResponse("Season " + this.getName() + " has begun!", true);
            }
        }
        else {
            return new ActionResponse("There is a season active. You can not start a new one until the previous is ended.", false);
        }
    }

    public ActionResponse endSeason()
    {
        if(isSeasonCurrentlyActive()) {
            if (this.getId() == 0)
                return new ActionResponse("No season found to end!", false);
            if (this.getDateStarted() == null)
                return new ActionResponse("Season has not started!", false);
            else {
                Date now = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DB.executeUpdateAsync("UPDATE seasons SET date_ended = ?", format.format(now));
                return new ActionResponse("Season " + this.getName() + " has ended!", true);
            }
        }
        else {
            return new ActionResponse("There is not a season active. You can not end a season without one in progress!", false);
        }
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

    // Private methods

    public boolean isSeasonCurrentlyActive()
    {
        CompletableFuture<DbRow> futureSeason;
        DbRow season;
        futureSeason = DB.getFirstRowAsync("SELECT * FROM seasons WHERE date_started IS NOT NULL AND date_ended IS NULL");
        try {
            season = futureSeason.get();
            return season != null;
        }
        catch (InterruptedException e) {
            RP.getPlugin().getLogger().info("Interrupted while getting RP Season: " + e.getMessage());
        }
        catch (ExecutionException e) {
            RP.getPlugin().getLogger().info("Could not execute query to get RP Season: " + e.getMessage());
        }
        return false;
    }

    public static <T> RPSeason getSeason(T t)
    {
        CompletableFuture<DbRow> futureSeason;
        DbRow season;
        if(t instanceof Integer) {
            futureSeason = DB.getFirstRowAsync("SELECT * FROM seasons WHERE id = ? AND date_ended IS NULL", t);
        }
        else if (t instanceof String) {
            futureSeason = DB.getFirstRowAsync("SELECT * FROM seasons WHERE season_name = ? AND date_ended IS NULL", t);
        }
        else {
            return new RPSeason();
        }
        try {
            season = futureSeason.get();
            if(season == null)
                return new RPSeason();
            return new RPSeason(
                    season.getInt("id"),
                    season.getString("season_name"),
                    season.getInt("season_type_id"),
                    season.getInt("Season_theme_id"),
                    season.getInt("allow_guilds"),
                    season.getString("date_created"),
                    season.getString("date_started"),
                    season.getString("date_ended"),
                    season.getInt("season_result_id")
            );
        }
        catch (InterruptedException e) {
            RP.getPlugin().getLogger().info("Interrupted while getting RP Season: " + e.getMessage());
        }
        catch (ExecutionException e) {
            RP.getPlugin().getLogger().info("Could not execute query to get RP Season: " + e.getMessage());
        }
        return new RPSeason();
    }

    // Getters and setters

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

    public int getAllowGuilds() {
        return allowGuilds;
    }

    public void setAllowGuilds(int allowGuilds) {
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
