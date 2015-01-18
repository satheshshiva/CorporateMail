package com.wipromail.sathesh.generalinterface;

import java.io.Serializable;

/**
 * Created by sathesh on 1/17/15.
 */
public interface UndoBarAction extends Serializable {

    public void execute();
}
