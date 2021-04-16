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
package org.matrix.doc;

/**
 *
 * @author blanquepa
 */
public class DocumentConstants
{
  public static final String DOC_ADMIN_ROLE = "DOC_ADMIN";
  public static final String UNIVERSAL_LANGUAGE = "%%";

  public static final int LAST_VERSION = 0;
  public static final int NEW_VERSION = -1;
  //Delete flags
  public static final int DELETE_ALL_VERSIONS = -2;
  public static final int DELETE_OLD_VERSIONS = -3;
  public static final int PERSISTENT_DELETE = -4;
  //Find flags
  public static final int FIND_ALL_VERSIONS = -1;

  /* Document IN/OUT properties */
  public static final String DOCID = "docId";
  public static final String LANGUAGE = "language";
  public static final String VERSION = "version";
  public static final String TITLE = "title";
  public static final String DOCTYPEID = "docTypeId";
  public static final String STATE = "state";
  public static final String AUTHORID = "authorId";
  public static final String READ_ROLE = "readRole";
  public static final String WRITE_ROLE = "writeRole";
  public static final String DELETE_ROLE = "deleteRole";
  public static final String CLASSID = "classId";
  public static final String CASEID = "caseId";
  public static final String CONTENTID = "contentId";
  public static final String RELATED_DOC_LIST = "relatedDocList";
  public static final String ACL_LIST = "aclList";
  public static final String CREATION_DATE = "creationDate";
}
