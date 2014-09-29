package com.wipromail.sathesh.sqlite.db.pojo.vo;

public class CachedMailBodyVO implements PojoVO{
    private int mail_type;
    private String folder_id;
	private String folder_name;
	private String item_id;
	private String mail_body;
	
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
	public String getMail_body() {
		return mail_body;
	}
	public void setMail_body(String mail_body) {
		this.mail_body = mail_body;
	}
    public int getMail_type() {
        return mail_type;
    }
    public void setMail_type(int mail_type) {
        this.mail_type = mail_type;
    }
}
