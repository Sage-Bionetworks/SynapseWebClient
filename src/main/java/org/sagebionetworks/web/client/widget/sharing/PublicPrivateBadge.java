package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublicPrivateBadge implements PublicPrivateBadgeView.Presenter {

	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private PublicPrivateBadgeView view;
	private PublicPrincipalIds publicPrincipalIds;
	private UserAccountServiceAsync userAccountService;
	private Entity entity;
	private AccessControlList acl;
	
	@Inject
	public PublicPrivateBadge(PublicPrivateBadgeView view, SynapseClientAsync synapseClient, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, UserAccountServiceAsync userAccountService) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.userAccountService = userAccountService;
		view.setPresenter(this);
	}	

	/**
	 * Configure public/private badge, return answer if it is public or private in the callback.
	 * @param entity
	 * @param callback
	 */
	public void configure(Entity entity, final AsyncCallback<Boolean> callback) {
		view.clear();
		this.entity = entity;
		getAcl(new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList result) {
				setAcl(result, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void configure(Entity entity) {
		view.clear();
		this.entity = entity;
		AsyncCallback<AccessControlList> callback1 = new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList result) {
				setAcl(result, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		};
		
		getAcl(callback1);
	}
	
	private void setAcl(final AccessControlList acl, final AsyncCallback<Boolean> isPublicCallback) {
		this.acl = acl;
		DisplayUtils.getPublicPrincipalIds(userAccountService, new AsyncCallback<PublicPrincipalIds>() {
			@Override
			public void onSuccess(PublicPrincipalIds result) {
				publicPrincipalIds = result;
				boolean isPublic = isPublic(acl, publicPrincipalIds); 
				view.configure(isPublic);
				if (isPublicCallback != null)
					isPublicCallback.onSuccess(isPublic);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (isPublicCallback != null)
					isPublicCallback.onFailure(caught);
				else if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage("Could not find the public group: " + caught.getMessage());
			}
		});
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
		int partsMask = EntityBundle.ACL;
		synapseClient.getEntityBundle(entity.getId(), partsMask, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				// retrieve ACL and user entity permissions from bundle
				try {
					callback.onSuccess(bundle.getAcl());
				} catch (Exception e) {
					onFailure(e);
				}
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
				};
			});
	}
	
	public AccessControlList getAcl() {
		return acl;
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}
		
}
