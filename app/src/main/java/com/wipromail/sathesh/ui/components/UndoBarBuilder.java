package com.wipromail.sathesh.ui.components;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cocosw.undobar.UndoBarController;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ui.interfaces.UndoBarAction;

/** This is the builder for the Undo Action bar library. to change the library we can make changes
 * only to this file and it will reflected across the application
 * Created by sathesh on 1/18/15.
 */
public class UndoBarBuilder extends UndoBarController.UndoBar implements Constants, UndoBarController.AdvancedUndoListener{

    public UndoBarBuilder(@NonNull Activity activity, UndoBarAction undoBarAction) {
        super(activity);
        Bundle b = new Bundle();
        b.putSerializable("serializable", undoBarAction);
        super.token(b);
        super.listener(this);
    }

    /** UndoActionBar will have the executeAction and UndoAction methods
     * which will be executedduring OnHide and OnUndo respectivly
     *
     * @param
     * @return
     */
//    public UndoBarBuilder _setUndoBarAction() {
//
//        return this;
//    }

    public UndoBarBuilder _setMessage(String message) {
        super.message(message);
        return this;

    }

    public UndoBarBuilder _setDuration(long duration) {
        super.duration(duration);
        return this;
    }

    public UndoBarBuilder _show() {
        super.show();
        return this;
    }

    /*** UNDO BAR LISTENERS ***/

    /** Undo Bar - on Hide
     *
     * @param parcelable
     */
    @Override
    public void onHide(@Nullable Parcelable parcelable) {
        if(BuildConfig.DEBUG) Log.d(TAG, "UndoBar - OnHide() called");
        if (parcelable != null) {
            UndoBarAction action = (UndoBarAction)((Bundle) parcelable).getSerializable("serializable");
            if(action!=null){
                action.executeAction();
            }
        }
    }

    /** Undo Bar - On Clear
     *
     * @param parcelables
     */
    @Override
    public void onClear(@NonNull Parcelable[] parcelables) {
        if(BuildConfig.DEBUG) Log.d(TAG, "OnClear - OnClear() called");

    }

    /** Undo Bar - On Undo
     *
     * @param parcelable
     */
    @Override
    public void onUndo(@Nullable Parcelable parcelable) {
        if(BuildConfig.DEBUG) Log.d(TAG, "UndoBar - OnUndo() called");
        if (parcelable != null) {
            UndoBarAction action = (UndoBarAction)((Bundle) parcelable).getSerializable("serializable");
            if(action!=null){
                action.undoAction();
            }
        }

    }

}
