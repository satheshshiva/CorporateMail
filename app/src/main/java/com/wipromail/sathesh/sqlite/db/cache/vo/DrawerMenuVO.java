package com.wipromail.sathesh.sqlite.db.cache.vo;

import java.io.Serializable;

/**
 * Created by Sathesh on 5/9/15.
 */
public class DrawerMenuVO  implements PojoVO, Serializable {
    private String name;
    private String folder_id;
    private int type;
    private String font_icon;

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
}
