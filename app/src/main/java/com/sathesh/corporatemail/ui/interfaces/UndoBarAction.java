package com.sathesh.corporatemail.ui.interfaces;

import java.io.Serializable;

/**
 * Created by sathesh on 1/17/15.
 */
public interface UndoBarAction extends Serializable {

    /** The action to perform when after the message is shown and going to hide
     *
     */
    public void executeAction();

    /** Undo button is clicked
     *
     */
    public void undoAction();
}
