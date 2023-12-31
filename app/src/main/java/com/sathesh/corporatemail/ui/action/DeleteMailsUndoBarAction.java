package com.sathesh.corporatemail.ui.action;

import android.content.Context;

import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser;
import com.sathesh.corporatemail.fragment.MailListViewFragment;
import com.sathesh.corporatemail.handlers.DeleteMultipleMailsHandler;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.threads.ui.DeleteMultipleMailsThread;
import com.sathesh.corporatemail.ui.interfaces.UndoBarAction;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;

/**
 * Created by sathesh on 1/18/15.
 */
public class DeleteMailsUndoBarAction implements UndoBarAction {
    private Context context;
    private MailListFragmentDataPasser fragment;
    private ArrayList<CachedMailHeaderVO> selectedVOs;

    public DeleteMailsUndoBarAction(Context context, MailListFragmentDataPasser fragment, ArrayList<CachedMailHeaderVO> selectedVOs){
        this.context=context;
        this.fragment = fragment;
        this.selectedVOs=selectedVOs;
    }

        /** method which deletes the selected VOs using network call
         *
         */
        @Override
        public void executeAction() {
            ArrayList<String> itemIds = new ArrayList<>();

            for(CachedMailHeaderVO vo : selectedVOs){
                itemIds.add(vo.getItem_id());
            }
            new DeleteMultipleMailsThread(
                    context,itemIds, false, new DeleteMultipleMailsHandler(fragment)
            ).start();
        }

        /** This method called when undo button is clicked.
         * Should restore the deleted cached items in UI
         *
         */
        @Override
        public void undoAction() {
            try {
                //delete the items in cache first and will update UI
                fragment.getMailHeadersCacheAdapter().creteNewData(selectedVOs);

                //update the UI list (for updating the cached deletions in UI)
                fragment.softRefreshList();
                fragment.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);
            } catch (Exception e) {
                Utilities.generalCatchBlock(e, this);
            }
        }

}
