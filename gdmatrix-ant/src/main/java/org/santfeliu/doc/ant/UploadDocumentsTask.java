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
package org.santfeliu.doc.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.activation.DataHandler;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.util.WSEndpoint;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.santfeliu.ant.js.AntScriptable;
import org.santfeliu.ant.ws.WSTask;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FileDataSource;

/**
 *
 * @author blanquepa
 *
 * <uploadDocuments>
 *   <fileset dir="${dir}">
 *     <include name="*.*" />
 *   </fileset>
 *   <property name="docTypeId" value="Document" />
 *   <property name="title">
 *     file.getName();
  *   </property>
 * </uploadDocuments>
 */
public class UploadDocumentsTask extends WSTask
{
  //nested elements
  private FileSet fileSet;
  private List<PropertyElement> properties = new ArrayList<PropertyElement>();
  private String fileVar = "file";

  public void add(FileSet fileSet)
  {
    this.fileSet = fileSet;
  }

  public void addProperty(PropertyElement property)
  {
    this.properties.add(property);
  }

  public String getFileVar()
  {
    return fileVar;
  }

  public void setFileVar(String fileVar)
  {
    this.fileVar = fileVar;
  }

  public PropertyElement createProperty()
  {
    PropertyElement property = new PropertyElement();
    this.properties.add(property);

    return property;
  }

  @Override
  public void execute()
  {
    WSEndpoint endpoint = getEndpoint(DocumentManagerService.class);
    DocumentManagerPort port =
      endpoint.getPort(DocumentManagerPort.class, getUsername(), getPassword());

    Document document = new Document();
    document.setIncremental(true);

    if (fileSet == null)
    {
      log("No fileset");
      //Properties
      for (PropertyElement taskProp : properties)
      {
        String value = taskProp.getValue(getScriptable());
        DictionaryUtils.setProperty(document, taskProp.getName(), value);
      }
      port.storeDocument(document);
    }
    else
    {
      Iterator it = fileSet.iterator();
      while (it.hasNext())
      {
        File file = ((FileResource)it.next()).getFile();
        setVariable(fileVar, file);

        log("Storing " + file.toString());

        //Content
        FileDataSource ds = new FileDataSource(file);
        DataHandler dh = new DataHandler(ds);
        Content content = new Content();
        content.setData(dh);
        content.setContentType(dh.getContentType());

        document.setContent(content);

        //Properties
        for (PropertyElement taskProp : properties)
        {
          String value = taskProp.getValue(getScriptable());
          DictionaryUtils.setProperty(document, taskProp.getName(), value);
        }

        port.storeDocument(document);
      }
    }
  }

  public class PropertyElement
  {
    private String name;
    private String value;
    private String expression;

    public PropertyElement()
    {
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getValue(AntScriptable scriptable)
    {
      if (this.value != null)
        return this.value;
      else
      {
        Object result;
        Context cx = ContextFactory.getGlobal().enterContext();
        try
        {
          result = cx.evaluateString(
            scriptable, expression, "<expression>", 1, null);
          result = ((NativeJavaObject)result).unwrap();
        }
        finally
        {
          Context.exit();
        }

        if (result != null)
          return result.toString();
        else
          return null;
      }
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public void addText(String expression)
    {
      this.expression = expression;
    }
  }
}
