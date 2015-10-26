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

import uws.job.UWSJob;

/**
 * <p>Job filter based on the start time.
 * Only jobs that have been started are retained.</p>
 * 
 * @author Gr&eacute;gory Mantelet (ARI)
 * @version 4.2 (10/2015)
 * @since 4.2
 */
public final class StartedFilter implements JobFilter {

	@Override
	public boolean match(UWSJob job) {
		return (job != null) && (job.getStartTime() != null);
	}

}
