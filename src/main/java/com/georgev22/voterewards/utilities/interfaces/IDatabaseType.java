package com.georgev22.voterewards.utilities.interfaces;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.voterewards.VoteRewardPlugin;
import com.georgev22.voterewards.utilities.player.User;
import com.google.common.collect.Lists;

import java.util.UUID;

import static com.georgev22.api.utilities.Utils.*;

public interface IDatabaseType {

    void save(User user) throws Exception;

    void load(User user, Callback callback) throws Exception;

    void setupUser(User user, Callback callback) throws Exception;

    default void reset(User user, boolean allTime) throws Exception {
        user.append("votes", 0)
                .append("services", Lists.newArrayList())
                .append("voteparty", 0)
                .append("daily", 0)
                .append("servicesLastVote", ObjectMap.newConcurrentObjectMap())
                .appendIfTrue("totalvotes", 0, allTime);
        save(user);
        MinecraftUtils.debug(VoteRewardPlugin.getInstance(), "User " + user.getName() + " has been reset!");
    }

    void delete(User user) throws Exception;

    boolean playerExists(User user) throws Exception;

    ObjectMap<UUID, User> getAllUsers() throws Exception;

}
