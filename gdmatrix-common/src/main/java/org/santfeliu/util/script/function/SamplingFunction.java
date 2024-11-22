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

import java.util.Random;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author realor
 */

/*
 * Usage: sampling(int samplingRate, String samplingName)
 *
 * returns: true 1/samplingRate times for this samplingName
 *
 * Example:
 *
 *   sampling(10, 'sample23') ==> true
 */

public class SamplingFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length == 2)
    {
      int sampleRate = getSampleRate(args[0]);
      if (sampleRate > 0)
      {
        int seed = getSeed(args[1]);
        Random r = new java.util.Random(seed);
        int num = r.nextInt(sampleRate);
        System.out.println("SEED:" + seed);
        System.out.println("RANDOM:" + num);
        return Boolean.valueOf(num == 0);
      }
    }
    return Boolean.FALSE;
  }

  private int getSampleRate(Object arg)
  {
    int sampleRate;
    if (arg instanceof Number)
    {
      sampleRate = ((Number)arg).intValue();
    }
    else
    {
      sampleRate = 0;
    }
    return sampleRate;
  }

  private int getSeed(Object obj)
  {
    String value = String.valueOf(Context.jsToJava(obj, String.class));
    return value.hashCode();
  }
}
