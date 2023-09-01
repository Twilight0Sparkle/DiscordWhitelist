package com.windstudio.discordwl.bot.Whitelist;

import java.util.ArrayList;
import java.util.List;

public class WhitelistData extends DataStorage {

    WhitelistData whitelistData;
    private List<String> players = new ArrayList<>();
    private List<String> administrators = new ArrayList<>();
    private boolean whitelist;
    private boolean whitelist_locked;
    private String message;
    private String lock_message;
    public WhitelistData(String path) {
        super(path);
    }

    public void load() {
        setDefault("whitelist", Boolean.valueOf(false));
        setDefault("whitelist_locked", Boolean.valueOf(false));
        setDefault("message", "&cYou are not whitelisted.");
        setDefault("lock_message", "&cThis server is under maintenance.");
        this.whitelist = getConfig().getBoolean("whitelist");
        this.whitelist_locked = getConfig().getBoolean("whitelist_locked");
        this.message = getConfig().getString("message");
        this.lock_message = getConfig().getString("lock_message");
    }

    public void save() {
        getConfig().set("whitelist", Boolean.valueOf(this.whitelist));
        getConfig().set("whitelist_locked", Boolean.valueOf(this.whitelist_locked));
        getConfig().set("message", this.message);
        getConfig().set("lock_message", this.lock_message);
        writeToFile();
    }

    public boolean isWhitelist() {
        return this.whitelist;
    }

    public boolean isWhitelist_locked() {
        return this.whitelist_locked;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public void setWhitelistLocked(boolean whitelist_locked) {
        this.whitelist_locked = whitelist_locked;
    }

    public String getMessage() {
        return this.message;
    }

    public String getLockMessage() {
        return this.lock_message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLockMessage(String lock_message) {
        this.lock_message = lock_message;
    }
}