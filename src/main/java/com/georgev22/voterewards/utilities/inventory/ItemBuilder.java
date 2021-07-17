package com.georgev22.voterewards.utilities.inventory;

import com.georgev22.externals.utilities.maps.ObjectMap;
import com.georgev22.externals.xseries.XEnchantment;
import com.georgev22.externals.xseries.XMaterial;
import com.georgev22.voterewards.utilities.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private final ItemStack itemStack;
    private Material material;
    private Short durability;
    private String title;
    private int amount;
    private final List<String> lores;
    private final List<ItemFlag> flags;
    private final ObjectMap<Enchantment, Integer> enchantments;
    private boolean unbreakable;
    private int customModelData;

    public ItemBuilder(XMaterial material) {
        this(material.parseMaterial());
    }

    public ItemBuilder(XMaterial material, boolean showAllAttributes) {
        this(material.parseMaterial(), showAllAttributes);
    }

    public ItemBuilder(Material material) {
        this(material, true);
    }

    public ItemBuilder(Material material, boolean showAllAttributes) {
        this.amount = 1;
        this.lores = Lists.newArrayList();
        this.flags = Lists.newArrayList();
        this.enchantments = ObjectMap.newHashObjectMap();
        this.unbreakable = false;
        this.customModelData = -1;
        Preconditions.checkArgument(material != null, "ItemStack cannot be null");
        this.itemStack = new ItemStack(material);
        this.showAllAttributes(showAllAttributes);
    }

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack, true);
    }

    public ItemBuilder(ItemStack itemStack, boolean showAllAttributes) {
        this.amount = 1;
        this.lores = Lists.newArrayList();
        this.flags = Lists.newArrayList();
        this.enchantments = ObjectMap.newHashObjectMap();
        this.unbreakable = false;
        this.customModelData = -1;
        Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
        this.itemStack = itemStack;
        this.showAllAttributes(showAllAttributes);
    }

    public ItemBuilder material(XMaterial material) {
        this.material = material.parseMaterial();
        return this;
    }

    public ItemBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    public ItemBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder lores(List<String> lores) {
        this.lores.addAll(lores);
        return this;
    }

    public ItemBuilder lores(String... lores) {
        this.lores.addAll(Arrays.asList(lores));
        return this;
    }

    public ItemBuilder lore(String line) {
        this.lores.add(line);
        return this;
    }

    public ItemBuilder enchantment(XEnchantment enchantment, int level) {
        this.enchantment(enchantment.parseEnchantment(), level);
        return this;
    }

    public ItemBuilder enchantment(XEnchantment enchantment) {
        this.enchantment(enchantment.parseEnchantment());
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        this.enchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder clearLores() {
        this.lores.clear();
        return this;
    }

    public ItemBuilder clearEnchantments() {
        this.enchantments.clear();
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder customModelData(int value) {
        this.customModelData = value;
        return this;
    }

    public ItemBuilder skull(String owner) {
        if (this.itemStack.getItemMeta() != null && this.itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(owner);
            this.itemStack.setItemMeta(skullMeta);
        }

        return this;
    }

    public ItemBuilder showAllAttributes(boolean show) {
        if (!show) {
            this.flags.addAll(Arrays.asList(ItemFlag.values()));
        } else {
            this.flags.removeAll(Arrays.asList(ItemFlag.values()));
        }

        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (glow)
            return glow();
        else
            return this;
    }

    public ItemBuilder glow() {
        return enchantment(Enchantment.ARROW_FIRE, 1).showAllAttributes(false);
    }

    public ItemStack build() {
        ItemStack itemStack = this.itemStack;
        if (this.material != null) {
            itemStack.setType(this.material);
        }

        for (Enchantment enchantment : this.enchantments.keySet()) {
            itemStack.addUnsafeEnchantment(enchantment, this.enchantments.get(enchantment));
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasLore()) {
            meta.setLore(Lists.newArrayList());
        }
        if (this.unbreakable) {
            meta.setUnbreakable(true);
        }

        if (this.amount > 0) {
            itemStack.setAmount(this.amount);
        }

        if (this.durability != null) {
            itemStack.setDurability(this.durability);
        }

        if (this.title != null) {
            meta.setDisplayName(Utils.colorize(this.title));
        }

        if (this.lores != null && this.lores.size() > 0) {
            meta.setLore(Utils.colorize(this.lores));
        }

        if (this.flags != null && this.flags.size() > 0) {
            meta.addItemFlags(this.flags.toArray(new ItemFlag[0]));
        }

        if (this.customModelData > -1) {
            meta.setCustomModelData(this.customModelData);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemBuilder clone() throws CloneNotSupportedException {
        ItemBuilder clone = (ItemBuilder) super.clone();
        return new ItemBuilder(this.itemStack.clone(), false);
    }
}
