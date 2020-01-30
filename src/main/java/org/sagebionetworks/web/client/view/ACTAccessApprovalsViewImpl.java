package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessApprovalsViewImpl implements ACTAccessApprovalsView {

	public interface ACTViewImplUiBinder extends UiBinder<Widget, ACTAccessApprovalsViewImpl> {
	}

	@UiField
	DateTimePicker expiresBeforeDatePicker;
	@UiField
	Div synAlertContainer;
	@UiField
	Div accessRequirementContainer;
	@UiField
	Div showHideAccessRequirementButtonContainer;
	@UiField
	Div tableData;
	@UiField
	Button clearDateFilter;
	@UiField
	Button clearUserFilter;
	@UiField
	Button clearAccessRequirementFilter;
	@UiField
	Panel accessRequirementUI;
	@UiField
	Div userSelectContainer;
	@UiField
	Div currentUserContainer;
	@UiField
	Button exportButton;
	@UiField
	Anchor downloadLink;
	private Presenter presenter;
	private Header headerWidget;
	DateTimeUtils dateTimeUtils;
	SynapseJavascriptClient jsClient;
	Widget widget;

	@Inject
	public ACTAccessApprovalsViewImpl(ACTViewImplUiBinder binder, Header headerWidget, SynapseJavascriptClient jsClient, DateTimeUtils dateTimeUtils) {
		widget = binder.createAndBindUi(this);
		this.jsClient = jsClient;
		this.dateTimeUtils = dateTimeUtils;
		this.headerWidget = headerWidget;
		headerWidget.configure();

		clearDateFilter.addClickHandler(event -> {
			presenter.onClearExpireBeforeFilter();
		});
		clearUserFilter.addClickHandler(event -> {
			presenter.onClearUserFilter();
		});
		clearAccessRequirementFilter.addClickHandler(event -> {
			presenter.onClearAccessRequirementFilter();
		});

		expiresBeforeDatePicker.addChangeDateHandler(event -> {
			presenter.onExpiresBeforeDateSelected(expiresBeforeDatePicker.getValue());
		});
		exportButton.addClickHandler(event -> {
			export(presenter.getExportData());
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setAccessRequirementUIVisible(boolean visible) {
		accessRequirementUI.setVisible(visible);
	}

	@Override
	public void setClearAccessRequirementFilterButtonVisible(boolean visible) {
		clearAccessRequirementFilter.setVisible(visible);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void resetExportButton() {
		exportButton.setEnabled(true);
		exportButton.setVisible(true);
		downloadLink.setHref("");
		downloadLink.setVisible(false);
		_revokeObjectUrl(downloadLink.getElement());
	}

	@Override
	public void clear() {
		resetExportButton();
	}

	@Override
	public void setExpiresBeforeDate(Date date) {
		expiresBeforeDatePicker.setValue(date);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLoadMoreContainer(IsWidget w) {
		tableData.clear();
		tableData.add(w);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setAccessRequirementWidget(IsWidget w) {
		accessRequirementContainer.clear();
		accessRequirementContainer.add(w);
	}

	@Override
	public void setShowHideButton(IsWidget button) {
		showHideAccessRequirementButtonContainer.clear();
		showHideAccessRequirementButtonContainer.add(button);
	}

	@Override
	public void setUserPickerWidget(IsWidget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}

	@Override
	public void setSelectedUserBadge(IsWidget w) {
		currentUserContainer.clear();
		currentUserContainer.add(w);
	}

	@Override
	public void setSelectedUserBadgeVisible(boolean visible) {
		currentUserContainer.setVisible(visible);
	}

	public void export(ArrayList<AccessorGroup> exportData) {
		// create the csv content
		StringBuilder csvContent = new StringBuilder();
		// get all user profiles
		HashSet<String> allUserIds = new HashSet<>();
		for (AccessorGroup accessorGroup : exportData) {
			allUserIds.add(accessorGroup.getSubmitterId());
			for (String accessorId : accessorGroup.getAccessorIds()) {
				allUserIds.add(accessorId);
			}
		}
		ArrayList<String> allUserIdsList = new ArrayList<>(allUserIds);
		allUserIds = null;
		jsClient.listUserProfiles(allUserIdsList, new AsyncCallback<List>() {
			@Override
			public void onFailure(Throwable caught) {
				SynapseJSNIUtilsImpl._consoleError(caught.getMessage());
			}

			@Override
			public void onSuccess(List profiles) {
				HashMap<String, UserProfile> id2Profile = new HashMap<>();
				for (Iterator it = profiles.iterator(); it.hasNext();) {
					UserProfile profile = (UserProfile) it.next();
					id2Profile.put(profile.getOwnerId(), profile);
				}
				csvContent.append("Submitter,Accessors,Expires On\n");
				for (AccessorGroup accessorGroup : exportData) {
					UserProfile profile = id2Profile.get(accessorGroup.getSubmitterId());
					csvContent.append(profile.getUserName() + "@synapse.org,");
					for (String accessorId : accessorGroup.getAccessorIds()) {
						UserProfile accessorProfile = id2Profile.get(accessorId);
						csvContent.append(accessorProfile.getUserName() + "@synapse.org ");
					}
					csvContent.append(",");
					if (accessorGroup.getExpiredOn() != null && accessorGroup.getExpiredOn().getTime() > 0) {
						csvContent.append(dateTimeUtils.getDateTimeString(accessorGroup.getExpiredOn()));
					}
					csvContent.append("\n");
				}
				_setDownloadContent(downloadLink.getElement(), csvContent.toString());
				exportButton.setVisible(false);
				downloadLink.setVisible(true);
			}
		});
	}

	private static native String _setDownloadContent(Element anchorElement, String csvContent) /*-{
		try {
			var blob = new Blob([ csvContent ], {
				type : 'text/plain'
			});
			anchorElement.href = URL.createObjectURL(blob);
			anchorElement.download = "accessApprovals.csv";
		} catch (err) {
			console.error(err);
		}
	}-*/;

	private static native String _revokeObjectUrl(Element anchorElement) /*-{
		try {
			if (anchorElement.download) {
				URL.revokeObjectURL(anchorElement.href);
				anchorElement.removeAttribute("download");
			}
		} catch (err) {
			console.error(err);
		}
	}-*/;

}
