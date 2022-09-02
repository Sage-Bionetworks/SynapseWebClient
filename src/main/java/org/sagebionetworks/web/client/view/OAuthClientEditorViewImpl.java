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
    private SynapseContextPropsProvider propsProvider;

    @Inject
    public OAuthClientEditorViewImpl(OAuthClientEditorViewImplUiBinder binder, Header headerWidget, SynapseContextPropsProvider propsProvider ) {
        initWidget(binder.createAndBindUi(this));
        this.headerWidget = headerWidget;
        this.propsProvider = propsProvider;
        headerWidget.configure();
    }

    @Override
    public void createReactComponentWidget() {
        OAuthClientEditor component = new OAuthClientEditor(this.propsProvider);
        componentContainer.clear();
        componentContainer.add(component);
    }
}
