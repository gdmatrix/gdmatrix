package org.santfeliu.web.obj.util;

/**
 * A RowStyleClassGenerator class has the mission to generate a style class name
 * taking a row object as parameter.
 * 
 * Used by pages that shows collections and needs styling depending on the 
 * data of every row.
 * 
 * @author blanquepa
 */

public abstract class RowStyleClassGenerator
{
  public abstract String getStyleClass(Object row);
}
