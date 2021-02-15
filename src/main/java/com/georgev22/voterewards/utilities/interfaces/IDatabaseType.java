package com.georgev22.voterewards.utilities.interfaces;

import com.georgev22.voterewards.utilities.maps.ObjectMap;
import com.georgev22.voterewards.utilities.player.User;

import java.io.IOException;

public interface IDatabaseType {

    void save(User user) throws Exception;

    void load(User user, Callback callback) throws Exception;

    void setupUser(User user, Callback callback) throws Exception;

    void reset(User user) throws Exception;

    void delete(User user) throws Exception;

    boolean playerExists(User user) throws Exception;

    ObjectMap getAllUsers() throws Exception;

}
