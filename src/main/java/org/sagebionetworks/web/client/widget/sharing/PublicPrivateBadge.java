package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublicPrivateBadge implements PublicPrivateBadgeView.Presenter {

	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private UserAccountServiceAsync userAccountService;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private PublicPrivateBadgeView view;
	private PublicPrincipalIds publicPrincipalIds;	
	private Entity entity;
	private AccessControlList acl;
	
	@Inject
	public PublicPrivateBadge(PublicPrivateBadgeView view, UserAccountServiceAsync userAccountService, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator,GlobalApplicationState globalApplicationState, AuthenticationController authenticationController) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.userAccountService = userAccountService;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}	
	
	public void configure(Entity entity) {
		this.entity = entity;
		//set publicPrincipalIds, and acl
		final AsyncCallback<PublicPrincipalIds> callback2 = new AsyncCallback<PublicPrincipalIds>() {
			@Override
			public void onSuccess(PublicPrincipalIds result) {
				publicPrincipalIds = result;
				view.configure(isPublic(acl, publicPrincipalIds));
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		};
		AsyncCallback<AccessControlList> callback1 = new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList result) {
				acl = result;
				getPublicPrincipalIds(callback2);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		};
		
		getAcl(callback1);
	}
	
	/**
	 * Using the acl and public principal ids, determine if this is public or not
	 * @return
	 */
	public static boolean isPublic(AccessControlList acl, PublicPrincipalIds publicPrincipalIds) {
		for (final ResourceAccess ra : acl.getResourceAccess()) {
			Long pricipalIdLong = ra.getPrincipalId();
			if (publicPrincipalIds.isPublic(pricipalIdLong))
				return true;
		}
		return false;
	}
	
	public void getAcl(final AsyncCallback<AccessControlList> callback) {
		int partsMask = EntityBundleTransport.ACL;
		synapseClient.getEntityBundle(entity.getId(), partsMask, new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport bundle) {
				// retrieve ACL and user entity permissions from bundle
				try {
					callback.onSuccess(nodeModelCreator.createJSONEntity(bundle.getAclJson(), AccessControlList.class));
				} catch (Exception e) {
					onFailure(e);
				}
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
				};
			});
	}
	
	public void getPublicPrincipalIds(AsyncCallback<PublicPrincipalIds> callback) {
		userAccountService.getPublicAndAuthenticatedGroupPrincipalIds(callback);
	}
	
	public AccessControlList getAcl() {
		return acl;
	}
	
	public PublicPrincipalIds getPublicPrincipalIds() {
		return publicPrincipalIds;
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}
		
}
