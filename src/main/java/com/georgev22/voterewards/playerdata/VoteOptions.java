package com.georgev22.voterewards.playerdata;

import org.bukkit.configuration.file.FileConfiguration;

import com.georgev22.voterewards.configmanager.FileManager;

public enum VoteOptions {

    VOTE_TITLE("title.vote"),

    VOTEPARTY_TITLE("title.voteparty"),

    UPDATER("updater"),

    DAILY("votes.daily.enabled"),

    OFFLINE("votes.offline"),

    PERMISSIONS("votes.permissions"),

    LUCKY("votes.lucky"),

    CUMULATIVE("votes.cumulative"),

    VOTE_PARTY("voteparty"),

    REMINDER("reminder"),

    SOUND("sound"),

    ;

    private final String pathName;

    VoteOptions(final String pathName) {
        this.pathName = pathName;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean("Options." + this.pathName, false);
    }

}
