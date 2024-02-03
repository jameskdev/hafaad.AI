package com.xm.aeclient;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class ImageGeneratorFragment extends Fragment {
    private static final String LOG_TAG = "ImageGeneratorFragment";
    AEViewModel avm;

    public ImageGeneratorFragment() {
    }

    public static ImageGeneratorFragment newInstance() {
        ImageGeneratorFragment fragment = new ImageGeneratorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_image_generator, container, false);
        avm = new ViewModelProvider(requireActivity()).get(AEViewModel.class);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.submit_btn) {
                    onClickGetBtn();
                } else if (view.getId() == R.id.save_img_button) {
                    onClickSaveBtn();
                } else {
                    Log.e(LOG_TAG, "Unknown click event occurred!");
                }
            }

            public void onClickGetBtn() {
                ArrayList<String> mPrompt = new ArrayList<>();
                ArrayList<Integer> mWeight = new ArrayList<>();
                mPrompt.add(((android.widget.EditText) v.findViewById(R.id.prompt_textbox)).getText().toString());
                mWeight.add(1);
                String mapi_keys = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("api_key", "");
                String cfg_scale = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("cfg_scale_pref", "7");
                String clip_guidance = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("clip_guidance_preset_pref", "NONE");
                String sampler_set = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("sampler_pref", "AUTO");
                String style_preset = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("style_preset_pref", "auto");
                String no_of_samples = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("no_of_samples_pref", "1");
                String steps_no = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("steps_pref", "30");
                String seed_no = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("seed_pref", "0");
                boolean r = avm.createGetRequest(cfg_scale, clip_guidance, sampler_set, style_preset, no_of_samples, steps_no, seed_no, mPrompt, mWeight, mapi_keys);
                if (!r) {
                    Toast.makeText(requireContext(), "Please set the API Keys first!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            public void onClickSaveBtn() {
                boolean res = false;
                try {
                    String mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).
                            getCanonicalPath() + "/" + String.valueOf(LocalDateTime.now().
                            toEpochSecond(ZoneOffset.UTC)) + ".png";
                    res = avm.saveImage(mPath, 0);
                    if (!res) {
                        Toast.makeText(requireContext(), "Error occurred while saving file!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Successfully saved file to: " + mPath,
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException ie) {
                    Toast.makeText(requireContext(), "Failed to get file path!",
                            Toast.LENGTH_SHORT).show();
                }
            }

        };

        avm.getAEStatus().observe(getViewLifecycleOwner(), aeJobStatus -> {
            if (aeJobStatus.isCurrentlyAtWork()) {
                ((Button) v.findViewById(R.id.submit_btn)).setEnabled(false);
            } else {
                ((Button) v.findViewById(R.id.submit_btn)).setEnabled(true);
            }
            if (aeJobStatus.isImgChanged()) {
                if(aeJobStatus.getNumOfImages() == 0) {
                    ((ImageView) v.findViewById(R.id.show_image)).setImageDrawable(AppCompatResources.getDrawable(requireContext(),
                            R.drawable.ic_launcher_background));
                    avm.setCurrentImgPos(-1);
                } else {
                    ((ImageView) v.findViewById(R.id.show_image)).
                            setImageBitmap(BitmapFactory.decodeByteArray(avm.getImage(0).getImageData(),
                                    0, avm.getImage(0).getImageData().length));
                    avm.setCurrentImgPos(0);
                }
                if (!aeJobStatus.isCurrentlyAtWork()) {
                    avm.ackResult();
                }
            }
            if (!aeJobStatus.getResMessage().isEmpty()) {
                if (aeJobStatus.getIsError() == 0) {
                    Toast.makeText(this.requireContext(), aeJobStatus.getResMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder a = new AlertDialog.Builder(requireActivity());
                    a.setTitle("Error").
                            setMessage(aeJobStatus.getResMessage()).setCancelable(true).
                            setPositiveButton("OK", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }

                            }).create().show();
                }
            }
        });

        ((Button) v.findViewById(R.id.submit_btn)).setOnClickListener(ocl);
        ((Button) v.findViewById(R.id.save_img_button)).setOnClickListener(ocl);
        ((EditText) v.findViewById(R.id.prompt_textbox)).setText(avm.getCurrentPrompt());
        if (avm.getCurrentImgPos() > -1) {
            ((ImageView) v.findViewById(R.id.show_image)).
                    setImageBitmap(BitmapFactory.decodeByteArray(avm.getImage(avm.getCurrentImgPos()).getImageData(),
                            0, avm.getImage(0).getImageData().length));
        }

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        avm.setCurrentPrompt(((EditText)requireView().findViewById(R.id.prompt_textbox)).
                getText().toString());
    }
}