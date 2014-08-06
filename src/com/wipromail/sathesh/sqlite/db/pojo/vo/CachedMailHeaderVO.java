package com.wipromail.sathesh.sqlite.db.pojo.vo;

import java.util.Date;

public class CachedMailHeaderVO implements PojoVO{

	private String folder_id;
	private String folder_name;
	private String item_id;
	private int mail_type;
	private String mail_from="";
	private String mail_to="";
	private String mail_subject="";
	private String mail_cc="";
	private Date mail_datetimereceived;
	private boolean mail_isread;
	private boolean mail_has_attachments;
	
	public String getFolder_id() {
		return folder_id;
	}
	public void setFolder_id(String folder_id) {
		this.folder_id = folder_id;
	}
	public String getFolder_name() {
		return folder_name;
	}
	public void setFolder_name(String folder_name) {
		this.folder_name = folder_name;
	}
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	
	public int getMail_type() {
		return mail_type;
	}
	public void setMail_type(int mail_type) {
		this.mail_type = mail_type;
	}
	public String getMail_from() {
		return mail_from;
	}
	public void setMail_from(String mail_from) {
		this.mail_from = mail_from;
	}
	public String getMail_to() {
		return mail_to;
	}
	public void setMail_to(String mail_to) {
		this.mail_to = mail_to;
	}
	public String getMail_cc() {
		return mail_cc;
	}
	public void setMail_cc(String mail_cc) {
		this.mail_cc = mail_cc;
	}
	public String getMail_subject() {
		return mail_subject;
	}
	public void setMail_subject(String mail_subject) {
		this.mail_subject = mail_subject;
	}
	
	public Date getMail_datetimereceived() {
		return mail_datetimereceived;
	}
	public void setMail_datetimereceived(Date mail_datetimereceived) {
		this.mail_datetimereceived = mail_datetimereceived;
	}
	public boolean isMail_isread() {
		return mail_isread;
	}
	public void setMail_isread(boolean mail_isread) {
		this.mail_isread = mail_isread;
	}
	public boolean isMail_has_attachments() {
		return mail_has_attachments;
	}
	public void setMail_has_attachments(boolean mail_has_attachments) {
		this.mail_has_attachments = mail_has_attachments;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CachedMailHeaderVO [folder_id=");
		builder.append(folder_id);
		builder.append(", folder_name=");
		builder.append(folder_name);
		builder.append(", item_id=");
		builder.append(item_id);
		builder.append(", mail_from=");
		builder.append(mail_from);
		builder.append(", mail_to=");
		builder.append(mail_to);
		builder.append(", mail_cc=");
		builder.append(mail_cc);
		builder.append(", mail_subject=");
		builder.append(mail_subject);
		builder.append(", mail_datetimereceived=");
		builder.append(mail_datetimereceived);
		builder.append(", mail_isread=");
		builder.append(mail_isread);
		builder.append(", mail_has_attachments=");
		builder.append(mail_has_attachments);
		builder.append("]");
		return builder.toString();
	}
}
