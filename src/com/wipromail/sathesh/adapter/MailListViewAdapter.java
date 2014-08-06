package com.wipromail.sathesh.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;

public class MailListViewAdapter extends ArrayAdapter<String> implements Constants{
	private final Context context;
	private  Map<Integer, String> dateHeader;

	private String[] mailItemIds;
	private int mailType;
	private List<CachedMailHeaderVO> mailListHeaderData;
	private CachedMailHeaderVO mailListHeader;

	/** Constructor
	 * @param context
	 * @param mailItemIds
	 * @param mailListHeaderData
	 * @param MailType
	 */
	public MailListViewAdapter(Context context, String[] mailItemIds,  List<CachedMailHeaderVO> mailListHeaderData , int mailType) {
		super(context, R.layout.listview_maillist,  mailItemIds);
		this.mailItemIds=mailItemIds;
		this.context = context;
		this.mailType=mailType;
		this.mailListHeaderData = mailListHeaderData;

		//populating date headers
		try {
			this.dateHeader = populateDateHeaderFromMailItems(mailListHeaderData);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		//loading fresh EWS data
		View rowView=loadInboxMailList(position, convertView, parent);
		return rowView;
	}

	//View adapter when loading from EWS call. This will load inboxMailList List which has List<Item>
	private View loadInboxMailList(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView=null;
		TextView listview_maillist_dateHeader,listview_maillist_from,listview_maillist_date,listview_maillist_subject,listview_maillist_header_dateHeader_right;
		ImageView listview_maillist_mailReadUnreadIcon,listview_maillist_hasAttachment;
		String dateHeaderText="";

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		try {

			//Header part - shows the day and date (Categorization)
			if(dateHeader!=null && dateHeader.get(position)!=null &&  !(dateHeader.get(position).equals(""))){
				//header present
				//loading the header present layout
				rowView = inflater.inflate(R.layout.listview_maillist_header, parent, false);

				//initializing controls
				listview_maillist_dateHeader = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader);
				listview_maillist_header_dateHeader_right = (TextView) rowView.findViewById(R.id.listview_maillist_header_dateHeader_right);

				//splitting header text
				dateHeaderText=dateHeader.get(position);
				if(dateHeaderText!=null && !(dateHeaderText.equals(""))){
					if(dateHeaderText.contains(INBOX_TEXT_DATE_DELIMITER)){
						//header splitted with delimiter
						String[] sections = dateHeaderText.split(INBOX_TEXT_DATE_DELIMITER);
						listview_maillist_dateHeader.setText(sections[0]);
						listview_maillist_header_dateHeader_right.setText(sections[1]);
					}
					else{
						//no delimiter
						listview_maillist_dateHeader.setText(dateHeaderText);
					}
				}
				else{
					listview_maillist_dateHeader.setText(dateHeaderText);
				}

			}
			else{
				//header is null (no categorisation)
				rowView = inflater.inflate(R.layout.listview_maillist, parent, false);
			}
			
			listview_maillist_from = (TextView) rowView.findViewById(R.id.listview_maillist_from);
			listview_maillist_date = (TextView) rowView.findViewById(R.id.listview_maillist_date);
			listview_maillist_subject = (TextView) rowView.findViewById(R.id.listview_maillist_subject);

			listview_maillist_mailReadUnreadIcon = (ImageView) rowView.findViewById(R.id.listview_maillist_mailReadUnreadIcon);
			listview_maillist_hasAttachment = (ImageView) rowView.findViewById(R.id.listview_maillist_header_attachmentIcon);

			//get the correct VO from the list of VOs by using the item position
			mailListHeader = mailListHeaderData.get(position);

			//Is Read
			if (mailListHeader.isMail_isread()) {
				//read
				//rowView.setBackgroundColor(Color.TRANSPARENT);

				listview_maillist_mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewReadIcon(context));	
				listview_maillist_from.setTypeface(Typeface.DEFAULT);
			}
			else
			{
				//unread
				rowView.setBackgroundColor(Color.TRANSPARENT);
				listview_maillist_mailReadUnreadIcon.setBackgroundDrawable(MailApplication.getMailViewUnreadIcon(context));
				listview_maillist_from.setTypeface(Typeface.DEFAULT_BOLD);
				listview_maillist_date.setTypeface(Typeface.DEFAULT_BOLD);
				listview_maillist_subject.setTypeface(Typeface.DEFAULT_BOLD);
			}

			//From
			// for sent items and drafts, display To instead of From..
			if(mailType == MailType.SENT_ITEMS || mailType == MailType.DRAFTS){
				listview_maillist_from.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_to()));
			}
			else{
				listview_maillist_from.setText(MailApplication.getCustomizedInboxFrom(mailListHeader.getMail_from()));
			}

			//Subject
			listview_maillist_subject.setText(mailListHeader.getMail_subject());

			//Date Time Received
			listview_maillist_date.setText(MailApplication.getCustomizedInboxDate(mailListHeader.getMail_datetimereceived()));
			
			//Has Attachment icon
			if(mailListHeader.isMail_has_attachments()){
				listview_maillist_hasAttachment.setVisibility(View.VISIBLE);
			}else{
				listview_maillist_hasAttachment.setVisibility(View.GONE);
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
	private Map<Integer, String> populateDateHeaderFromMailItems(List<CachedMailHeaderVO> mailListHeaderData) throws Exception{
		// TODO Auto-generated method stub
		CharSequence prevHeader="", thisHeader="";
		dateHeader  = new HashMap<Integer, String>();
		int counter=0;
		for(CachedMailHeaderVO mailListHeader: mailListHeaderData){
			thisHeader=MailApplication.getCustomizedInboxDateHeader(mailListHeader.getMail_datetimereceived());
			if(!(prevHeader.equals(thisHeader))){
				dateHeader.put(counter, String.valueOf(thisHeader)) ;
				prevHeader=thisHeader;
			}
			else{
				dateHeader.put(counter, "") ;
			}
			counter++;
		}
		return dateHeader;
	}

	public String[] getMailItemIds() {
		return mailItemIds;
	}

	public void setMailItemIds(String[] mailItemIds) {
		this.mailItemIds = mailItemIds;
	}

	public List<CachedMailHeaderVO> getMailListHeaderData() {
		return mailListHeaderData;
	}

	public void setMailListHeaderData(List<CachedMailHeaderVO> mailListHeaderData) {
		this.mailListHeaderData = mailListHeaderData;
	}
} 