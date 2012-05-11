package org.sagebionetworks.web.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.sagebionetworks.repo.model.Study;

/**
 * 
 * @author jmhill
 *
 */
public class RandomDatasetTest {
	
	@Ignore
	@Test
	public void testCreateRandom(){
		// Create a random dataset
		Study random = RandomDataset.createRandomDataset();
		assertNotNull(random);
		System.out.println(random);
		// Check all fields of the dataset
		assertNotNull(random.getName());
		assertNotNull(random.getCreatedOn());
		assertNotNull(random.getCreatedBy());
		assertNotNull(random.getDescription());
		assertNotNull(random.getId());
//		assertNotNull(random.getLayerPreviews());
		assertNotNull(random.getVersionLabel());
//		assertNotNull(random.getHasClinicalData());
//		assertNotNull(random.getHasExpressionData());
	}

}
