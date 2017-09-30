package com.dystudio.criminalintent;


import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks
    ,CrimeFragment.Callbacks
{
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {

        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail).addToBackStack(null)
                    .commit();
            onCrimeUpdated(crime); //added solely for update subtitle when adding a new crime in twopane scenario.

        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
        listFragment.updateSubtitle();
    }

    @Override
    public void onCrimeDeleted(Crime crime) {

        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container)).commit();
        ((CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).updateUI();
        ((CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).updateSubtitle();
    }
}
