package com.georgev22.voterewards.utilities.colors;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class ColorCalculations {

    public static List<Color> getColorsInBetween(Color color, Color color2, int n) {
        double n2 = (double) (color2.getRed() - color.getRed()) / (double) n;
        double n3 = (double) (color2.getGreen() - color.getGreen()) / (double) n;
        double n4 = (double) (color2.getBlue() - color.getBlue()) / (double) n;
        ArrayList<Color> list = Lists.newArrayList();

        for (int i = 1; i <= n; ++i) {
            list.add(Color.from((int) Math.round((double) color.getRed() + n2 * (double) i), (int) Math.round((double) color.getGreen() + n3 * (double) i), (int) Math.round((double) color.getBlue() + n4 * (double) i)));
        }

        return list;
    }
}
