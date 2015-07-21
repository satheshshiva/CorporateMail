package com.wipromail.sathesh.sqlite.db.cache.vo;

import java.io.Serializable;

/**
 * Created by Sathesh on 7/16/15.
 */
public class MoreFoldersVO implements PojoVO, Serializable {
    private String name;
    private String parent_name;
    private String folder_id;
    private int type;
    private String font_icon;

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

    public String getFont_icon() {
        return font_icon;
    }

    public void setFont_icon(String font_icon) {
        this.font_icon = font_icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoreFoldersVO{");
        sb.append("name='").append(name).append('\'');
        sb.append(", parent_name='").append(parent_name).append('\'');
        sb.append(", folder_id='").append(folder_id).append('\'');
        sb.append(", type=").append(type);
        sb.append(", font_icon='").append(font_icon).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
