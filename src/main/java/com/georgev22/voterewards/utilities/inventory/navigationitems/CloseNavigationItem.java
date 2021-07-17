package com.georgev22.voterewards.utilities.inventory.navigationitems;

import com.georgev22.voterewards.utilities.inventory.NavigationType;
import org.bukkit.inventory.ItemStack;

public final class CloseNavigationItem extends NavigationItemCloneable {

    public CloseNavigationItem(ItemStack itemStack) {
        this(itemStack, 4);
    }

    public CloseNavigationItem(ItemStack itemStack, int slot) {
        super(itemStack, slot);
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.CLOSE;
    }

    @Override
    public CloseNavigationItem clone() {
        return new CloseNavigationItem(getItemStack().clone());
    }
}
