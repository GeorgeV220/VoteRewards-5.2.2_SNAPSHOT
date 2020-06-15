package com.georgev22.voterewards.playerdata;

import com.georgev22.voterewards.configmanager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

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

    COMMAND_REWARDS("commands.rewards"),

    COMMAND_FAKEVOTE("commands.fakevote"),

    COMMAND_VOTEPARTY("commands.voteparty"),

    COMMAND_VOTES("commands.votes"),

    COMMAND_VOTE("commands.vote"),

    COMMAND_VOTETOP("commands.votetop"),

    COMMAND_VOTEREWARDS("commands.voterewards"),

    ;

    private final String pathName;

    VoteOptions(final String pathName) {
        this.pathName = pathName;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean("Options." + this.pathName, true);
    }

}
