package com.wipromail.sathesh.ui.vo;

/**
 * Created by sathesh on 1/17/15.
 */

import android.widget.CheckedTextView;

import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;

/** private class for holding the data contents and the customized date in a single object
 * @author sathesh
 *
 */
public  class MailListViewContent{
    private int type=-1;

    public static interface types{
        public final int DATE_HEADER=1;
        public final int MAIL=2;
        public final int LOADING_MORE_MAILS=3;
    }

    private String date_left="";
    private String date_right="";
    private CachedMailHeaderVO mailVO =null;
    private long loading_totalMailCount=-1;
    private long loading_totalCached=-1;
    private long loading_loadingCount=-1;
    private CheckedTextView checkedTextView;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LocalContent [date_left=");
        builder.append(date_left);
        builder.append(",  date_right=");
        builder.append(date_right);
        builder.append(",  mailVO=");
        builder.append(mailVO);
        builder.append("]");
        return builder.toString();
    }

    /** Getter and Setters * */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate_left() {
        return date_left;
    }

    public void setDate_left(String date_left) {
        this.date_left = date_left;
    }

    public String getDate_right() {
        return date_right;
    }

    public void setDate_right(String date_right) {
        this.date_right = date_right;
    }

    public CachedMailHeaderVO getMailVO() {
        return mailVO;
    }

    public void setMailVO(CachedMailHeaderVO mailVO) {
        this.mailVO = mailVO;
    }

    public long getLoading_totalMailCount() {
        return loading_totalMailCount;
    }

    public void setLoading_totalMailCount(long loading_totalMailCount) {
        this.loading_totalMailCount = loading_totalMailCount;
    }

    public long getLoading_totalCached() {
        return loading_totalCached;
    }

    public void setLoading_totalCached(long loading_totalCached) {
        this.loading_totalCached = loading_totalCached;
    }

    public long getLoading_loadingCount() {
        return loading_loadingCount;
    }

    public void setLoading_loadingCount(long loading_loadingCount) {
        this.loading_loadingCount = loading_loadingCount;
    }
    public CheckedTextView getCheckedTextView() {
        return checkedTextView;
    }

    public void setCheckedTextView(CheckedTextView checkedTextView) {
        this.checkedTextView = checkedTextView;
    }

}