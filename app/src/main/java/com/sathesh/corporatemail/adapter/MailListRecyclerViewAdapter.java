package com.sathesh.corporatemail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailListRecyclerViewAdapter extends RecyclerView.Adapter<MailListRecyclerViewAdapter.ViewHolder> implements Constants{
    private final Context context;

    private List<CachedMailHeaderVO> listVOs;
    private CachedMailHeaderVO mailListHeader;
    private List<LocalContent> listLocalContent;

    /** Constructor
     * @param context
     */
    public MailListRecyclerViewAdapter(Context context, List<CachedMailHeaderVO> listVOs) {
        this.context = context;
        this.listVOs=listVOs;

        try {
            //populate the localcontent list which will have the list view contents order with date headers, mail and more mail loading symbol etc.,
            this.listLocalContent = makeLocalContent(listVOs);

        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    /** This creates a local object containing date header, mails and loading synmbol to easily display in UI
     * First it iterates the mailListHeaderData which has the list of all the VOs(mail headers)
     * Starts with getting the customized date for the first mail. dae_left and date_right has the headers to be displayed in left and right section.
     * Then goes to next mail. if same date then just adds the VO. If different date then gets the date header. If same date header as the previous one,
     * then just adds the VO, otherwise adds the date header and VO. Goes on till the end of list VOs.
     *
     * @param mailListHeaderData
     * @return
     * @throws Exception
     */
    @SuppressLint("UseSparseArrays")
    private List<LocalContent> makeLocalContent(List<CachedMailHeaderVO> mailListHeaderData) throws Exception{
        Date thisDate,prevDate=null;
        List<String> dateHeaderList;
        String date_left="", date_right="", prev_date_left="",prev_date_right="";
        if(listLocalContent!=null){
            //clear list of local content
            listLocalContent.clear();
        }
        else{
            //if it is not created create a new list
            listLocalContent = new ArrayList<LocalContent>();
        }
        LocalContent localContent;
        for(CachedMailHeaderVO mailListHeader: mailListHeaderData){
            thisDate = mailListHeader.getMail_datetimereceived();
            //if this date is same as previously processed date then do nothing and increment counter
            if(!thisDate.equals(prevDate)){
                //get the customized label text for this date
                dateHeaderList=MailApplication.getCustomizedInboxDateHeader(context, thisDate);

                if(dateHeaderList!=null ){
                    if(dateHeaderList.size()>0){
                        date_left=dateHeaderList.get(0);
                    }
                    else{
                        date_left="";
                    }
                    if(dateHeaderList.size()>1){
                        date_right=dateHeaderList.get(1);
                    }
                    else{
                        date_right="";
                    }
                }

                //if the label text is same as the previous label text then do nothing and increment counter

                if(!prev_date_left.equals(date_left) || !prev_date_right.equals(date_right)){
                    //Date Header preset condition.

                    //store the date in the seperate object in the list
                    localContent = new LocalContent();
                    localContent.date_left=date_left;
                    localContent.date_right=date_right;
                    localContent.type= LocalContent.types.DATE_HEADER;

                    listLocalContent.add(localContent);

                    //store the vo in a sepearate object in the list
                    localContent = new LocalContent();
                    localContent.vo=mailListHeader;
                    localContent.type= LocalContent.types.MAIL;
                    listLocalContent.add(localContent);

                    prev_date_left=date_left;
                    prev_date_right=date_right;
                    prevDate=thisDate;

                    continue;
                }
            }
            //create a new LocalContent object with only vo and not date
            localContent = new LocalContent();
            localContent.vo=mailListHeader;
            localContent.type= LocalContent.types.MAIL;
            listLocalContent.add(localContent);
        }
        return listLocalContent;
    }


    /** private class for holding the data contents and the customized date in a single object
     * @author sathesh
     *
     */
    private static  class LocalContent{
        private int type=-1;

        private static interface types{
            public final int DATE_HEADER=1;
            public final int MAIL=2;
            public final int LOADING_MORE_MAILS=3;
        }

        private String date_left="";
        private String date_right="";
        private CachedMailHeaderVO vo=null;
        private long loading_totalMailCount=-1;
        private long loading_totalCached=-1;
        private long loading_loadingCount=-1;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("LocalContent [date_left=");
            builder.append(date_left);
            builder.append(",  date_right=");
            builder.append(date_right);
            builder.append(",  vo=");
            builder.append(vo);
            builder.append("]");
            return builder.toString();
        }
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        // code here if you want to pass the date headers or getMore listview button and identify OnListItemClick
        if(this.listLocalContent!=null && this.listLocalContent.get(position)!=null) {
            return this.listLocalContent.get(position).vo;
        }
        else {
            return null;
        }
    }

    public void notifyDataSetChangedCall() {

        try {
            //refresh the local content list from VOs
            this.listLocalContent = makeLocalContent(listVOs);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
        super.notifyDataSetChanged();
    }


    public List<CachedMailHeaderVO> getListVOs() {
        return listVOs;
    }

    public void setListVOs(List<CachedMailHeaderVO> listVOs) {
        this.listVOs = listVOs;
    }

    /** Will get invoked when the listview is scrolled to the last.
     * Will show a progress icon
     **
     * @param totalCached	How many are in cache
     * @param totalMails	How many many mail are there in the folder
     */
    public void showMoreMailsLoadingAnimation(int loadingCount, long totalCached, long totalMails) {
        LocalContent localContent = new LocalContent();
        localContent.type= LocalContent.types.LOADING_MORE_MAILS;
        localContent.loading_totalCached=totalCached;
        localContent.loading_totalMailCount=totalMails;
        localContent.loading_loadingCount=loadingCount;

        //no need to call  makeLocalContent(listVOs)since only when extra row will be added
        listLocalContent.add(localContent);
        super.notifyDataSetChanged();

    }

    /** The layout initilization for the recycler view
     *
     * @param viewGroup
     * @param rowType - The type of layout to be returned from the overriden method getItemViewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int rowType) {
        View v= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_maillist, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(this.listLocalContent!=null && this.listLocalContent.get(position)!=null ) {
            LocalContent localContent = this.listLocalContent.get(position);

            switch(localContent.type){

                //ROW TYPE: DATE HEADER
                case LocalContent.types.DATE_HEADER:

                    ViewHolder.layoutDateHeader.setVisibility(View.VISIBLE);
                    ViewHolder.layoutMail.setVisibility(View.GONE);
                    ViewHolder.layoutMoreMailsProgress.setVisibility(View.GONE);

                    ViewHolder.dateHeaderLeftView.setText(localContent.date_left);
                    ViewHolder.dateHeaderRightView.setText(localContent.date_right);
                    break;

                //ROW TYPE: MAIL
                case LocalContent.types.MAIL:

                    ViewHolder.layoutDateHeader.setVisibility(View.GONE);
                    ViewHolder.layoutMail.setVisibility(View.VISIBLE);
                    ViewHolder.layoutMoreMailsProgress.setVisibility(View.GONE);

                    mailListHeader = localContent.vo;

                    //Is Read
                    if (mailListHeader.isMail_isread()) {
                        //read
                        //rowView.setBackgroundColor(Color.TRANSPARENT);

                        ViewHolder.mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewReadIcon(context));
                        ViewHolder.fromView.setTypeface(Typeface.DEFAULT);
                    }
                    else
                    {
                        //unread
                        ViewHolder.mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewUnreadIcon(context));
                        ViewHolder.fromView.setTypeface(Typeface.DEFAULT_BOLD);
                        ViewHolder.dateView.setTypeface(Typeface.DEFAULT_BOLD);
                        ViewHolder.subjectView.setTypeface(Typeface.DEFAULT_BOLD);
                    }

                    //From
                    // for sent items and drafts, display To instead of From..
                    if(mailListHeader.getMail_type() == MailType.SENT_ITEMS || mailListHeader.getMail_type() == MailType.DRAFTS){
                        ViewHolder.fromView.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_to()));
                    }
                    else{
                        ViewHolder.fromView.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_from()));
                    }

                    //Subject
                    ViewHolder.subjectView.setText(mailListHeader.getMail_subject());

                    //Date Time Received
                    ViewHolder.dateView.setText(MailApplication.getCustomizedInboxDate(mailListHeader.getMail_datetimereceived()));

                    //Has Attachment icon
                    if(mailListHeader.isMail_has_attachments()){
                        ViewHolder.hasAttachment.setVisibility(View.VISIBLE);
                    }else{
                        ViewHolder.hasAttachment.setVisibility(View.GONE);
                    }

                    break;

                //ROW TYPE: LOADING MORE MAILS
                case LocalContent.types.LOADING_MORE_MAILS:

                    ViewHolder.layoutDateHeader.setVisibility(View.GONE);
                    ViewHolder.layoutMail.setVisibility(View.GONE);
                    ViewHolder.layoutMoreMailsProgress.setVisibility(View.VISIBLE);

                    ViewHolder.moreLoadingText1View.setText(context.getString(R.string.mailListView_moreloading));

                    //set the remaining no of mails in the second text view
                    if(localContent.loading_totalMailCount>localContent.loading_totalCached){
                        //calculate the remaining no of mails by subtracting the total no of mails in folder and in cache
                        long remaining=(localContent.loading_totalMailCount - localContent.loading_totalCached);
                        ViewHolder.moreLoadingText2View.setText(context.getString(R.string.mailListView_moreloading_remaining, String.valueOf(remaining)));
                        ViewHolder.moreLoadingText2View.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if(this.listLocalContent!=null )
            return this.listLocalContent.size();
        else
            return 0;
    }

    /** Gets the view type to be used in onCreateViewHolder
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        LocalContent localContent=null;
        if (this.listLocalContent != null && this.listLocalContent.get(position) != null) {
            localContent = this.listLocalContent.get(position);
        }
        else {
            Log.e(LOG_TAG, "MailListRecyclerViewAdapter -> listconent is null");
        }
        return localContent.type;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private static TextView fromView, dateView, dateHeaderLeftView,subjectView,dateHeaderRightView,moreLoadingText1View,moreLoadingText2View;
        private static LinearLayout layoutDateHeader, layoutMail, layoutMoreMailsProgress;
        private static ImageView mailReadUnreadIcon,hasAttachment;

        public ViewHolder(View itemView) {
            super(itemView);
            try{
                //initializing controls
                layoutDateHeader = (LinearLayout)itemView.findViewById(R.id.layout_type_header);
                layoutMail= (LinearLayout)itemView.findViewById(R.id.layout_type_mail);
                layoutMoreMailsProgress= (LinearLayout)itemView.findViewById(R.id.layout_type_moremails);

                dateHeaderLeftView = (TextView) itemView.findViewById(R.id.listview_maillist_header_dateHeader);
                dateHeaderRightView = (TextView) itemView.findViewById(R.id.listview_maillist_header_dateHeader_right);

                fromView = (TextView) itemView.findViewById(R.id.from);
                dateView = (TextView) itemView.findViewById(R.id.date);
                subjectView = (TextView) itemView.findViewById(R.id.subject);

                mailReadUnreadIcon = (ImageView) itemView.findViewById(R.id.mailReadUnreadIcon);
                hasAttachment = (ImageView) itemView.findViewById(R.id.header_attachmentIcon);

                moreLoadingText1View = (TextView) itemView.findViewById(R.id.loadingMore1Text);
                moreLoadingText2View = (TextView) itemView.findViewById(R.id.loadingMore2Text);

            }
            catch (Exception e) {
                Utilities.generalCatchBlock(e,
                        "Exception while initializing view holder for recycler view",this);
            }
        }
    }
}