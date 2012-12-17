package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.LinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ProvenanceConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.YouTubeConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;

public interface WidgetFactory {

	public ProvenanceWidget createProvenanceWidget();
	public ProvenanceConfigEditor createProvenanceWidgetEditor();
	
	public YouTubeWidget createYouTubeWidget();
	public YouTubeConfigEditor createYouTubeWidgetEditor();
	
	public ImageWidget createImageWidget();
	public ImageConfigEditor createImageWidgetEditor();
	
	public LinkConfigEditor createLinkWidgetEditor();
}
