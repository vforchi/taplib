package uws.job.serializer.filter;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import uws.UWSException;
import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;

public class TestStartedFilter {

	@Test
	public void testMatch() {
		StartedFilter filter = new StartedFilter();
		
		assertFalse(filter.match(null));
		
		UWSJob testJob = new UWSJob(new UWSParameters());
		assertFalse(filter.match(testJob));
		
		testJob = new UWSJob("123456", null, new UWSParameters(), -1, (new Date()).getTime(), -1, null, null);
		assertTrue(filter.match(testJob));
	}

}
