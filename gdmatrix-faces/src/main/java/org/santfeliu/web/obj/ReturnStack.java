package org.santfeliu.web.obj;

import java.io.Serializable;

import java.util.Stack;

public class ReturnStack implements Serializable
{
  private Stack<Entry> stack;

  public ReturnStack()
  {
    stack = new Stack<Entry>();
  }

  public void push(String searchMid, String returnMid, 
    String objectId, String valueBinding, Object beans)
  {
    stack.push(new Entry(searchMid, returnMid, objectId, valueBinding, beans));
  }

  public Entry peek()
  {
    return stack.peek();
  }

  public Entry pop()
  {
    return stack.pop();
  }
  
  public boolean isEmpty()
  {
    return stack.isEmpty();
  }
  
  public class Entry implements Serializable
  {
    private String searchMid;
    private String returnMid;
    private String objectId;
    private String valueBinding;
    private Object beans;

    Entry(String searchMid, String returnMid, 
      String objectId, String valueBinding, Object beans)
    {
      this.searchMid = searchMid;
      this.returnMid = returnMid;
      this.objectId = objectId;
      this.valueBinding = valueBinding;
      this.beans = beans;
    }
    
    public String getSearchMid()
    {
      return searchMid;
    }

    public String getReturnMid()
    {
      return returnMid;
    }
    
    public String getObjectId()
    {
      return objectId;
    }

    public String getValueBinding()
    {
      return valueBinding;
    }
    
    public Object getBeans()
    {
      return beans;
    }
  }
}
