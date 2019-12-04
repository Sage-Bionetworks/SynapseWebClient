package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the second step of the wizard
 * 
 * @author Jay
 *
 */
public interface CreateTableViewWizardStep2View extends IsWidget {

	void setEditor(IsWidget widget);

	void setJobTracker(IsWidget widget);

	void setJobTrackerVisible(boolean visible);
}
