package com.georgev22.voterewards.utilities.colors;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Animation {

    private static final ObjectMap<String, List<String>> animationCache = ObjectMap.newHashObjectMap();

    public static String wave(String string, Color... colors) {
        return wave(string, true, 5, 10, colors);
    }

    public static String wave(String string, boolean bold, int start, int end, Color... colors) {
        Preconditions.checkArgument(colors.length > 1, "Not enough colors provided");
        String str = "wave-" + string + "-" + bold + "-" + start + "-" + end + "-" + Arrays.stream(colors).map(Color::getColorCode).collect(Collectors.joining("-"));
        if (animationCache.containsKey(str)) {
            return currentFrame(animationCache.get(str));
        } else {
            ArrayList<String> frames = Lists.newArrayList();
            int i = 0;
            int var9 = colors.length;

            for (Color color : colors) {
                Color color2 = colors[colors.length == i + 1 ? 0 : i + 1];
                frames.addAll(Collections.nCopies(start, color.getAppliedTag() + (bold ? "§l" : "") + string));
                ArrayList<String> list = Lists.newArrayList();
                list.addAll(Collections.nCopies(string.length(), color.getAppliedTag()));
                list.addAll(ColorCalculations.getColorsInBetween(color, color2, end).stream().map(Color::getAppliedTag).collect(Collectors.toList()));
                list.addAll(Collections.nCopies(string.length(), color2.getAppliedTag()));

                for (int j = 0; j <= list.size() - string.length(); ++j) {
                    StringBuilder stringBuilder = new StringBuilder();
                    byte b2 = 0;
                    char[] var17 = string.toCharArray();
                    int var18 = var17.length;

                    for (char c : var17) {
                        String str1 = list.get(b2 + j);
                        stringBuilder.append(str1).append(bold ? "§l" : "").append(c);
                        ++b2;
                    }

                    frames.add(stringBuilder.toString());
                }

                frames.addAll(Collections.nCopies(start, color2.getAppliedTag() + (bold ? "§l" : "") + string));
                ++i;
            }

            animationCache.put(str, frames);
            return currentFrame(frames);
        }
    }

    public static String fading(String string, Color... colors) {
        return fading(string, true, 10, 20, colors);
    }

    public static String fading(String string, boolean bold, int start, int end, Color... colors) {
        Preconditions.checkArgument(colors.length > 1, "Not enough colors provided");
        String str = "fading-" + string + "-" + bold + "-" + start + "-" + end + "-" + Arrays.stream(colors).map(Color::getColorCode).collect(Collectors.joining("-"));
        if (animationCache.containsKey(str)) {
            return currentFrame(animationCache.get(str));
        } else {
            ArrayList<String> frames = Lists.newArrayList();
            int i = 0;
            int var9 = colors.length;

            for (Color color : colors) {
                Color color2 = colors[colors.length == i + 1 ? 0 : i + 1];
                frames.addAll(Collections.nCopies(start, color.getAppliedTag() + (bold ? "§l" : "") + string));

                for (Color color3 : ColorCalculations.getColorsInBetween(color, color2, end)) {
                    frames.add(color3.getAppliedTag() + (bold ? "§l" : "") + string);
                }

                ++i;
            }

            animationCache.put(str, frames);
            return currentFrame(frames);
        }
    }

    private static String currentFrame(List<String> frames) {
        long l = System.currentTimeMillis() / 50L;
        int i = (int) (l % (long) frames.size());
        return frames.get(i);
    }
}
