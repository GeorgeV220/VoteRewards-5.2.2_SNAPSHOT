package com.georgev22.voterewards.utilities.options;

import com.georgev22.voterewards.configmanager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

public enum VoteOptions {

    DEBUG("debug"),

    VOTE_TITLE("title.vote"),

    VOTEPARTY_TITLE("title.voteparty"),

    UPDATER("updater"),

    DAILY("votes.daily.enabled"),

    DAILY_HOURS("votes.daily.hours"),

    OFFLINE("votes.offline"),

    PERMISSIONS("votes.permissions"),

    WORLD("votes.world"),

    DISABLE_SERVICES("votes.disable services"),

    LUCKY("votes.lucky"),

    CUMULATIVE("votes.cumulative"),

    CUMULATIVE_MESSAGE("votes.cumulative message"),

    VOTE_PARTY("voteparty"),

    REMINDER("reminder.enabled"),

    REMINDER_SEC("reminder.time"),

    SOUND("sound"),

    MESSAGE("message"),

    VOTETOP_HEADER("votetop.header"),

    VOTETOP_FOOTER("votetop.footer"),

    VOTETOP_VOTERS("votetop.voters"),

    COMMAND_REWARDS("commands.rewards"),

    COMMAND_FAKEVOTE("commands.fakevote"),

    COMMAND_VOTEPARTY("commands.voteparty"),

    COMMAND_VOTES("commands.votes"),

    COMMAND_VOTE("commands.vote"),

    COMMAND_VOTETOP("commands.votetop"),

    COMMAND_VOTEREWARDS("commands.voterewards"),

    COMMAND_HOLOGRAM("commands.hologram"),

    ;
    private final String pathName;

    VoteOptions(final String pathName) {
        this.pathName = pathName;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean("Options." + this.pathName, true);
    }

    public Object getValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.get("Options." + this.pathName, 0);
    }

}
