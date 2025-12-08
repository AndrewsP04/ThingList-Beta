package com.example.thinglistbeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Very simple in-memory storage for items created in the app.
 * This is NOT persistent – it resets when the app process is killed,
 * but it's perfect for your class demo.
 */
public class ItemRepository {

    private static final List<ThingItem> ITEMS = new ArrayList<>();

    public static void addItem(ThingItem item) {
        if (item != null) {
            ITEMS.add(item);
        }
    }

    public static List<ThingItem> getItems() {
        // return a copy so callers don't accidentally modify internal list
        return new ArrayList<>(ITEMS);
    }

    public static void clear() {
        ITEMS.clear();
    }
}
