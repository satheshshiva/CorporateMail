package com.wipromail.sathesh.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;
import com.wipromail.sathesh.ui.vo.MailListViewContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailListViewAdapter extends BaseAdapter implements Constants{
    private final Context context;

    private List<CachedMailHeaderVO> mailListVOs;
    private CachedMailHeaderVO mailListHeader;
    private LayoutInflater inflater;
    private List<MailListViewContent> contentList;
    private MailListViewFragment parent;

    /** Constructor
     * @param context
     * @param parent
     */
    public MailListViewAdapter(Context context, MailListViewFragment parent, List<CachedMailHeaderVO> mailListVOs) {
        this.context = context;
        this.mailListVOs = mailListVOs;
        this.parent=parent;

        try {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //populate the content list which will have the list view contents order with date headers, mail and more mail loading symbol etc.,
            this.contentList = buildListViewContent(mailListVOs);

        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View rowView=null;
        TextView fromView, dateView, dateHeaderLeftView,subjectView,moreLoadingText1View,moreLoadingText2View;
        CheckedTextView dateHeaderCheckedView;
        ImageView mailReadUnreadIcon,hasAttachment;

        try {
            //Header part - shows the day and date (Categorization)
            // and also loads the corresponding layout file
            if(this.contentList !=null && this.contentList.get(position)!=null ){
                MailListViewContent listViewContent=this.contentList.get(position);

                switch (listViewContent.getType()){
                    //DATE_HEADER view
                    case MailListViewContent.types.DATE_HEADER:
                        if(!(listViewContent.getDate_left().equals("")) || !(listViewContent.getDate_right().equals(""))){
                            // Date Header Present. Show Date Header View
                            rowView = inflater.inflate(R.layout.listview_maillist_header, viewGroup, false);

                            //initializing controls
                            dateHeaderLeftView = (TextView) rowView.findViewById(R.id.dateHeader);
                            dateHeaderCheckedView = (CheckedTextView) rowView.findViewById(R.id.dateHeader_checked);

                            dateHeaderLeftView.setText(listViewContent.getDate_left());
                            listViewContent.setCheckedTextView(dateHeaderCheckedView);
                            return rowView;

                        }
                        break;

                    //MAIL view
                    case MailListViewContent.types.MAIL:
                        //Show Mail Header View
                        rowView = inflater.inflate(R.layout.listview_maillist, viewGroup, false);

                        mailListHeader = listViewContent.getMailVO();

                        fromView = (TextView) rowView.findViewById(R.id.from);
                        dateView = (TextView) rowView.findViewById(R.id.date);
                        subjectView = (TextView) rowView.findViewById(R.id.subject);

                        mailReadUnreadIcon = (ImageView) rowView.findViewById(R.id.mailReadUnreadIcon);
                        hasAttachment = (ImageView) rowView.findViewById(R.id.header_attachmentIcon);

                        //Is Read
                        if (mailListHeader.isMail_isread()) {
                            //read
                            //rowView.setBackgroundColor(Color.TRANSPARENT);

                            mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewReadIcon(context));
                            fromView.setTypeface(Typeface.DEFAULT);
                        }
                        else
                        {
                            //unread
                            //rowView.setBackgroundColor(Color.TRANSPARENT);
                            mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewUnreadIcon(context));
                            fromView.setTypeface(Typeface.DEFAULT_BOLD);
                            dateView.setTypeface(Typeface.DEFAULT_BOLD);
                            subjectView.setTypeface(Typeface.DEFAULT_BOLD);
                        }

                        //From
                        // for sent items and drafts, display To instead of From..
                        if(mailListHeader.getMail_type() == MailType.SENT_ITEMS || mailListHeader.getMail_type() == MailType.DRAFTS){
                            fromView.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_to()));
                        }
                        else{
                            fromView.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_from()));
                        }

                        //Subject
                        subjectView.setText(mailListHeader.getMail_subject());

                        //Date Time Received
                        dateView.setText(MailApplication.getCustomizedInboxDate(mailListHeader.getMail_datetimereceived()));

                        //Has Attachment icon
                        if(mailListHeader.isMail_has_attachments()){
                            hasAttachment.setVisibility(View.VISIBLE);
                        }else{
                            hasAttachment.setVisibility(View.GONE);
                        }
                        break;

                    case MailListViewContent.types.LOADING_MORE_MAILS:
                        //shows the loading symbol with the no of mails remaining
                        rowView = inflater.inflate(R.layout.listview_maillist_more_loading, viewGroup, false);
                        moreLoadingText1View = (TextView) rowView.findViewById(R.id.loadingMore1Text);
                        moreLoadingText2View = (TextView) rowView.findViewById(R.id.loadingMore2Text);

                        //set the Loading more text in the big 1st text view
					/*if(listViewContent.loading_loadingCount>0){
						moreLoadingText1View.setText(context.getString(R.string.mailListView_moreloading_next_x,listViewContent.loading_loadingCount ));
					}
					else{*/
                        moreLoadingText1View.setText(context.getString(R.string.mailListView_moreloading));
                        //}

                        //set the remaining no of mails in the second text view
                        if(listViewContent.getLoading_totalMailCount()>listViewContent.getLoading_totalCached()){
                            //calculate the remaining no of mails by subtracting the total no of mails in folder and in cache
                            long remaining=(listViewContent.getLoading_totalMailCount() - listViewContent.getLoading_totalCached());
                            moreLoadingText2View.setText(context.getString(R.string.mailListView_moreloading_remaining, String.valueOf(remaining)));
                            moreLoadingText2View.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

        }
        catch (ArrayIndexOutOfBoundsException e1) {
            if(BuildConfig.DEBUG){
                Log.e(TAG, "MailListViewAdapter - arrayindex out of bounds");
                e1.printStackTrace();
            }
        }
        catch (Exception e) {
            if(BuildConfig.DEBUG){
                e.printStackTrace();
                Log.e(TAG, "MailListViewAdapter - Cannot get details for the item");
            }
        }

        return rowView;
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
    private List<MailListViewContent> buildListViewContent(List<CachedMailHeaderVO> mailListHeaderData) throws Exception{
        Date thisDate,prevDate=null;
        List<String> dateHeaderList;
        String date_left="", date_right="", prev_date_left="",prev_date_right="";
        if(contentList !=null){
            //clear list of content list
            contentList.clear();
        }
        else{
            //if it is not created create a new list
            contentList = new ArrayList<MailListViewContent>();
        }
        MailListViewContent localContent;
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
                    localContent = new MailListViewContent();
                    localContent.setDate_left(date_left);
                    localContent.setDate_right(date_right);
                    localContent.setType(MailListViewContent.types.DATE_HEADER);

                    contentList.add(localContent);

                    //store the vo in a sepearate object in the list
                    localContent = new MailListViewContent();
                    localContent.setMailVO(mailListHeader);
                    localContent.setType(MailListViewContent.types.MAIL);
                    contentList.add(localContent);

                    prev_date_left=date_left;
                    prev_date_right=date_right;
                    prevDate=thisDate;

                    continue;
                }
            }
            //create a new MailListViewContent object with only vo and not date
            localContent = new MailListViewContent();
            localContent.setMailVO(mailListHeader);
            localContent.setType(MailListViewContent.types.MAIL);
            contentList.add(localContent);
        }
        return contentList;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        if(this.contentList !=null )
            return this.contentList.size();
        else
            return 0;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
            return this.contentList.get(position);

    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateAndNotify() {

        try {
            //refresh the cotent list list from VOs
            this.contentList = buildListViewContent(mailListVOs);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
        super.notifyDataSetChanged();
    }

    public List<CachedMailHeaderVO> getMailListVOs() {
        return mailListVOs;
    }

    public void setMailListVOs(List<CachedMailHeaderVO> mailListVOs) {
        this.mailListVOs = mailListVOs;
    }

    /** Will get invoked when the listview is scrolled to the last.
     * Will show a progress icon
     **
     * @param totalCached	How many are in cache
     * @param totalMails	How many many mail are there in the folder
     */
    public void showMoreMailsLoadingAnimation(int loadingCount, long totalCached, long totalMails) {
        MailListViewContent localContent = new MailListViewContent();
        localContent.setType(MailListViewContent.types.LOADING_MORE_MAILS);
        localContent.setLoading_totalCached(totalCached);
        localContent.setLoading_totalMailCount(totalMails);
        localContent.setLoading_loadingCount(loadingCount);

        //no need to call  buildListViewContent(mailListVOs)since only when extra row will be added
        contentList.add(localContent);
        super.notifyDataSetChanged();

    }
} 