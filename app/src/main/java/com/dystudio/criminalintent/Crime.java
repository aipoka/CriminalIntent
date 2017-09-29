package com.dystudio.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private final UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private final boolean mRequirePolice;
    private String mSuspect;

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

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }
}
