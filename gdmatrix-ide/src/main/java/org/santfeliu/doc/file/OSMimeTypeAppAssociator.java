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
package org.santfeliu.doc.file;


import org.santfeliu.util.system.windows.WindowsUtils;


/**
 *
 * @author unknown
 */
public class OSMimeTypeAppAssociator implements MimeTypeAppAssociator
{
/*
  AssociationService associationService;
  
  public OSMimeTypeAppAssociator() 
  {
    associationService = new AssociationService();
  }

  public String getApplicationPath(String mimeType, String operation)
    throws Exception
  {
    String verb = toVerb(operation);
    if (verb != null)
    {
      Association association = associationService.getMimeTypeAssociation(mimeType);
      if (association != null)
      {
        Action action = association.getActionByVerb(verb);
        if (action != null)
          return action.getCommand();
      }
    }
    return null; 
  }

  public void setApplicationPath(String mimeType, String operation, 
                                 String path)
    throws Exception
  {
    String verb = toVerb(operation);
    if (verb != null)
    {
      Association association = associationService.getMimeTypeAssociation(mimeType);
      if (association != null)
      {
        Action action = association.getActionByVerb(verb);
        if (action != null)
          action.setCommand(path);
        else
          action = new Action(verb, path);
      }
      else
      {
        association = new Association();
        association.addAction(new Action(verb, path));
      }
      try
      {
        associationService.registerUserAssociation(association);
      }
      catch (AssociationAlreadyRegisteredException e1)
      {
        associationService.unregisterUserAssociation(association);
        associationService.registerUserAssociation(association);
      }
    }
  }

  
  private String toVerb(String operation)
  {
    if (MimeTypeAppAssociator.OPEN.equals(operation))
      return "open";
    else if (MimeTypeAppAssociator.EDIT.equals(operation))
      return "edit";
    else if (MimeTypeAppAssociator.PREVIEW.equals(operation))
      return "preview";
    else
      return null;
  }
  
*/

  public String getApplicationPath(String mimeType, String operation) 
   throws Exception
  {
    String app = WindowsUtils.getMimeTypeApplication(mimeType, operation);
    System.out.println("Application for [" + 
      mimeType + ", " + operation + "]: " + app);
    return app;
  }
 
 public void setApplicationPath(String mimeType, String operation, String path)
  throws Exception
  {
  }
}
