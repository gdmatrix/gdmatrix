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
package org.santfeliu.util.script.function;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.sequence.SQLSequenceStore;
import org.santfeliu.util.sequence.Sequence;
import org.santfeliu.util.sequence.SequenceManager;

/**
 *
 * @author blanquepa
 *
 * Usage: sequenceNextVal(String counterName, [String format], [String initialValue])
 *
 * counterName: Identifier name in TABLESEQ table.
 * format: JS expression in the form ${expression}. It formats counter value.
 *         There are two reserved words: 'year' (represents the current year in YYYY format)
 *         and 'number' (represents the counter value). By default format = ${number}.
 * initialValue: The first value that sequence starts to count. It must be a value that express
 *               a number. Not alphanumeric values allowed.
 *               If format contains 'year' then initValue has to reserve the first 4 digits to store
 *               current year value ('YYYY[d]+').
 *
 * returns: text representing the value of the counterName stored in sequence
 *          data store (TABLESEQ table), formated as format parameter expresses.
 *          The invocation to this function increments +1 the current value of
 *          the sequence.
 *
 * Examples:
 *
 *   ${sequenceNextVal('FamiliaCase.famcod')} => "1" (1 stored  internally)
 *   ${sequenceNextVal('FamiliaCase.numexp','${year}-${number}')} => "2015-1" (20151 stored internally)
 *   ${sequenceNextVal('FamiliaCase.numexp','${year}/${number}','0000001')} => 2015/001 (2015001 stored internally)
 *
 */
public class SequenceNextValFunction extends BaseFunction
{
  protected static final Logger logger = Logger.getLogger("SequenceValue");

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    Object result = null;

    if (thisObj == null) return null;

    if (args.length > 0)
    {
      String counterName = String.valueOf(args[0]);
      String format = null;
      if (args.length > 1) format = String.valueOf(args[1]);
      logger.log(Level.INFO, "counterName: {0}", counterName);
      try
      {
        SQLSequenceStore store = new SQLSequenceStore("jdbc/matrix");
        SequenceManager manager = new SequenceManager(store);
        if (args.length > 2)
          manager.setInitialValue(String.valueOf(args[2]));
        Sequence seq = manager.getSequence(counterName, format);
        result = seq.getValue(format);
      }
      catch(Exception ex)
      {
        logger.log(Level.SEVERE, "Error: {0}", ex.toString());
        throw new JavaScriptException(ex.getMessage(), "SequenceValue", 0);
      }
    }

    return result;
  }
}
