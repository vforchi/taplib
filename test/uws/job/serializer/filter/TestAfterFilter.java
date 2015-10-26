package uws.job.serializer.filter;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import uws.job.UWSJob;
import uws.job.parameters.UWSParameters;

public class TestAfterFilter {

	@Test
	public void testMatch() {
		GregorianCalendar cal = new GregorianCalendar(2010, 3, 1, 1, 0, 0);

		JobFilter filter = new AfterFilter(cal.getTime());
		
		// 1 second after => OK!
		cal.set(2010, 3, 1, 1, 0, 1);
		UWSJob testJob = new UWSJob("123456", null, new UWSParameters(), -1, cal.getTimeInMillis(), -1, null, null);
		assertTrue(filter.match(testJob));
		
		// Now => OK!
		testJob = new UWSJob("123456", null, new UWSParameters(), -1, (new Date()).getTime(), -1, null, null);
		assertTrue(filter.match(testJob));

		// Exactly same date => Nop!
		cal.set(2010, 3, 1, 1, 0, 0);
		testJob = new UWSJob("123456", null, new UWSParameters(), -1, cal.getTimeInMillis(), -1, null, null);
		assertFalse(filter.match(testJob));
		
		// 1 second before => Nop!
		cal.set(2010, 3, 1, 0, 59, 59);
		testJob = new UWSJob("123456", null, new UWSParameters(), -1, cal.getTimeInMillis(), -1, null, null);
		assertFalse(filter.match(testJob));
		
		// No start time => Nop!
		testJob = new UWSJob("123456", null, new UWSParameters(), -1, -1, -1, null, null);
		assertFalse(filter.match(testJob));
		
		// No job => Nop!
		assertFalse(filter.match(null));
	}

}
