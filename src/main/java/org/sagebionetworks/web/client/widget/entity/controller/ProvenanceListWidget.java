package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.LinkedList;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListWidget implements ProvenanceListWidgetView.Presenter, IsWidget {

	ProvenanceListWidgetView view;
	PortalGinInjector ginInjector;
	List<ProvenanceEntry> rows;
	EntityFinder entityFinder;
	ProvenanceURLDialogWidget urlDialog;

	@Inject
	public ProvenanceListWidget(final ProvenanceListWidgetView view, final PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		rows = new LinkedList<ProvenanceEntry>();
		this.view.setPresenter(this);
	}

	@Override
	public void configure(List<ProvenanceEntry> provEntries) {
		rows = provEntries;
		for (final ProvenanceEntry entry : rows) {
			view.addRow(entry);
			entry.setRemoveCallback(new Callback() {
				@Override
				public void invoke() {
					rows.remove(entry);
					view.removeRow(entry);
				}
			});
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}


	@Override
	public void addEntityRow() {
		entityFinder.clearState();
		entityFinder.configure(true, new SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference ref) {
				if (ref.getTargetId() != null) {
					final EntityRefProvEntryView newEntry = ginInjector.getEntityRefEntry();
					rows.add(newEntry);
					String targetId = ref.getTargetId();
					Long version = ref.getTargetVersionNumber();
					newEntry.configure(targetId, version != null ? version.toString() : "Current");
					newEntry.setAnchorTarget(DisplayUtils.getSynapseHistoryToken(targetId, version));
					newEntry.setRemoveCallback(new Callback() {
						@Override
						public void invoke() {
							rows.remove(newEntry);
							view.removeRow(newEntry);
						}
					});
					view.addRow(newEntry);
					entityFinder.hide();
				}
			}
		});
		entityFinder.show();
	}


	@Override
	public void addURLRow() {
		urlDialog.configure(new Callback() {
			@Override
			public void invoke() {
				final URLProvEntryView newEntry = ginInjector.getURLEntry();
				rows.add(newEntry);
				String name = urlDialog.getURLName();
				String address = urlDialog.getURLAddress();
				if (name.trim().isEmpty()) {
					name = address;
				}
				newEntry.configure(name, address);
				newEntry.setAnchorTarget(address);
				newEntry.setRemoveCallback(new Callback() {
					@Override
					public void invoke() {
						rows.remove(newEntry);
						view.removeRow(newEntry);
					}
				});
				view.addRow(newEntry);
				urlDialog.hide();
			}
		});
		urlDialog.show();
	}

	public void clear() {
		rows.clear();
		view.clear();
	}

	public List<ProvenanceEntry> getEntries() {
		return rows;
	}

	public void setEntityFinder(final EntityFinder entityFinder) {
		this.entityFinder = entityFinder;
	}

	public void setURLDialog(final ProvenanceURLDialogWidget urlDialog) {
		this.urlDialog = urlDialog;

	}
}
