package com.georgev22.voterewards.utilities.colors;

import com.georgev22.voterewards.utilities.MinecraftVersion;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Color {

    private final String colorCode;
    private final int r;
    private final int g;
    private final int b;

    public static Color from(String colorCode) {
        return new Color(colorCode);
    }

    public static Color from(int r, int g, int b) {
        java.awt.Color color = new java.awt.Color(r, g, b);
        return from(Integer.toHexString(color.getRGB()).substring(2));
    }

    private Color(String colorCode) {
        this.colorCode = colorCode.replace("#", "");
        java.awt.Color color = new java.awt.Color(Integer.parseInt(this.colorCode, 16));
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public int getRed() {
        return this.r;
    }

    public int getGreen() {
        return this.g;
    }

    public int getBlue() {
        return this.b;
    }

    public String getAppliedTag() {
        boolean bool = MinecraftVersion.getCurrentVersion().isAboveOrEqual(MinecraftVersion.V1_16_R1);
        return bool ? "§x" + Arrays.stream(this.colorCode.split("")).map((paramString) -> "§" + paramString).collect(Collectors.joining()) : MinecraftColor.getClosest(this).getAppliedTag();
    }

    public String getColorTag() {
        return "{#" + this.colorCode + "}";
    }

    public String getTag() {
        return "#" + this.colorCode;
    }

    public static int difference(Color color1, Color color2) {
        return Math.abs(color1.r - color2.r) + Math.abs(color1.g - color2.g) + Math.abs(color1.b - color2.b);
    }

    public String toString() {
        return this.getAppliedTag();
    }
}
