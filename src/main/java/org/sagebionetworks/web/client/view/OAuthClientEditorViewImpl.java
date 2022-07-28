package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.presenter.OAuthClientEditorPresenter;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OAuthClientEditorViewImpl implements OAuthClientEditorView {
    ReactComponentDiv container;

    private OAuthClientEditorPresenter presenter;
    private SynapseContextPropsProvider propsProvider;
    private Header headerWidget;

    @Inject
    public OAuthClientEditorViewImpl(Header headerWidget, SynapseContextPropsProvider propsProvider) {
        container = new ReactComponentDiv();
        this.headerWidget = headerWidget;
        this.propsProvider = propsProvider;
        headerWidget.configure();
    }

    @Override
    public void setPresenter(OAuthClientEditorPresenter presenter) {
        this.presenter =presenter;
        headerWidget.configure();
        headerWidget.refresh();
        Window.scrollTo(0,0);
        render();
    }

    @Override
    public Widget asWidget() { return container.asWidget(); }

    @Override
    public void render() {
        EmptyProps props = EmptyProps.create();
        ReactElement component = React.createElementWithSynapseContext(SRC.SynapseComponents.OAuthManagement, props, propsProvider.getJsInteropContextProps());
        ReactDOM.render(component, container.getElement());
    }
}
