package com.everneth.rp.models;

public class GuildMember {
    private int guildMemberId;
    private int guildId;

    public GuildMember()
    {
        this.guildId = 0;
        this.guildMemberId = 0;
    }

    public GuildMember(int guildMemberId, int guildId)
    {
        this.guildMemberId = guildMemberId;
        this.guildId = guildId;
    }

    public int getGuildMemberId() {
        return guildMemberId;
    }

    public void setGuildMemberId(int guildMemberId) {
        this.guildMemberId = guildMemberId;
    }

    public int getGuildId() {
        return guildId;
    }

    public void setGuildId(int guildId) {
        this.guildId = guildId;
    }
}
