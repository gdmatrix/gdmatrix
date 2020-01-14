package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author blanquepa
 */
public class ScanDocumentForm extends Form
{
  @Override
  public Set getReadVariables()
  {
    HashSet set = new HashSet();

    String resultVarName = (String)getParameters().get("resultVar");
    if (resultVarName != null) set.add(resultVarName);
    set.add("token");

    return set;
  }    
  
  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();

    String resultVarName = (String)getParameters().get("resultVar");
    if (resultVarName != null) set.add(resultVarName);
    set.add("token");

    return set;
  }  
}
