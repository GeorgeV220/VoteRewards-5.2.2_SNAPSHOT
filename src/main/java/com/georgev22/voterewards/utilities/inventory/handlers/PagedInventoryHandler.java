package com.georgev22.voterewards.utilities.inventory.handlers;

import com.georgev22.voterewards.utilities.inventory.IPagedInventory;
import com.georgev22.voterewards.utilities.inventory.PageModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public abstract class PagedInventoryHandler {

    PagedInventoryHandler() {
    }

    public static class Handler {

        private final IPagedInventory iPagedInventory;
        private final PageModifier pageModifier;
        private final Player player;

        Handler(IPagedInventory iPagedInventory, InventoryView inventoryView, Player player) {
            this.iPagedInventory = iPagedInventory;
            this.pageModifier = new PageModifier(inventoryView.getTopInventory());
            this.player = player;
        }

        public IPagedInventory getPagedInventory() {
            return iPagedInventory;
        }

        public PageModifier getPageModifier() {
            return pageModifier;
        }

        public Player getPlayer() {
            return player;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Handler handler))
                return false;

            return iPagedInventory.equals(handler.iPagedInventory)
                    && pageModifier.equals(handler.pageModifier)
                    && player.getUniqueId().equals(handler.player.getUniqueId());
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

}
