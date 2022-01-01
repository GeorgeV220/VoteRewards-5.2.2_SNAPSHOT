package com.georgev22.voterewards.utilities;

import com.georgev22.api.colors.Color;
import com.georgev22.api.externals.xseries.XMaterial;
import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.georgev22.api.utilities.Utils.Assertions.notNull;

public enum OptionsUtil {

    DEBUG_VOTE_PRE("debug.vote.preVote", false, Optional.empty()),

    DEBUG_VOTE_AFTER("debug.vote.afterVote", false, Optional.empty()),

    DEBUG_LOAD("debug.load", false, Optional.empty()),

    DEBUG_SAVE("debug.save", false, Optional.empty()),

    DEBUG_CREATE("debug.create", false, Optional.empty()),

    DEBUG_USELESS("debug.useless info", false, Optional.empty()),

    DEBUG_DELETE("debug.delete", false, Optional.empty()),

    DEBUG_VOTES_CUMULATIVE("debug.votes.cumulative", false, Optional.empty()),

    DEBUG_VOTES_DAILY("debug.votes.daily", false, Optional.empty()),

    DEBUG_VOTES_LUCKY("debug.votes.lucky", false, Optional.empty()),

    DEBUG_VOTES_REGULAR("debug.votes.regular", false, Optional.empty()),

    DEBUG_VOTES_WORLD("debug.votes.world", false, Optional.empty()),

    DEBUG_VOTES_PERMISSIONS("debug.votes.permissions", false, Optional.empty()),

    DEBUG_VOTES_OFFLINE("debug.votes.offline", false, Optional.empty()),

    VOTE_TITLE("title.vote", false, Optional.empty()),

    VOTEPARTY_TITLE("title.voteparty", false, Optional.empty()),

    UPDATER("updater", true, Optional.empty()),

    MONTHLY_ENABLED("monthly.enabled", false, Optional.empty()),

    MONTHLY_MINUTES("monthly.minutes", 30L, Optional.empty()),

    PURGE_ENABLED("purge.enabled", false, Optional.empty()),

    PURGE_DAYS("purge.days", 60L, Optional.empty()),

    PURGE_MINUTES("purge.minutes", 30L, Optional.empty()),

    DAILY("votes.daily.enabled", false, Optional.empty()),

    DAILY_HOURS("votes.daily.hours", 12, Optional.empty()),

    OFFLINE("votes.offline", false, Optional.empty()),

    PERMISSIONS("votes.permissions", false, Optional.empty()),

    WORLD("votes.world.enabled", false, Optional.empty()),

    WORLD_SERVICES("votes.world.services", false, Optional.empty()),

    SERVICES("votes.services", false, Optional.empty()),

    LUCKY("votes.lucky.enabled", false, Optional.empty()),

    LUCKY_NUMBERS("votes.lucky.numbers", 50, Optional.empty()),

    CUMULATIVE("votes.cumulative", false, Optional.empty()),

    CUMULATIVE_MESSAGE("votes.cumulative message", true, Optional.empty()),

    REMINDER("reminder.enabled", false, Optional.empty()),

    REMINDER_SEC("reminder.time", 120, Optional.empty()),

    SOUND("sound.vote", false, Optional.empty()),

    MESSAGE_VOTE("message.vote", false, Optional.empty()),

    MESSAGE_VOTEPARTY("message.voteparty", false, Optional.empty()),

    VOTETOP_HEADER("votetop.header", true, Optional.empty()),

    VOTETOP_LINE("votetop.line", false, Optional.empty()),

    VOTETOP_FOOTER("votetop.footer", true, Optional.empty()),

    VOTETOP_VOTERS("votetop.voters", 5, Optional.empty()),

    VOTETOP_GUI("votetop.gui.enabled", false, Optional.empty()),

    VOTETOP_GUI_TYPE("votetop.gui.type", "monthly", Optional.empty()),

    VOTETOP_ALL_TIME_ENABLED("votetop.all time.enabled", false, Optional.empty()),

    VOTETOP_ALL_TIME_VOTERS("votetop.all time.voters", 3, Optional.empty()),

    COMMAND_REWARDS("commands.rewards", true, Optional.empty()),

    COMMAND_FAKEVOTE("commands.fakevote", true, Optional.empty()),

    COMMAND_VOTEPARTY("commands.voteparty", true, Optional.empty()),

    COMMAND_VOTES("commands.votes", true, Optional.empty()),

