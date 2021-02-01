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
package org.santfeliu.matrix.client.ui.scanner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author realor
 */
public class CmdTwain
{
  public static final String BW_MODE = "BW";
  public static final String GRAY_MODE = "GRAY";
  public static final String RGB_MODE = "RGB";
  
  private File programDirectory;  
  private String mode = RGB_MODE;
  private int dpi = 200;
  private int contrast = 0;
  private int brightness = 0;
  private boolean feeder = true;
  private boolean duplex = true;
  private boolean autoscan = true;
  private boolean autofeed = true;
//  private String paper;
//  private String paperSizeUnit;
//  private double paperWidth = 0;
//  private double paperHeight = 0;
  private ScanPaper paper;

  public CmdTwain()
  {
    this.paper = new ScanPaper();
  }
    
  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public int getDpi()
  {
    return dpi;
  }

  public void setDpi(int dpi)
  {
    this.dpi = dpi;
  }
  
  public int getContrast()
  {
    return contrast;
  }

  public void setContrast(int contrast)
  {
    this.contrast = contrast;
  }

  public int getBrightness()
  {
    return brightness;
  }

  public void setBrightness(int brightness)
  {
    this.brightness = brightness;
  }

  public boolean isFeeder()
  {
    return feeder;
  }

  public void setFeeder(boolean feeder)
  {
    this.feeder = feeder;
  }

  public boolean isDuplex()
  {
    return duplex;
  }

  public void setDuplex(boolean duplex)
  {
    this.duplex = duplex;
  }

  public boolean isAutoscan()
  {
    return autoscan;
  }

  public void setAutoscan(boolean autoscan)
  {
    this.autoscan = autoscan;
  }

  public boolean isAutofeed()
  {
    return autofeed;
  }

  public void setAutofeed(boolean autofeed)
  {
    this.autofeed = autofeed;
  }

  public ScanPaper getPaper()
  {
    return paper;
  }

  public void setPaper(ScanPaper paper)
  {
    this.paper = paper;
  }

  public BufferedImage[] scan() throws Exception
  {
    install();
    String target = programDirectory.getAbsolutePath().replace('/', '\\');
    Runtime runtime = Runtime.getRuntime();
    String cmd = programDirectory.getAbsolutePath() + 
      "/CmdTwain.exe " + getOptionsString() + " " + target + "\\image.bmp";
    Logger.getLogger(getClass().getName()).fine(cmd);
    Process exec = runtime.exec(cmd);
    exec.waitFor();

    File dir = new File(target);
    File[] imageFiles = dir.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File file)
      {
        return file.getName().startsWith("image");
      }
    });
    if (imageFiles.length > 0)
    {
      BufferedImage[] images = new BufferedImage[imageFiles.length];
      Arrays.sort(imageFiles, new Comparator()
      {
        @Override
        public int compare(Object o1, Object o2)
        {
          File file1 = (File)o1;
          File file2 = (File)o2;
          String name1 = file1.getName();
          String name2 = file2.getName();
          name1 = name1.replace("-", "0");
          name2 = name2.replace("-", "0");
          return name1.compareTo(name2);
        }
      });
      for (int i = 0; i < imageFiles.length; i++)
      {
        File imageFile = imageFiles[i];
        Logger.getLogger(getClass().getName()).log(
          Level.FINE, "delete {0}", imageFile.getName());
        images[i] = ImageIO.read(imageFile);
        imageFile.delete();
      }
      return images;
    }
    return null;
  }

  public void selectSource() throws Exception
  {
    install();
    Runtime runtime = Runtime.getRuntime();
    String cmd = programDirectory.getAbsolutePath() + "/cmdtwain.exe /source";
    Logger.getLogger(getClass().getName()).fine(cmd);
    Process exec = runtime.exec(cmd);
    exec.waitFor();
  }
  
  public void install() throws IOException
  {
    if (programDirectory == null)
    {
      String home = System.getProperty("user.home");
      programDirectory = new File(home + "/cmdtwain");
      if (!programDirectory.exists())
      {
        programDirectory.mkdirs();
      }
      installResource("CmdTwain.exe", programDirectory);
      installResource("CmdTwain.ini", programDirectory);
      installResource("Scan2bmps.exe", programDirectory);
      installResource("Scan2bmps.ini", programDirectory);
      installResource("libgcc_s_dw2-1.dll", programDirectory);
    }
  }
  
  private String getOptionsString()
  {
    StringBuilder buffer = new StringBuilder();
    
    String pSize = paper.getFormattedWidth(ScanPaper.Units.IN) + " " 
      + paper.getFormattedHeight(ScanPaper.Units.IN);
    pSize = pSize.replaceAll(",", ".");

    buffer.append("/IN /WH ").append(pSize);
    buffer.append(" /");
    buffer.append(mode);
    buffer.append(" ");
    buffer.append("/DPI=");
    buffer.append(dpi);
    buffer.append(" ");
    buffer.append("/BMP");
    
    if (brightness != 0)
    {
      buffer.append(" /S BR ");
      buffer.append(brightness);
    }
    if (contrast != 0)
    {
      buffer.append(" /S CO ");
      buffer.append(contrast);
    }
    buffer.append(" /S ADF ");
    buffer.append(feeder ? '1' : '0');

    buffer.append(" /S DPX ");
    buffer.append(duplex ? '1' : '0');
    
    buffer.append(" /S AF ");
    buffer.append(autofeed ? '1' : '0');

    buffer.append(" /S AS ");
    buffer.append(autoscan ? '1' : '0');

    return buffer.toString();
  }
  
  private void installResource(String filename, File targetDirectory)
    throws IOException
  {
    File out = new File(targetDirectory, filename);
    if (out.exists()) return;

    Class cls = this.getClass();
    InputStream is = cls.getResourceAsStream("cmdtwain/" + filename);
    try
    {
      byte[] data = new byte[1024];
      FileOutputStream os = new FileOutputStream(out);
      try
      {
        int nr = is.read(data);
        while (nr != -1)
        {
          os.write(data, 0, nr);
          nr = is.read(data);
        }
      }
      finally
      {
        os.close();
      }        
    }
    finally
    {
      is.close();
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      CmdTwain installer = new CmdTwain();
      installer.install();
    }
    catch (Exception ex)
    {      
      ex.printStackTrace();
    }
  }
}
