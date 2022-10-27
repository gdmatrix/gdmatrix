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
package org.matrix.presence;

/**
 *
 * @author realor
 */
public class PresenceConstants
{
  public static final String PRESENCE_ADMIN_ROLE = "PRESENCE_ADMIN";
  
  public static final String VALID_IP_ADDRESS_PARAM = "validIpAddress";
  public static final String NO_COMPENSABLE_IP_ADDRESSES_PARAM = "noCompensableIpAddresses";
  public static final String COMPENSATION_MIN_REST_HOURS_PARAM = "compensationMinRestHours";
  public static final String ENTRY_EDITION_DAYS_PARAM = "entryEditionDays";
  public static final String COUNTERS_INIT_DAY_PARAM = "countersInitDay";
  public static final String NO_WORK_ENTRY_TYPE_ID_PARAM = "noWorkEntryTypeId";

  public static final String PENDENT_STATUS = "P";
  public static final String IN_PROCESS_STATUS = "I";
  public static final String APPROVED_STATUS = "A";
  public static final String DENIED_STATUS = "D";
  public static final String CONSOLIDATED_STATUS = "C";
  public static final String CANCELLED_STATUS = "X";
}
