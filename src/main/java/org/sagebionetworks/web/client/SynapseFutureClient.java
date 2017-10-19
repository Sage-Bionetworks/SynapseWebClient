package org.sagebionetworks.web.client;

import java.util.function.Consumer;

import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.web.client.utils.FutureUtils;

import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SynapseFutureClient {
	private SynapseClientAsync innerClient;

	@Inject
	public SynapseFutureClient(SynapseClientAsync innerClient) {
		this.innerClient = innerClient;
	}

	FluentFuture<InviteeVerificationSignedToken> getInviteeVerificationSignedToken(String membershipInvitationId) {
		Consumer<AsyncCallback<InviteeVerificationSignedToken>> closure = cb -> innerClient.getInviteeVerificationSignedToken(membershipInvitationId, cb);
		return FutureUtils.getFuture(closure);
	}
}
