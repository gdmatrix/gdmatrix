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
package org.santfeliu.doc.uploader;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.santfeliu.matrix.ide.MatrixIDE;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 *
 * @author realor
 */
  public class FileInfo
  {
    private final File file;
    private final int position;
    private HashMap metadata;
    private UploadInfo uploadInfo;

    FileInfo(File file, int position)
    {
      this.file = file;
      this.position = position;
    }

    public File getFile()
    {
      return file;
    }

    public String getName()
    {
      return file.getName();
    }

    public String getSimpleName()
    {
      String filename = file.getName();
      int index = filename.lastIndexOf(".");
      return index == -1 ? filename: filename.substring(0, index);
    }

    public String getExtension()
    {
      String filename = file.getName();
      int index = filename.lastIndexOf(".");
      return index == -1 ? "" : filename.substring(index + 1);
    }

    public String getPath()
    {
      String path = file.getAbsolutePath();
      return path.replace("\\", "/");
    }

    public int getPosition()
    {
      return position;
    }

    public FileInfo getParent()
    {
      File parent = file.getParentFile();
      return parent == null ? null : new FileInfo(parent, 0);
    }

    public String getState()
    {
      String state;

      if (getUploadInfo().getError() != null)
      {
        state = "ERROR";
      }
      else if (isUploaded())
      {
        if (isModified()) state = "MODIFIED";
        else state = "SYNCED";
      }
      else state = "NEW";
      return state;
    }

    public boolean isUploaded()
    {
      return getUploadInfo().isUploaded();
    }

    public boolean isModified()
    {
      UploadInfo info = getUploadInfo();
      if (!info.isUploaded()) return false;
      long lastModified = info.getLastModified();
      return lastModified != file.lastModified();
    }

    public long getLastModified()
    {
      return file.lastModified();
    }

    public long getLength()
    {
      return file.length();
    }

    public Map getMetadata()
    {
      if (metadata == null)
      {
        loadMetadata();
      }
      return metadata;
    }

    public boolean isImage()
    {
      String extension = getExtension().toLowerCase();
      if ("jpg".equals(extension)) return true;
      if ("jpeg".equals(extension)) return true;
      if ("gif".equals(extension)) return true;
      if ("png".equals(extension)) return true;
      if ("psd".equals(extension)) return true;
      if ("bmp".equals(extension)) return true;
      return false;
    }

    public boolean isExifImage()
    {
      String extension = getExtension().toLowerCase();
      if ("jpg".equals(extension)) return true;
      if ("jpeg".equals(extension)) return true;
      return false;
    }

    @Override
    public String toString()
    {
      return file.getAbsolutePath();
    }

    public UploadInfo getUploadInfo()
    {
      if (uploadInfo == null)
      {
        uploadInfo = new UploadInfo(file);
      }
      return uploadInfo;
    }

    private void loadMetadata()
    {
      metadata = new HashMap();
      try
      {
        // read all metadata
        if (isExifImage())
        {
          com.drew.metadata.Metadata md = ImageMetadataReader.readMetadata(file);
          Iterator<Directory> iter = md.getDirectories().iterator();
          while (iter.hasNext())
          {
            Directory d = iter.next();
            Collection<Tag> tags = d.getTags();
            for (Tag tag : tags)
            {
              String tagName = tag.getTagName();
              int tagType = tag.getTagType();
              Object value = d.getObject(tagType);
              String svalue;
              if (value instanceof String)
              {
                svalue = (String)value;
              }
              else if (value instanceof Integer)
              {
                svalue = String.valueOf((Integer)value);
              }
              else if (value instanceof Rational)
              {
                Rational rational = (Rational)value;
                svalue = String.valueOf(rational.doubleValue());
              }
              else if (value instanceof Rational[])
              {
                Rational[] rationals = (Rational[])value;
                if (rationals.length == 3)
                {
                  // assume deg:min:sec
                  double angle = rationals[0].doubleValue(); // degrees
                  angle += rationals[1].doubleValue() / 60; // minutes
                  angle += rationals[2].doubleValue() / 3600; // seconds
                  svalue = String.valueOf(angle);
                }
                else svalue = null;
              }
              else svalue = null;
              if (svalue != null)
              {
                metadata.put(tagName, svalue);
              }
            }
          }
          GpsDirectory gpsDir = md.getFirstDirectoryOfType(GpsDirectory.class);
          if (gpsDir != null)
          {
            GeoLocation geoLocation = gpsDir.getGeoLocation();
            if (geoLocation != null)
            {
              LatLng ll = new LatLng(geoLocation.getLatitude(),
                geoLocation.getLongitude());
              UTMRef UTMRef = ll.toUTMRef();
              metadata.put("UTM_ETRS89_x", UTMRef.getEasting());
              metadata.put("UTM_ETRS89_y", UTMRef.getNorthing());
              metadata.put("UTM_ED50_x", UTMRef.getEasting() + 94.2);
              metadata.put("UTM_ED50_y", UTMRef.getNorthing() + 204.2);
              metadata.put("UTM_lat_zone", String.valueOf(UTMRef.getLngZone()));
              metadata.put("UTM_lon_zone", String.valueOf(UTMRef.getLngZone()));
            }
          }
        }
      }
      catch (Exception ex)
      {
        MatrixIDE.log(ex);
      }
    }
  }

