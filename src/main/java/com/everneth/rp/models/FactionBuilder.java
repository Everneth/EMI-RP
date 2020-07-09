package com.everneth.rp.models;

public class FactionBuilder {
    private int id;
    private String name;
    private int leaderId;

    public FactionBuilder() { }

    public Faction buildGuild()
    {
        return new Faction(id, name, leaderId);
    }

    public FactionBuilder name(String _name)
    {
        this.name = _name;
        return this;
    }

    public FactionBuilder age(int id)
    {
        this.id = id;
        return this;
    }

    public FactionBuilder leader(int leaderId)
    {
        this.leaderId = leaderId;
        return this;
    }
}
