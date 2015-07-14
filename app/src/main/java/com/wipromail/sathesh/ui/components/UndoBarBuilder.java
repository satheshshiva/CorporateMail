package com.wipromail.sathesh.ui.components;

import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ui.interfaces.UndoBarAction;

/**
 * Created by Sathesh on 7/14/15.
 */

public class UndoBarBuilder implements View.OnClickListener, Constants
{
    private Snackbar snackbar;
    private UndoBarAction undoBarAction;
    private  boolean undoClicked = false;

    public UndoBarBuilder(MyActivity activity, final UndoBarAction undoBarAction, View viewParent, String msg){

        //build a Snackbar widget
        snackbar =  Snackbar.make(viewParent,
                msg,
                Snackbar.LENGTH_LONG)
                .setAction(activity.getString(R.string.undoBar_undo_lbl), this)
                .setActionTextColor(Color.YELLOW);

        //change the color for the snackbar text
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        this.undoBarAction = undoBarAction;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!undoClicked){
                    undoBarAction.executeAction();
                }
            }
        } , 4000);
    }

    /** will be shown only when this method is called
     *
     */
    public void show(){
        snackbar.show();
    }

    // undo button is clicked
    @Override
    public void onClick(View v) {
        undoClicked=true;
        undoBarAction.undoAction();
    }
}