    COMMAND_VOTE("commands.vote", true, Optional.empty()),

    COMMAND_VOTETOP("commands.votetop", true, Optional.empty()),

    COMMAND_VOTEREWARDS("commands.voterewards", true, Optional.empty()),

    COMMAND_HOLOGRAM("commands.hologram", true, Optional.empty()),

    COMMAND_NPC("commands.npc", true, Optional.empty()),

    DATABASE_HOST("database.SQL.host", "localhost", Optional.empty()),

    DATABASE_PORT("database.SQL.port", 3306, Optional.empty()),

    DATABASE_USER("database.SQL.user", "youruser", Optional.empty()),

    DATABASE_PASSWORD("database.SQL.password", "yourpassword", Optional.empty()),

    DATABASE_DATABASE("database.SQL.database", "VoteRewards", Optional.empty()),

    DATABASE_TABLE_NAME("database.SQL.table name", "voterewards_users", Optional.empty()),

    DATABASE_SQLITE("database.SQLite.file name", "voterewards", Optional.empty()),

    DATABASE_MONGO_HOST("database.MongoDB.host", "localhost", Optional.empty()),

    DATABASE_MONGO_PORT("database.MongoDB.port", 27017, Optional.empty()),

    DATABASE_MONGO_USER("database.MongoDB.user", "youruser", Optional.empty()),

    DATABASE_MONGO_PASSWORD("database.MongoDB.password", "yourpassword", Optional.empty()),

    DATABASE_MONGO_DATABASE("database.MongoDB.database", "VoteRewards", Optional.empty()),

    DATABASE_MONGO_COLLECTION("database.MongoDB.collection", "voterewards_users", Optional.empty()),

    DATABASE_TYPE("database.type", "File", Optional.empty()),

    VOTEPARTY("voteparty.enabled", false, Optional.empty()),

    VOTEPARTY_PARTICIPATE("voteparty.participate", true, Optional.empty()),

    VOTEPARTY_CRATE("voteparty.crate.enabled", true, Optional.empty()),

    VOTEPARTY_RANDOM("voteparty.random rewards", true, Optional.empty()),

    VOTEPARTY_COOLDOWN("voteparty.cooldown.enabled", true, Optional.empty()),

    VOTEPARTY_COOLDOWN_SECONDS("voteparty.cooldown.seconds", 5, Optional.empty()),

    VOTEPARTY_PLAYERS("voteparty.players.enabled", false, Optional.empty()),

    VOTEPARTY_PLAYERS_NEED("voteparty.players.need", 5, Optional.empty()),

    VOTEPARTY_SOUND_START("sound.voteparty", false, Optional.empty()),

    VOTEPARTY_SOUND_CRATE("sound.crate", false, Optional.empty()),

    VOTEPARTY_REGIONS("voteparty.regions", false, Optional.empty()),

    VOTEPARTY_VOTES("voteparty.votes", 2, Optional.empty()),

    VOTEPARTY_BARS("voteparty.progress.bars", 10, Optional.empty()),

    VOTEPARTY_COMPLETE_COLOR("voteparty.progress.complete color", "&a", Optional.empty()),

    VOTEPARTY_NOT_COMPLETE_COLOR("voteparty.progress.not complete color", "&c", Optional.empty()),

    VOTEPARTY_BAR_SYMBOL("voteparty.progress.bar symbol", "|", Optional.empty()),

    VOTEPARTY_CRATE_ITEM("voteparty.crate.item", "PISTON", Optional.empty()),

    VOTEPARTY_CRATE_NAME("voteparty.crate.name", "&cVoteParty Crate", Optional.empty()),

    VOTEPARTY_CRATE_LORES("voteparty.crate.lores", Collections.singletonList("&cPlace me"), Optional.empty()),

    VOTEPARTY_REWARDS("voteparty.rewards",
            Arrays.asList("eco give %player% 6547", "give %player% minecraft:nether_star 31"), Optional.empty()),

    SOUND_VOTE("sound.sounds.vote received.sound", "NOTE_PIANO", Optional.empty()),

    SOUND_VOTE_CHANNEL("sound.sounds.vote received.channel", "AMBIENT", Optional.empty()),

    SOUND_CRATE_OPEN("sound.sounds.crate open.sound", "CHEST_OPEN", Optional.empty()),

