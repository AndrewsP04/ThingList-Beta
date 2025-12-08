package com.example.thinglistbeta;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class AddItemMethodFragment extends Fragment {

    // Option rows
    private LinearLayout optionTakePhoto;
    private LinearLayout optionScanBarcode;
    private LinearLayout optionUploadReceipt;
    private LinearLayout optionEnterManually;

    // Check icons on the right
    private ImageView iconTakePhoto;
    private ImageView iconScan;
    private ImageView iconUpload;
    private ImageView iconManual;

    // Current selected method: "photo", "barcode", "receipt", "manual"
    private String selectedMethod = "photo";  // default
    // Which method just launched the camera
    private String cameraMethod = null;

    // Camera launcher
    private ActivityResultLauncher<Intent> cameraLauncher;

    public AddItemMethodFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the camera result handler
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCameraResult
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_item_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Root option rows
        optionTakePhoto     = view.findViewById(R.id.optionTakePhoto);
        optionScanBarcode   = view.findViewById(R.id.optionScanBarcode);
        optionUploadReceipt = view.findViewById(R.id.optionUploadReceipt);
        optionEnterManually = view.findViewById(R.id.optionEnterManually);

        // Right-side checkboxes
        iconTakePhoto = view.findViewById(R.id.iconTakePhotoSelected);
        iconScan      = view.findViewById(R.id.iconScanSelected);
        iconUpload    = view.findViewById(R.id.iconUploadSelected);
        iconManual    = view.findViewById(R.id.iconManualSelected);

        // Selection clicks
        optionTakePhoto.setOnClickListener(v -> {
            selectedMethod = "photo";
            updateSelectionUi();
        });

        optionScanBarcode.setOnClickListener(v -> {
            selectedMethod = "barcode";
            updateSelectionUi();
        });

        optionUploadReceipt.setOnClickListener(v -> {
            selectedMethod = "receipt";
            updateSelectionUi();
        });

        optionEnterManually.setOnClickListener(v -> {
            selectedMethod = "manual";
            updateSelectionUi();
        });

        // Back arrow in top bar
        view.findViewById(R.id.btnBack).setOnClickListener(
                v -> requireActivity().onBackPressed()
        );

        // Check icon in top bar (Next)
        view.findViewById(R.id.btnNextFromMethod).setOnClickListener(v -> onNextClicked());

        // Initial visual state
        updateSelectionUi();
    }

    /** Called when the top-right check is tapped */
    private void onNextClicked() {
        switch (selectedMethod) {
            case "photo":
            case "barcode":
            case "receipt":
                cameraMethod = selectedMethod;
                launchCamera();
                break;

            case "manual":
            default:
                // No camera needed; go straight to Edit screen
                navigateToEditItem(null);
                break;
        }
    }

    /** Launches the camera */
    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(requireActivity().getPackageManager()) == null) {
            Toast.makeText(requireContext(),
                    "Camera not available on this device. Using manual entry instead.",
                    Toast.LENGTH_LONG).show();
            navigateToEditItem(null);
            return;
        }

        cameraLauncher.launch(intent);
    }

    /** Handles camera result, saves thumbnail, and passes path + autofill data */
    private void handleCameraResult(ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) {
            Toast.makeText(requireContext(),
                    "Camera cancelled",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = result.getData();
        Bitmap thumbnail = null;
        if (data != null && data.getExtras() != null) {
            Object extra = data.getExtras().get("data");
            if (extra instanceof Bitmap) {
                thumbnail = (Bitmap) extra;
            }
        }

        if (thumbnail == null) {
            Toast.makeText(requireContext(),
                    "No image data from camera",
                    Toast.LENGTH_SHORT).show();
            navigateToEditItem(null);
            return;
        }

        // Save bitmap to a temp file
        String imagePath = null;
        try {
            File cacheDir = requireContext().getCacheDir();
            File imgFile = new File(cacheDir,
                    "item_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imgFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            imagePath = imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Failed to save image",
                    Toast.LENGTH_SHORT).show();
        }

        // Build bundle for EditItemFragment
        Bundle autoFilled = new Bundle();

        if ("photo".equals(cameraMethod)) {
            autoFilled.putString("autoName", "Detected Item");
            autoFilled.putString("autoDescription", "Details extracted from photo.");
        } else if ("barcode".equals(cameraMethod)) {
            autoFilled.putString("autoBarcode", "1234567890123");
            autoFilled.putString("autoName", "Sample Barcode Item");
        } else if ("receipt".equals(cameraMethod)) {
            autoFilled.putString("autoName", "Receipt Item");
            autoFilled.putString("autoPrice", "19.99");
            autoFilled.putString("autoDescription", "Parsed from receipt image.");
        }

        autoFilled.putString("method", cameraMethod);
        if (imagePath != null) {
            autoFilled.putString("imagePath", imagePath);
        }

        navigateToEditItem(autoFilled);
    }

    /** Updates selected card background + checkmarks */
    private void updateSelectionUi() {

        // Reset backgrounds
        optionTakePhoto.setBackgroundResource(R.drawable.bg_method_option);
        optionScanBarcode.setBackgroundResource(R.drawable.bg_method_option);
        optionUploadReceipt.setBackgroundResource(R.drawable.bg_method_option);
        optionEnterManually.setBackgroundResource(R.drawable.bg_method_option);

        // Reset icons to unchecked
        iconTakePhoto.setImageResource(R.drawable.outline_check_box_outline_blank_24);
        iconScan.setImageResource(R.drawable.outline_check_box_outline_blank_24);
        iconUpload.setImageResource(R.drawable.outline_check_box_outline_blank_24);
        iconManual.setImageResource(R.drawable.outline_check_box_outline_blank_24);

        // Apply selected state
        switch (selectedMethod) {
            case "photo":
                optionTakePhoto.setBackgroundResource(R.drawable.bg_method_option_selected);
                iconTakePhoto.setImageResource(R.drawable.outline_check_box_24);
                break;

            case "barcode":
                optionScanBarcode.setBackgroundResource(R.drawable.bg_method_option_selected);
                iconScan.setImageResource(R.drawable.outline_check_box_24);
                break;

            case "receipt":
                optionUploadReceipt.setBackgroundResource(R.drawable.bg_method_option_selected);
                iconUpload.setImageResource(R.drawable.outline_check_box_24);
                break;

            case "manual":
                optionEnterManually.setBackgroundResource(R.drawable.bg_method_option_selected);
                iconManual.setImageResource(R.drawable.outline_check_box_24);
                break;
        }
    }

    /** Navigates to EditItemFragment, optionally with autofill + imagePath */
    private void navigateToEditItem(@Nullable Bundle args) {
        Fragment edit = new EditItemFragment();
        if (args != null) {
            edit.setArguments(args);
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, edit)
                .addToBackStack(null)
                .commit();
    }
}
