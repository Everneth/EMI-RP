package com.everneth.rp.models;

import java.util.ArrayList;

public class Faction {
    private int id;
    private String name;
    private int leaderId;
    ArrayList<String> members;

    public Faction(int id, String name, int leaderId)
    {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
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

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }
}
