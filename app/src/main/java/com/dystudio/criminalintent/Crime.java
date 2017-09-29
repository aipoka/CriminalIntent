package com.dystudio.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private final UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private final boolean mRequirePolice;

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
        mRequirePolice = (mId.hashCode() % 2) == 0;
    }

    public Crime() {
        this(UUID.randomUUID());
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public boolean isRequirePolice() {
        return mRequirePolice;
    }

}
