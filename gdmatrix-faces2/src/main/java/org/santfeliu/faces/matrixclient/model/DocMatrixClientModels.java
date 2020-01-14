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
package org.santfeliu.faces.matrixclient.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

/**
 *
 * @author blanquepa
 */
public class DocMatrixClientModels implements Serializable
{
  public static final String DOCTYPES_PARAMETER = "docTypes";
  public static final String MAXFILESIZE_PARAMETER = "maxFileSize";
  
  public static final int SEND_COMMAND = 0;
  public static final int UPDATE_COMMAND = 1;
  public static final int EDIT_COMMAND = 2;
  
  private final Map<String,String> userDocTypes;
  private String docTypeId;
  private boolean mandatoryDocType;
  private String maxFileSize;
  private DefaultMatrixClientModel[] models;

  
  public DocMatrixClientModels(Map userDocTypes)
  {
    this(userDocTypes, null);
  }
  
  public DocMatrixClientModels(Map userDocTypes, String maxFileSize)
  {
    models = new DefaultMatrixClientModel[3];
    this.userDocTypes = userDocTypes;
    this.maxFileSize = maxFileSize;
    
    models[SEND_COMMAND] = new DefaultMatrixClientModel();    
    models[UPDATE_COMMAND] = new DefaultMatrixClientModel();
    models[EDIT_COMMAND] = new DefaultMatrixClientModel();
  }

  public DefaultMatrixClientModel getSendModel()
  {
    DefaultMatrixClientModel sendModel = models[SEND_COMMAND];
    //MaxFileSize
    if (maxFileSize != null)
      sendModel.putParameter(MAXFILESIZE_PARAMETER, maxFileSize);

    //DocTypeId
    if (docTypeId != null)
    {
      Type auxType = TypeCache.getInstance().getType(docTypeId);
      if (auxType != null)
      {
        String docType = (String) userDocTypes.get(auxType.getTypeId());
        if (mandatoryDocType)
        {
          Map altTypesMap = new HashMap();
          altTypesMap.put(docTypeId, docType);
          sendModel.putParameter(DOCTYPES_PARAMETER, altTypesMap);    
        }
        else
        {
          sendModel.putParameter(DocumentConstants.DOCTYPEID, auxType.getTypeId());
          sendModel.putParameter(DOCTYPES_PARAMETER, userDocTypes);
        }
      }
    }
    else
      sendModel.putParameter(DOCTYPES_PARAMETER, userDocTypes);

    return sendModel;
  }
  
  public DefaultMatrixClientModel getUpdateModel()
  {
    DefaultMatrixClientModel updateModel = models[UPDATE_COMMAND];
    updateModel.putParameter(DOCTYPES_PARAMETER, userDocTypes);
    if (maxFileSize != null)    
      updateModel.putParameter(MAXFILESIZE_PARAMETER, maxFileSize);    
    
    return updateModel;
  }
  
  public DefaultMatrixClientModel getEditModel()
  {
    return models[EDIT_COMMAND];
  }  
  
  /**
   * Sets the default docTypeId. 
   * Client will show document types combo with type selected.
   * @param docTypeId 
   */
  public void setDefaultDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
    this.mandatoryDocType = false;
  }
  
  /**
   * Sets a mandatory docTypeId. 
   * Client will show only this type in the combo. No other possibilities allowed.
   * @param docTypeId 
   */
  public void setMandatoryDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
    this.mandatoryDocType = true;
  }

  public String getMaxFileSize()
  {
    return maxFileSize;
  }

  public void setMaxFileSize(String maxFileSize)
  {
    this.maxFileSize = maxFileSize;
  }

}
