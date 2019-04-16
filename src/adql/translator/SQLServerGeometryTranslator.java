package adql.translator;

/*
 * This file is part of ADQLLibrary.
 *
 * ADQLLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ADQLLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ADQLLibrary.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2017 - European Southern Observatory (ESO)
 */

import adql.db.DBType;
import adql.db.STCS;
import adql.db.STCS.Region;
import adql.parser.ParseException;
import adql.query.operand.ADQLColumn;
import adql.query.operand.ADQLOperand;
import adql.query.operand.function.geometry.*;
import com.microsoft.sqlserver.jdbc.Geography;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class that implements the translation of ADQL spatial functions
 * into SQL specific to MS SQL Server.
 *
 * MS SQL Server uses Latitude and Longitude instead of RA and Dec,
 * where RA = Long + 180 and Dec = Lat.
 *
 * Box and Region are currently not implemented.
 *
 * @author Vincenzo Forch&igrave (ESO), vforchi@eso.org, vincenzo.forchi@gmail.com
 */
public class SQLServerGeometryTranslator extends SQLServerTranslator {

	public static int srid = 104001;
	/**
	 * the precision used in the str function to convert coordinate values: if this is not
	 * specified, they are rounded to integers
	 */
    public static final String strPrecision = ", 11, 7";

	@Override
	public String translate(final ExtractCoord extractCoord) throws TranslationException {
		throw new TranslationException(extractCoord.getName() + " is not supported");
	}

	@Override
	public String translate(final ExtractCoordSys extractCoordSys) throws TranslationException {
		throw new TranslationException("COORDSYS has been deprecated and is not supported");
	}

	@Override
	public String translate(final AreaFunction area) throws TranslationException {
		StringBuffer buf = new StringBuffer();
		buf.append("degrees(degrees((");
		buf.append(translate(area.getParameter()));
		buf.append(").STArea()").append("))");
		return buf.toString();
	}

	@Override
	public String translate(final CentroidFunction centroid) throws TranslationException {
		StringBuffer buf = new StringBuffer("(");
		buf.append(translate(centroid.getParameter(0)));
		buf.append(").EnvelopeCenter()");
		return buf.toString();
	}

	@Override
	public String translate(final DistanceFunction distance) throws TranslationException {
		StringBuffer buf = new StringBuffer();
		buf.append("degrees((");
		buf.append(translate(distance.getP1()));
		buf.append(").STDistance(");
		buf.append(translate(distance.getP2()));
		buf.append("))");
		return buf.toString();
	}

