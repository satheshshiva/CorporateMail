package com.sathesh.corporatemail.sqlite.db.cache.vo;

import java.sql.Date;

public class CachedAttachmentMetaVO implements PojoVO {
    private String attachment_id;
    private String item_id;
    private String file_name;
    private int size_bytes;
    private String human_readable_size;
    private String file_path;
    private String content_type;
    private Date created_date;
    private Date last_accessed_type;

    public String getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(String attachment_id) {
        this.attachment_id = attachment_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getHuman_readable_size() {
        return human_readable_size;
    }

    public void setHuman_readable_size(String human_readable_size) {
        this.human_readable_size = human_readable_size;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public Date getLast_accessed_type() {
        return last_accessed_type;
    }

    public void setLast_accessed_type(Date last_accessed_type) {
        this.last_accessed_type = last_accessed_type;
    }

    public int getSize_bytes() {
        return size_bytes;
    }

    public void setSize_bytes(int size_bytes) {
        this.size_bytes = size_bytes;
    }
}
