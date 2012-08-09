package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Snapshot;
import org.sagebionetworks.repo.model.SnapshotGroup;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * 
 * @author dburdick
 *
 */
public interface SnapshotWidgetView extends IsWidget, SynapseWidgetView {
	
	public interface Presenter {		
		
		SnapshotGroup addGroup(String name, String description);
		
		void updateGroup(int groupIndex, String name, String description);
		
		void removeGroup(int groupIndex);
		
		void addGroupRecord(int groupIndex, String entityId, String version, String note);
		
		void updateGroupRecord(int groupIndex, int rowIndex, String note);
		
		void removeGroupRecord(int groupIndex, int rowIndex);

		void loadRowDetails();
		
	}

	void setPresenter(Presenter presenter);
	
	void setSnapshot(Snapshot entity, boolean canEdit, boolean readOnly);

	void setSnapshotGroupRecordDisplay(int groupIndex, int rowIndex, SnapshotGroupRecordDisplay display);

}
