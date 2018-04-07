package com.sathesh.corporatemail.sqlite.db.cache.vo;

import java.io.Serializable;

/**
 * Created by Sathesh on 5/9/15.
 */
public class FolderVO implements PojoVO, Serializable, Cloneable {
    private String name;
    private String folder_id;
    private int type;
    private String font_icon;
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

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    // we are using to check whether the folder is in favourites (comparing DrawerMenu table object and MoreFolder table object)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(folder_id==null) return false;

        FolderVO folderVO = (FolderVO) o;

        return folder_id.equals(folderVO.folder_id);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FolderVO{");
        sb.append("name='").append(name).append('\'');
        sb.append(", folder_id='").append(folder_id).append('\'');
        sb.append(", type=").append(type);
        sb.append(", font_icon='").append(font_icon).append('\'');
        sb.append(", parent_name='").append(parent_name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public FolderVO clone() throws CloneNotSupportedException {
        FolderVO folderVO = (FolderVO)super.clone();
        return folderVO;
    }
}
