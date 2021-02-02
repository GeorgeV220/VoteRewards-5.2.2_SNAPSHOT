package com.georgev22.voterewards.utilities.player;

import com.georgev22.voterewards.utilities.maps.ConcurrentObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public class User extends ConcurrentObjectMap {

    private final UUID uuid;

    /**
     * Creates an User instance.
     *
     * @param uuid Player Unique identifier
     */
    public User(UUID uuid) {
        this.uuid = uuid;
        put("uuid", uuid.toString());
    }

    /**
     * Creates a User instance initialized with the given map.
     * <p>
     * You must add UUID to your map
     *
     * @param uuid User Unique ID
     * @param map  initial map
     * @see User#User(UUID)
     */
    public User(UUID uuid, final Map<String, Object> map) {
        super(map);
        this.uuid = uuid;
    }

    /**
     * Returns User's Unique ID
     *
     * @return User's Unique ID
     */
    public UUID getUniqueID() {
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
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(getUniqueID());
    }
}
