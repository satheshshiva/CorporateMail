package com.wipromail.sathesh.ui.interfaces;

import java.io.Serializable;

/**
 * Created by sathesh on 1/17/15.
 */
public interface UndoBarAction extends Serializable {

    public void executeAction();
    public void undoAction();
}
