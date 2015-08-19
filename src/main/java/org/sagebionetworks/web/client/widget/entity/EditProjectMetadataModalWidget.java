package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.ui.IsWidget;

public interface EditProjectMetadataModalWidget extends IsWidget{
	public void configure(Project project, Callback handler);
}
