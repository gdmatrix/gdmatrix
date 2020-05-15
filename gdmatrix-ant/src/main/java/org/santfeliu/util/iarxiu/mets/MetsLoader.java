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
package org.santfeliu.util.iarxiu.mets;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author blanquepa
 */

public class MetsLoader
{
  private Mets mets;

  public MetsLoader(Mets mets)
  {
    this.mets = mets;
  }

  public void load(InputStream is)
    throws Exception
  {
    DocumentBuilderFactory dbf =
      DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc = dbf.newDocumentBuilder().parse(is);

    Element metsNode = doc.getDocumentElement();
    String tagName = metsNode.getLocalName();

    if (!MetsConstants.METS_TAG.equals(tagName))
      throw new Exception("'" + MetsConstants.METS_TAG + "' tag expected");

    String type = metsNode.getAttribute(MetsConstants.TYPE_ATTRIBUTE);
    mets.setType(type);

    loadMets(metsNode);
  }

  private void loadMets(Element element) throws Exception
  {
    Node node = getFirstChild(element);
    while (node != null)
    {
      if (node instanceof Element)
      {
        element = (Element)node;
        String tagName = element.getLocalName();
        if (MetsConstants.DMD_SECTION_TAG.equals(tagName))
        {
          String id = element.getAttribute(MetsConstants.ID_ATTRIBUTE);
          System.out.print("loading dmdSec " + id + " ");
          loadDmdSection(element, id);
        }
        else if (MetsConstants.AMD_SECTION_TAG.equals(tagName))
        {
          String id = element.getAttribute(MetsConstants.ID_ATTRIBUTE);
          System.out.print("loading amdSec " + id + " ");
          loadAmdSection(element, id);
        }
        else if (MetsConstants.FILE_SECTION_TAG.equals(tagName))
        {
          System.out.println("loading fileSec");
          loadFileSection(element);
        }
        else if (MetsConstants.STRUCT_MAP_SECTION_TAG.equals(tagName))
        {
          System.out.println("loading structMap");
          loadStructMapSection(element);
        }
        else throw new Exception("Unexpected tag: " + tagName);
      }
      node = getNextSibling(node);
    }
  }

//PIT Sections
  private void loadStructMapSection(Element element) throws Exception
  {
    Node node = getFirstChild(element);
    if (!MetsConstants.DIV_TAG.equals(node.getLocalName()))
      throw new Exception("'" + MetsConstants.DIV_TAG +  "' tag expected");

    if (node instanceof Element)
    {
      Div struct = new Div();
      mets.setStruct(struct);

      Div div = new Div();
      while (node != null)
      {
        loadStructMapDiv((Element)node, div);
        mets.getStruct().getDivs().add(div);
        node = getNextSibling(node);
      }
    }
  }

  private void loadDmdSection(Element element, String id) throws Exception
  {
    try
    {
      loadMdSection(element, id, true);
      System.out.println("OK ");
    }
    catch (Exception ex)
    {
      System.out.println("ignored " + ex.getMessage());
    }
  }

  private void loadAmdSection(Element element, String id)
    throws Exception
  {
    try
    {
      Node node = getFirstChild(element);
      String tagName = node.getLocalName();

      if (MetsConstants.TECHMD_TAG.equals(tagName) ||
          MetsConstants.DIGIPROV_TAG.equals(tagName) ||
          MetsConstants.RIGHTS_TAG.equals(tagName) ||
          MetsConstants.SOURCE_TAG.equals(tagName))
      {
        if (node instanceof Element)
        {
          loadMdSection((Element)node, id, false);
        }
      }
      else throw new Exception("'" + MetsConstants.TECHMD_TAG + "','"
        + MetsConstants.DIGIPROV_TAG + "','" + MetsConstants.RIGHTS_TAG
        + "' or '" + MetsConstants.SOURCE_TAG + "' tag expected");


      System.out.println("OK");
    }
    catch (Exception ex)
    {
      System.out.println("ignored " + ex.getMessage());
    }
  }

