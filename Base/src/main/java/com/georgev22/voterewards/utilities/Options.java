package com.georgev22.voterewards.utilities;

import com.georgev22.voterewards.configmanager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public enum Options {

    DEBUG_VOTE_PRE("debug.vote.preVote"),

    DEBUG_VOTE_AFTER("debug.vote.afterVote"),

    DEBUG_LOAD("debug.load"),

    DEBUG_SAVE("debug.save"),

    DEBUG_VOTES_CUMULATIVE("debug.votes.cumulative"),

    DEBUG_VOTES_DAILY("debug.votes.daily"),

    DEBUG_VOTES_LUCKY("debug.votes.lucky"),

    DEBUG_VOTES_REGULAR("debug.votes.regular"),

    DEBUG_VOTES_WORLD("debug.votes.world"),

    DEBUG_VOTES_PERMISSIONS("debug.votes.permissions"),

    DEBUG_VOTES_OFFLINE("debug.votes.offline"),

    VOTE_TITLE("title.vote"),

    VOTEPARTY_TITLE("title.voteparty"),

    UPDATER("updater"),

    DAILY("votes.daily.enabled"),

    DAILY_HOURS("votes.daily.hours"),

    OFFLINE("votes.offline"),

    PERMISSIONS("votes.permissions"),

    WORLD("votes.world"),

    DISABLE_SERVICES("votes.disable services"),

    LUCKY("votes.lucky.enabled"),

    LUCKY_NUMBERS("votes.lucky.numbers"),

    CUMULATIVE("votes.cumulative"),

    CUMULATIVE_MESSAGE("votes.cumulative message"),

    REMINDER("reminder.enabled"),

    REMINDER_SEC("reminder.time"),

    SOUND("sound.vote"),

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

    DATABASE_HOST("database.DB.host"),

    DATABASE_PORT("database.DB.port"),

    DATABASE_USER("database.DB.user"),

    DATABASE_PASSWORD("database.DB.password"),

    DATABASE_DATABASE("database.DB.database"),

    DATABASE_SQLITE("database.SQLite.file name"),

    DATABASE_TYPE("database.type"),

    VOTEPARTY("voteparty.enabled"),

    VOTEPARTY_PARTICIPATE("voteparty.participate"),

    VOTEPARTY_CRATE("voteparty.crate.enabled"),

    VOTEPARTY_RANDOM("voteparty.random rewards"),

    VOTEPARTY_COOLDOWN("voteparty.cooldown.enabled"),

    VOTEPARTY_COOLDOWN_SECONDS("voteparty.cooldown.seconds"),

    VOTEPARTY_SOUND_START("sound.voteparty"),

    VOTEPARTY_SOUND_CRATE("sound.crate"),

    VOTEPARTY_REGIONS("voteparty.regions"),

    VOTEPARTY_VOTES("voteparty.votes"),

    VOTEPARTY_BARS("voteparty.progress.bars"),

    VOTEPARTY_COMPLETE_COLOR("voteparty.progress.complete color"),

    VOTEPARTY_NOT_COMPLETE_COLOR("voteparty.progress.not complete color"),

    VOTEPARTY_BAR_SYMBOL("voteparty.progress.bar symbol"),

    VOTEPARTY_CRATE_ITEM("voteparty.crate.item"),

    VOTEPARTY_CRATE_NAME("voteparty.crate.name"),

    VOTEPARTY_CRATE_LORES("voteparty.crate.lores"),

    VOTEPARTY_REWARDS("voteparty.rewards"),

    SOUND_VOTE("sound.sounds.vote received"),

    SOUND_CRATE_OPEN("sound.sounds.crate open"),

    SOUND_VOTEPARTY_START("sound.sounds.voteparty start"),

    ;
    private final String pathName;

    Options(final String pathName) {
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

    public List<String> getStringList() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getStringList("Options." + this.pathName);
    }
}
