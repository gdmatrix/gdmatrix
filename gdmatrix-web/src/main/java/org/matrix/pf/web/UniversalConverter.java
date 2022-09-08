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
package org.matrix.pf.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author blanquepa
 */
@FacesConverter(value = "universalConverter")
public class UniversalConverter implements Converter
{
  @Override
  public String getAsString(FacesContext context, UIComponent component, 
    Object entity)
  {
    String result = null;
        
    try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);)
    {
      oos.writeObject(entity);      
      result = Base64.getMimeEncoder().encodeToString(bos.toByteArray());
    }
    catch (IOException ex)
    {
      Logger.getLogger(UniversalConverter.class.getName())
        .log(Level.SEVERE, null, ex);
    }
    return result;
  }

  @Override
  public Object getAsObject(FacesContext context, UIComponent component, 
    String uuid)
  {
    Object result;  
    try
    {
      byte[] ser = Base64.getMimeDecoder().decode(uuid);
    
      try(ByteArrayInputStream bis = new ByteArrayInputStream(ser);
        ObjectInputStream ois = new ObjectInputStream(bis);)
      {
        result = (Object) ois.readObject();
      }
    }
    catch (Exception ex)
    {
      result = uuid;
    }
    return result;
  }
 
}
