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
package org.santfeliu.doc.swing.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import org.matrix.security.AccessControl;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Document;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.santfeliu.doc.swing.DocumentBasePanel;
import org.santfeliu.doc.swing.DocumentListPanel;
import org.santfeliu.doc.swing.DocumentSimplePanel;
import org.santfeliu.swing.Utilities;

/**
 *
 * @author unknown
 */
public class NewDocumentAction extends AbstractAction
{
  private DocumentBasePanel currentDocumentPanel;
  private String template;
  private Map values;
  private Map classes;
  private String copyType;
  
  public static String NEW_TRANSLATION = "NEW_TRANSLATION";
  public static String NEW_COMPONENT = "NEW_COMPONENT";
  public static String NEW_RELATED = "NEW_RELATED";
  public static String NEW_REDACTION = "NEW_REDACTION";
  public static String NEW_COPY = "NEW_COPY";
  
  public NewDocumentAction(DocumentBasePanel documentPanel)
  {
    this(documentPanel, null, null);
  }
  
  public NewDocumentAction(DocumentBasePanel documentPanel, String template, 
    String copyType)
  {
    this.currentDocumentPanel = documentPanel;
    this.template = template;
    this.putValue(Action.NAME, 
     (template != null ? template : documentPanel.getLocalizedText("new")));
    this.copyType = copyType;
  }

  public void actionPerformed(ActionEvent e)
  {
    try
    {
      DocumentSimplePanel documentPanel = new DocumentSimplePanel();
      documentPanel.setUsername(currentDocumentPanel.getUsername());
      documentPanel.setPassword(currentDocumentPanel.getPassword());
      if (currentDocumentPanel.getWsDirectoryURL() != null)
        documentPanel.setWsDirectoryURL(currentDocumentPanel.getWsDirectoryURL());
      else
        documentPanel.setWsdlLocation(currentDocumentPanel.getWsdlLocation());

      if (values == null) values = new HashMap();
      
      DocumentListPanel listPanel = (DocumentListPanel)currentDocumentPanel;

      String linkPropname = listPanel.getPropname();
      String linkValue = listPanel.getValue();
      if (linkPropname != null)
      {
        values.put(linkPropname, linkValue);
      }
      
      if (copyType != null)
      {
        Document document = listPanel.getClient().loadDocument(
          listPanel.getDocId(), 
          Integer.valueOf(listPanel.getDocVersion()));
        List<Property> propertyList = document.getProperty();
        for (Property property : propertyList)
        {
          values.put(property.getName(), property.getValue().get(0));
        }
        values.put(DocumentConstants.TITLE, document.getTitle());
        values.put(DocumentSimplePanel.DOCUMENT_TYPE, document.getDocTypeId());
        if (NEW_COPY.equals(copyType))
        {
          if (document.getContent() != null)
          {
            values.put(DocumentConstants.CONTENTID,
              document.getContent().getContentId());
            values.put(DocumentSimplePanel.MIMETYPE,
              document.getContent().getContentType());
            values.put(DocumentSimplePanel.SIZE,
              document.getContent().getSize());
            values.put(DocumentSimplePanel.FORMATDESC,
              document.getContent().getFormatDescription());
            values.put(DocumentSimplePanel.FORMATID,
              document.getContent().getFormatId());
          }
          documentPanel.putMultiValuedFields(document, values);
          List<AccessControl> accessControlList = document.getAccessControl();
          if (accessControlList.size() > 0)
          {
            values.put(DocumentConstants.ACL_LIST, accessControlList);
          }
        }
        List<RelatedDocument> relDocList = 
          generateRelatedDocuments(document);
        if (relDocList.size() > 0)
        {
          values.put(DocumentConstants.RELATED_DOC_LIST, relDocList);
        }
      }
      
      if (template != null)
      {
        Map map = (Map)classes.get(template);
        if (map != null)
        {
          values.putAll(map);
        }
      }
      
      if (values == null && classes == null)
      {
        documentPanel.clearFormValues();
      }
      else
      {
        documentPanel.setFormValues(values);
      }
      
      documentPanel.getNewVersionCheckBox().setSelected(true);
      JDialog dialog = Utilities.createDialog(
        documentPanel.getLocalizedText("document"), 500, 640, true,
        currentDocumentPanel, documentPanel);
      dialog.setVisible(true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      currentDocumentPanel.showError(ex);
    }  
  }

  public void setClasses(Map classes)
  {
    this.classes = classes;
  }

  public Map getClasses()
  {
    return classes;
  }

  public void setValues(Map values)
  {
    this.values = values;
  }

  public Map getValues()
  {
    return values;
  }
  
  private List<RelatedDocument> generateRelatedDocuments(Document document)
  {
    String docId = document.getDocId();
    Integer version = document.getVersion();
    List<RelatedDocument> relDocList = new ArrayList<RelatedDocument>();
    if (NEW_COPY.equals(copyType))
    {
      relDocList.addAll(document.getRelatedDocument());
    }
    else
    {
      RelatedDocument relDoc = new RelatedDocument();
      relDoc.setDocId(docId);
      relDoc.setVersion(version);
      if (NEW_TRANSLATION.equals(copyType))
      {
        relDoc.setRelationType(RelationType.REV_TRANSLATION);
        relDoc.setName(DocumentConstants.UNIVERSAL_LANGUAGE);
      }
      else if (NEW_COMPONENT.equals(copyType))
      {
        relDoc.setRelationType(RelationType.REV_COMPONENT);
        relDoc.setName("");
      }
      else if (NEW_RELATED.equals(copyType))
      {
        relDoc.setRelationType(RelationType.REV_RELATED);
        relDoc.setName("");
      }
      else if (NEW_REDACTION.equals(copyType))
      {
        relDoc.setRelationType(RelationType.REV_REDACTION);
        relDoc.setName("");
      }
      relDocList.add(relDoc);
    }
    return relDocList;
  }
}
