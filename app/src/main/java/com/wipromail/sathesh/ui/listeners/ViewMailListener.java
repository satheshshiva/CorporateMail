/**
 * 
 */
package com.wipromail.sathesh.ui.listeners;

import android.view.View;
import android.view.View.OnClickListener;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.ViewMailFragment;

/**
 * @author sathesh
 *
 */
public class ViewMailListener implements OnClickListener, Constants {

	private ViewMailFragment parent;

	public ViewMailListener(ViewMailFragment viewMailFragment){
		this.parent = viewMailFragment;
	}
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.ViewMailToShowMoreBtn:
			showMoreToBtnOnClick(view);
			break;

		case R.id.ViewMailCCShowMoreBtn:
			showMoreCCBtnOnClick(view);
			
			break;
		}
	}

	public void showMoreToBtnOnClick(View view) {
		if(parent.isToShowMoreFlag() ==false){
            //show all contacts
            parent.buildHeaderText(parent.getToIdView(), parent.getToReceivers(), null);  //param null will display all contacts
			parent.getToShowMoreBtn().setText(parent.getString(R.string.viewmail_showless_lbl));
			parent.setToShowMoreFlag(true);
		}
		else{
            parent.buildHeaderText(parent.getToIdView(), parent.getToReceivers(), MAX_TO_RECEIVERS_DISPLAY);
			parent.getToShowMoreBtn().setText(parent.getString(R.string.viewmail_showmore_lbl));
			parent.setToShowMoreFlag(false);
		}
	}

	public void showMoreCCBtnOnClick(View view) {
		if(parent.isCcShowMoreFlag() ==false){
            parent.buildHeaderText(parent.getCcIdView(), parent.getCcReceivers(), null); //param null will display all contacts
			parent.getcCShowMoreBtn().setText(parent.getString(R.string.viewmail_showless_cc_lbl));
			parent.setCcShowMoreFlag(true);
		}
		else{
            parent.buildHeaderText(parent.getCcIdView(), parent.getCcReceivers(), MAX_CC_RECEIVERS_DISPLAY);
			parent.getcCShowMoreBtn().setText(parent.getString(R.string.viewmail_showmore_cc_lbl));
			parent.setCcShowMoreFlag(false);
		}

	}
}
