package com.wipromail.sathesh.ui.components;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.datapasser.MailListActivityDataPasser;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.sqlite.db.cache.dao.DrawerMenuDAO;
import com.wipromail.sathesh.sqlite.db.cache.vo.FolderVO;
import com.wipromail.sathesh.util.Utilities;

/**
 * Created by sathesh on 1/18/15.
 */
public class FavouritesDialog {


    public static void removeFavourite(final MailListActivityDataPasser _acivity, final FolderVO vo, final DrawerMenuDAO dao) {
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder((MyActivity)_acivity);
        builder.setTitle(R.string.dialog_favourite_title)
                .setMessage(R.string.dialog_favourite_remove)
                .setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            // delete in the sqlite db
                            dao.deleteFavourite(vo);
                            // refresh the drawer menu 1
                            _acivity.refreshDrawerListRecyclerView();
                            //refresh the drawer menu 2
                            _acivity.refreshDrawerListRecyclerView2();
                        } catch (Exception e) {
                            Utilities.generalCatchBlock(e, this);
                        }
                    }
                })
                .setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .create();
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void addFavourite(final MailListActivityDataPasser _acivity, final FolderVO vo, final DrawerMenuDAO dao) {
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder((MyActivity)_acivity);
        builder.setTitle(R.string.dialog_favourite_title)
                .setMessage(R.string.dialog_favourite_add)
                .setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {

                            FolderVO faveFolderVO = vo.clone();
                            faveFolderVO.setType(DrawerMenuRowType.FAVOURITE_FOLDERS);
                            dao.createOrUpdate(faveFolderVO);
                            // refresh the drawer menu 1
                            _acivity.refreshDrawerListRecyclerView();
                            //refresh the drawer menu 2
                            _acivity.refreshDrawerListRecyclerView2();
                        } catch (Exception e) {
                            Utilities.generalCatchBlock(e, this);
                        }
                    }
                })
                .setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .create();
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}