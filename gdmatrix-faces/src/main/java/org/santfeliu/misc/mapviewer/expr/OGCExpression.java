package org.santfeliu.misc.mapviewer.expr;

import java.util.HashMap;

/**
 *
 * @author realor
 */
public class OGCExpression
{
  final static HashMap<String, String> nativeToOgc = 
    new HashMap<String, String>();
  final static HashMap<String, String> ogcToNative =
    new HashMap<String,String>();
  
  static
  {
    register("Add", Function.ADD);
    register("Sub", Function.SUB);
    register("Mul", Function.MUL);
    register("Div", Function.DIV);
    register("And", Function.AND);
    register("Or", Function.OR);
    register("Not", Function.NOT);
    register("PropertyIsEqualTo", Function.EQUAL_TO);
    register("PropertyIsNotEqualTo", Function.NOT_EQUAL_TO);
    register("PropertyIsLessThan", Function.LESS_THAN);
    register("PropertyIsLessThanOrEqualTo", Function.LESS_EQUAL_THAN);
    register("PropertyIsGreaterThan", Function.GREATER_THAN);
    register("PropertyIsGreaterThanOrEqualTo", Function.GREATER_EQUAL_THAN);
    register("PropertyIsLike", Function.LIKE);
    register("PropertyIsNull", Function.IS_NULL);
    register("PropertyIsBetween", Function.BETWEEN);
  }
  
  static void register(String ogcFunction, String nativeFunction)
  {
    nativeToOgc.put(nativeFunction, ogcFunction);
    ogcToNative.put(ogcFunction, nativeFunction);
  }
  
  public static String getNativeFunction(String ogcFunction)
  {
    return ogcToNative.get(ogcFunction);
  }
  
  public static String getOgcFunction(String nativeFunction)
  {
    return nativeToOgc.get(nativeFunction);
  }
}
