package com.wipromail.sathesh.sqlite.db.cache.vo;

import java.io.Serializable;

/**
 * Created by Sathesh on 5/9/15.
 */
public class FoldersVO implements PojoVO, Serializable {
    private String name;
    private String folder_id;
    private int type;
    private String font_icon;
    private boolean is_fave;
    private String parent_name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFont_icon() {
        return font_icon;
    }

    public void setFont_icon(String font_icon) {
        this.font_icon = font_icon;
    }

    public boolean is_fave() {
        return is_fave;
    }

    public void setIs_fave(boolean is_fave) {
        this.is_fave = is_fave;
    }

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }
}
