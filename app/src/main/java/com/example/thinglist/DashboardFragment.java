package com.example.thinglist;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvItemCount;
    private TextView tvMonetaryValue;
    private TableLayout tableInventory;

    private List<InventoryItem> allItems;
    private List<InventoryItem> displayedItems;

    public DashboardFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvItemCount     = view.findViewById(R.id.tvItemCount);
        tvMonetaryValue = view.findViewById(R.id.tvMonetaryValue);
        tableInventory  = view.findViewById(R.id.tableInventory);

        MaterialButton btnSort   = view.findViewById(R.id.btnSort);
        MaterialButton btnFilter = view.findViewById(R.id.btnFilter);

        // Initial load: demo + user-added items
        refreshDataAndUi();

        btnSort.setOnClickListener(v -> showSortDialog());
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        // When you come back from Add/Edit/Detail, pull latest items
        refreshDataAndUi();
    }

    // -----------------------
    //  DATA + TABLE + TOTALS
    // -----------------------

    /** Rebuilds allItems from demo + repository, then refreshes the table and stats. */
    private void refreshDataAndUi() {
        allItems = buildInventoryFromDemoAndRepo();
        displayedItems = new ArrayList<>(allItems);
        populateTableAndTotals(displayedItems);
    }

    /** Combine hardcoded demo inventory with items from ItemRepository. */
    private List<InventoryItem> buildInventoryFromDemoAndRepo() {
        List<InventoryItem> list = new ArrayList<>();

        // 1) Demo data
        list.addAll(getDemoInventory());

        // 2) User-added items stored as ThingItem in ItemRepository
        List<ThingItem> repoItems = ItemRepository.getItems();
        for (ThingItem ti : repoItems) {
            if (ti == null) continue;

            String name        = ti.name;
            String description = ti.description;
            String priceStr    = ti.price;      // text from EditItem
            String status      = ti.status;     // use as type/category
            String imagePath   = ti.imagePath;  // 🔹 KEEP the photo path
            int quantity       = 1;             // you can extend later

            double priceVal = 0.0;
            if (priceStr != null) {
                String clean = priceStr.replace("$", "").replace(",", "").trim();
                try {
                    priceVal = Double.parseDouble(clean);
                } catch (NumberFormatException ignored) { }
            }

            list.add(new InventoryItem(
                    name != null ? name : "Untitled Item",
                    quantity,
                    status != null ? status : "Misc",
                    description != null ? description : "",
                    priceVal,
                    imagePath
            ));
        }

        return list;
    }

    private void populateTableAndTotals(List<InventoryItem> items) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        int totalQuantity = 0;
        double totalValue = 0.0;

        // Remove all data rows, keep header (index 0)
        int childCount = tableInventory.getChildCount();
        if (childCount > 1) {
            tableInventory.removeViews(1, childCount - 1);
        }

        int paddingDp = 4;
        int paddingPx = (int) (paddingDp * requireContext()
                .getResources().getDisplayMetrics().density);

        for (InventoryItem item : items) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Row click → open details for this item (with imagePath)
            row.setTag(item);
            row.setClickable(true);
            row.setOnClickListener(v -> {
                InventoryItem clicked = (InventoryItem) v.getTag();
                openItemDetail(clicked);
            });

            // Name
            row.addView(makeCell(item.name, false, paddingPx));

            // Qty
            row.addView(makeCell(String.valueOf(item.quantity), true, paddingPx));

            // Type
            row.addView(makeCell(item.type, false, paddingPx));

            // Description
            row.addView(makeCell(item.description, false, paddingPx));

            // Price (per item)
            row.addView(makeCell(currency.format(item.price), true, paddingPx));

            tableInventory.addView(row);

            totalQuantity += item.quantity;
            totalValue += item.price * item.quantity;
        }

        tvItemCount.setText(String.valueOf(totalQuantity));
        tvMonetaryValue.setText(currency.format(totalValue));
    }

    private TextView makeCell(String text, boolean alignEnd, int paddingPx) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        if (alignEnd) {
            tv.setGravity(Gravity.END);
        }
        return tv;
    }

    /**
     * Open ItemDetailFragment for a clicked row.
     * Demo items have imagePath = null, user-added ones have real paths.
     */
    private void openItemDetail(InventoryItem item) {
        String priceStr = String.format(Locale.getDefault(), "%.2f", item.price);

        String name        = item.name;
        String description = item.description;
        String price       = priceStr;
        String location    = "";              // extend later if needed
        String status      = item.type;       // use type/status
        String imagePath   = item.imagePath;  // 🔹 PASS THROUGH PHOTO PATH

        Fragment detail = ItemDetailFragment.newInstance(
                name,
                description,
                price,
                location,
                status,
                imagePath
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, detail)
                .addToBackStack(null)
                .commit();
    }

    // -----------------------
    //  SORT / FILTER
    // -----------------------

    private void showSortDialog() {
        String[] options = {
                "Name (A–Z)",
                "Quantity (High → Low)",
                "Type (A–Z)",
                "Price (High → Low)"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort by")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Name
                            Collections.sort(displayedItems,
                                    Comparator.comparing(item -> item.name.toLowerCase()));
                            break;
                        case 1: // Quantity desc
                            Collections.sort(displayedItems,
                                    (a, b) -> Integer.compare(b.quantity, a.quantity));
                            break;
                        case 2: // Type
                            Collections.sort(displayedItems,
                                    Comparator.comparing(item -> item.type.toLowerCase()));
                            break;
                        case 3: // Price desc
                            Collections.sort(displayedItems,
                                    (a, b) -> Double.compare(b.price, a.price));
                            break;
                    }
                    populateTableAndTotals(displayedItems);
                })
                .show();
    }

    private void showFilterDialog() {
        String[] options = {
                "All",
                "Electronics",
                "Stationery",
                "Kitchenware",
                "Furniture",
                "Sports",
                "Accessories",
                "Bags",
                "Misc"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by type")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];

                    // Always rebuild allItems so we include any newly added repo items
                    allItems = buildInventoryFromDemoAndRepo();
                    displayedItems.clear();

                    if ("All".equals(selected)) {
                        displayedItems.addAll(allItems);
                    } else {
                        for (InventoryItem item : allItems) {
                            if (item.type.equals(selected)) {
                                displayedItems.add(item);
                            }
                        }
                    }
                    populateTableAndTotals(displayedItems);
                })
                .show();
    }

    // -----------------------
    //  DEMO DATA
    // -----------------------

    private List<InventoryItem> getDemoInventory() {
        List<InventoryItem> list = new ArrayList<>();
        list.add(new InventoryItem("Wireless Mouse", 45, "Electronics",
                "Ergonomic wireless mouse with USB receiver", 29.99, null));
        list.add(new InventoryItem("Notebook Set", 120, "Stationery",
                "Pack of 3 ruled notebooks, A5 size", 12.50, null));
        list.add(new InventoryItem("Coffee Mug", 78, "Kitchenware",
                "Ceramic mug with heat-resistant handle", 8.99, null));
        list.add(new InventoryItem("USB-C Cable", 200, "Electronics",
                "6ft braided charging cable", 15.99, null));
        list.add(new InventoryItem("Desk Lamp", 34, "Furniture",
                "LED desk lamp with adjustable brightness", 45.00, null));
        list.add(new InventoryItem("Water Bottle", 92, "Sports",
                "Stainless steel insulated bottle, 32oz", 24.99, null));
        list.add(new InventoryItem("Keyboard", 28, "Electronics",
                "Mechanical keyboard with RGB lighting", 89.99, null));
        list.add(new InventoryItem("Sticky Notes", 150, "Stationery",
                "Colorful sticky notes, 400 sheets", 6.99, null));
        list.add(new InventoryItem("Phone Stand", 65, "Accessories",
                "Adjustable aluminum phone holder", 18.99, null));
        list.add(new InventoryItem("Backpack", 42, "Bags",
                "Laptop backpack with USB charging port", 55.00, null));
        list.add(new InventoryItem("Headphones", 56, "Electronics",
                "Wireless over-ear headphones with ANC", 129.99, null));
        list.add(new InventoryItem("Pen Set", 180, "Stationery",
                "Set of 10 ballpoint pens, black ink", 9.99, null));
        list.add(new InventoryItem("Monitor Stand", 38, "Furniture",
                "Wooden monitor riser with storage", 35.00, null));
        list.add(new InventoryItem("Yoga Mat", 71, "Sports",
                "Non-slip exercise mat with carrying strap", 32.50, null));
        list.add(new InventoryItem("Webcam", 25, "Electronics",
                "1080p HD webcam with built-in microphone", 69.99, null));

        return list;
    }

    // Simple model with imagePath
    private static class InventoryItem {
        String name;
        int quantity;
        String type;
        String description;
        double price;
        String imagePath;

        InventoryItem(String name,
                      int quantity,
                      String type,
                      String description,
                      double price,
                      String imagePath) {
            this.name = name;
            this.quantity = quantity;
            this.type = type;
            this.description = description;
            this.price = price;
            this.imagePath = imagePath;
        }
    }
}
