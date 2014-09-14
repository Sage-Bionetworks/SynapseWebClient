package QueryTokenProvider;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;

public class QueryTokenProviderTest {
	
	Query query;
	QueryTokenProvider provider;
	
	@Before
	public void before(){
		provider = new QueryTokenProvider(new AdapterFactoryImpl());
		// Setup a complex query
		query = new Query();
		query.setSql("select one, two, three from syn123 where name=\"bar\" and type in('one','two','three'");
		query.setLimit(101L);
		query.setOffset(33L);
		query.setIsConsistent(true);
		SortItem one = new SortItem();
		one.setColumn("one");
		one.setDirection(SortDirection.ASC);
		SortItem two = new SortItem();
		two.setColumn("one");
		two.setDirection(SortDirection.DESC);
		query.setSort(Arrays.asList(one, two));
	}
	
	@Test
	public void testRoundTrip(){
		// To JSON
		String json = provider.queryToToken(query);
		Query clone = provider.tokenToQuery(json);
		assertEquals(query, clone);
	}
	
	@Test
	public void testBadToken(){
		assertEquals("Invalid tokens should return null and not throw exceptions.", null, provider.tokenToQuery("Not a valid token"));
	}

}
