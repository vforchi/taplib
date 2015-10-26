package uws.job.serializer.filter;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 - Astronomisches Rechen Institut (ARI) 
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import uws.job.UWSJob;

/**
 * <p>This object aims to filter a list of jobs using a list of filters.</p>
 * 
 * <p>
 * 	A job is retained by the {@link #match(UWSJob)} function <b>only if</b> it passes <b>ALL</b> the listed filters.
 * 	In other words, this function operates a logical AND between all filters. Thus, if only a single filter is not passed,
 * 	the job is "rejected".
 * </p>
 * 
 * <p>
 * 	As said, this object operates a logical AND between filters. To perform a logical OR or more complex condition,
 * 	a specific {@link JobFilter} must be defined.
 * </p>
 * 
 * <p>
 * 	The only existing part of this class that can be overwritten is the constructor. Thus, it is easily possible
 * 	to add filters or to define its own list of filters.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (ARI)
 * @version 4.2 (10/2015)
 * @since 4.2
 */
public class JobFilters implements Iterable<JobFilter> {
	
	/** List of all filters to apply. All of these filters must match in order to keep a job. */
	protected final List<JobFilter> filters = new ArrayList<JobFilter>();

	public JobFilters(final HttpServletRequest request) {
		// TODO Extract job filters from the HTTP-GET parameters of the given HTTP request!
	}
	
	/**
	 * <p>Tell whether the given job matches all the job filters.
	 * In other words, this function operates a logical AND between all listed filters.</p>
	 * 
	 * <p><i>Note:
	 * 	If the given job is NULL, <code>false</code> will be returned.
	 * 	In case of error while evaluating one of the filters on the given job,
	 * 	<code>false</code> will be returned as well.
	 * </i></p>
	 * 
	 * @param job	A job to filter.
	 * 
	 * @return	<code>true</code> if the job matches all the filters, <code>false</code> otherwise.
	 */
	public final boolean match(final UWSJob job){
		if (job == null)
			return false;
		
		for(JobFilter filter : filters){
			if (!filter.match(job))
				return false;
		}
		
		return true;
	}

	@Override
	public final Iterator<JobFilter> iterator() {
		return filters.iterator();
	}

}
