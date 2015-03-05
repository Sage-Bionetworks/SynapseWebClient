package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ShinySiteConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.TabbedTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;

/**
 * Factory for creating new editors.
 * 
 * @author John
 *
 */
public interface EditorFactory {

	/*
	 *  Markdown Widgets
	 */
	////// Editors
	public YouTubeConfigEditor getYouTubeConfigEditor();
	public ProvenanceConfigEditor getProvenanceConfigEditor();
	public ImageConfigEditor getImageConfigEditor();
	public AttachmentConfigEditor getAttachmentConfigEditor();
	public LinkConfigEditor getLinkConfigEditor();
	public APITableConfigEditor getSynapseAPICallConfigEditor();
	public TabbedTableConfigEditor getTabbedTableConfigEditor();
	public EntityListConfigEditor getEntityListConfigEditor();
	public ShinySiteConfigEditor getShinySiteConfigEditor();
}
