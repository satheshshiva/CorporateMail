/**
 * 
 */
package com.sathesh.corporatemail.ui.listeners;

import android.view.View;
import android.view.View.OnClickListener;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.ViewMailFragment;

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
	 * @see android.view.View.OnClickListener#onDrawerLayoutRecyclerViewClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
	}

}
