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
	public MailListViewAdapter(Context context, int layout, List<CachedMailHeaderVO>  listVOs) {
		this.context = context;
		this.listVOs=listVOs;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		try {
			this.listLocalContent = updatedLocalContent(listVOs);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView=null;
		TextView fromView, dateView, dateHeaderLeftView,subjectView,dateHeaderRightView;
		ImageView mailReadUnreadIcon,hasAttachment;

		try {


			//Header part - shows the day and date (Categorization) 
			// and also loads the corresponding layout file
			if(this.listLocalContent!=null && this.listLocalContent.get(position)!=null ){
				LocalContent localContent=this.listLocalContent.get(position);

				if(!(this.listLocalContent.get(position).date_left.equals("")) || !(this.listLocalContent.get(position).date_right.equals(""))){
					// Date Header Present. Show Date Header View
					rowView = inflater.inflate(R.layout.listview_maillist_header, parent, false);

					//initializing controls
					dateHeaderLeftView = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader);
					dateHeaderRightView = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader_right);

					dateHeaderLeftView.setText(localContent.date_left);
					dateHeaderRightView.setText(localContent.date_right);

					return rowView;

				}
				else{
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

	/** This creates a hashmap of label for the date categorization
	 * @param mailListHeaderData
	 * @return
	 * @throws Exception
	 */
	@SuppressLint("UseSparseArrays")
	private List<LocalContent> updatedLocalContent(List<CachedMailHeaderVO> mailListHeaderData) throws Exception{
		// TODO Auto-generated method stub
		Date thisDate,prevDate=null;
		List<String> dateHeaderList;
		String date_left="", date_right="", prev_date_left="",prev_date_right="";
		List<LocalContent> listListContent = new ArrayList<LocalContent>();
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

				//if the label text is same as the prevous label text then do nothing and increment counter

				if(!prev_date_left.equals(date_left) || !prev_date_right.equals(date_right)){
					//Date Header preset condition.

					//store the date in the seperate object in the list
					localContent = new LocalContent();
					localContent.date_left=date_left;
					localContent.date_right=date_right;

					listListContent.add(localContent);

					//store the vo in a sepearate object in the list
					localContent = new LocalContent();
					localContent.vo=mailListHeader;
					listListContent.add(localContent);

					prev_date_left=date_left;
					prev_date_right=date_right;
					prevDate=thisDate;

					continue;
				}
			}
			//create a new LocalContent object with only vo and not date
			localContent = new LocalContent();
			localContent.vo=mailListHeader;
			listListContent.add(localContent);
		}
		return listListContent;
	}


	/** private class for holding the data contents and the customized date in a single object
	 * @author sathesh
	 *
	 */
	private class LocalContent{
		private String date_left="";
		private String date_right="";
		private CachedMailHeaderVO vo=null;
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalContent [date_left=");
			builder.append(date_left);
			builder.append(", \\n date_right=");
			builder.append(date_right);
			builder.append(", \\n vo=");
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
		super.notifyDataSetChanged();
		try {
			this.listLocalContent = updatedLocalContent(listVOs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
		}
	}


	public List<CachedMailHeaderVO> getListVOs() {
		return listVOs;
	}

	public void setListVOs(List<CachedMailHeaderVO> listVOs) {
		this.listVOs = listVOs;
	}
} 