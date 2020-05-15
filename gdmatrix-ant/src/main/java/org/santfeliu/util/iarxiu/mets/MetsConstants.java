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

/**
 *
 * @author blanquepa
 */
public class MetsConstants
{
  public static final String XMLNS = "http://www.loc.gov/METS/";
  public static final String METS_PREFIX = "METS";

  public static final String METS_TAG = "mets";
  //DmdSec
  public static final String DMD_SECTION_TAG = "dmdSec";

  //AmdSec
  public static final String AMD_SECTION_TAG = "amdSec";
  public static final String TECHMD_TAG = "techMD";
  public static final String DIGIPROV_TAG = "digiprovMD";
  public static final String RIGHTS_TAG = "rightsMD";
  public static final String SOURCE_TAG = "sourceMD";

  //DmdSec & AmdSec
  public static final String MDWRAP_TAG = "mdWrap";
  public static final String XMLDATA_TAG = "xmlData";  

  //FileSec
  public static final String FILE_SECTION_TAG = "fileSec";
  public static final String FILEGRP_TAG = "fileGrp";
  public static final String FILE_TAG = "file";
  public static final String FLOCAT_TAG = "FLocat";
  public static final String FCONTENT_TAG = "FContent";
  public static final String BINDATA_TAG = "binData";

  //StructMap
  public static final String STRUCT_MAP_SECTION_TAG = "structMap";
  public static final String DIV_TAG = "div";
  public static final String FPTR_TAG = "fptr";

  public static final String TYPE_ATTRIBUTE = "TYPE";
  public static final String MDTYPE_ATTRIBUTE = "MDTYPE";
  public static final String OTHERMDTYPE_ATTRIBUTE = "OTHERMDTYPE";
  public static final String MIMETYPE_ATTRIBUTE = "MIMETYPE";
  public static final String CHECKSUM_ATTRIBUTE = "CHECKSUM";
  public static final String CHECKSUMTYPE_ATTRIBUTE = "CHECKSUMTYPE";
  public static final String CREATED_ATTRIBUTE = "CREATED";
  public static final String HREF_ATTRIBUTE = "xlink:href";
  public static final String FILEID_ATTRIBUTE = "FILEID";
  public static final String ID_ATTRIBUTE = "ID";
  public static final String LABEL_ATTRIBUTE = "LABEL";
  public static final String DMDID_ATTRIBUTE = "DMDID";
  public static final String ADMID_ATTRIBUTE = "ADMID";
  public static final String LOCTYPE_ATTRIBUTE = "LOCTYPE";

  public static final String OTHER_MDTYPE_VALUE = "OTHER";
  public static final String DC_MDTYPE_VALUE = "DC";
  public static final String PREMIS_MDTYPE_VALUE = "PREMIS";
}