	@Override
	public String translate(final ContainsFunction contains) throws TranslationException {
		StringBuffer buf = new StringBuffer("(");

		/*
		 * The standard specifies that this function has two parameters and it returns
		 * true if the first one is contained in the second one.
		 * If the left parameter is a region we use STWithin, to make sure that the DB
		 * uses the index.
		 */
		if (contains.getLeftParam().getValue() instanceof ADQLColumn) {
			buf.append(translate(contains.getLeftParam()));
			buf.append(").STWithin(");
			buf.append(translate(contains.getRightParam()));
		} else {
			buf.append(translate(contains.getRightParam()));
			buf.append(").STContains(");
			buf.append(translate(contains.getLeftParam()));
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public String translate(final IntersectsFunction intersects) throws TranslationException {
		StringBuffer buf = new StringBuffer("(");
		/*
		 *  if the right parameter is a column we use it as the first operand, to make the DB
		 *  use the index.
		 */
		if (intersects.getRightParam().getValue() instanceof ADQLColumn) {
			buf.append(translate(intersects.getRightParam()));
			buf.append(").STIntersects(");
			buf.append(translate(intersects.getLeftParam()));
		} else {
			buf.append(translate(intersects.getLeftParam()));
			buf.append(").STIntersects(");
			buf.append(translate(intersects.getRightParam()));
		}
		buf.append(")");
		return buf.toString();
	}

	@Override
	public StringBuffer appendIdentifier(final StringBuffer str, final String id, final boolean caseSensitive) {
		/* in SQLServer columns are escaped with square brackets */
		if (caseSensitive && !id.matches("\"[^\"]*\""))
			return str.append('[').append(id).append(']');
		else
			return str.append(id);
	}

	@Override
	public String translate(final PointFunction point) throws TranslationException {
		return translatePoint(point.getCoord1(), point.getCoord2());
	}

	@Override
	public String translate(final CircleFunction circle) throws TranslationException {
		StringBuffer buf = new StringBuffer("(");
		buf.append(translatePoint(circle.getCoord1(), circle.getCoord2()));
		buf.append(").STBuffer(radians(cast(");
		buf.append(translate(circle.getRadius()));
		buf.append(" as double precision)))");
		return buf.toString();
	}

	/**
	 * Helper function to convert two operands into a POINT
	 * @param coord1
	 * @param coord2
	 * @return
	 * @throws TranslationException
	 */
	private String translatePoint(ADQLOperand coord1, ADQLOperand coord2) throws TranslationException {
		/*
		 * the point needs to be translated to something like
		 * 'POINT('+str(180.0-s_ra, 11, 7)+' '+ str(s_dec, 11, 7)+')'
		 */
		StringBuffer buf = new StringBuffer();
		buf.append("geography::STGeomFromText('POINT(");
		buf.append("'+str(180 -(");
		buf.append(translate(coord1));
		buf.append(")").append(strPrecision).append(")+' '+str(");
		buf.append(translate(coord2)).append(strPrecision);
		buf.append(")+')', 104001)");
		return buf.toString();
	}

	@Override
	public String translate(final BoxFunction box) throws TranslationException {
		throw new TranslationException("BOX is currently not implemented");
	}

	@Override
	public String translate(final PolygonFunction polygon) throws TranslationException {
		StringBuffer buf = new StringBuffer();
		buf.append("geography::STGeomFromText('POLYGON((");

		for (int i = 1; i < polygon.getNbParameters(); i+=2) {
			buf.append("'+str(180-(");
			buf.append(translate(polygon.getParameter(i)));
			buf.append(")").append(strPrecision).append(")+' '+str(");
			buf.append(translate(polygon.getParameter(i+1))).append(strPrecision);
			buf.append(")+',");
		}

		/* In SQLServer the polygon has to be closed, so we add the first point again */
		buf.append("'+str(180 - (");
		buf.append(translate(polygon.getParameter(1)));
		buf.append(")").append(strPrecision).append(")+' '+str(");
		buf.append(translate(polygon.getParameter(2))).append(strPrecision);
		buf.append(")+'))', 104001)");
		return buf.toString();
	}

	private static String regionToWKT(final Region region) {
		if (region.type == STCS.RegionType.POLYGON) {
			StringBuffer buf = new StringBuffer("POLYGON((");
			for (int i = 0; i < region.coordinates.length; i++) {
				buf.append(180 - region.coordinates[i][0]);
				buf.append(" ");
				buf.append(region.coordinates[i][1]);
				buf.append(",");
			}

			/* In SQLServer the polygon has to be closed, so we add the first point again */
			buf.append(180 - region.coordinates[0][0]);
			buf.append(" ");
			buf.append(region.coordinates[0][1]);
			buf.append("))");
			return buf.toString();
		} else if (region.type == STCS.RegionType.UNION) {
			StringBuffer wkt = new StringBuffer("GEOMETRYCOLLECTION(");
			wkt.append(
					Arrays.stream(region.regions)
							.map(SQLServerGeometryTranslator::regionToWKT)
							.collect(Collectors.joining(","))
			);
			wkt.append(")");
			return wkt.toString();
		}
		return null;
	}

	@Override
	public String translate(final RegionFunction region) throws TranslationException {
		throw new TranslationException("REGION is currently not implemented");
	}

    public static final STCS.CoordSys coordSys = new STCS.CoordSys(STCS.Frame.J2000, null, null);

	@Override
	public Region translateGeometryFromDB(final Object jdbcColValue) throws ParseException {
		return CLRToRegionParser.parseRegion((byte[]) jdbcColValue);
	}

	@Override
	public Object translateGeometryToDB(final Region region) throws ParseException {
		try {
			if (region.type == STCS.RegionType.POSITION) {
				byte[] wkb = Geography.point(region.coordinates[0][1], 180-region.coordinates[0][0], 104001).serialize();
				wkb[5] = (byte) 12;
				return wkb;
			} else if (region.type == STCS.RegionType.POLYGON || region.type == STCS.RegionType.UNION) {
				String wkt = regionToWKT(region);
				byte[] wkb = Geography.STGeomFromText(wkt, srid).serialize();
				wkb[5] = (byte) 4;
				return wkb;
			}
		} catch (SQLServerException e) {
			throw new ParseException(e.getMessage());
		}

        throw new ParseException("Unsupported Region " + region.type);
	}

	@Override
	public DBType convertTypeFromDB(final int dbmsType, final String rawDbmsTypeName, String dbmsTypeName, final String[] params) {
		if ("geography".equals(dbmsTypeName))
			return new DBType(DBType.DBDatatype.REGION);
		else
			return super.convertTypeFromDB(dbmsType, rawDbmsTypeName, dbmsTypeName, params);
	}

    @Override
    public String convertTypeToDB(DBType type) {
	    if (type.type == DBType.DBDatatype.POINT || type.type == DBType.DBDatatype.REGION)
            return "geography";
	    else
            return super.convertTypeToDB(type);
    }
}
