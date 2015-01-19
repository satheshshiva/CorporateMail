package com.wipromail.sathesh.ui.components;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.handlers.DeleteMailHandler;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.threads.ui.DeleteMailThread;
import com.wipromail.sathesh.util.Utilities;

/**
 * Created by sathesh on 1/18/15.
 */
public class PermanentMailDeleteDialog {


    public void mailPermanentDelete(final ViewMailActivity _acivity, final CachedMailHeaderAdapter mailHeaderAdapter, final CachedMailHeaderVO vo) {
        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(_acivity);
        builder.setTitle(R.string.dialog_deletemail_title)
                .setMessage(R.string.dialog_deletemail_msg)
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
}