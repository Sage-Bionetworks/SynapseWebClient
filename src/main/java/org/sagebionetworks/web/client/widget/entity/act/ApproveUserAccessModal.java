package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.ACTAccessApproval;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ACTApprovalStatus;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClient;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.server.servlet.NotificationTokenType;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ApproveUserAccessModal implements ApproveUserAccessModalView.Presenter, IsWidget {
	
	public static final String EMAIL_SUBJECT = "Data access approval";
	
	private String accessRequirement;
	private String userId;
	private String fileHandleId;
	private String entityId;
	private String message;
	
	private ApproveUserAccessModalView view;
	private SynapseAlert synAlert;
	private SynapseSuggestBox peopleSuggestWidget;
	private EntityFinder entityFinderWidget;
	private Map<String, AccessRequirement> arMap;
	private SynapseClientAsync synapseClient;
	RequestBuilderWrapper requestBuilder;
	
	@Inject
	public ApproveUserAccessModal(ApproveUserAccessModalView view,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			EntityFinder entityFinder,
			UserGroupSuggestionProvider provider, 
			SynapseClientAsync synapseClient,
			RequestBuilderWrapper requestBuilder) {
		this.view = view;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.entityFinderWidget = entityFinder;
		this.synapseClient = synapseClient;
		this.requestBuilder = requestBuilder;
		peopleSuggestWidget.setSuggestionProvider(provider);
		this.view.setPresenter(this);
		this.view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		entityFinder.configure(EntityFilter.FILE, true, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				onEntitySelected(selected);
			}
		});
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
	}
	
	protected void onEntitySelected(Reference selected) {
		entityFinderWidget.hide();
		int mask = EntityBundle.ENTITY | EntityBundle.FILE_HANDLES;
		entityId = entityFinderWidget.getSelectedEntity().getTargetId();
//		synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundle>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//				synAlert.handleException(caught);
//			}
//
//			@Override
//			public void onSuccess(EntityBundle result) {
//				FileHandle f = DisplayUtils.getFileHandle(result);
//				if (f != null && entityId != null) {
//					view.setEmailButtonText(f.getFileName());
//					fileHandleId = f.getId();
//					getFileURL();
//				} else {
//					synAlert.showError("Error loading file??");
//				}
//			}
//			
//		});
	}
//	
//	private void getFileURL() {
//		FileHandleAssociation fha = new FileHandleAssociation();
//        fha.setAssociateObjectId(entityId);
//        fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
//        fha.setFileHandleId(fileHandleId);
//        synapseClient.getFileURL(fha, new AsyncCallback<String>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//				synAlert.handleException(caught);
//				//failing here
//			}
//
//			@Override
//			public void onSuccess(String url) {
//				requestBuilder.configure(RequestBuilder.GET, url);
//				//requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
//				try {
//					requestBuilder.sendRequest(null, new RequestCallback() {
//
//						@Override
//						public void onResponseReceived(Request request,
//								Response response) {
//							int statusCode = response.getStatusCode();
//							if (statusCode == Response.SC_OK) {
//								message = response.getText();
//							} else {
//								synAlert.showError("Could not read text from chosen file");
//							}
//						}
//
//						@Override
//						public void onError(Request request, Throwable exception) {
//							synAlert.handleException(exception);
//						}
//					});
//				} catch (final Exception e) {
//					synAlert.handleException(e);
//				}
//			}
//			
//        	
//        });
//	}
	
	public void selectEmail() {
		this.entityFinderWidget.show();
	}
	
	public void sendEmail() {
		if (userId == null) {
			synAlert.showError("You must select a user to approve");
			return;
		}
		if (fileHandleId == null) {
			synAlert.showError("You must select an email synId");
			return;
		}
		if (accessRequirement == null) {
			accessRequirement = view.getAccessRequirement();
		}
//		if (message == null) {
//			synAlert.showError("Missing message body");
//			return;
//		}
//		view.setSendEmailProcessing(true);
//		Set<String> recipients = new HashSet<String>();
//		recipients.add(userId);
//
//		synapseClient.sendMessage(recipients, EMAIL_SUBJECT, message, null, new AsyncCallback<String>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//				view.setSendEmailProcessing(false);
//				synAlert.handleException(caught);;
//			}
//
//			@Override
//			public void onSuccess(String result) {
//				view.setSendEmailProcessing(false);
//				view.hide();
//				view.showInfo("Email sent.");
//			}
//		});
	}
	

	public void configure(List<ACTAccessRequirement> accessRequirements) {
		this.arMap = new HashMap<String, AccessRequirement>();
		List<String> list = new ArrayList<String>();
		for (ACTAccessRequirement ar : accessRequirements) {
			arMap.put(Long.toString(ar.getId()), ar);
			list.add(Long.toString(ar.getId()));
		}
		view.setSynAlert(synAlert.asWidget());
		view.setStates(list);
		if (list.size() > 0) {
			view.setAccessRequirement(list.get(0), GovernanceServiceHelper.getAccessRequirementText(arMap.get(list.get(0))));			
		}
	}
	
	public void show() {
		synAlert.clear();
		view.show();
	}

	@Override
	public void onSubmit() {
		if (userId == null) {
			synAlert.showError("You must select a user to approve");
			return;
		}
		if (accessRequirement == null) {
			accessRequirement = view.getAccessRequirement();
		}
		view.setApproveProcessing(true);
		ACTAccessApproval aa  = new ACTAccessApproval();
		aa.setAccessorId(userId);  //user id
		aa.setApprovalStatus(ACTApprovalStatus.APPROVED);
		aa.setRequirementId(Long.parseLong(accessRequirement)); //requirement id
		synapseClient.createAccessApproval(aa, new AsyncCallback<AccessApproval>() {

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.setApproveProcessing(false);
			}

			@Override
			public void onSuccess(AccessApproval result) {
				view.setApproveProcessing(false);
				view.hide();
				view.showInfo("Approved user.");
			}
			
		});
	}
	
	public void onUserSelected(SynapseSuggestion suggestion) {
		this.userId = suggestion.getId();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}

	@Override
	public void onStateSelected(String state) {
		accessRequirement = state;
		view.setAccessRequirement(state, GovernanceServiceHelper.getAccessRequirementText(arMap.get(state)));
	}
		
}
