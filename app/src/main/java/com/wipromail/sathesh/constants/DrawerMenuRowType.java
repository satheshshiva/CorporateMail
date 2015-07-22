package com.wipromail.sathesh.constants;

public interface DrawerMenuRowType {

    int INBOX=1;
    int DRAFTS=2;
    int SENT_ITEMS=3;
    int DELETED_ITEMS=4;

    int MORE_FOLDERS=10;

    int FAVOURITES_HEADER=20;
    int FAVOURITE_HELPTEXT =21;
    int FAVOURITE_FOLDERS =22;

    int CONTACTS_HEADER=120;
    int SEARCH_CONTACT =121;

    int SETTINGS=1000;
    int ABOUT=1001;

    int EMPTY_ROW=1010;

    interface MoreFolders {
        int HEADER=1;
        int FOLDER=2;
        int EMPTY_ROW=1010;

    }
}
