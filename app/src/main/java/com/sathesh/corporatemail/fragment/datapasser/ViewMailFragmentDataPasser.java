/**
 * 
 */
package com.sathesh.corporatemail.fragment.datapasser;

import android.view.View;
import android.webkit.WebView;

import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedAttachmentMetaVO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.ui.components.ProgressDisplayNotificationBar;
import com.sathesh.corporatemail.web.StandardWebView;

import java.util.List;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

/**
 * @author sathesh
 *
 */
public interface ViewMailFragmentDataPasser {

	void showAttachments();

	/**
	 * @throws Exception 
	 * 
	 */
	void forwardMail() throws Exception;

	/**
	 * @param b
	 * @throws Exception 
	 */
	void replyMail(boolean b) throws Exception;

	ProgressDisplayNotificationBar getProgressStatusDispBar();

	StandardWebView getStandardWebView();

	WebView getWebview();

	int getTotalInlineImages();

	/**
	 * @return
	 */
	EmailMessage getMessage();

	void setCurrentStatus(ViewMailFragment.Status currentStatus);

	/**
	 * @return
	 */
	int getMailType();

    String getItemId();

	void displayHeadersAndBody();

	void showBody(String html1);

	MyActivity getMyActivity();

	int getRemainingInlineImages();

	ViewMailFragment.Status getCurrentStatus();
    CachedMailHeaderVO getCachedMailHeaderVO();
    void mailAsReadInCache();
    void expandBtnOnClick(View view);

    void setAttachmentsMeta(List<CachedAttachmentMetaVO> attachmentMetas);
}