  private void loadFileSection(Element element) throws Exception
  {
    Node node = getFirstChild(element);
    while (node != null)
    {
      String tagName = node.getLocalName();
      if (MetsConstants.FILEGRP_TAG.equals(tagName))
      {
        if (node instanceof Element)
        {
          element = (Element)node;
          String id = element.getAttribute(MetsConstants.ID_ATTRIBUTE);
          System.out.print("loading file " + id + " ");
          FileGrp fileGrp = new FileGrp();
          if (fileGrp != null)
          {
            fileGrp.setFilePath(mets.getFilePath());
            loadFile(element, fileGrp);
            mets.fileGrps.put(id, fileGrp);
            System.out.println("OK");
          }
          else
            System.out.println("ignored");
        }
      }
      else throw new Exception("'" + MetsConstants.FILEGRP_TAG + "' tag expected");

      node = getNextSibling(node);
    }
  }

//Metadata section contents
  private void loadMdSection(Element element, String id, boolean isDmdMetadata)
    throws Exception
  {
    Node node = getFirstChild(element);
    String tagName = node.getLocalName();
    if (MetsConstants.MDWRAP_TAG.equals(tagName))
    {
      if (node instanceof Element)
      {
        element = (Element)node;
        String mdType = element.getAttribute(MetsConstants.MDTYPE_ATTRIBUTE);

        //TODO: Aceptar otros tipos y schemas
        Metadata metadata = null;
        if (MetsConstants.OTHER_MDTYPE_VALUE.equals(mdType))
          metadata = new OtherMetadata();
        else if (MetsConstants.DC_MDTYPE_VALUE.equals(mdType))
          metadata = new DCMetadata();
        else if (MetsConstants.PREMIS_MDTYPE_VALUE.equals(mdType))
          metadata = new GenericXmlMetadata();  //TODO: specific Metadata
        else 
          metadata = new GenericXmlMetadata();

        try
        {
          metadata.setId(id);
          metadata.setType(mdType);
          metadata.load(element, mets);
        }
        catch (Exception ex)
        {
          if (!(metadata instanceof GenericXmlMetadata))
          {
            metadata = new GenericXmlMetadata();
            metadata.setId(id);
            metadata.setType(mdType);
            metadata.load(element, mets);
          }
        }

        if (isDmdMetadata)
          mets.dmdMetadatas.put(id, metadata);
        else
          mets.amdMetadatas.put(id, metadata);
      }
    }
    else throw new Exception("'" + MetsConstants.MDWRAP_TAG + "' tag expected");



  }

//  private void loadMetadata(Element element, Metadata metadata)
//    throws Exception
//  {
//    Node node = getFirstChild(element);
//    if (node instanceof Element)
//    {
//      element = (Element)node;
//      QName namespace = new QName(element.getNamespaceURI(),
//        element.getLocalName(), element.getPrefix());
//      if (!namespace.equals(metadata.namespace))
//        metadata.namespace = namespace;
//    }
//
//    //vocabulary elements (suposed only one level element)
//    node = getFirstChild(element);
//    while (node != null)
//    {
//      String name = node.getLocalName();
//      String value = node.getTextContent();
//      metadata.setProperty(name, value);
//      node = getNextSibling(node);
//    }
//  }

//File section contents
  private void loadFile(Element element, FileGrp content) throws Exception
  {
    Node node = getFirstChild(element);
    String tagName = node.getLocalName();
    if (MetsConstants.FILE_TAG.equals(tagName))
    {
      if (node instanceof Element)
      {
        element = (Element)node;
        String mimeType = element.getAttribute(MetsConstants.MIMETYPE_ATTRIBUTE);
        content.setMimeType(mimeType);
        String checksum = element.getAttribute(MetsConstants.CHECKSUM_ATTRIBUTE);
        content.setChecksum(checksum);
        String checksumType =
          element.getAttribute(MetsConstants.CHECKSUMTYPE_ATTRIBUTE);
        content.setChecksumType(checksumType);
        String createdDateTime =
          element.getAttribute(MetsConstants.CREATED_ATTRIBUTE);
        content.setCreatedDateTime(createdDateTime);

        //Admid
        String amdid = element.getAttribute(MetsConstants.ADMID_ATTRIBUTE);
        if (amdid != null && amdid.length() > 0)
        {
          String[] amdids = amdid.split(" ");
          for (int i = 0; i < amdids.length; i++)
          {
            Metadata metadata = mets.amdMetadatas.get(amdids[i]);
            if (metadata != null)
            {
              content.metadatas.add(metadata);
            }
          }
        }

        node = getFirstChild(element);
        tagName = node.getLocalName();
        if (MetsConstants.FLOCAT_TAG.equals(tagName))
        {
          if (node instanceof Element)
          {
            element = (Element)node;
            String href = element.getAttribute(MetsConstants.HREF_ATTRIBUTE);
            if (href == null || href.trim().length() == 0)
              throw new Exception("'" + MetsConstants.HREF_ATTRIBUTE + "' attribute expected");
            content.setHref(href);
          }
        }
        else
        {
          if (node instanceof Element)
          {
            element = (Element)node;
            String data = element.getTextContent();
            content.setData(data);
          }
        }
      }
    }
    else throw new Exception("'" + MetsConstants.FILE_TAG + "' tag expected");
  }

//StructMap Section contents
//  private void loadStructMapCase(Element element, PITCase cas) throws Exception
//  {
//    //Name
//    String name = element.getAttribute(METSConstants.LABEL_ATTRIBUTE);
//    cas.setName(name);
//
//    //Dmdid
//    String dmdid = element.getAttribute(METSConstants.DMDID_ATTRIBUTE);
//    if (dmdid != null && dmdid.length() > 0)
//    {
//      String[] dmdids = dmdid.split(" ");
//      for (int i = 0; i < dmdids.length; i++)
//      {
//        PITMetadata metadata = cas.initMetadata(dmdids[i]);
//        pitObjectsMap.put(dmdids[i], metadata);
//      }
//    }
//
//    //Admid
//    String amdid = element.getAttribute(METSConstants.ADMID_ATTRIBUTE);
//    if (amdid != null && amdid.length() > 0)
//    {
//      String[] amdids = amdid.split(" ");
//      for (int i = 0; i < amdids.length; i++)
//      {
//        PITMetadata metadata = cas.initMetadata(amdids[i]);
//        pitObjectsMap.put(amdids[i], metadata);
//      }
//    }
//
//    //Documents
//    Node node = getFirstChild(element);
//    while (node != null)
//    {
//      if (node.getLocalName().equals(METSConstants.DIV_TAG))
//      {
//        if (node instanceof Element)
//          loadStructMapDocument((Element)node, cas.addDocument());
//      }
//      else throw new Exception("'" + METSConstants.DIV_TAG + "' tag expected");
//      node = getNextSibling(node);
//    }
//  }
//

