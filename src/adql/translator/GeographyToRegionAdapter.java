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
 * Copyright 2020 - European Southern Observatory (ESO)
 */

import adql.db.STCS;
import adql.db.STCS.Region;
import adql.parser.ParseException;
import com.microsoft.sqlserver.jdbc.Geography;
import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * Class that converts MS SQL Server internal geometry format into STCS.Region.
 * It uses the new Geography class, that has been introduced in version 8
 * of the SQLServer driver
 *
 * This class currently implements only the geometries used in the ESO
 * databases, i.e.
 * - Point
 * - Polygon
 * - GeometryCollection
 *
 * The class also assumes the following values in the preamble:
 * SRD=104001
 *
 * Author: Vincenzo Forchi`, vforchi@eso.org, vincenzo.forchi@gmail.com
 */
public class GeographyToRegionAdapter extends Geography {

    public static final STCS.CoordSys coordSys = new STCS.CoordSys(STCS.Frame.J2000, null, null);

    public GeographyToRegionAdapter(byte[] sqlServerInternalRepresentation) throws SQLServerException {
        super(sqlServerInternalRepresentation);
    }

    public Region toRegion() throws Exception {
        if (getSrid() != 104001) {
            throw new ParseException("Wrong coordinate system " + getSrid());
        }

        switch (this.STGeographyType()) {
            case "POINT":
                return parsePoint();
            case "POLYGON":
                return parsePolygon();
            case "GEOMETRYCOLLECTION":
                return parseGeometryCollection();
            default:
                return null;
        }
    }

    private Region parsePoint() {
        return new Region(coordSys, new double[]{ 180.0 - this.getLongitude(), this.getLatitude() });
    }

    private Region parsePolygon() {
        int lastPoint = numberOfPoints;
        if (figures.length > 1) {
            lastPoint = figures[1].getPointOffset() - 1;
        }
        return new Region(coordSys, getPoints(0, lastPoint));

//        if (figures.length > 1) {
//            return new Region(STCS.RegionType.UNION, coordSys, getRegionsFromFigures());
//        } else {
//            return new Region(coordSys, getPoints(0, numberOfPoints));
//        }
    }
    
    private Region parseGeometryCollection() {
        Region[] regions = getRegionsFromFigures();
        if (numberOfFigures == 1) {
            return regions[0];
        } else {
            return new Region(STCS.RegionType.UNION, coordSys, regions);
        }
    }

    private Region[] getRegionsFromFigures() {
        Region[] regions = new Region[numberOfFigures];
        int lastFirstPoint = numberOfPoints;
        for (int reg = numberOfFigures-1; reg >=0; reg--) {
            int firstPoint = figures[reg].getPointOffset();
            int lastPoint = Math.min(numberOfPoints, lastFirstPoint);
            lastFirstPoint = firstPoint;
            regions[reg] = new Region(coordSys, getPoints(firstPoint, lastPoint));
            if (figures[reg].getFiguresAttribute() == (byte) 0) {
                regions[reg] = new Region(regions[reg]);
            }
        }
        return regions;
    }

    private double[][] getPoints(int first, int last) {
        // we skip the last point because the polygon is closed
        double[][] points = new double[last-first-1][2];
        for (int point = first; point < last-1; point++) {
            points[point-first][0] = 180.0 - xValues[point];
            points[point-first][1] = yValues[point];
        }
        return points;
    }

}
