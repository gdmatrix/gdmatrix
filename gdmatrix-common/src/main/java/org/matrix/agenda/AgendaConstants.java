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
package org.matrix.agenda;

/**
 *
 * @author blanquepa
 */
public class AgendaConstants
{
  public static final String AGENDA_ADMIN_ROLE = "AGENDA_ADMIN";

  public static final String NODENAME = "agenda.nodeName";

//---------> Para eliminar
  public static final String CONTENT ="content";
  public static final String INITTIME ="inittime";
  public static final String ENDTIME ="endtime";
  public static final String THEMES ="themes";
  public static final String EVENTYPE ="eventype";
  public static final String MAXROWS ="maxrows";
  public static final String ORDERBY = "orderby";
  public static final String THEMEID = "temacod";

  public static final String EVENTID ="eventid";
  public static final String EVENTNAME ="eventname";
  public static final String EVENTTYPEID ="eventtypeid";
  public static final String EVENTTYPENAME ="eventtypename";
  public static final String OBSERV = "observ";

  public static final String PERSONID = "personid";
  public static final String STARTCHANGEDATETIME = "startchangedt";
  public static final String ENDCHANGEDATETIME = "endchangedt";
  
  public static final String START_DATE_COMPARATOR = "S";
  public static final String END_DATE_COMPARATOR = "E";
  public static final String ACTIVE_DATE_COMPARATOR = "R";
  
  public static final String DELETED_EVENT_DATETIME = "00010101000000";
  
}
