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
package org.santfeliu.elections;

import java.awt.Color;

import java.util.ArrayList;


/**
 *
 * @author unknown
 */
public class Party
{
  private String id;
  private String abbreviation;
  private String description;
  private ArrayList councillors = new ArrayList();
  private String logo;
  private Color color;
  private int order;

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setAbbreviation(String abbreviation)
  {
    this.abbreviation = abbreviation;
  }

  public String getAbbreviation()
  {
    return abbreviation;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setOrder(int order)
  {
    this.order = order;
  }

  public int getOrder()
  {
    return order;
  }

  public Councillor getCouncillor(int index) // one based
  {
    return (Councillor)councillors.get(index - 1);
  }
  
  public void addCouncillor(String name, String imageURL)
  {
    Councillor councillor = new Councillor(this);
    councillor.setOrder(councillors.size() + 1);
    councillor.setName(name);
    councillor.setImageURL(imageURL);
    councillors.add(councillor);
  }

  public void setLogo(String logo)
  {
    this.logo = logo;
  }

  public String getLogo()
  {
    return logo;
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  public Color getColor()
  {
    return color;
  }

  public String toString()
  {
    return id + " " + abbreviation + " " + description + "\n";
  }  
}
