package com.georgev22.voterewards.utilities;

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

    ;

    private final String pathName;

    PartyOptions(final String pathName) {
        this.pathName = pathName;
    }

    public boolean isEnabled() {
        final FileConfiguration file = FileManager.getInstance().getConfig().getFileConfiguration();
        return file.getBoolean("VoteParty." + this.pathName, false);
    }
}
