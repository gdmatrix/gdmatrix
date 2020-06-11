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
package org.santfeliu.kernel.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author realor
 */
public class KernelUtils
{
  public static final int INVALID_NIF = 0;
  public static final int NATIONAL_NIF = 1;
  public static final int RESIDENT_NIF = 2;
  public static final int LEGAL_NIF = 3;
  public static final char CONTROL_ERROR = ' ';

  private static final Pattern NIF8_PATTERN =
    Pattern.compile("[A-Z0-9][0-9]{7}");

  /**
   * Returns the type of the given NIF number
   * 
   * @param nif the NIF number
   * @return the NIF type (NATIONAL_NIF, RESIDENT_NIF or LEGAL_NIF) or
   * INVALID_NIF if NIF is not valid.
   */
  public static int getNIFType(String nif)
  {
    if (nif == null || nif.length() == 0) return INVALID_NIF;

    char firstChar = nif.charAt(0);
    if (Character.isDigit(firstChar)) return NATIONAL_NIF;
    if ("XYZ".indexOf(firstChar) != -1) return RESIDENT_NIF;
    if ("ABCDEFGHJNPQRSUVW".indexOf(firstChar) != -1) return LEGAL_NIF;
    return INVALID_NIF;
  }

  /**
   * Validates a NIF number
   *
   * @param nif the NIF number to validate (9 characters long)
   * @return true if number is valid and false otherwise
   */
  public static boolean isValidNIF(String nif)
  {
    if (nif == null || nif.length() != 9) return false;
    
    char calculatedControl = calculateNIFControl(nif);
    if (calculatedControl == CONTROL_ERROR) return false;
    
    char control = nif.charAt(8);

    return control == calculatedControl;
  }

  /**
   * Calculates the control digit of the given NIF number
   *
   * @param nif the NIF number from which calculate the control digit. 
   * Must be 8 or 9 characters long.
   * @return the control digit or CONTROL_ERROR if nif is invalid
   */
  public static char calculateNIFControl(String nif)
  {
    if (nif == null) return CONTROL_ERROR;

    // take first 8 chars
    String code;
    switch (nif.length())
    {
      case 8:
        code = nif;
        break;
      case 9:
        code = nif.substring(0, 8);
        break;
      default:
        return CONTROL_ERROR;
    }

    Matcher matcher = NIF8_PATTERN.matcher(code);
    if (!matcher.matches()) return CONTROL_ERROR;
    
    int nifType = getNIFType(nif);
    if (nifType == NATIONAL_NIF || nifType == RESIDENT_NIF)
    {
      if (nifType == RESIDENT_NIF)
      {
        char firstChar = code.charAt(0);
        code = code.substring(1);
        if (firstChar == 'Y') code = "1" + code;
        else if (firstChar == 'Z') code = "2" + code;
      }
      try
      {
        int index = Integer.parseInt(code) % 23;
        return "TRWAGMYFPDXBNJZSQVHLCKE".charAt(index);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    else if (nifType == LEGAL_NIF)
    {
      int sum = code.charAt(2) + code.charAt(4) + code.charAt(6) - 3 * '0';

      for (int i = 1; i < 8; i += 2)
      {
        int val = 2 * (code.charAt(i) - 48);
        sum += val >= 10 ? 1 + val - 10 : val;
      }
      int index = 10 - (sum % 10);
      if (index == 10) index = 0;

      char firstChar = code.charAt(0);

      if ("ABCDEFGHJUV".indexOf(firstChar) != -1)
        return (char)('0' + index);

      if ("NPQRSW".indexOf(firstChar) != -1)
        return "JABCDEFGHI".charAt(index);
    }
    return CONTROL_ERROR;
  }
}