    SOUND_CRATE_OPEN_CHANNEL("sound.sounds.crate open.channel", "AMBIENT", Optional.empty()),

    SOUND_VOTEPARTY_START("sound.sounds.voteparty start.sound", "ENDERDRAGON_DEATH", Optional.empty()),

    SOUND_VOTEPARTY_START_CHANNEL("sound.sounds.voteparty start.channel", "HOSTILE", Optional.empty()),

    EXPERIMENTAL_FEATURES("experimental features", false, Optional.empty()),

    DISCORD("discord", false, Optional.empty()),

    ;
    private final String pathName;
    private final Object value;
    private final Optional<String>[] oldPaths;
    private static final VoteRewardPlugin mainPlugin = VoteRewardPlugin.getInstance();

    @SafeVarargs
    @Contract(pure = true)
    OptionsUtil(final String pathName, final Object value, Optional<String>... oldPaths) {
        this.pathName = pathName;
        this.value = value;
        this.oldPaths = oldPaths;
    }

    public boolean getBooleanValue() {
        return mainPlugin.getConfig().getBoolean(getPath(), Boolean.parseBoolean(String.valueOf(getDefaultValue())));
    }

    public Object getObjectValue() {
        return mainPlugin.getConfig().get(getPath(), getDefaultValue());
    }

    public String getStringValue() {
        return mainPlugin.getConfig().getString(getPath(), String.valueOf(getDefaultValue()));
    }

    public @NotNull Long getLongValue() {
        return mainPlugin.getConfig().getLong(getPath(), Long.parseLong(String.valueOf(getDefaultValue())));
    }

    public @NotNull Integer getIntValue() {
        return mainPlugin.getConfig().getInt(getPath(), Integer.parseInt(String.valueOf(getDefaultValue())));
    }

    public @NotNull Double getDoubleValue() {
        return mainPlugin.getConfig().getDouble(getPath(), Double.parseDouble(String.valueOf(getDefaultValue())));
    }

    public @NotNull List<String> getStringList() {
        return mainPlugin.getConfig().getStringList(getPath());
    }

    public ItemStack getItemStack(boolean isSavedAsItemStack) {
        if (isSavedAsItemStack) {
            return mainPlugin.getConfig().getItemStack(getPath(), (ItemStack) getDefaultValue());
        } else {
            if (mainPlugin.getConfig().get(getPath()) == null) {
                return (ItemStack) getDefaultValue();
            }
            ItemBuilder itemBuilder = new ItemBuilder(
                    notNull("Material", XMaterial.valueOf(mainPlugin.getConfig().getString(getPath() + ".item")).parseMaterial()))
                    .amount(mainPlugin.getConfig().getInt(getPath() + ".amount"))
                    .title(mainPlugin.getConfig().getString(getPath() + ".title"))
                    .lores(mainPlugin.getConfig().getStringList(getPath() + ".lores"))
                    .showAllAttributes(
                            mainPlugin.getConfig().getBoolean(getPath() + ".show all attributes"))
                    .glow(mainPlugin.getConfig().getBoolean(getPath() + ".glow"));
            return itemBuilder.build();
        }
    }

    /**
     * Converts and return a String List of color codes to a List of Color classes that represent the colors.
     *
     * @return a List of Color classes that represent the colors.
     */
    public @NotNull List<Color> getColors() {
        List<Color> colors = Lists.newArrayList();
        for (String stringColor : getStringList()) {
            colors.add(Color.from(stringColor));
        }

        return colors;
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    public @NotNull String getPath() {
        if (mainPlugin.getConfig().get("Options." + getDefaultPath()) == null) {
            if (getOldPaths().length > 0) {
                for (Optional<String> path : getOldPaths()) {
                    if (path.isPresent()) {
                        if (mainPlugin.getConfig().get("Options." + path.get()) != null) {
                            return "Options." + path.get();
                        }
                    }
                }
            }
        }
        return "Options." + getDefaultPath();
    }

    /**
     * Returns the default path.
     *
     * @return the default path.
     */
    @Contract(pure = true)
    public @NotNull String getDefaultPath() {
        return this.pathName;
    }

    /**
     * Returns the old path if it exists.
     *
     * @return the old path if it exists.
     */
    public Optional<String>[] getOldPaths() {
        return oldPaths;
    }

    /**
     * Returns the default value if the path have no value.
     *
     * @return the default value if the path have no value.
     */
    public Object getDefaultValue() {
        return value;
    }
}
