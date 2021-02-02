package com.georgev22.voterewards.utilities.maps;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.georgev22.voterewards.utilities.Assertions.notNull;
import static java.lang.String.format;

public class LinkedObjectMap<K, V> extends LinkedHashMap<K, V> implements ObjectMap<K, V> {

    /**
     * Creates a new empty {@link LinkedObjectMap} instance.
     *
     * @return a new empty {@link LinkedObjectMap} instance.
     */
    public static LinkedObjectMap newObjectMap() {
        return new LinkedObjectMap();
    }

    /**
     * Creates a {@link LinkedObjectMap} instance with the same mappings as the specified map.
     *
     * @param map the mappings to be placed in the new map
     * @return a new {@link LinkedObjectMap#LinkedObjectMap(Map)} initialized with the mappings from {@code map}
     */
    public static LinkedObjectMap newObjectMap(final Map map) {
        return new LinkedObjectMap(map);
    }

    /**
     * Creates an LinkedObjectMap instance.
     */
    public LinkedObjectMap() {
    }

    /**
     * Creates a LinkedObjectMap instance initialized with the given map.
     *
     * @param map initial map
     */
    public LinkedObjectMap(final Map<K, V> map) {
        putAll(map);
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
    public LinkedObjectMap<K, V> append(final K key, final V value) {
        if (containsKey(key)) {
            replace(key, value);
        } else {
            put(key, value);
        }
        return this;
    }

    /**
     * Gets the value of the given key as an Integer.
     *
     * @param key the key
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public Integer getInteger(final Object key) {
        return getInteger(key, 0);
    }

    /**
     * Gets the value of the given key as a primitive int.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public int getInteger(final Object key, final int defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not an long
     */
    public Long getLong(final Object key) {
        return getLong(key, 0L);
    }

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not an long
     */
    public Long getLong(final Object key, final long defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Double getDouble(final Object key) {
        return getDouble(key, 0D);
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Double getDouble(final Object key, final double defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key the key
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final Object key) {
        return getString(key, "");
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final Object key, final String defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a Boolean.
     *
     * @param key the key
     * @return the value as a Boolean, which may be null
     * @throws java.lang.ClassCastException if the value is not an boolean
     */
    public Boolean getBoolean(final Object key) {
        return getBoolean(key, false);
    }

    /**
     * Gets the value of the given key as a primitive boolean.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a primitive boolean
     * @throws java.lang.ClassCastException if the value is not a boolean
     */
    public boolean getBoolean(final Object key, final boolean defaultValue) {
        return get(key, defaultValue);
    }

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key the key
     * @return the value as a Date, which may be null
     * @throws java.lang.ClassCastException if the value is not a Date
     */
    public Date getDate(final Object key) {
        return getDate(key, new Date());
    }

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a Date, which may be null
     * @throws java.lang.ClassCastException if the value is not a Date
     */
    public Date getDate(final Object key, final Date defaultValue) {
        return get(key, defaultValue);
    }

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
    public <T> List<T> getList(Object key, Class<T> clazz) {
        return getList(key, clazz, null);
    }

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
    public <T> List<T> getList(final Object key, final Class<T> clazz, final List<T> defaultValue) {
        notNull("clazz", clazz);
        List<T> value = get(key, List.class);
        if (value == null) {
            return defaultValue;
        }

        for (Object item : value) {
            if (!clazz.isAssignableFrom(item.getClass())) {
                throw new ClassCastException(format("List element cannot be cast to %s", clazz.getName()));
            }
        }
        return value;
    }

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
    public <T> T get(final Object key, final Class<T> clazz) {
        notNull("clazz", clazz);
        return clazz.cast(get(key));
    }

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
    public <T> T get(final Object key, final T defaultValue) {
        notNull("defaultValue", defaultValue);
        Object value = get(key);
        return value == null ? defaultValue : (T) value;
    }

}
