package com.wipromail.sathesh.sqlite.db.cache.vo;

public class CachedMailBodyVO implements PojoVO{
    private int mail_type;
    private String folder_id;
	private String folder_name;
	private String item_id;
	private String mail_body;
    private String mail_from_delimited;
    private String mail_to_delimited;
    private String mail_cc_delimited;
    private String mail_bcc_delimited;
    private String has_inline_imgs;

	
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

    public String getMail_from_delimited() {
        return mail_from_delimited;
    }

    public void setMail_from_delimited(String mail_from_delimited) {
        this.mail_from_delimited = mail_from_delimited;
    }

    public String getMail_to_delimited() {
        return mail_to_delimited;
    }

    public void setMail_to_delimited(String mail_to_delimited) {
        this.mail_to_delimited = mail_to_delimited;
    }

    public String getMail_cc_delimited() {
        return mail_cc_delimited;
    }

    public void setMail_cc_delimited(String mail_cc_delimited) {
        this.mail_cc_delimited = mail_cc_delimited;
    }

    public String getMail_bcc_delimited() {
        return mail_bcc_delimited;
    }

    public void setMail_bcc_delimited(String mail_bcc_delimited) {
        this.mail_bcc_delimited = mail_bcc_delimited;
    }

    public String getHas_inline_imgs() {
        return has_inline_imgs;
    }

    public void setHas_inline_imgs(String has_inline_imgs) {
        this.has_inline_imgs = has_inline_imgs;
    }
}
