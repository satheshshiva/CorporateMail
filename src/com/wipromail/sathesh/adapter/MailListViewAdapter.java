package com.wipromail.sathesh.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;

public class MailListViewAdapter extends BaseAdapter implements Constants{
	private final Context context;

	private int mailType;
	private List<CachedMailHeaderVO> listVOs;
	private CachedMailHeaderVO mailListHeader;
	private LayoutInflater inflater;
	private List<LocalContent> listLocalContent;

	/** Constructor
	 * @param context
	 * @param mailItemIds
	 * @param mailListHeaderData
	 * @param MailType
	 */
	public MailListViewAdapter(Context context, List<CachedMailHeaderVO>  listVOs) {
		this.context = context;
		this.listVOs=listVOs;

		try {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			//populate the localcontent list which will have the list view contents order with date headers, mail and more mail loading symbol etc.,
			this.listLocalContent = makeLocalContent(listVOs);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView=null;
		TextView fromView, dateView, dateHeaderLeftView,subjectView,dateHeaderRightView,moreLoadingText1View,moreLoadingText2View;
		ImageView mailReadUnreadIcon,hasAttachment;

		try {
			//Header part - shows the day and date (Categorization) 
			// and also loads the corresponding layout file
			if(this.listLocalContent!=null && this.listLocalContent.get(position)!=null ){
				LocalContent localContent=this.listLocalContent.get(position);

				//DATE_HEADER view
				if(localContent.type==LocalContent.types.DATE_HEADER){
					if(!(localContent.date_left.equals("")) || !(localContent.date_right.equals(""))){
						// Date Header Present. Show Date Header View
						rowView = inflater.inflate(R.layout.listview_maillist_header, parent, false);

						//initializing controls
						dateHeaderLeftView = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader);
						dateHeaderRightView = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader_right);

						dateHeaderLeftView.setText(localContent.date_left);
						dateHeaderRightView.setText(localContent.date_right);

						return rowView;

					}
				}

				//MAIL view
				else if(localContent.type==LocalContent.types.MAIL){
					//Show Mail Header View
					rowView = inflater.inflate(R.layout.listview_maillist, parent, false);

					mailListHeader = localContent.vo;

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
						rowView.setBackgroundColor(Color.TRANSPARENT);
						mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewUnreadIcon(context));
						fromView.setTypeface(Typeface.DEFAULT_BOLD);
						dateView.setTypeface(Typeface.DEFAULT_BOLD);
						subjectView.setTypeface(Typeface.DEFAULT_BOLD);
					}

					//From
					// for sent items and drafts, display To instead of From..
					if(mailType == MailType.SENT_ITEMS || mailType == MailType.DRAFTS){
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
				}
				else if(localContent.type==LocalContent.types.LOADING_MORE_MAILS){
					//shows the loading symbol with the no of mails remaining
					rowView = inflater.inflate(R.layout.listview_maillist_more_loading, parent, false);
					moreLoadingText1View = (TextView) rowView.findViewById(R.id.loadingMore1Text);
					moreLoadingText2View = (TextView) rowView.findViewById(R.id.loadingMore2Text);

					//set the Loading more text in the big 1st text view
					/*if(localContent.loading_loadingCount>0){
						moreLoadingText1View.setText(context.getString(R.string.mailListView_moreloading_next_x,localContent.loading_loadingCount ));
					}
					else{*/
					moreLoadingText1View.setText(context.getString(R.string.mailListView_moreloading));
					//}

					//set the remaining no of mails in the second text view
					if(localContent.loading_totalMailCount>localContent.loading_totalCached){
						//calculate the remaining no of mails by subtracting the total no of mails in folder and in cache
						long remaining=(localContent.loading_totalMailCount - localContent.loading_totalCached);
						moreLoadingText2View.setText(context.getString(R.string.mailListView_moreloading_remaining, String.valueOf(remaining)));
					}
				}
			}

		} 
		catch (ArrayIndexOutOfBoundsException e1) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG){
				Log.e(TAG, "MailListViewAdapter - arrayindex out of bounds");
				e1.printStackTrace();
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
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
	private List<LocalContent> makeLocalContent(List<CachedMailHeaderVO> mailListHeaderData) throws Exception{
		// TODO Auto-generated method stub
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
					localContent.type=LocalContent.types.DATE_HEADER;

					listLocalContent.add(localContent);

					//store the vo in a sepearate object in the list
					localContent = new LocalContent();
					localContent.vo=mailListHeader;
					localContent.type=LocalContent.types.MAIL;
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
			localContent.type=LocalContent.types.MAIL;
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
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(this.listLocalContent!=null )
			return this.listLocalContent.size();
		else
			return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		// code here if you want to pass the date headers or getMore listview button and identify OnListItemClick
		if(this.listLocalContent!=null && this.listLocalContent.get(position)!=null)
			return this.listLocalContent.get(position).vo;
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public void notifyDataSetChanged() {

		try {
			//refresh the local content list from VOs
			this.listLocalContent = makeLocalContent(listVOs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
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
		// TODO Auto-generated method stub

		LocalContent localContent = new LocalContent();
		localContent.type=LocalContent.types.LOADING_MORE_MAILS;
		localContent.loading_totalCached=totalCached;
		localContent.loading_totalMailCount=totalMails;
		localContent.loading_loadingCount=loadingCount;

		//no need to call  makeLocalContent(listVOs)since only when extra row will be added
		listLocalContent.add(localContent);
		super.notifyDataSetChanged();

	}
} 