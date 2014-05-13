package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableTestUtils {

	public static APITableInitializedColumnRenderer getInitializedRenderer(AsyncCallback<APITableInitializedColumnRenderer> mockCallback) {
		ArgumentCaptor<APITableInitializedColumnRenderer> captor = ArgumentCaptor.forClass(APITableInitializedColumnRenderer.class);
		verify(mockCallback).onSuccess(captor.capture());
		return captor.getValue();
	}
	
	public static void setInputValue(String v, String inputColumnName, Map<String, List<String>> columnData) {
		List<String> inputColumnData = new ArrayList<String>();
		inputColumnData.add(v);
		columnData.put(inputColumnName, inputColumnData);
	}

}
