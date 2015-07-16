package com.wipromail.sathesh.sqlite.db.cache.vo;

import java.io.Serializable;

/**
 * Created by Sathesh on 7/16/15.
 */
public class MoreFoldersVO implements PojoVO, Serializable {
    private String name;
    private String parent_name;
    private String folder_id;
    private boolean is_header;
    private int ordering;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public boolean is_header() {
        return is_header;
    }

    public void setIs_header(boolean is_header) {
        this.is_header = is_header;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoreFoldersVO{");
        sb.append("name='").append(name).append('\'');
        sb.append(", parent_name='").append(parent_name).append('\'');
        sb.append(", folder_id='").append(folder_id).append('\'');
        sb.append(", is_header=").append(is_header);
        sb.append(", ordering=").append(ordering);
        sb.append('}');
        return sb.toString();
    }
}
