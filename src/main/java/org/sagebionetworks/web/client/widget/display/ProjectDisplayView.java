package org.sagebionetworks.web.client.widget.display;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProjectDisplayView extends IsWidget {
	public interface Presenter {
		void onSave();
		void onCancel();
	}

	void setSynAlertWidget(IsWidget asWidget);
	
	void setPresenter(Presenter presenter);

	void hide();

	void show();

	void setWiki(boolean value);
	void setFiles(boolean value);
	void setTables(boolean value);
	void setChallenge(boolean value);
	void setDiscussion(boolean value);
	void setDocker(boolean value);
	
	boolean getWiki();
	boolean getFiles();
	boolean getTables();
	boolean getChallenge();
	boolean getDiscussion();
	boolean getDocker();
	void clear();
	void showErrorMessage(String errorMessage);
	
}