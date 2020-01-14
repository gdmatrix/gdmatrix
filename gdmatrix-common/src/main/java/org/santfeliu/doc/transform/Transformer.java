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
package org.santfeliu.doc.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import org.matrix.doc.Document;

/**
 *
 * @author realor
 */
public abstract class Transformer
{
  protected String id;
  protected HashMap<String, String> properties = new HashMap<String, String>();

  public abstract String getDescription();

  public abstract List<Transformation> getSupportedTransformations();
  
  public abstract DataHandler transform(Document document,
    String transformationName, Map options) throws TransformationException;

  public Transformer()
  {
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public HashMap<String, String> getProperties()
  {
    return properties;
  }

  @Override
  public String toString()
  {
    return getId() + ":" + getDescription();
  }
}

