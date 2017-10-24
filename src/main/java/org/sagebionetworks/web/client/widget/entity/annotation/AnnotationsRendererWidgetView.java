package org.sagebionetworks.web.client.widget.entity.annotation;

import java.util.List;

import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnnotationsRendererWidgetView extends IsWidget{
	void configure(List<Annotation> annotations);
	void showNoAnnotations();
}
