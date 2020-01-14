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

import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author blanquepa
 */
public class ScriptableBaseTest
{
  
  public ScriptableBaseTest()
  {
  }
  
  @Test
  public void testScript1()
  {
    System.out.println("script 1");
    String code = "var a=1;a;";
    evaluateCode(code);
  }
  
  @Test
  public void testScript1b()
  {
    System.out.println("script 1b");
    String code = "a=1;a;";
    evaluateCode(code);
  }

  @Test
  public void testScript2()
  {
    System.out.println("script 2");
    String code = "var a=null;a;";
    evaluateCode(code);
  }
  
  @Test
  public void testScript2b()
  {
    System.out.println("script 2b");
    String code = "a=null;a;";
    evaluateCode(code);    
  }

  @Test
  public void testScript3()
  {
    System.out.println("script 3");
    String code = "var a=1;a=null;a;";
    evaluateCode(code);
  }
  
  @Test
  public void testScript3b()
  {
    System.out.println("script 3b");
    String code = "a=1;a=null;a;";
    evaluateCode(code);
  }
  
  @Test
  public void testScript4()
  {
    System.out.println("script 4");
    String code = "var a;";
    evaluateCode(code);
  }  
  
  @Test
  public void testScript4b()
  {
    System.out.println("script 4b");
    String code = "a;";
    
    Context cx = ContextFactory.getGlobal().enterContext();   
    ScriptableBase instance = new ScriptableBase(cx);    
    Object result = cx.evaluateString(instance, code, "<code>", 1, null); 

    assertEquals(null, result);
  }  
  
  @Test
  public void testScript5()
  {
    System.out.println("script 5");
    String code = "var a=1;function f(){a=null};f();a;";
    evaluateCode(code);
  }    
  
  @Test
  public void testScript5b()
  {
    System.out.println("script 5b");
    String code = "var a=1;function f(){var a=null};f();a;";
    evaluateCode(code);
  }   

  @Test
  public void testScript5c()
  {
    System.out.println("script 5c");
    String code = "a=1;function f(a){var a=null};f(a);a;";
    evaluateCode(code);
  }  
  
  @Test
  public void testScript6()
  {
    System.out.println("script 6");
    String code = "var a=1;function f(){var a=2};f();a;";
    evaluateCode(code);
  } 
  
  @Test
  public void testScript6b()
  {
    System.out.println("script 6b");
    String code = "var a=1;function f(){a=2};f();a;";
    evaluateCode(code);
  }   
  
  @Test
  public void testScript6c()
  {
    System.out.println("script 6c");
    String code = "a=1;function f(){var a=2};f();a;";
    evaluateCode(code);
  }    


  @Test
  public void testScript6d()
  {
    System.out.println("script 6d");
    String code = "a=1;function f(){a=2};f();a;";
    evaluateCode(code);
  }
  
  @Test
  public void testScript7()
  {
    System.out.println("script 7");
    String code = "var a=1;function f(){var a=null;return a};a=f();a";
    evaluateCode(code);
  }    
  
  @Test
  public void testScript8()
  {
    System.out.println("script 8");
    String code = "var a=1;function f(){return b};a=f();a";
    Context cx = ContextFactory.getGlobal().enterContext();   
    ScriptableBase instance = new ScriptableBase(cx);    
    Object result = cx.evaluateString(instance, code, "<code>", 1, null); 

    assertEquals(null, result);
  }  

  /**
   * Compares ScriptableBase scope versus standard scriptable scope.
   * @param code 
   */
  private void evaluateCode(String code)
  {
    Context cx = ContextFactory.getGlobal().enterContext(); 
    
    //ScriptableBase instance
    ScriptableBase instance = new ScriptableBase(cx);    
    Object result1 = cx.evaluateString(instance, code, "<code>", 1, null);    

    //Standard scriptable instance
    Scriptable instance2 = cx.initStandardObjects();
    Object result2 = cx.evaluateString(instance2, code, "<code>", 1, null);    
    
    //Compares ScriptableBase vs. Scriptable
    assertEquals(result1, result2);
  }
  
}