  private void loadStructMapDiv(Element element, Div div)
    throws Exception
  {
    //Name
    String label = element.getAttribute(MetsConstants.LABEL_ATTRIBUTE);
    div.setLabel(label);
    System.out.println("loading div " + label);

    //Dmdid
    String dmdid = element.getAttribute(MetsConstants.DMDID_ATTRIBUTE);
    if (dmdid != null && dmdid.length() > 0)
    {
      String[] dmdids = dmdid.split(" ");
      for (int i = 0; i < dmdids.length; i++)
      {
        Metadata metadata = mets.dmdMetadatas.get(dmdids[i]);
        if (metadata != null)
        {
          div.getDmdMetadatas().add(metadata);
        }
      }
    }

    //Admid
    String amdid = element.getAttribute(MetsConstants.ADMID_ATTRIBUTE);
    if (amdid != null && amdid.length() > 0)
    {
      String[] amdids = amdid.split(" ");
      for (int i = 0; i < amdids.length; i++)
      {
        Metadata metadata = mets.amdMetadatas.get(amdids[i]);
        if (metadata != null)
        {
          div.getAmdMetadatas().add(metadata);
        }
      }
    }

    //Contents
    Node node = getFirstChild(element);
    while (node != null)
    {
      if (node.getLocalName().equals(MetsConstants.DIV_TAG))
      {
        Div childDiv = new Div();
        loadStructMapDiv((Element)node.cloneNode(true), childDiv);
        div.getDivs().add(childDiv);
      }
      else if (node.getLocalName().equals(MetsConstants.FPTR_TAG))
      {
        String name = element.getAttribute(MetsConstants.LABEL_ATTRIBUTE);
        String id = ((Element)node).getAttribute(MetsConstants.FILEID_ATTRIBUTE);
        FileGrp content = mets.fileGrps.get(id);
        content.setId(id);
        content.setName(name);
        div.setFileGrp(content);
      }
      else throw new Exception(MetsConstants.DIV_TAG + "' or ' " +
        MetsConstants.FPTR_TAG + " tag expeceted");

      node = getNextSibling(node);
    }
  }
//
//  private void loadStructMapContent(Element element, PITContent content)
//    throws Exception
//  {
//    //Name
//    String name = element.getAttribute(METSConstants.LABEL_ATTRIBUTE);
//    content.name = name;
//
//    //Admid
//    String amdid = element.getAttribute(METSConstants.ADMID_ATTRIBUTE);
//    if (amdid != null && amdid.length() > 0)
//    {
//      String[] amdids = amdid.split(" ");
//      for (int i = 0; i < amdids.length; i++)
//      {
//        PITMetadata metadata = content.initMetadata(amdids[i]);
//        pitObjectsMap.put(amdids[i], metadata);
//      }
//    }
//
//    //Files
//    Node node = getFirstChild(element);
//    if (node.getLocalName().equals(METSConstants.FPTR_TAG))
//    {
//      if (node instanceof Element)
//      {
//        element = (Element)node;
//        String id = element.getAttribute(METSConstants.FILEID_ATTRIBUTE);
//        content.id = id;
//        pitObjectsMap.put(id, content);
//      }
//    }
//    else throw new Exception("'" + METSConstants.FPTR_TAG + "' tag expected");
//  }


  private Node getFirstChild(Element element)
  {
    Node node = element.getFirstChild();
    if (node != null && node.getNodeType() == Node.TEXT_NODE)
      node = node.getNextSibling();

    return node;
  }

  private Node getNextSibling(Node node)
  {
    Node result = node.getNextSibling();
      if (result != null && result.getNodeType() == Node.TEXT_NODE)
        result = result.getNextSibling();

    return result;
  }
}
