package com.example.thinglist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ItemDetailFragment extends Fragment {

    private static final String ARG_NAME        = "arg_name";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_PRICE       = "arg_price";
    private static final String ARG_LOCATION    = "arg_location";
    private static final String ARG_STATUS      = "arg_status";
    private static final String ARG_IMAGE_PATH  = "arg_image_path";

    private String name;
    private String description;
    private String price;
    private String location;
    private String status;
    private String imagePath;

    public ItemDetailFragment() { }

    public static ItemDetailFragment newInstance(String name,
                                                 String description,
                                                 String price,
                                                 String location,
                                                 String status,
                                                 @Nullable String imagePath) {
        ItemDetailFragment f = new ItemDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_NAME, name);
        b.putString(ARG_DESCRIPTION, description);
        b.putString(ARG_PRICE, price);
        b.putString(ARG_LOCATION, location);
        b.putString(ARG_STATUS, status);
        b.putString(ARG_IMAGE_PATH, imagePath);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            name        = args.getString(ARG_NAME, "");
            description = args.getString(ARG_DESCRIPTION, "");
            price       = args.getString(ARG_PRICE, "");
            location    = args.getString(ARG_LOCATION, "");
            status      = args.getString(ARG_STATUS, "Active");
            imagePath   = args.getString(ARG_IMAGE_PATH, null);
        } else {
            status = "Active";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 🔑 THIS WAS MISSING – actually inflate the layout
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack   = view.findViewById(R.id.btnBackDetail);
        ImageButton btnEdit   = view.findViewById(R.id.btnEditDetail);
        Button btnClaim       = view.findViewById(R.id.btnGenerateClaim);
        Button btnMarkLost    = view.findViewById(R.id.btnMarkLost);

        ImageView ivPhoto      = view.findViewById(R.id.ivDetailPhoto);
        TextView tvName        = view.findViewById(R.id.tvDetailName);
        TextView tvStatus      = view.findViewById(R.id.tvStatus);
        TextView tvDescription = view.findViewById(R.id.tvDetailDescription);
        TextView tvPrice       = view.findViewById(R.id.tvDetailPrice);
        TextView tvLocation    = view.findViewById(R.id.tvDetailLocation);

        // Bind data
        tvName.setText(isEmpty(name) ? "Unnamed item" : name);
        tvStatus.setText(isEmpty(status) ? "Active" : status);
        tvDescription.setText(description);
        tvPrice.setText(isEmpty(price) ? "Purchase Price:" : price);
        tvLocation.setText(isEmpty(location) ? "Location:" : location);

        // Bind image
        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            if (bmp != null) {
                ivPhoto.setImageBitmap(bmp);
            }
        }

        // Tap to preview image fullscreen-ish
        ivPhoto.setOnClickListener(v -> showImagePreview());

        // Back
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Edit – go back into EditItemFragment with current data (simple version)
        btnEdit.setOnClickListener(v -> {
            Fragment edit = EditItemFragment.newInstance();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, edit)
                    .addToBackStack(null)
                    .commit();
        });

        // Stub buttons
        btnClaim.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Generate Claim Packet (TODO)",
                        Toast.LENGTH_SHORT).show()
        );

        btnMarkLost.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Marked as Lost/Damaged (TODO)",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void showImagePreview() {
        if (TextUtils.isEmpty(imagePath)) return;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        if (bmp == null) return;

        ImageView iv = new ImageView(requireContext());
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = (int) (16 * requireContext().getResources()
                .getDisplayMetrics().density);
        iv.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setView(iv)
                .setPositiveButton("Close", null)
                .show();
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
