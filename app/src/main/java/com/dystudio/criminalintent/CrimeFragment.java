package com.dystudio.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class CrimeFragment extends Fragment {
    private static final String DIALOG_IMG_DETAIL = "detailed image";
    private Crime mCrime;
    private Button mDateButton;
    private Button mTimeButton;
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private Button mSuspectButton;
    private static final int REQUEST_CONTACT = 2;
    private Button mDialButton;
    private static final int REQUEST_DIAL = 3;
    private static final int REQUEST_PHOTO = 4;

    private File mPhotoFile;

    private ImageView mPhotoView;
    private int mPhotoView_width;
    private int mPhotoView_height;
    private Callbacks mCallbacks;
//    private Button mJumpToFirstButton;
//    private Button mJumpToLastButton;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);

        void onCrimeDeleted(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        EditText titleField = v.findViewById(R.id.crime_title);
        titleField.setText(mCrime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        // mDateButton.setEnabled(false);
        mDateButton.setOnClickListener((View view) -> {
            FragmentManager manager = getFragmentManager();
            DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
            dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
            dialog.show(manager, DIALOG_DATE);

        });
        CheckBox solvedCheckBox = v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(mCrime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener((CompoundButton btn, boolean b) -> {
            mCrime.setSolved(b);
            updateCrime();
        });

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener((View view) -> {
            FragmentManager manager = getFragmentManager();
            TimePickerFragment timeDialog = TimePickerFragment.newInstance(mCrime.getDate());
            timeDialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
            timeDialog.show(manager, DIALOG_TIME);

        });

        Button reportButton = v.findViewById(R.id.crime_report);
        reportButton.setOnClickListener((View view) -> {

//            Intent i = new Intent(Intent.ACTION_SEND);
//            i.setType("text/plain");
//            i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
//            startActivity(i);
            ShareCompat.IntentBuilder.from(getActivity()).setChooserTitle(R.string.chooser_title)
                    .setSubject(getString(R.string.crime_report_suspect))
                    .setType("text/plain")
                    .setText(getCrimeReport())
                    .startChooser();
        });


        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        //pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener((View view) -> startActivityForResult(pickContact, REQUEST_CONTACT));
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }


        mDialButton = v.findViewById(R.id.dial);
        mDialButton.setOnClickListener((View view) -> {
//            Intent intent = new Intent(Intent.ACTION_CALL);
//            intent.setData(Uri.parse("tel:13061725882"));
//            startActivity(intent);
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            //intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_DIAL);

        });

        ImageButton photoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        photoButton.setEnabled(canTakePhoto);
        photoButton.setOnClickListener((View view) -> {
            Uri uri = FileProvider.getUriForFile(getActivity(), "com.dystudio.criminalintent.fileprovider", mPhotoFile);
            //  Log.d("abc", mPhotoFile.toString());
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            List<ResolveInfo> cameraActivities = getActivity()
                    .getPackageManager().queryIntentActivities(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo activity : cameraActivities) {
                getActivity().grantUriPermission(activity.activityInfo.packageName,
                        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            startActivityForResult(captureImage, REQUEST_PHOTO);
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        ViewTreeObserver observer =
                mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            if (mPhotoView.isShown()) {
                mPhotoView_width = mPhotoView.getWidth();
                mPhotoView_height = mPhotoView.getHeight();
                Log.d("abc", "in observer " + mPhotoView_width + ":" + mPhotoView_height);
                updatePhotoView(mPhotoView_width, mPhotoView_height);
            }
        });


        //updatePhotoView();
        mPhotoView.setOnClickListener((View view) -> {
            FragmentManager manager = getFragmentManager();
            DetailDialogFragment detailDialogFragment = DetailDialogFragment.newInstance(mPhotoFile);
            detailDialogFragment.show(manager, DIALOG_IMG_DETAIL);
        });

        return v;


    }

    private void updatePhotoView(int photoView_width, int photoView_height) {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            //Log.d("abc", "no file captured something is wrong");
            mPhotoView.setImageDrawable(null);
        } else {
            Log.d("abc", "in update " + photoView_width + ":" + photoView_height);
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), photoView_width, photoView_height);
            //Log.d("abc", mPhotoFile.getAbsolutePath());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            // Log.d("abc", "no file captured something is wrong");
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            // Log.d("abc", mPhotoFile.getAbsolutePath());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int
            resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            @SuppressWarnings("deprecation") int hour = date.getHours();
            @SuppressWarnings("deprecation") int minute = date.getMinutes();
            Date _date = mCrime.getDate();
            //noinspection deprecation
            _date.setHours(hour);
            //noinspection deprecation
            _date.setMinutes(minute);
            mCrime.setDate(_date);
            updateCrime();
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            assert contactUri != null;
            try (Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null,
                            null, null)) {

                if ((c != null ? c.getCount() : 0) == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            }
        } else if (requestCode == REQUEST_DIAL && data != null) {
            String phoneNo;
            String name = null;
            Uri uri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(uri != null ? uri : null, null, null, null, null);
            if (cursor != null ? cursor.moveToFirst() : false) {
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                phoneNo = cursor.getString(phoneIndex);
                // cursor.getCount();
                // Log.d("abc", phoneIndex + ":" + phoneNo + ":" + cursor.getCount());
                mDialButton.setText(phoneNo);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {


                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNo));
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CALL_PHONE},
                            124);
                    // Log.d("abc", "permission is not granted");
                }
            } else //noinspection ConstantConditions
                if (requestCode == REQUEST_PHOTO) {
                Uri uri2 =
                        FileProvider.getUriForFile(getActivity(),
                                "com.dystudio.criminalintent.fileprovider",
                                mPhotoFile);
                getActivity().revokeUriPermission(uri2,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //updatePhotoView();
                updateCrime();
                updatePhotoView(mPhotoView_width, mPhotoView_height);
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void updateDate() {
        mDateButton.setText(formatDate(mCrime.getDate()));
    }

    private void updateTime() {
        mTimeButton.setText(formatTime(mCrime.getDate()));
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
        CrimeListFragment.enableMenuIcon(menu, true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).removeCrime(mCrime);
                // CrimeLab.get(getActivity()).getCrimeLookupTable().remove(mCrime.getId());
                if (getActivity().findViewById(R.id.detail_fragment_container) == null)
                    getActivity().finish();
                else {
                    mCallbacks.onCrimeDeleted(mCrime);

                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString =
                    getString(R.string.crime_report_solved);
        } else {
            solvedString =
                    getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString =
                DateFormat.format(dateFormat,
                        mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect =
                    getString(R.string.crime_report_no_suspect);
        } else {
            suspect =
                    getString(R.string.crime_report_suspect, suspect);
        }
        String report =
                getString(R.string.crime_report,
                        mCrime.getTitle(), dateString,
                        solvedString, suspect);
        return report;
    }

    public static String formatDate(Date date) {
        // return String.valueOf(DateFormat.format("EEE, yyyy.MM.dd 'at' HH:mm:ss a z", date));
        return String.valueOf(DateFormat.format("EEE, yyyy.MM.dd", date));
    }

    private static String formatTime(Date date) {
        return String.valueOf(DateFormat.format(" HH:mm:ss a z", date));
    }
}
