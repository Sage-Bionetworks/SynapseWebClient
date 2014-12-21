package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.web.client.widget.asynch.BatchTableFileHandleRequests;
import org.sagebionetworks.web.client.widget.asynch.TableFileHandleRequest;
import org.sagebionetworks.web.shared.table.CellAddress;

import com.google.gwt.core.client.Callback;

public class BatchTableFileHandleRequestsTest {
	
	BatchTableFileHandleRequests batch;
	String tableId;
	String[][] fileHandles = new String[][]{
			new String[] {"333","444","555"},
			new String[] {"111",null,"222"},
			new String[] {"222","222",null},
			new String[] {"555",null,null},
	};
	
	 Map<String, List<Callback<FileHandle, Throwable>>> mapFileHandleToCallback;
	 List<Callback<FileHandle, Throwable>> allCallbacks;

	@Before
	public void before(){
		mapFileHandleToCallback = new LinkedHashMap<String, List<Callback<FileHandle,Throwable>>>();
		allCallbacks = new LinkedList<Callback<FileHandle, Throwable>>();
		tableId = "syn123";
		batch = new BatchTableFileHandleRequests(tableId);
		// create a request for each non-null fileHandle.
		long versionCount = 0;
		for(long row=0; row<fileHandles.length; row++){
			for(long col=0;col<fileHandles[0].length; col++){
				String fileHandleId = fileHandles[(int) row][(int) col];
				if(fileHandleId != null){
					Callback<FileHandle,Throwable> callback = Mockito.mock(Callback.class);
					List<Callback<FileHandle, Throwable>> list = mapFileHandleToCallback.get(fileHandleId);
					if(list == null){
						list = new LinkedList<Callback<FileHandle, Throwable>>();
						mapFileHandleToCallback.put(fileHandleId, list);
					}
					list.add(callback);
					allCallbacks.add(callback);
					Long rowId = row;
					Long rowVersion = row+1;
					String columnId = ""+col;
					TableFileHandleRequest request = new TableFileHandleRequest(fileHandleId, new CellAddress(tableId, columnId, rowId, rowVersion), callback);
					batch.addRequest(request);
				}
			}
		}
	}
	
	@Test
	public void testBuildReferenceSet(){
		RowReferenceSet set = batch.buildReferenceSet();
		assertNotNull(set);
		assertEquals(tableId, set.getTableId());
		assertNotNull(set.getHeaders());
		assertNotNull(set.getRows());
		assertEquals(3, set.getHeaders().size());
		for(int col=0; col<3; col++){
			assertEquals(""+col, set.getHeaders().get(col).getId());
		}
		assertEquals(4, set.getRows().size());
		for(int row=0; row< 4; row++){
			RowReference ref = set.getRows().get(row);
			assertEquals(row, ref.getRowId().longValue());
			assertEquals(row+1, ref.getVersionNumber().longValue());
		}
	}
	
	@Test
	public void testBuildMapOfFileHandleIdToCallback(){
		assertEquals(mapFileHandleToCallback, batch.buildMapOfFileHandleIdToCallback());
	}
	
	@Test
	public void testGetAllCallbacks(){
		assertEquals(allCallbacks, batch.getAllCallbacks());
	}
}
