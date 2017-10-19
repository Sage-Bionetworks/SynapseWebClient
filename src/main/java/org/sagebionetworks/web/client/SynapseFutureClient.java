package org.sagebionetworks.web.client;

import java.util.function.Consumer;

import org.sagebionetworks.repo.model.InviteeVerificationSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.utils.FutureUtils;

import com.google.common.util.concurrent.FluentFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class SynapseFutureClient {
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;

	@Inject
	public SynapseFutureClient(SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient) {
		this.synapseClient = synapseClient;
		this.jsClient = jsClient;
	}

	public FluentFuture<InviteeVerificationSignedToken> getInviteeVerificationSignedToken(String membershipInvitationId) {
		Consumer<AsyncCallback<InviteeVerificationSignedToken>> closure = cb -> synapseClient.getInviteeVerificationSignedToken(membershipInvitationId, cb);
		return FutureUtils.getFuture(closure);
	}

	public FluentFuture<Void> updateInviteeId(String membershipInvitationId, InviteeVerificationSignedToken token) {
		Consumer<AsyncCallback<Void>> closure = cb -> synapseClient.updateInviteeId(membershipInvitationId, token, cb);
		return FutureUtils.getFuture(closure);
	}

	public FluentFuture<SignedTokenInterface> hexDecodeAndDeserialize(String tokenTypeName, String signedTokenString) {
		Consumer<AsyncCallback<SignedTokenInterface>> closure = cb -> synapseClient.hexDecodeAndDeserialize(tokenTypeName, signedTokenString, cb);
		return FutureUtils.getFuture(closure);
	}

	public FluentFuture<MembershipInvtnSubmission> getMembershipInvitation(MembershipInvtnSignedToken token) {
		Consumer<AsyncCallback<MembershipInvtnSubmission>> closure = cb -> jsClient.getMembershipInvitation(token, cb);
		return FutureUtils.getFuture(closure);
	}

	public FluentFuture<Team> getTeam(String teamId) {
		Consumer<AsyncCallback<Team>> closure = cb -> jsClient.getTeam(teamId, cb);
		return FutureUtils.getFuture(closure);
	}

	public FluentFuture<UserProfile> getUserProfile(String userId) {
		Consumer<AsyncCallback<UserProfile>> closure = cb -> jsClient.getUserProfile(userId, cb);
		return FutureUtils.getFuture(closure);
	}
}
