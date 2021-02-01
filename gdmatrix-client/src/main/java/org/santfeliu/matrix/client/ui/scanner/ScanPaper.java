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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 *
 * @author blanquepa
 */
public class ScanPaper
{
  private Size size;
  private Units units;
  private double width;
  private double height;
  private final DecimalFormat df;
  
  public enum Size
  {
    //Paper sizes in inches
    A0(33.11,46.811),
    A1(23.386,33.11),
    A2(16.535,23.386),
    A3(11.693,16.535),
    A4(8.268,11.693), //Default
    A5(5.827,8.268),
    A6(4.134,5.827),
    LEGAL(8.5,14.0),    
    LETTER(8.5,11.0),
    CUSTOM(8.268,11.693);

    private final double width;
    private final double height; 
    
    Size(double width, double height)
    {
      this.width = width;
      this.height = height;
    }
    
    public String getName()
    {
      return name();
    }
    
    public double getWidth()
    {
      return width;
    }
    
    public double getWidth(Units unit)
    {
      return width * (unit != null ? unit.getConversion() : 1.0);
    }
    
    public double getHeight()
    {
      return height;
    }

    public double getHeight(Units unit)
    {
      return height * (unit != null ? unit.getConversion() : 1.0);
    }
  }
  
  public enum Units
  {
    CM(2.54),
    IN(1); //Inches are the internal reference unit
    
    private final double conversion;
    
    Units(double conversion)
    {
      this.conversion = conversion;
    }
    
    public double getConversion()
    {
      return this.conversion;
    }
    
    public String getName()
    {
      return name();
    }   
  } 
  
  public ScanPaper()
  {
    this.df = new DecimalFormat("#.##", 
      DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    this.size = Size.A4;
    this.units = Units.CM;
    this.width = size.getWidth(units);
    this.height = size.getHeight(units);
  }
  
  public ScanPaper(Size size, Units units)
  {
    this.df = new DecimalFormat("#.##", 
      DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    this.size = size;
    this.units = units;
    this.width = size.getWidth(units);
    this.height = size.getHeight(units);
  }
  
  public ScanPaper(Size size, Units units, String swidth, String sheight)
  {
    this(size, units);
    setWidth(swidth);
    setHeight(sheight);
  }
  
  public Size getSize()
  {
    return size;
  }

  public void setSize(Size size)
  {
    this.size = size;
    if (size != Size.CUSTOM)
    {
      this.width = size.getWidth(units);
      this.height = size.getHeight(units);
    }
  }

  public Units getUnits()
  {
    return units;
  }

  public void setUnits(Units units)
  {
    if (size == Size.CUSTOM)
    {
      this.width = 
        this.width / this.units.getConversion() * units.getConversion();
      this.height = 
        this.height / this.units.getConversion() * units.getConversion();
    }
    else
    {
      this.width = size.getWidth(units);
      this.height = size.getHeight(units);
    }
    this.units = units;
  }

  public double getWidth()
  {
    return width;
  }

  public void setWidth(double width)
  {
    if (Size.CUSTOM == this.size)
      this.width = width;
    else
      this.width = this.size.getWidth(units);
  }
  
  public final void setWidth(String swidth)
  {
    try
    {
      Number number = df.parse(swidth);
      setWidth(number.doubleValue());
    }
    catch (ParseException ex)
    {
      this.width = this.size.getHeight(units);
    }
  }

  public double getHeight()
  {
    return height;
  }

  public void setHeight(double height)
  {
    if (Size.CUSTOM == this.size)
      this.height = height;
    else
      this.height = this.size.getHeight(units);
  }
  
  public final void setHeight(String sheight)
  {
    try
    {
      Number number = df.parse(sheight);
      setHeight(number.doubleValue());
    }
    catch (ParseException ex)
    {
      this.height = this.size.getHeight(units);
    }
  }
  
  public String getFormattedWidth()
  {
    return df.format(this.width);
  }
  
  public String getFormattedHeigth()
  {
    return df.format(this.height);
  }
  
  public String getFormattedWidth(Units units)
  {
    double inches = (this.width / this.units.getConversion());
    return df.format(inches * units.getConversion());
  }
  
  public String getFormattedHeight(Units units)
  {
    double inches = (this.height / this.units.getConversion());
    return df.format(inches * units.getConversion());
  }
  
}
