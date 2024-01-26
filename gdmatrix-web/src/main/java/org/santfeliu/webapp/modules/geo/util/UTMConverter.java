/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.webapp.modules.geo.util;

/**
 *
 * @author vgrem
 */
public class UTMConverter
{

  /* Ellipsoid model constants (actual values here are for WGS84) */
  private static final double MAJOR_RADIUS = 6378137.0;
  private static final double MINOR_RADIUS = 6356752.314;
  private static final double SCALE_FACTOR = 0.9996;

  /**
   * Converts x and y coordinates in the Universal Transverse Mercator
   * projection to a latitude/longitude pair.
   *
   * @param x The easting of the point, in meters.
   * @param y The northing of the point, in meters.
   * @param zone The UTM zone in which the point lies.
   * @param southhemi True if the point is in the southern hemisphere; false
   * otherwise.
   */
  public static LatLng convertToLatLng(double x, double y, int zone, boolean southhemi)
  {

    x -= 500000.0;
    x /= SCALE_FACTOR;

    /* If in southern hemisphere, adjust y accordingly. */
    if (southhemi)
    {
      y -= 10000000.0;
    }

    y /= SCALE_FACTOR;

    double cmeridian = getCentralMeridian(zone);
    return mapPointToLatLng(x, y, cmeridian);
  }

  /**
   *   * Converts x and y coordinates in the Transverse Mercator projection to a
   * latitude/longitude pair. Note that Transverse Mercator is not the same as
   * UTM; a scale factor is required to convert between them.
   *
   * Reference: Hoffmann-Wellenhof, B., Lichtenegger, H., and Collins, J., GPS:
   * Theory and Practice, 3rd ed. New York: Springer-Verlag Wien, 1994.
   *
   * @param x The easting of the point, in meters.
   * @param y The northing of the point, in meters.
   * @param lambda0 Longitude of the central meridian to be used, in radians.
   * @return latitude/longitude pair of coordinates
   */
  private static LatLng mapPointToLatLng(double x, double y, double lambda0)
  {

    /* Get the value of phif, the footpoint latitude. */
    double phif = getFootpointLatitude(y);

    /* Precalculate ep2 */
    double ep2 = (Math.pow(MAJOR_RADIUS, 2.0) - Math.pow(MINOR_RADIUS, 2.0))
      / Math.pow(MINOR_RADIUS, 2.0);

    /* Precalculate cos (phif) */
    double cf = Math.cos(phif);

    /* Precalculate nuf2 */
    double nuf2 = ep2 * Math.pow(cf, 2.0);

    /* Precalculate Nf and initialize Nfpow */
    double Nf = Math.pow(MAJOR_RADIUS, 2.0) / (MINOR_RADIUS * Math.sqrt(1 + nuf2));
    double Nfpow = Nf;

    /* Precalculate tf */
    double tf = Math.tan(phif);
    double tf2 = tf * tf;
    double tf4 = tf2 * tf2;

    /* Precalculate fractional coefficients for x**n in the equations
           below to simplify the expressions for latitude and longitude. */
    double x1frac = 1.0 / (Nfpow * cf);

    Nfpow *= Nf;
    /* now equals Nf**2) */
    double x2frac = tf / (2.0 * Nfpow);

    Nfpow *= Nf;
    /* now equals Nf**3) */
    double x3frac = 1.0 / (6.0 * Nfpow * cf);

    Nfpow *= Nf;
    /* now equals Nf**4) */
    double x4frac = tf / (24.0 * Nfpow);

    Nfpow *= Nf;
    /* now equals Nf**5) */
    double x5frac = 1.0 / (120.0 * Nfpow * cf);

    Nfpow *= Nf;
    /* now equals Nf**6) */
    double x6frac = tf / (720.0 * Nfpow);

    Nfpow *= Nf;
    /* now equals Nf**7) */
    double x7frac = 1.0 / (5040.0 * Nfpow * cf);

    Nfpow *= Nf;
    /* now equals Nf**8) */
    double x8frac = tf / (40320.0 * Nfpow);

    /* Precalculate polynomial coefficients for x**n.
           -- x**1 does not have a polynomial coefficient. */
    double x2poly = -1.0 - nuf2;

    double x3poly = -1.0 - 2 * tf2 - nuf2;

    double x4poly = 5.0 + 3.0 * tf2 + 6.0 * nuf2 - 6.0 * tf2 * nuf2
      - 3.0 * (nuf2 * nuf2) - 9.0 * tf2 * (nuf2 * nuf2);

    double x5poly = 5.0 + 28.0 * tf2 + 24.0 * tf4 + 6.0 * nuf2 + 8.0 * tf2 * nuf2;

    double x6poly = -61.0 - 90.0 * tf2 - 45.0 * tf4 - 107.0 * nuf2
      + 162.0 * tf2 * nuf2;

    double x7poly = -61.0 - 662.0 * tf2 - 1320.0 * tf4 - 720.0 * (tf4 * tf2);

    double x8poly = 1385.0 + 3633.0 * tf2 + 4095.0 * tf4 + 1575 * (tf4 * tf2);

    /* Calculate latitude */
    double lat_rad = phif + x2frac * x2poly * (x * x)
      + x4frac * x4poly * Math.pow(x, 4.0)
      + x6frac * x6poly * Math.pow(x, 6.0)
      + x8frac * x8poly * Math.pow(x, 8.0);

    /* Calculate longitude */
    double lng_rad = lambda0 + x1frac * x
      + x3frac * x3poly * Math.pow(x, 3.0)
      + x5frac * x5poly * Math.pow(x, 5.0)
      + x7frac * x7poly * Math.pow(x, 7.0);

    return new LatLng(Math.toDegrees(lat_rad), Math.toDegrees(lng_rad));
  }

