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
package org.santfeliu.doc.transform.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.fmt.odf.ODFUtils;
import org.santfeliu.doc.transform.Transformation;
import org.santfeliu.doc.transform.TransformationException;
import org.santfeliu.doc.transform.Transformer;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author realor
 */
public class MergeTransformer extends Transformer
{
  public static String MERGE = "merge";
  public ArrayList<Transformation> transformations;

  @Override
  public String getDescription()
  {
    return "MergeTransformer";
  }

  @Override
  public List<Transformation> getSupportedTransformations()
  {
    if (transformations == null)
    {
      transformations = new ArrayList<Transformation>();
      transformations.add(new Transformation(id, MERGE,
        "application/vnd.oasis.opendocument.text", null,
        "application/vnd.oasis.opendocument.text", null,
        null,
        "Merge document variables"));
    }
    return transformations;
  }

  @Override
  public DataHandler transform(Document document, String transformationName,
    Map options) throws TransformationException
  {
    try
    {
      Content content = document.getContent();
      DataHandler data = content.getData();
      File file = IOUtils.writeToFile(data);
      File mergedFile = File.createTempFile("merged", ".odt");
      ODFUtils.merge(file, mergedFile, options);
      file.delete();
      DataHandler dh = new DataHandler(new TemporaryDataSource(mergedFile));
      return dh;
    }
    catch (Exception ex)
    {
      throw new TransformationException(ex);
    }
  }
}
