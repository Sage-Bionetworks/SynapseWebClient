package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityGroup;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author dburdick
 *
 */
public interface SnapshotWidgetView extends IsWidget, SynapseWidgetView {
	
	public interface Presenter {		
		
		EntityGroup addGroup(String name, String description);
		
		void updateGroup(int groupIndex, String name, String description);
		
		void removeGroup(int groupIndex);
		
		void addGroupRecord(int groupIndex, String entityId, String version, String note);
		
		void updateGroupRecord(int groupIndex, int rowIndex, String note);
		
		void removeGroupRecord(int groupIndex, int rowIndex);

		void loadRowDetails();
		
		void setShowEditor(boolean show);
		
	}

	void setPresenter(Presenter presenter);
	
	void setSnapshot(Summary entity, boolean canEdit, boolean readOnly, boolean showEdit);

	void setEntityGroupRecordDisplay(int groupIndex, int rowIndex, EntityGroupRecordDisplay display);

}
