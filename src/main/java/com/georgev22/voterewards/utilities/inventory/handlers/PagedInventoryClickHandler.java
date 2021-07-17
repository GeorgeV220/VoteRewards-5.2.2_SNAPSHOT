package com.georgev22.voterewards.utilities.inventory.handlers;

import com.georgev22.voterewards.utilities.inventory.IPagedInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public abstract class PagedInventoryClickHandler extends PagedInventoryHandler {

    public abstract void handle(ClickHandler clickHandler);

    public static class ClickHandler extends Handler {

        private final InventoryClickEvent event;

        public ClickHandler(IPagedInventory iPagedInventory, InventoryClickEvent event) {
            super(iPagedInventory, event.getView(), (Player) event.getWhoClicked());
            this.event = event;
        }

        public InventoryAction getAction() {
            return event.getAction();
        }

        public ClickType getClick() {
            return event.getClick();
        }

        public ItemStack getCurrentItem() {
            return event.getCurrentItem();
        }

        public ItemStack getCursor() {
            return event.getCursor();
        }

        public int getHotbarButton() {
            return event.getHotbarButton();
        }

        public int getRawSlot() {
            return event.getRawSlot();
        }

        public int getSlot() {
            return event.getSlot();
        }

        public InventoryType.SlotType getSlotType() {
            return event.getSlotType();
        }

        public boolean isLeftClick() {
            return event.isLeftClick();
        }

        public boolean isMiddleClick() {
            return event.getClick().equals(ClickType.MIDDLE);
        }

        public boolean isRightClick() {
            return event.isRightClick();
        }

        public boolean isShiftClick() {
            return event.isShiftClick();
        }

        public boolean isCancelled(boolean b) {
            return event.isCancelled();
        }

        public void setCurrentItem(ItemStack itemStack) {
            event.setCurrentItem(itemStack);
        }

        public void setCancelled(boolean b) {
            event.setCancelled(b);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof ClickHandler))
                return false;

            ClickHandler clickHandler = (ClickHandler) obj;
            return super.equals(obj) && event.equals(clickHandler.event);
        }
    }

}
