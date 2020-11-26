package com.georgev22.voterewards.utilities;

import com.georgev22.voterewards.VoteRewardPlugin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class Utils {

    private static VoteRewardPlugin m = VoteRewardPlugin.getInstance();

    private Utils() {
        throw new AssertionError();
    }

    public static String convertSeconds(long input, String secondInput, String secondsInput, String minuteInput,
                                        String minutesInput, String hourInput, String hoursInput, String dayInput, String daysInput,
                                        String invalidInput) {
        if (input < 0) {
            Utils.printMsg(
                    "An attempt to convert a negative number was made for: " + input + ", making the number absolute.");
            input = Math.abs(input);
        }

        final StringBuilder builder = new StringBuilder();

        boolean comma = false;

        /* Days */
        final long days = TimeUnit.SECONDS.toDays(input);
        if (days > 0) {
            builder.append(days + " " + (days == 1 ? dayInput : daysInput));
            comma = true;
        }

        /* Hours */
        final long hours = (TimeUnit.SECONDS.toHours(input) - TimeUnit.DAYS.toHours(days));
        if (hours > 0) {
            if (comma) {
                builder.append(", ");
            }
            builder.append(hours + " " + (hours == 1 ? hourInput : hoursInput));
            comma = true;
        }

        /* Minutes */
        final long minutes = (TimeUnit.SECONDS.toMinutes(input) - TimeUnit.HOURS.toMinutes(hours)
                - TimeUnit.DAYS.toMinutes(days));
        if (minutes > 0) {
            if (comma) {
                builder.append(", ");
            }
            builder.append(minutes + " " + (minutes == 1 ? minuteInput : minutesInput));
            comma = true;
        }

        /* Seconds */
        final long seconds = (TimeUnit.SECONDS.toSeconds(input) - TimeUnit.MINUTES.toSeconds(minutes)
                - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days));
        if (seconds > 0) {
            if (comma) {
                builder.append(", ");
            }
            builder.append(seconds + " " + (seconds == 1 ? secondInput : secondsInput));
        }

        /* Result */
        final String result = builder.toString();
        return result.equals("") ? invalidInput : result;
    }

    // public static String convertSeconds(long input) {
    // return convertSeconds(input, "second", "seconds", "minute", "minutes",
    // "hour", "hours", "day", "days",
    // "invalid time");
    // }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */
    public static boolean isLong(final String input) {
        return Longs.tryParse(StringUtils.deleteWhitespace(input)) != null;
    }

    public static boolean isDouble(final String input) {
        return Doubles.tryParse(StringUtils.deleteWhitespace(input)) != null;
    }

    public static boolean isInt(final String input) {
        return Ints.tryParse(StringUtils.deleteWhitespace(input)) != null;
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */
    public static boolean isList(final FileConfiguration file, final String path) {
        return isList(file.get(path));
    }

    public static boolean isList(final Object obj) {
        return obj instanceof List;
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */

    public static void broadcastMsg(final String input) {
        Bukkit.broadcastMessage(colorize(input));
    }

    public static void broadcastMsg(final List<String> input) {
        input.forEach(Utils::broadcastMsg);
    }

    public static void broadcastMsg(final Object input) {
        broadcastMsg(String.valueOf(input));
    }

    public static void printMsg(final String input) {
        Bukkit.getConsoleSender().sendMessage(colorize(input));
    }

    public static void printMsg(final List<String> input) {
        input.forEach(Utils::printMsg);
    }

    public static void printMsg(final Object input) {
        printMsg(String.valueOf(input));
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */

    public static void msg(final CommandSender target, final String message) {
        Validate.notNull(target, "The target can't be null");
        if (message == null) {
            return;
        }
        target.sendMessage(colorize(message));
    }

    public static void msg(final CommandSender target, final String... message) {
        Validate.notNull(target, "The target can't be null");
        if (message == null || message.length == 0) {
            return;
        }
        Validate.noNullElements(message, "The string array can't have null elements.");
        target.sendMessage(colorize(message));
    }

    public static void msg(final CommandSender target, final List<String> message) {
        Validate.notNull(target, "The target can't be null");
        if (message == null || message.isEmpty()) {
            return;
        }
        Validate.noNullElements(message, "The list can't have null elements.");
        msg(target, message.stream().toArray(String[]::new));
    }

    /* ----------------------------------------------------------------- */

    public static void msg(final CommandSender target, final String message, final Map<String, String> map,
                           final boolean ignoreCase) {
        msg(target, placeHolder(message, map, ignoreCase));
    }

    public static void msg(final CommandSender target, final List<String> message, final Map<String, String> map,
                           final boolean ignoreCase) {
        msg(target, placeHolder(message, map, ignoreCase));
    }

    public static void msg(final CommandSender target, final String[] message, final Map<String, String> map,
                           final boolean ignoreCase) {
        msg(target, placeHolder(message, map, ignoreCase));
    }

    public static void msg(final CommandSender target, final FileConfiguration file, final String path) {
        msg(target, file, path, null, false);
    }

    public static void msg(final CommandSender target, final FileConfiguration file, final String path,
                           final Map<String, String> map, final boolean replace) {
        Validate.notNull(file, "The file can't be null");
        Validate.notNull(file, "The path can't be null");

        if (!file.isSet(path)) {
            throw new IllegalArgumentException("The path: " + path + " doesn't exist.");
        }

        if (isList(file, path)) {
            msg(target, file.getStringList(path), map, replace);
        } else {
            msg(target, file.getString(path), map, replace);
        }
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */

    public static String placeHolder(String str, final Map<String, String> map, final boolean ignoreCase) {
        Validate.notNull(str, "The string can't be null!");
        if (map == null) {
            return new String(str);
        }
        for (final Entry<String, String> entr : map.entrySet()) {
            str = ignoreCase ? replaceIgnoreCase(str, entr.getKey(), entr.getValue())
                    : str.replace(entr.getKey(), entr.getValue());
        }
        return new String(str);
    }

    private static String replaceIgnoreCase(final String text, String searchString, final String replacement) {

        if (text == null || text.length() == 0) {
            return text;
        }
        if (searchString == null || searchString.length() == 0) {
            return text;
        }
        if (replacement == null) {
            return text;
        }

        int max = -1;

        final String searchText = text.toLowerCase();
        searchString = searchString.toLowerCase();
        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= 16;

        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }
        return buf.append(text, start, text.length()).toString();
    }

    public static String[] placeHolder(final String[] array, final Map<String, String> map, final boolean ignoreCase) {
        Validate.notNull(array, "The string array can't be null!");
        Validate.noNullElements(array, "The string array can't have null elements!");
        final String[] newarr = Arrays.copyOf(array, array.length);
        if (map == null) {
            return newarr;
        }
        for (int i = 0; i < newarr.length; i++) {
            newarr[i] = placeHolder(newarr[i], map, ignoreCase);
        }
        return newarr;
    }

    public static List<String> placeHolder(final List<String> coll, final Map<String, String> map,
                                           final boolean ignoreCase) {
        Validate.notNull(coll, "The string collection can't be null!");
        Validate.noNullElements(coll, "The string collection can't have null elements!");
        return map == null ? coll
                : coll.stream().map(str -> placeHolder(str, map, ignoreCase)).collect(Collectors.toList());
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */

    /**
     * Returns a translated string.
     *
     * @param msg The message to be translated
     * @return A translated message
     */
    public static String colorize(final String msg) {
        Validate.notNull(msg, "The string can't be null!");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Returns a translated string array.
     *
     * @return A translated message array
     */
    public static String[] colorize(final String... array) {
        Validate.notNull(array, "The string array can't be null!");
        Validate.noNullElements(array, "The string array can't have null elements!");
        final String[] newarr = Arrays.copyOf(array, array.length);
        for (int i = 0; i < newarr.length; i++) {
            newarr[i] = colorize(newarr[i]);
        }
        return newarr;
    }

    /**
     * Returns a translated string collection.
     *
     * @param coll The collection to be translated
     * @return A translated message
     */
    public static List<String> colorize(final List<String> coll) {
        Validate.notNull(coll, "The string collection can't be null!");
        Validate.noNullElements(coll, "The string collection can't have null elements!");
        final List<String> newColl = Lists.newArrayList(coll);
        newColl.replaceAll(Utils::colorize);
        return newColl;
    }

    /* ----------------------------------------------------------------- */

    //

    /* ----------------------------------------------------------------- */

    public static void debug(final JavaPlugin plugin, final Map<String, String> map, String... messages) {
        Utils.printMsg("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        Utils.printMsg("=");
        final String name, desc;
        name = plugin.getDescription().getName() + " ";
        desc = plugin.getDescription().getVersion();
        Utils.printMsg("           " + name + desc);
        Utils.printMsg("=");
        for (final String msg : messages) {
            Utils.printMsg(placeHolder(msg, map, false));
        }
        Utils.printMsg("=");
        Utils.printMsg("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
    }

    public static void debug(final JavaPlugin plugin, String... messages) {
        debug(plugin, null, messages);
    }

    public static void debug(final JavaPlugin plugin, List<String> messages) {
        debug(plugin, null, messages.toArray(new String[0]));
    }

    public static ItemStack[] getItems(final ItemStack item, int amount) {

        final int maxSize = item.getMaxStackSize();
        if (amount <= maxSize) {
            item.setAmount(Math.max(amount, 1));
            return new ItemStack[]{item};
        }
        final List<ItemStack> resultItems = Lists.newArrayList();
        do {
            item.setAmount(Math.min(amount, maxSize));
            resultItems.add(new ItemStack(item));
            amount = amount >= maxSize ? amount - maxSize : 0;
        } while (amount != 0);
        return resultItems.toArray(new ItemStack[0]);
    }

    private static String formatNumber(Locale lang, double input) {
        Validate.notNull(lang);
        return NumberFormat.getInstance(lang).format(input);
    }

    public static String formatNumber(double input) {
        return formatNumber(Locale.US, input);
    }

    public static String getProgressBar(double current, double max, int totalBars, String symbol, String completedColor,
                                        String notCompletedColor) {
        final double percent = (float) Math.min(current, max) / max;
        final int progressBars = (int) ((int) totalBars * percent);
        final int leftOver = totalBars - progressBars;

        final StringBuilder sb = new StringBuilder();

        sb.append(colorize(completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }

        sb.append(colorize(notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    /**
     * Add all players in a map
     *
     * @return Map
     */
    public static Map<String, Integer> getTopPlayersMap() {
        Map<String, Integer> top = Maps.newHashMap();
        if (m.database) {
            try {
                PreparedStatement s = m.getConnection().prepareStatement("SELECT * FROM `users`");
                ResultSet rs = s.executeQuery();
                while (rs.next()) {
                    top.put(rs.getString("name"), rs.getInt("votes"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return top;

        } else {
            File[] files = new File(VoteRewardPlugin.getInstance().getDataFolder(), "userdata").listFiles();

            for (File file : Objects.requireNonNull(files)) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                if (cfg.get("last-name") == null)
                    continue;
                top.put(cfg.getString("last-name"), cfg.getInt("total-votes"));
            }
            return top;
        }
    }

    /**
     * Get the top player
     *
     * @return top player's name
     */
    public static String getTopPlayer() {
        Map<String, Integer> result = getTopPlayersMap().entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder())).limit(1).collect(Collectors.toMap(
                        Entry::getKey, Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return String.valueOf(result.entrySet()).split("=")[0].replace("[", "");
    }

    /**
     *
     */
    public static Map<String, Integer> getTopPlayers(int limit) {
        return getTopPlayersMap().entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Entry::getKey, Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }


    public static String getTopPlayer(int number) {
        return String.valueOf(getTopPlayers(number).keySet().toArray()[number]);
    }

    /**
     * Get the greatest values in a map
     *
     * @param map
     * @param n
     * @param <K>
     * @param <V>
     * @return Map
     */
    public static <K, V extends Comparable<? super V>> List<Entry<K, V>> findGreatest(Map<K, V> map, int n) {
        Comparator<? super Entry<K, V>> comparator = (Comparator<Entry<K, V>>) (e0, e1) -> {
            V v0 = e0.getValue();
            V v1 = e1.getValue();
            return v1.compareTo(v0);
        };
        PriorityQueue<Entry<K, V>> highest = new PriorityQueue<>(n, comparator);
        for (Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }

        List<Entry<K, V>> result = new ArrayList<>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    public static ItemStack resetItemMeta(final ItemStack item) {
        final ItemStack copy = item.clone();
        copy.setItemMeta(Bukkit.getItemFactory().getItemMeta(copy.getType()));
        return copy;
    }


    public static String getArgs(String[] args, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = num; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        return sb.toString().trim();
    }

    public static String[] getArguments(String[] args, int num) {

        StringBuilder sb = new StringBuilder();
        for (int i = num; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }

        return sb.toString().trim().split(" ");
    }

    public static String[] reverse(String[] a) {
        List<String> list = Arrays.asList(a);
        Collections.reverse(list);
        return (String[]) list.toArray();
    }

}
