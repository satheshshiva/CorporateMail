package com.wipromail.sathesh.ui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ActionMode;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.handlers.DeleteMailHandler;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.threads.ui.DeleteMailThread;
import com.wipromail.sathesh.threads.ui.DeleteMultipleMailsThread;
import com.wipromail.sathesh.util.Utilities;

import java.util.ArrayList;

/**
 * Created by sathesh on 1/18/15.
 */
public class MailDeleteDialog {


    public void mailPermanentDelete(final ViewMailActivity _acivity, final CachedMailHeaderAdapter mailHeaderAdapter, final CachedMailHeaderVO vo) {
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(_acivity);
        builder.setTitle(R.string.dialog_deletemail_title)
                .setMessage(R.string.dialog_deletemail_perm_msg)
                .setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            _acivity.finish();
                            mailHeaderAdapter.deleteItemVo(vo);
                            //spawn a thread for deleting the mail permanently
                            new DeleteMailThread(
                                    _acivity, vo.getItem_id(), true, new DeleteMailHandler(_acivity)
                            ).start();
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


    public void mailDeleteDialog(final ViewMailActivity _acivity, final CachedMailHeaderAdapter mailHeaderAdapter, final CachedMailHeaderVO vo) {
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(_acivity);
        builder.setTitle(R.string.dialog_deletemail_title)
                .setMessage(R.string.dialog_deletemail_msg)
                .setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            mailHeaderAdapter.deleteItemVo(vo);

                            //spawn a thread for deleting the mail
                            new DeleteMailThread(
                                    _acivity, vo.getItem_id(), false, new DeleteMailHandler(_acivity)
                            ).start();
                            _acivity.finish();
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

    public void multipleMailsDeleteDialog(final MailListViewFragment parent, final ActionMode mode, final ArrayList<CachedMailHeaderVO> vos) {
        final Context context = parent.getActivity();
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_deletemail_title)
                .setMessage(R.string.dialog_delete_multiple_mail_perm_msg)
                .setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            parent.getMailHeadersCacheAdapter().deleteItems(vos);

                            //update the UI list (for updating the cached deletions in UI)
                            parent.softRefreshList();

                            ArrayList<String> itemIds = new ArrayList<>();

                            for(CachedMailHeaderVO vo : vos){
                                itemIds.add(vo.getItem_id());
                            }
                            new DeleteMultipleMailsThread(
                                    context,itemIds,true, null
                            ).start();

                            mode.finish();
                        } catch (Exception e) {
                            Utilities.generalCatchBlock(e, this);
                        }
                    }
                })
                .setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                        mode.finish();
                    }
                })
                .create();
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}