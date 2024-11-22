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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.util.DataSourceWrapper;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author realor
 */
public class TransformationManager
{
  private static ArrayList<Transformer> transformers;
  static final Logger logger =
    Logger.getLogger("TransformationManager");

  public static void init()
  {
    File configDir = MatrixConfig.getDirectory();
    init(new File(configDir, "transformation.xml"));
  }

  public static void init(File setupFile)
  {
    try
    {
      if (setupFile.exists() && setupFile.isFile())
      {
        logger.log(Level.INFO, "Parsing file {0}", setupFile);
        parseFile(setupFile);
      }
      else logger.log(Level.WARNING, "File not found: {0}", setupFile);
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "Parse error: {0}", ex.toString());
    }
  }

  public static List<Transformer> getTransformers()
  {
    if (transformers == null) init();
    return transformers == null ?
      Collections.EMPTY_LIST : transformers;
  }

  public static Transformer getTransformer(String transformerId)
  {
    Transformer transformer = null;
    Iterator<Transformer> iter = getTransformers().iterator();
    boolean found = false;
    while (!found && iter.hasNext())
    {
      transformer = iter.next();
      found = transformer.getId().equals(transformerId);
    }
    return found ? transformer : null;
  }

  public static Transformation findTransformations(
    Transformation requestedTransformation,
    List<Transformation> transformations)
  {
    Iterator<Transformer> iter = getTransformers().iterator();
    while (iter.hasNext())
    {
      Transformer transformer = iter.next();
      List<Transformation> supportedTransformations =
        transformer.getSupportedTransformations();

      Iterator<Transformation> iter2 =
        supportedTransformations.iterator();
      while (iter2.hasNext())
      {
        Transformation transformation = iter2.next();
        if (transformation.isSuitableFor(requestedTransformation))
        {
          if (transformations == null) return transformation;
          else transformations.add(transformation);
        }
      }
    }
    return transformations != null && !transformations.isEmpty() ?
      transformations.get(0) : null;
  }

  public static Transformer findTransformers(
    Transformation requestedTransformation, List<Transformer> transformers)
  {
    Iterator<Transformer> iter = getTransformers().iterator();
    while (iter.hasNext())
    {
      Transformer transformer = iter.next();
      List<Transformation> supportedTransformations =
        transformer.getSupportedTransformations();

      Iterator<Transformation> iter2 =
        supportedTransformations.iterator();
      while (iter2.hasNext())
      {
        Transformation transformation = iter2.next();
        if (transformation.isSuitableFor(requestedTransformation))
        {
          if (transformers == null) return transformer;
          else if (!transformers.contains(transformer))
          {
            transformers.add(transformer);
          }
        }
      }
    }
    return transformers != null && !transformers.isEmpty() ?
      transformers.get(0) : null;
  }

  public static DataHandler transform(File file, TransformationRequest request)
    throws TransformationException
  {
    DataHandler dataHandler = new DataHandler(new FileDataSource(file));
    return transform(dataHandler, request);
  }

  public static DataHandler transform(URL url, TransformationRequest request)
    throws TransformationException
  {
    DataHandler dataHandler = new DataHandler(new URLDataSource(url));
    return transform(dataHandler, request);
  }

  public static DataHandler transform(DataHandler dataHandler,
    TransformationRequest request) throws TransformationException
  {
    Content content = new Content();
    content.setData(dataHandler);
    content.setContentType(dataHandler.getContentType());
    return transform(content, request);
  }

  public static DataHandler transform(Content content,
    TransformationRequest request) throws TransformationException
  {
    Document document = new Document();
    document.setContent(content);
    return transform(document, request);
  }

  public static DataHandler transform(Document document,
    TransformationRequest request) throws TransformationException
  {
    if (isTrivialTransformation(document, request))
    {
      Content content = document.getContent();
      DataHandler dataHandler = content.getData();
      String contentType = content.getContentType();
      if (!contentType.equals(dataHandler.getContentType()))
      {
        // different content types! fix contentType in returned dataHandler
        DataSource dataSource = new DataSourceWrapper(
          dataHandler.getDataSource(), contentType);
        dataHandler = new DataHandler(dataSource);
      }
      return dataHandler;
    }

    Transformation requiredTransformation =
      new Transformation(document, request);
    Transformation selectedTransformation =
      findTransformations(requiredTransformation, null);

    if (selectedTransformation == null)
      throw new TransformationException("Unsupported transformation");

    String transformerId = selectedTransformation.getTransformerId();
    Transformer transformer = getTransformer(transformerId);
    String transformationName = selectedTransformation.getName();
    Map options = request.getOptions();
    return transformer.transform(document, transformationName, options);
  }

  private static void parseFile(File setupFile)
    throws Exception
  {
    transformers = new ArrayList();
    FileInputStream is = new FileInputStream(setupFile);
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      org.w3c.dom.Document document = builder.parse(is);

      Node node = document.getFirstChild();
      while (!(node instanceof Element)) node = node.getNextSibling();

      node = node.getFirstChild();
      while (node != null)
      {
        if (node instanceof Element)
        {
          Element element = (Element)node;
          String tag = element.getTagName();
          if ("transformer".equals(tag))
          {
            Transformer transformer = parseTransformer(element);
            transformers.add(transformer);
          }
        }
        node = node.getNextSibling();
      }
    }
    finally
    {
      is.close();
    }
  }

  private static boolean isTrivialTransformation(Document document,
    TransformationRequest request)
  {
    String transformerId = request.getTransformerId();
    String name = request.getTransformationName();
    String targetContentType = request.getTargetContentType();
    String targetFormatId = request.getTargetFormatId();
    Content content = document.getContent();

    // transformer or transformation name specificied ? no trivial
    if (transformerId != null || name != null) return false;

    // different contentType ? no trivial
    if (targetContentType != null &&
      !targetContentType.equals(content.getContentType())) return false;

    // different formatId ? no trivial
    if (targetFormatId != null &&
      !targetFormatId.equals(content.getFormatId())) return false;

    return true;
  }

  private static Transformer parseTransformer(Element element)
    throws Exception
  {
    String className = element.getAttribute("class");
    Class cls = Class.forName(className);
    Transformer transformer = (Transformer)cls.getConstructor().newInstance();
    String transformerId = element.getAttribute("id");
    if (transformerId == null || transformerId.length() == 0)
      throw new Exception("transformedId is missing");
    transformer.setId(transformerId);

    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element propElem = (Element)node;
        if (propElem.getTagName().equals("property"))
        {
          String name = propElem.getAttribute("name");
          String value = propElem.getAttribute("value");
          if (value == null || value.length() == 0)
            value = propElem.getTextContent();
          transformer.getProperties().put(name, value);
        }
      }
      node = node.getNextSibling();
    }
    return transformer;
  }

  public static void main(String[] args)
  {
    try
    {
      System.setProperty("java.io.tmpdir", "c:/tomcat/temp");

      TransformationManager.init(
        new File("c:/matrix/conf/transformation.xml"));
//      System.out.println(TransformationManager.getTransformers());
//      System.out.println();
//
//      ArrayList<Transformation> list = new ArrayList<Transformation>();
//      Transformation rt = new Transformation("cms", "html", null, null, null, null, null, null);
//      TransformationManager.findTransformations(null, list);
//      for (Transformation t : list)
//      {
//        System.out.println(t);
//      }

//      ct.setTargetContentType("application/pdf");
//      List<TransformationRequest> list =
//        TransformationManager.findTransformations(new Content(), req, 10);
//      for (TransformationRequest ct2 : list)
//      {
//        System.out.println(ct2);
//      }
//      System.out.println();

//      TransformationRequest req = new TransformationRequest();
//      req.setTransformerId("cms");
//      req.setTransformationName("content");
//      DataHandler output =
//        TransformationManager.transform(new File("c:/test.p7m"), req);
//      System.out.println("output: " + output);
//      System.out.println("request: " + req);
//      if (output != null)
//      {
//        System.out.println("output:" + output);
//        IOUtils.writeToFile(output, new File("c:/out.doc"));
//      }

      TransformationRequest req = new TransformationRequest();
      req.setTransformerId("p7m");
      req.setTransformationName("pdf");
      DataHandler dh =
        TransformationManager.transform(new File("c:/document.p7m"), req);
      IOUtils.writeToFile(dh, new File("c:/out.pdf"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