  /**
   * Computes the footpoint latitude for use in converting transverse Mercator
   * coordinates to ellipsoidal coordinates.
   *
   * Reference: Hoffmann-Wellenhof, B., Lichtenegger, H., and Collins, J., GPS:
   * Theory and Practice, 3rd ed. New York: Springer-Verlag Wien, 1994.
   *
   * @param y The UTM northing coordinate, in meters.
   * @return The footpoint latitude, in radians.
   */
  private static double getFootpointLatitude(double y)
  {

    /* Precalculate n (Eq. 10.18) */
    double n = (MAJOR_RADIUS - MINOR_RADIUS) / (MAJOR_RADIUS + MINOR_RADIUS);

    /* Precalculate alpha_ (Eq. 10.22) */
 /* (Same as alpha in Eq. 10.17) */
    double alpha_ = ((MAJOR_RADIUS + MINOR_RADIUS) / 2.0)
      * (1 + (Math.pow(n, 2.0) / 4) + (Math.pow(n, 4.0) / 64));

    /* Precalculate y_ (Eq. 10.23) */
    double y_ = y / alpha_;

    /* Precalculate beta_ (Eq. 10.22) */
    double beta_ = (3.0 * n / 2.0) + (-27.0 * Math.pow(n, 3.0) / 32.0)
      + (269.0 * Math.pow(n, 5.0) / 512.0);

    /* Precalculate gamma_ (Eq. 10.22) */
    double gamma_ = (21.0 * Math.pow(n, 2.0) / 16.0)
      + (-55.0 * Math.pow(n, 4.0) / 32.0);

    /* Precalculate delta_ (Eq. 10.22) */
    double delta_ = (151.0 * Math.pow(n, 3.0) / 96.0)
      + (-417.0 * Math.pow(n, 5.0) / 128.0);

    /* Precalculate epsilon_ (Eq. 10.22) */
    double epsilon_ = (1097.0 * Math.pow(n, 4.0) / 512.0);

    /* Now calculate the sum of the series (Eq. 10.21) */
    double result = y_ + (beta_ * Math.sin(2.0 * y_))
      + (gamma_ * Math.sin(4.0 * y_))
      + (delta_ * Math.sin(6.0 * y_))
      + (epsilon_ * Math.sin(8.0 * y_));

    return result;
  }

  /**
   * Determines the central meridian for the given UTM zone
   *
   * @param zone An integer value designating the UTM zone, range [1,60]
   * @return The central meridian for the given UTM zone, in radians, or zero if
   * the UTM zone parameter is outside the range [1,60]. Range of the central
   * meridian is the radian equivalent of [-177,+177]
   */
  private static double getCentralMeridian(int zone)
  {
    return Math.toRadians(-183.0 + (zone * 6.0));
  }

  public static class LatLng
  {
    public double lat;
    public double lng;

    public LatLng(double lat, double lng)
    {
      this.lat = lat;
      this.lng = lng;
    }

    @Override
    public String toString()
    {
      return "(" + lat + " " + lng + ")";
    }
  }

  public static void main(String args[])
  {
    LatLng pos = UTMConverter.convertToLatLng(420302.026, 4581897.712, 31, false);
    System.out.println(pos);
  }
}
