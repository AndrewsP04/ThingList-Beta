package com.example.thinglist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditItemFragment extends Fragment {

    private EditText etName;
    private EditText etDescription;
    private EditText etPrice;
    private EditText etLocation;
    private ImageView ivItemPhoto;

    // current image file we’re using for this item
    private String imagePath;

    // launchers for camera / gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public EditItemFragment() { }

    public static EditItemFragment newInstance() {
        EditItemFragment fragment = new EditItemFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Camera result
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCameraResult
        );

        // Gallery result
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleGalleryResult
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName        = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice       = view.findViewById(R.id.etPrice);
        etLocation    = view.findViewById(R.id.etLocation);
        ivItemPhoto   = view.findViewById(R.id.ivItemPhoto);

        ImageButton btnBack = view.findViewById(R.id.btnBackEdit);
        ImageButton btnSave = view.findViewById(R.id.btnSaveItem);

        applyAutoFilledArgs();   // prefill fields + image from AddItemMethod

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnSave.setOnClickListener(v -> onSaveClicked());

        // 🔹 Clicking the photo:
        //  - if we already have an image -> open preview
        //  - if no image yet -> offer camera / gallery
        ivItemPhoto.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(imagePath)) {
                showImagePreview();
            } else {
                showPhotoSourceDialog();
            }
        });
    }

    // -------------------- autofill from AddItemMethod --------------------

    private void applyAutoFilledArgs() {
        Bundle args = getArguments();
        if (args == null) return;

        String method    = args.getString("method", "");
        String autoName  = args.getString("autoName", "");
        String autoDesc  = args.getString("autoDescription", "");
        String autoPrice = args.getString("autoPrice", "");
        imagePath        = args.getString("imagePath", null);

        if (!TextUtils.isEmpty(autoName))  etName.setText(autoName);
        if (!TextUtils.isEmpty(autoDesc))  etDescription.setText(autoDesc);
        if (!TextUtils.isEmpty(autoPrice)) etPrice.setText(autoPrice);

        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            if (bmp != null) {
                ivItemPhoto.setImageBitmap(bmp);
            }
        }

        if (!TextUtils.isEmpty(method) && getContext() != null) {
            String msg = null;
            switch (method) {
                case "photo":   msg = "Details pre-filled from photo.";   break;
                case "barcode": msg = "Details pre-filled from barcode."; break;
                case "receipt": msg = "Details pre-filled from receipt."; break;
            }
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // -------------------- Save button --------------------

    private void onSaveClicked() {
        String name  = safeText(etName);
        String desc  = safeText(etDescription);
        String price = safeText(etPrice);
        String loc   = safeText(etLocation);
        String status = "Active";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            etName.requestFocus();
            return;
        }

        // 🔹 Save to simple in-memory repo so Dashboard can show it
        ThingItem item = new ThingItem(name, desc, price, loc, status, imagePath);
        ItemRepository.addItem(item);

        // 🔹 Navigate to details, including imagePath
        Fragment detail = ItemDetailFragment.newInstance(
                name,
                desc,
                price,
                loc,
                status,
                imagePath
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, detail)
                .addToBackStack(null)
                .commit();
    }

    private String safeText(EditText et) {
        return et == null ? "" : et.getText().toString().trim();
    }

    // -------------------- Photo source when editing manually --------------------

    private void showPhotoSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add photo")
                .setItems(new CharSequence[]{"Take photo", "Choose from gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else if (which == 1) {
                        launchGallery();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) == null) {
            Toast.makeText(requireContext(),
                    "Camera not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        cameraLauncher.launch(intent);
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    // -------------------- Handle camera / gallery results --------------------

    private void handleCameraResult(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) return;

        Intent data = result.getData();
        Bitmap thumbnail = null;
        if (data != null && data.getExtras() != null) {
            Object extra = data.getExtras().get("data");
            if (extra instanceof Bitmap) {
                thumbnail = (Bitmap) extra;
            }
        }
        if (thumbnail == null) return;

        imagePath = saveBitmapToCache(thumbnail);
        if (imagePath != null) {
            ivItemPhoto.setImageBitmap(thumbnail);
        }
    }

    private void handleGalleryResult(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) return;

        Intent data = result.getData();
        if (data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                imagePath = saveBitmapToCache(bmp);
                if (imagePath != null) {
                    ivItemPhoto.setImageBitmap(bmp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveBitmapToCache(Bitmap bmp) {
        try {
            File cacheDir = requireContext().getCacheDir();
            File imgFile = new File(cacheDir,
                    "item_edit_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imgFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------- Simple image preview dialog --------------------

    private void showImagePreview() {
        if (TextUtils.isEmpty(imagePath)) return;

        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        if (bmp == null) return;

        ImageView iv = new ImageView(requireContext());
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setPadding(16, 16, 16, 16);

        new AlertDialog.Builder(requireContext())
                .setView(iv)
                .setPositiveButton("Close", null)
                .show();
    }
}
