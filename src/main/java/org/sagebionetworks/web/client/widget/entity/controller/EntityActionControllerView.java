package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Abstraction for the view 
 * @author John
 *
 */
public interface EntityActionControllerView extends ShowsErrors, IsWidget {
	

	/**
	 * Show the user a confirm dialog.
	 * @param string
	 * @param action
	 */
	void showConfirmDialog(String title, String string, Callback callback);
	

	/**
	 * Show info to the user.
	 * @param string
	 * @param string
	 */
	void showInfo(String header, String message);
	
	/**
	 * Show info dialog to the user.
	 */
	void showInfoDialog(String header, String message);

	/**
	 * Prompt the user to enter a string value.
	 * @param prompt
	 * @param callback
	 */
	void showPromptDialog(String prompt, PromptCallback callback);

	void addWidget(IsWidget asWidget);
	void showDeleteWikiModal(String wikiPageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap);
	void setAnnotationsRendererWidget(IsWidget w);
	void showAnnotations();
	void hideAnnotations();
	void setFileHistoryWidget(IsWidget w);
	void showFileHistory();
	void hideFileHistory();
	void setPresenter(Presenter p);
	
	public interface Presenter {
		void onConfirmDeleteWiki();
	}
}
