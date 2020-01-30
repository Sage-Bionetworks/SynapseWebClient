package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImportTableViewColumnsButton implements IsWidget {
	public static final String BUTTON_TEXT = "Import columns";
	public Button button;
	public PortalGinInjector ginInjector;
	SynapseJavascriptClient jsClient;
	CallbackP<List<ColumnModel>> callback;
	EntityFinder finder;

	@Inject
	public ImportTableViewColumnsButton(Button button, final EntityFinder finder, SynapseJavascriptClient jsClient) {
		this.button = button;
		this.jsClient = jsClient;
		this.finder = finder;
		button.setText(BUTTON_TEXT);
		button.setSize(ButtonSize.DEFAULT);
		button.setType(ButtonType.DEFAULT);
		button.setIcon(IconType.ARROW_CIRCLE_O_DOWN);
		finder.configure(EntityFilter.PROJECT_OR_TABLE, false, new DisplayUtils.SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				onTableViewSelected(selected.getTargetId());
			}
		});
		button.addStyleName("margin-left-10");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				finder.show();
			}
		});
	}

	public void onTableViewSelected(String entityId) {
		// get the column schema
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludeTableBundle(true);
		jsClient.getEntityBundle(entityId, bundleRequest, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				if (!(bundle.getEntity() instanceof Table)) {
					finder.showError("Please select a Table or View.");
					return;
				}
				finder.hide();
				List<ColumnModel> columns = bundle.getTableBundle().getColumnModels();
				for (ColumnModel cm : columns) {
					cm.setId(null);
				}
				if (callback != null) {
					callback.invoke(columns);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				finder.showError(caught.getMessage());
			}
		});
	}

	public void configure(CallbackP<List<ColumnModel>> callback) {
		this.callback = callback;
	}

	public Widget asWidget() {
		return button.asWidget();
	}

}
