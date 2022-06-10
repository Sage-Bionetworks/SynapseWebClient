package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DataAccessManagementViewImpl implements DataAccessManagementView {
    public interface DataAccessManagementViewImplUiBinder extends UiBinder<Widget, DataAccessManagementViewImpl> {
    }

    private DataAccessManagementPresenter presenter;
	private SynapseContextPropsProvider propsProvider;
    private Header headerWidget;

	@UiField
	ReactComponentDiv reactComponent;

    Widget widget;

    @Inject
    public DataAccessManagementViewImpl(DataAccessManagementViewImplUiBinder binder, Header headerWidget, SynapseContextPropsProvider propsProvider) {
        widget = binder.createAndBindUi(this);
        this.headerWidget = headerWidget;
		this.propsProvider = propsProvider;
        headerWidget.configure();
    }

    @Override
    public void setPresenter(DataAccessManagementPresenter presenter) {
        this.presenter = presenter;
        headerWidget.configure();
        headerWidget.refresh();
        Window.scrollTo(0,0);
		render();
    }

    @Override
    public Widget asWidget() { return widget; }

    @Override
    public void render() {
		ReactDOM.unmountComponentAtNode(reactComponent.getElement());
		ReactDOM.render(
				React.createElementWithSynapseContext(SRC.SynapseComponents.ReviewerDashboard, null, propsProvider.getJsInteropContextProps()),
				reactComponent.getElement()
		);
	}
}
