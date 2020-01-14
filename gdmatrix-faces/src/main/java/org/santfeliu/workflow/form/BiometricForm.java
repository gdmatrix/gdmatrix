package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class BiometricForm extends Form
{
  public BiometricForm()
  {
  }

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    set.add("biometricPDFDocId");
    return set;
  }
}
