package com.dystudio.criminalintent;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailDialogFragment extends DialogFragment {

    private ImageView mImageView;
    private static final String ARG_FILE = "file";

    public static DetailDialogFragment newInstance(File photoFile) {
        // Required empty public constructor
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILE, photoFile);
        DetailDialogFragment fragment = new DetailDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        File photoFile = (File) getArguments().getSerializable(ARG_FILE);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_detail_dialog, null);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        Log.d("abc","w:H"+width+":"+height);

        mImageView = view.findViewById(R.id.dialog_image_detail);

        ViewTreeObserver observer =
                mImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            if (mImageView.isShown()) {
                int image_width = mImageView.getWidth();
                int image_height = mImageView.getHeight();


                if (photoFile == null || !photoFile.exists()) {
                  //  Log.d("abc", "no file captured something is wrong");
                    mImageView.setImageDrawable(null);
                } else {
                    Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), image_width, image_height);
                  //  Log.d("abc", photoFile.getAbsolutePath());
                    mImageView.setImageBitmap(bitmap);
                }
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.dialog)
                .setView(view)
                .setTitle(getString(R.string.zoom_in_img))
                .create();
       // dialog.getWindow().setLayout(width, height);
        return dialog;

    }

}
