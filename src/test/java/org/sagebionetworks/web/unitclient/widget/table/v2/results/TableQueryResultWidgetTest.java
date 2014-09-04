package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableQueryResultWidgetTest {
	
	TableQueryResultView mockView;
	SynapseClientAsync mockSynapseClient;
	PortalGinInjector mockGinInjector;
	AdapterFactory adapterFactory;
	TableQueryResultWidget widget;

	@Before
	public void before(){
		mockView = Mockito.mock(TableQueryResultView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		adapterFactory = new AdapterFactoryImpl();
		widget = new TableQueryResultWidget(mockView, mockSynapseClient, mockGinInjector, adapterFactory);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setTableSchema(anyString(), any(List.class), any(AsyncCallback.class));
	}
	
	@Test
	public void test(){
		
	}
}
