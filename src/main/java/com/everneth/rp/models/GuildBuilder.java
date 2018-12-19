package com.everneth.rp.models;

public class GuildBuilder {
    private int id;
    private String name;
    private int leaderId;

    public GuildBuilder() { }

    public Guild buildGuild()
    {
        return new Guild(id, name, leaderId);
    }

    public GuildBuilder name(String _name)
    {
        this.name = _name;
        return this;
    }

    public GuildBuilder age(int id)
    {
        this.id = id;
        return this;
    }

    public GuildBuilder leader(int leaderId)
    {
        this.leaderId = leaderId;
        return this;
    }
}
