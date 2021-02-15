package com.georgev22.voterewards.utilities.maps;

import org.bukkit.Location;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ObjectMap<K, V> extends Map<K, V> {

    /**
     * Creates a new empty {@link LinkedObjectMap} instance.
     *
     * @return a new empty {@link LinkedObjectMap} instance.
     */
    static ObjectMap newLinkedObjectMap() {
        return new LinkedObjectMap();
    }

    /**
     * Creates a new empty {@link ConcurrentObjectMap} instance.
     *
     * @return a new empty {@link ConcurrentObjectMap} instance.
     */
    static ObjectMap newConcurrentObjectMap() {
        return new ConcurrentObjectMap();
    }

    /**
     * Creates a new empty {@link HashObjectMap} instance.
     *
     * @return a new empty {@link HashObjectMap} instance.
     */
    static ObjectMap newHashObjectMap() {
        return new HashObjectMap();
    }

    /**
     * Creates a {@link LinkedObjectMap} instance with the same mappings as the specified map.
     *
     * @param map the mappings to be placed in the new map
     * @return a new {@link LinkedObjectMap#LinkedObjectMap(ObjectMap)} initialized with the mappings from {@code map}
     */
    static ObjectMap newLinkedObjectMap(ObjectMap map) {
        return new LinkedObjectMap(map);
    }

    /**
     * Creates a {@link ConcurrentObjectMap} instance with the same mappings as the specified map.
     *
     * @param map the mappings to be placed in the new map
     * @return a new {@link ConcurrentObjectMap#ConcurrentObjectMap(ObjectMap)} initialized with the mappings from {@code map}
     */
    static ObjectMap newConcurrentObjectMap(ObjectMap map) {
        return new ConcurrentObjectMap(map);
    }

    /**
     * Creates a {@link HashObjectMap} instance with the same mappings as the specified map.
     *
     * @param map the mappings to be placed in the new map
     * @return a new {@link HashObjectMap#HashObjectMap(ObjectMap)} initialized with the mappings from {@code map}
     */
    static ObjectMap newHashObjectMap(ObjectMap map) {
        return new HashObjectMap(map);
    }


    /**
     * Put/replace the given key/value pair into this User and return this.  Useful for chaining puts in a single expression, e.g.
     * <pre>
     * user.append("a", 1).append("b", 2)}
     * </pre>
     *
     * @param key   key
     * @param value value
     * @return this
     */
    ObjectMap<K, V> append(final K key, final V value);

    /**
     * Gets the value of the given key as an Integer.
     *
     * @param key the key
     * @return the value as an integer, which may be null
     * @throws ClassCastException if the value is not an integer
     */
    Integer getInteger(final Object key);

    /**
     * Gets the value of the given key as a primitive int.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as an integer, which may be null
     * @throws ClassCastException if the value is not an integer
     */
    int getInteger(final Object key, final int defaultValue);

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws ClassCastException if the value is not an long
     */
    Long getLong(final Object key);

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a long, which may be null
     * @throws ClassCastException if the value is not an long
     */
    Long getLong(final Object key, final long defaultValue);

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws ClassCastException if the value is not an double
     */
    Double getDouble(final Object key);

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a double, which may be null
     * @throws ClassCastException if the value is not an double
     */
    Double getDouble(final Object key, final double defaultValue);

    /**
     * Gets the value of the given key as a String.
     *
     * @param key the key
     * @return the value as a String, which may be null
     * @throws ClassCastException if the value is not a String
     */
    String getString(final Object key);

    /**
     * Gets the value of the given key as a String.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a String, which may be null
     * @throws ClassCastException if the value is not a String
     */
    String getString(final Object key, final String defaultValue);

    /**
     * Gets the value of the given key as a Boolean.
     *
     * @param key the key
     * @return the value as a Boolean, which may be null
     * @throws ClassCastException if the value is not an boolean
     */
    Boolean getBoolean(final Object key);

    /**
     * Gets the value of the given key as a primitive boolean.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a primitive boolean
     * @throws ClassCastException if the value is not a boolean
     */
    boolean getBoolean(final Object key, final boolean defaultValue);

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key the key
     * @return the value as a Date, which may be null
     * @throws ClassCastException if the value is not a Date
     */
    Date getDate(final Object key);

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a Date, which may be null
     * @throws ClassCastException if the value is not a Date
     */
    Date getDate(final Object key, final Date defaultValue);

    /**
     * Gets the value of the given key as a Location.
     *
     * @param key the key
     * @return the value as a Location, which may be null
     * @throws ClassCastException if the value is not a Location
     */
    Location getLocation(final Object key);

    /**
     * Gets the list value of the given key, casting the list elements to the given {@code Class<T>}.  This is useful to avoid having
     * casts in client code, though the effect is the same.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the list value to
     * @param <T>   the type of the class
     * @return the list value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the elements in the list value of the given key is not of type T or the value is not a list
     */
    <T> List<T> getList(Object key, Class<T> clazz);

    /**
     * Gets the list value of the given key, casting the list elements to {@code Class<T>} or returning the default list value if null.
     * This is useful to avoid having casts in client code, though the effect is the same.
     *
     * @param key          the key
     * @param clazz        the non-null class to cast the list value to
     * @param defaultValue what to return if the value is null
     * @param <T>          the type of the class
     * @return the list value of the given key, or the default list value if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    <T> List<T> getList(final Object key, final Class<T> clazz, final List<T> defaultValue);

    /**
     * Gets the value of the given key, casting it to the given {@code Class<T>}.  This is useful to avoid having casts in client code,
     * though the effect is the same.  So to get the value of a key that is of type String, you would write {@code String name =
     * doc.get("name", String.class)} instead of {@code String name = (String) doc.get("x") }.
     *
     * @param key   the key
     * @param clazz the non-null class to cast the value to
     * @param <T>   the type of the class
     * @return the value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    <T> T get(final Object key, final Class<T> clazz);

    /**
     * Gets the value of the given key, casting it to {@code Class<T>} or returning the default value if null.
     * This is useful to avoid having casts in client code, though the effect is the same.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @param <T>          the type of the class
     * @return the value of the given key, or null if the instance does not contain this key.
     * @throws ClassCastException if the value of the given key is not of type T
     */
    <T> T get(final Object key, final T defaultValue);

}
