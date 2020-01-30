package org.sagebionetworks.web.client.widget.subscription;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscribeButtonWidget implements SubscribeButtonWidgetView.Presenter, SynapseWidgetPresenter {

	private SubscribeButtonWidgetView view;
	SynapseJavascriptClient jsClient;
	SynapseAlert synAlert;
	SubscriptionObjectType type;
	String id;
	Subscription currentSubscription;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	Callback onSubscribeCallback, onUnsubscribeCallback;
	ActionMenuWidget actionMenu;
	ActionMenuWidget.ActionListener subscribeActionListener, unsubscribeActionListener;
	boolean iconOnly;

	@Inject
	public SubscribeButtonWidget(SubscribeButtonWidgetView view, SynapseJavascriptClient jsClient, SynapseAlert synAlert, AuthenticationController authController, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		iconOnly = false;
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
		subscribeActionListener = action -> {
			onSubscribe();
		};
		unsubscribeActionListener = action -> {
			onUnsubscribe();
		};
	}


	public SubscribeButtonWidget showIconOnly() {
		iconOnly = true;
		return this;
	}

	public void clear() {
		view.clear();
	}

	public void showFollowButton() {
		if (actionMenu != null) {
			actionMenu.setActionListener(Action.FOLLOW, subscribeActionListener);
			actionMenu.setActionText(Action.FOLLOW, "Follow " + StringUtils.toTitleCase(type.toString()));
		}

		if (iconOnly) {
			view.showFollowIcon();
		} else {
			view.showFollowButton();
		}
	}

	public void showUnfollowButton() {
		if (actionMenu != null) {
			actionMenu.setActionListener(Action.FOLLOW, unsubscribeActionListener);
			actionMenu.setActionText(Action.FOLLOW, "Unfollow " + StringUtils.toTitleCase(type.toString()));
		}

		if (iconOnly) {
			view.showUnfollowIcon();
		} else {
			view.showUnfollowButton();
		}
	}

	public void setOnSubscribeCallback(Callback c) {
		onSubscribeCallback = c;
	}

	public void setOnUnsubscribeCallback(Callback c) {
		onUnsubscribeCallback = c;
	}


	/**
	 * @param type Topic subscription object type
	 * @param id Topic subscription object id
	 * @param actionMenu if provided, will set the follow/unfollow command, and listen for action
	 */
	public void configure(SubscriptionObjectType type, String id, ActionMenuWidget actionMenu) {
		this.id = id;
		this.type = type;
		this.actionMenu = actionMenu;
		if (!authController.isLoggedIn()) {
			showFollowButton();
		} else {
			getSubscriptionState();
		}
	}

	/**
	 * @param subscription Can be configured with an existing subscription. Will not look for
	 *        subscription, and will render with a way to unsubscribe.
	 */
	public void configure(Subscription subscription) {
		this.id = subscription.getObjectId();
		this.type = subscription.getObjectType();
		this.currentSubscription = subscription;
		showUnfollowButton();
	}

	public void getSubscriptionState() {
		view.clear();
		synAlert.clear();
		SubscriptionRequest request = new SubscriptionRequest();
		request.setObjectType(type);
		List<String> idList = new ArrayList<String>();
		idList.add(id);
		request.setIdList(idList);
		jsClient.listSubscription(request, new AsyncCallback<SubscriptionPagedResults>() {
			@Override
			public void onSuccess(SubscriptionPagedResults results) {
				if (results.getTotalNumberOfResults() > 0) {
					// currently subscribed.
					currentSubscription = results.getResults().get(0);
					showUnfollowButton();
				} else {
					// not currently subscribed
					showFollowButton();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public void onSubscribe() {
		synAlert.clear();
		// if not logged in, then send to login first
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			view.showLoading();
			Topic topic = new Topic();
			topic.setObjectId(id);
			topic.setObjectType(type);
			jsClient.subscribe(topic, new AsyncCallback<Subscription>() {
				public void onFailure(Throwable caught) {
					view.hideLoading();
					synAlert.handleException(caught);
				};

				@Override
				public void onSuccess(Subscription result) {
					// success
					currentSubscription = result;
					showUnfollowButton();
					if (onSubscribeCallback != null) {
						onSubscribeCallback.invoke();
					}
				}
			});
		}
	}

	@Override
	public void onUnsubscribe() {
		synAlert.clear();
		view.showLoading();
		jsClient.unsubscribe(currentSubscription.getSubscriptionId(), new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				view.hideLoading();
				synAlert.handleException(caught);
			};

			@Override
			public void onSuccess(Void result) {
				currentSubscription = null;
				showFollowButton();
				if (onUnsubscribeCallback != null) {
					onUnsubscribeCallback.invoke();
				}
			}
		});
	}

	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * For testing
	 * 
	 * @return
	 */
	public boolean isIconOnly() {
		return iconOnly;
	}

	public Subscription getCurrentSubscription() {
		return currentSubscription;
	}

	public void setButtonSize(ButtonSize size) {
		view.setButtonSize(size);
	}
}
