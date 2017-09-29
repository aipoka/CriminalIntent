package com.dystudio.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    protected static int NEEDPOLICE = 1;
    protected static int NONEEDFORPOLICE = 0;
    private int mPositionClicked;
    // private boolean mIsSubTitleShown = false;
    private static String SUBTITLEFLAG = "subtitle";
    private boolean mSubtitleVisible;
    private static String TAG = "CIF";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        log("oncreate is called " + savedInstanceState);
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SUBTITLEFLAG);
        }
        log("in oncreate" + mSubtitleVisible);
        if (mSubtitleVisible)
            updateSubtitle();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("on createview is called");
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        log("resume is called.");
        updateUI();
        //getArguments().get(SUBTITLEFLAG)
//        Bundle bundle = getArguments();
//
//        if (bundle != null && (boolean) bundle.get(SUBTITLEFLAG)) {
//            Log.d("CI", "on resume : "+bundle+" :"+bundle.get(SUBTITLEFLAG));
        updateSubtitle();  //maybe get/set tag when an menu item is selected.
        //}

    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
            //  mAdapter.notifyItemChanged(mAdapter.getPositionClicked());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        enableMenuIcon(menu, true);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    public static void enableMenuIcon(Menu menu, boolean flag) {
        try {
            Class<?> clazz = Class.forName("android.support.v7.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, flag);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                //CrimeLab.get(getActivity()).getCrimeLookupTable().put(crime.getId(),crime);
                Intent intent = CrimePagerActivity
                        .newIntent(getActivity(),
                                crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:

                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                //mIsSubTitleShown = true;
                updateSubtitle();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        log("pause is called.");
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putBoolean(SUBTITLEFLAG, mSubtitleVisible);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log("onsaveinstancestate is called");
        log("flag is" + mSubtitleVisible);
        outState.putBoolean(SUBTITLEFLAG, mSubtitleVisible);
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        int quatity = 0;
        if (crimeCount == 1 || crimeCount == 0)
            quatity = 1;
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, quatity, crimeCount);
        //String subtitle = getString(R.string.subtitle_format, crimeCount);
        if (!mSubtitleVisible) {
            subtitle = null;
        }
        Log.d(TAG, "flag is " + mSubtitleVisible);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }



    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Button mPoliceButton;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            //itemView.setOnClickListener(this);
            wireUI();
        }

        private void wireUI() {
            //  itemView.setOnClickListener((View v)->
            //        Toast.makeText(getActivity(), mCrime.getTitle() + " clicked!", Toast.LENGTH_SHORT).show());
            itemView.setOnClickListener((View v) -> {
                //this v is actually the itemview for the viewholder
                mAdapter.setPositionClicked((int) v.getTag());

                Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());

                //Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId());
                startActivity(intent);
            });
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, boolean requirePolice) {
            super(inflater.inflate(R.layout.list_item_crime_with_police, parent, false));
            //itemView.setOnClickListener(this);
            wireUI();
            mPoliceButton = (Button) itemView.findViewById(R.id.contact_police);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());

            mDateTextView.setText(CrimeFragment.formatDate(mCrime.getDate()));
            //mDateTextView.setText(mCrime.getDate().toString());

            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
            if (mCrime.isRequirePolice()) {
                mPoliceButton.setText(R.string.contact_police);
                mPoliceButton.setVisibility(!crime.isSolved() ? View.VISIBLE : View.GONE);

            }

        }


        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(), mCrime.getTitle() + " clicked!", Toast.LENGTH_SHORT).show();
        }
    }


    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;
        private int mPositionClicked;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == NONEEDFORPOLICE)
                return new CrimeHolder(layoutInflater, parent);
            else
                return new CrimeHolder(layoutInflater, parent, true);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {

            return mCrimes.get(position).isRequirePolice() ? NEEDPOLICE : NONEEDFORPOLICE;
        }

        public int getPositionClicked() {
            return mPositionClicked;
        }

        public void setPositionClicked(int positionClicked) {
            mPositionClicked = positionClicked;
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
