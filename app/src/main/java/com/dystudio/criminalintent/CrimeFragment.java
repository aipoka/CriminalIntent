package com.dystudio.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;


public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private Button mDateButton;
    private Button mTimeButton;
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
//    private Button mJumpToFirstButton;
//    private Button mJumpToLastButton;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME ="DialogTime";

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);

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
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        // mDateButton.setEnabled(false);
        mDateButton.setOnClickListener((View view)->{
            FragmentManager manager = getFragmentManager();
            DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
            dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
            dialog.show(manager, DIALOG_DATE);

        });
        CheckBox solvedCheckBox = v.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(mCrime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener((CompoundButton btn, boolean b) -> mCrime.setSolved(b));

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener((View view)->{
            FragmentManager manager = getFragmentManager();
            TimePickerFragment timeDialog = TimePickerFragment.newInstance(mCrime.getDate());
            timeDialog.setTargetFragment(CrimeFragment.this,REQUEST_TIME);
            timeDialog.show(manager, DIALOG_TIME);

        });
        return v;


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
            updateDate();
        }
        if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            @SuppressWarnings("deprecation") int hour = date.getHours();
            @SuppressWarnings("deprecation") int minute = date.getMinutes();
            Date _date = mCrime.getDate();
            //noinspection deprecation
            _date.setHours(hour);
            //noinspection deprecation
            _date.setMinutes(minute);
            mCrime.setDate(_date);
            updateTime();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);

    }

    private void updateDate() {
        mDateButton.setText(formatDate(mCrime.getDate()));
    }
    private void updateTime() {
        mTimeButton.setText(formatTime(mCrime.getDate()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
        CrimeListFragment.enableMenuIcon(menu,true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).removeCrime(mCrime);
               // CrimeLab.get(getActivity()).getCrimeLookupTable().remove(mCrime.getId());
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String formatDate(Date date){
       // return String.valueOf(DateFormat.format("EEE, yyyy.MM.dd 'at' HH:mm:ss a z", date));
        return String.valueOf(DateFormat.format("EEE, yyyy.MM.dd", date));
    }

    private static String formatTime(Date date){
        return String.valueOf(DateFormat.format(" HH:mm:ss a z", date));
    }
}
