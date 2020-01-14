package com.audifilm.matrix.security;

import org.matrix.dic.DictionaryConstants;

public enum Action
{
  A(0, DictionaryConstants.CREATE_ACTION),
  D(1, DictionaryConstants.DELETE_ACTION),
  M(2, DictionaryConstants.WRITE_ACTION),
  I(3, DictionaryConstants.READ_ACTION);

  final public int index;
  final public String actionName;

  Action(int index, String actionName)
  {
    this.index = index;
    this.actionName = actionName;
  }

  public int getIndex()
  {
    return index;
  }

  public String getActionName()
  {
    return actionName;
  }

  static public Action getAction(String matrixAction)
  {
    if (matrixAction==null) return null;
    for(Action action: values()) {
      if (action.getActionName().equals(matrixAction)) return action;
    }
    return null;
  }

}
