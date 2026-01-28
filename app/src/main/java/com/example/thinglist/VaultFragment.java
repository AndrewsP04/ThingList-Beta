package com.example.thinglist;   // <-- change if your package is different

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VaultFragment extends Fragment {

    private TextView tvTotalItems;
    private TextView tvTotalValue;
    private TextView tvItemCount;
    private ChipGroup chipGroupCategories;
    private LinearLayout layoutVaultItems;

    // our in-memory data source (later you can replace with DB / API)
    private final List<VaultItem> allItems = new ArrayList<>();

    public VaultFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vault, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalItems       = view.findViewById(R.id.tvTotalItems);
        tvTotalValue       = view.findViewById(R.id.tvTotalValue);
        tvItemCount        = view.findViewById(R.id.tvItemCount);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        layoutVaultItems    = view.findViewById(R.id.layoutVaultItems);

        // 1) Load your items (right now hard-coded sample data)
        seedItems();

        // 2) Update header with TOTALS for all items
        updateHeaderTotals();

        // 3) Show list filtered by "All"
        updateList("All");

        // 4) Filter when chips change
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String category = "All";
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipDocuments)      category = "Documents";
                else if (id == R.id.chipJewelry)   category = "Jewelry";
                else if (id == R.id.chipCash)      category = "Cash";
                else if (id == R.id.chipElectronics) category = "Electronics";
                else if (id == R.id.chipArt)       category = "Art";
                else if (id == R.id.chipOther)     category = "Other";
            }
            updateList(category);        // list + "X items" label
            updateHeaderTotals();        // header always reflects ALL items
        });
    }

    /** Replace this later with real DB/API data */
    private void seedItems() {
        allItems.clear();

        allItems.add(new VaultItem(
                "Passport (John Doe)",
                "Valid until 2029",
                "Documents",
                "Section A",
                "3/4/2024",
                0
        ));
        allItems.add(new VaultItem(
                "Emergency Cash Reserve",
                "Emergency fund -\nmixed denominations",
                "Cash",
                "Compartment C",
                "1/31/2024",
                5000
        ));
        allItems.add(new VaultItem(
                "Silver Coins Collection",
                "Rare silver dollar collection\n(24 coins)",
                "Cash",
                "Compartment D",
                "1/19/2024",
                2400
        ));
        allItems.add(new VaultItem(
                "Gold Wedding Band",
                "18K gold wedding band,\ncustom engraved",
                "Jewelry",
                "Drawer 1A",
                "1/14/2024",
                3500
        ));
        allItems.add(new VaultItem(
                "Diamond Earrings",
                "2ct diamond stud earrings,\nplatinum setting",
                "Jewelry",
                "Drawer 1B",
                "12/9/2023",
                8200
        ));
        allItems.add(new VaultItem(
                "Property Deed",
                "Original property deed for 123 Main Street",
                "Documents",
                "Section B",
                "11/21/2023",
                0
        ));
        allItems.add(new VaultItem(
                "Vintage Rolex",
                "Rolex Submariner\n1960s vintage",
                "Jewelry",
                "Watch Box",
                "9/17/2023",
                15000
        ));
        allItems.add(new VaultItem(
                "Birth Certificates",
                "Family birth certificates (3)",
                "Documents",
                "Section A",
                "8/29/2023",
                0
        ));
    }

    /** Header = TOTAL items + TOTAL value (ignores filters) */
    private void updateHeaderTotals() {
        // total number of items
        tvTotalItems.setText(String.valueOf(allItems.size()));

        // sum of all item values
        double sum = 0;
        for (VaultItem item : allItems) {
            sum += item.value;
        }
        tvTotalValue.setText(formatCurrency(sum));
    }

    /** List + "X items" label = depends on current filter */
    private void updateList(String categoryFilter) {
        layoutVaultItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        List<VaultItem> filtered = new ArrayList<>();

        for (VaultItem item : allItems) {
            if (!"All".equals(categoryFilter)
                    && !item.category.equalsIgnoreCase(categoryFilter)) {
                continue;
            }
            filtered.add(item);

            View card = inflater.inflate(R.layout.item_vault_card,
                    layoutVaultItems, false);

            TextView tvTitle    = card.findViewById(R.id.tvItemTitle);
            TextView tvSubtitle = card.findViewById(R.id.tvItemSubtitle);
            TextView tvCategory = card.findViewById(R.id.tvItemCategoryTag);
            TextView tvLocation = card.findViewById(R.id.tvItemLocation);
            TextView tvDate     = card.findViewById(R.id.tvItemDate);
            TextView tvValue    = card.findViewById(R.id.tvItemValue);

            tvTitle.setText(item.title);
            tvSubtitle.setText(item.subtitle);
            tvCategory.setText(item.category);
            tvLocation.setText("📍 " + item.location);
            tvDate.setText(item.date);

            if (item.value > 0) {
                tvValue.setVisibility(View.VISIBLE);
                tvValue.setText(formatCurrency(item.value));
            } else {
                tvValue.setVisibility(View.GONE);
            }

            layoutVaultItems.addView(card);
        }

        // "X items" label uses filtered list length
        tvItemCount.setText(filtered.size() + " items");
    }

    private String formatCurrency(double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return format.format(value);
    }

    // simple data model
        private record VaultItem(String title, String subtitle, String category, String location,
                                 String date, double value) {
    }
}
