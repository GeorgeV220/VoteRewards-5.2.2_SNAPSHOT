package com.georgev22.voterewards.utilities.options;

import com.georgev22.voterewards.configmanager.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

public enum PartyOptions {

    PARTICIPATE("participate"),

    CRATE("crate.enabled"),

    RANDOM("random rewards"),

    COOLDOWN("cooldown.enabled"),

    SOUND_START("sound"),

    SOUND_CRATE("crate.sound"),

    REGIONS("regions"),

    VOTES("votes"),

    BARS("progress.bars"),

    COMPLETE_COLOR("progress.complete color"),

    NOT_COMPLETE_COLOR("progress.not complete color"),

    BAR_SYMBOL("progress.bar symbol"),

    ;

    private final String pathName;

    PartyOptions(final String pathName) {
        this.pathName = pathName;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean("VoteParty." + this.pathName, false);
    }

    public Object getValue() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.get("VoteParty." + this.pathName, 0);
    }
}
