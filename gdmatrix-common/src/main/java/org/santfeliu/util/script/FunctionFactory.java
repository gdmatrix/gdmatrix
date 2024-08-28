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
package org.santfeliu.util.script;

import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.function.*;

/**
 *
 * @author realor
 */
public class FunctionFactory
{
  private static final BlankNullFunction blankNullFunction = new BlankNullFunction();
  private static final DecimalFormatFunction decimalFormatFunction = new DecimalFormatFunction();
  private static final HtmlEncodeFunction htmlEncodeFunction = new HtmlEncodeFunction();
  private static final TrimFunction trimFunction = new TrimFunction();
  private static final XmlEncodeFunction xmlEncodeFunction = new XmlEncodeFunction();
  private static final ParseDateFunction parseDateFunction = new ParseDateFunction();
  private static final FormatDateFunction formatDateFunction = new FormatDateFunction();
  private static final AddDateFunction addDateFunction = new AddDateFunction();
  private static final SamplingFunction samplingFunction = new SamplingFunction();
  private static final ToLocalIdFunction toLocalIdFunction = new ToLocalIdFunction();
  private static final SqlExecuteQueryFunction sqlExecuteQueryFunction = new SqlExecuteQueryFunction();
  private static final UrlEncodeFunction urlEncodeFunction = new UrlEncodeFunction();
  private static final DBConnectionFunction dbConnectionFunction = new DBConnectionFunction();
  private static final InputMultipleFunction inputMultipleFunction = new InputMultipleFunction();
  private static final TodayFunction todayFunction = new TodayFunction();
  private static final WebCounterFunction webCounterFunction = new WebCounterFunction();
  private static final JSONFunction jsonFunction = new JSONFunction();
  private static final IncludeFunction includeFunction = new IncludeFunction();
  private static final WSPortFunction wsCallFunction = new WSPortFunction();
  private static final LinkDocumentsFunction linkDocumentsFunction = new LinkDocumentsFunction();
  private static final SequenceNextValFunction sequenceNextValFunction = new SequenceNextValFunction();
  private static final IncludeScriptFunction includeScriptFunction = new IncludeScriptFunction();

  public static void initFunctions(Scriptable scriptable)
  {
    // init built-in functions
    scriptable.put("blankNull", scriptable, blankNullFunction);
    scriptable.put("decimalFormat", scriptable, decimalFormatFunction);
    scriptable.put("htmlEncode", scriptable, htmlEncodeFunction);
    scriptable.put("trim", scriptable, trimFunction);
    scriptable.put("xmlEncode", scriptable, xmlEncodeFunction);
    scriptable.put("parseDate", scriptable, parseDateFunction);
    scriptable.put("formatDate", scriptable, formatDateFunction);
    scriptable.put("addDate", scriptable, addDateFunction);
    scriptable.put("sampling", scriptable, samplingFunction);
    scriptable.put("toLocalId", scriptable, toLocalIdFunction);
    scriptable.put("sqlExecuteQuery", scriptable, sqlExecuteQueryFunction);
    scriptable.put("urlEncode", scriptable, urlEncodeFunction);
    scriptable.put("DBConnection", scriptable, dbConnectionFunction);
    scriptable.put("inputMultiple", scriptable, inputMultipleFunction);
    scriptable.put("today", scriptable, todayFunction);
    scriptable.put("webCounter", scriptable, webCounterFunction);
    scriptable.put("Json", scriptable, jsonFunction); // decrecated
    scriptable.put("include", scriptable, includeFunction);
    scriptable.put("wsport", scriptable, wsCallFunction);
    scriptable.put("linkDocuments", scriptable, linkDocumentsFunction);
    scriptable.put("sequenceNextVal", scriptable, sequenceNextValFunction);
    scriptable.put("includeScript", scriptable, includeScriptFunction);
  }
}
