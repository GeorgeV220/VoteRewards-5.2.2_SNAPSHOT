package com.georgev22.voterewards.utilities;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author GeorgeV22
 */
public class HologramManager {

    private final Map<String, Hologram> hologramMap = Maps.newHashMap();

    public Map<String, Hologram> getHologramMap() {
        return hologramMap;
    }
}
