package com.georgev22.voterewards.utilities.inventory.navigationitems;

import com.georgev22.voterewards.utilities.inventory.NavigationType;
import org.bukkit.inventory.ItemStack;

public final class NextNavigationItem extends NavigationItemCloneable {

    public NextNavigationItem(ItemStack itemStack) {
        this(itemStack, 8);
    }

    public NextNavigationItem(ItemStack itemStack, int slot) {
        super(itemStack, slot);
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.NEXT;
    }

    @Override
    public NextNavigationItem clone() {
        return new NextNavigationItem(getItemStack().clone());
    }
}
