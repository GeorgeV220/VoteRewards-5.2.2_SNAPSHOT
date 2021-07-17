package com.georgev22.voterewards.utilities.inventory.handlers;

import com.georgev22.voterewards.utilities.inventory.IPagedInventory;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PagedInventoryCustomNavigationHandler extends PagedInventoryClickHandler.ClickHandler {

    public PagedInventoryCustomNavigationHandler(IPagedInventory iPagedInventory, InventoryClickEvent event) {
        super(iPagedInventory, event);
    }

}
