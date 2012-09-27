package org.sagebionetworks.web.unitclient.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class GovernanceServiceHelperTest {
	
	@Test
	public void testSignTermsOfUse() throws Exception {
		final String principalId = "101";
		final Long accessRequirementId = 102L;
		final Set<String> callbackInvoked = new HashSet<String>(); // simple mutable object to be used in callback
		final Callback onSuccess = new Callback(){
			@Override
			public void invoke() {
				callbackInvoked.add("callback invoked");
			}};
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable param) {
				fail("Exception not expected");
			}};
		final SynapseClientAsync synapseClient = (SynapseClientAsync) Proxy.newProxyInstance(
				GovernanceServiceHelperTest.class.getClassLoader(), 
				new Class<?>[]{SynapseClientAsync.class}, 
				 new InvocationHandler() {
					@Override
					public Object invoke(Object synapseClient, Method method, Object[] args)
							throws Throwable {
						if (method.equals(SynapseClientAsync.class.getMethod("createAccessApproval", EntityWrapper.class, AsyncCallback.class))) {
							EntityWrapper ew = (EntityWrapper)args[0];
							AdapterFactory adapterFactory = new AdapterFactoryImpl();
							JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
							
			    			@SuppressWarnings("unchecked")
							AccessApproval aa = jsonEntityFactory.createEntity(ew.getEntityJson(), 
										(Class<AccessApproval>)Class.forName(ew.getEntityClassName()));
			    			assertEquals(accessRequirementId, aa.getRequirementId());
			    			AsyncCallback callback = (AsyncCallback)args[1];
							callback.onSuccess(null);
						}
						return null;
					}
				 }
		);
		
		final JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
		
		GovernanceServiceHelper.signTermsOfUse(principalId,
				accessRequirementId,
				onSuccess,
				onFailure,
				synapseClient,
				jsonObjectAdapter
				);
		
		assertFalse(callbackInvoked.isEmpty());
	}
}
