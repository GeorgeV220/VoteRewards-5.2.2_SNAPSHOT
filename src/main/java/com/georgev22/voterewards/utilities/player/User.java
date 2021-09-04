package com.georgev22.voterewards.utilities.player;

import com.georgev22.externals.utilities.maps.ConcurrentObjectMap;
import com.georgev22.externals.utilities.maps.ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class User extends ConcurrentObjectMap {

    private final UUID uuid;

    /**
     * Creates a User instance.
     *
     * @param uuid Player Unique identifier
     */
    public User(UUID uuid) {
        this.uuid = uuid;
        append("uuid", uuid);
    }

    /**
     * Creates a User instance initialized with the given map.
     *
     * @param uuid User Unique ID
     * @param map  initial map
     * @see User#User(UUID)
     */
    public User(UUID uuid, final ObjectMap<String, Object> map) {
        super(map.append("uuid", uuid));
        this.uuid = uuid;
    }

    /**
     * Returns User's Unique ID
     *
     * @return User's Unique ID
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Gets the player, regardless if they are offline or
     * online.
     * <p>
     * This will return an object even if the player does not exist. To this
     * method, all players will exist.
     *
     * @return an offline player
     */
    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getUniqueId());
    }

    public Player getPlayer() {
        return getOfflinePlayer().getPlayer();
    }

    /**
     * Returns the name of this player
     *
     * @return Player name or null
     */
    @Nullable
    public String getName() {
        return getString("name", getOfflinePlayer().getName());
    }

    /**
     * Get user total votes
     *
     * @return user total votes
     */
    public int getVotes() {
        return getInteger("votes", 0);
    }

    /**
     * Get user daily votes
     *
     * @return user daily votes
     */
    public int getDailyVotes() {
        return getInteger("daily", 0);
    }

    /**
     * Get the last time when the user voted
     *
     * @return the last time when the user voted
     */
    public long getLastVoted() {
        return getLong("last", 0L);
    }

    /**
     * Get user virtual crates
     *
     * @return user virtual crates
     */
    public int getVoteParties() {
        return getInteger("voteparty", 0);
    }

    /**
     * Get user all time votes
     *
     * @return user all time votes
     */
    public int getAllTimeVotes() {
        return getInteger("totalvotes", 0);
    }

    public ObjectMap<String, Long> getServicesLastVote() {
        return (ObjectMap<String, Long>) get("servicesLastVote", ObjectMap.newHashObjectMap());
    }

    /**
     * Get all services that the user have voted
     * when he was offline
     *
     * @return services
     */
    public List<String> getServices() {
        return getList("services", String.class);
    }
}
