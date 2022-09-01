package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.oauthclient.OAuthClientEditor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OAuthClientEditorViewImpl extends Composite implements OAuthClientEditorView {

    public interface OAuthClientEditorViewImplUiBinder extends UiBinder<Widget, OAuthClientEditorViewImpl>{
    }

    @UiField
    SimplePanel componentContainer;
    private Header headerWidget;

    @Inject
    public OAuthClientEditorViewImpl(OAuthClientEditorViewImplUiBinder binder, Header headerWidget) {
        initWidget(binder.createAndBindUi(this));
        this.headerWidget = headerWidget;
        headerWidget.configure();
    }


    @Override
    public void createReactComponentWidget(SynapseContextPropsProvider propsProvider) {
        OAuthClientEditor component = new OAuthClientEditor(propsProvider);
        componentContainer.clear();
        componentContainer.add(component);
    }
}
