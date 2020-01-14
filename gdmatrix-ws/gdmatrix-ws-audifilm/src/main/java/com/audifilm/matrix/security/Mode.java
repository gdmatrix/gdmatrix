package com.audifilm.matrix.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author comasfc
 */
public class Mode {

  final static private int MODE_LENGTH = 8;
  final static public char INDETERMINAT = 0;
  final static public char DENEGAT = ' ';

  final static public Mode MODE_SUPERUSUARI = new Mode("ADMI");
  final static public Mode MODE_DENEGAT = new Mode("    ");

  char [] mode;

  static public Mode getModeDenegat()
  {
    return MODE_DENEGAT;
  }
  static public Mode getModeSuperusuari()
  {
    return MODE_SUPERUSUARI;
  }

  public Mode()
  {
    mode = new char[MODE_LENGTH];
    Arrays.fill(mode, INDETERMINAT);
  }

  public Mode(char [] mode)
  {
    this();
    if (mode==null) return;
    int minLength = Math.min(this.mode.length, mode.length);
    for(int i=0; i<minLength; i++)
    {
      this.mode[i] = mode[i];
    }
  }

  public Mode(String mode)
  {
    this();
    if (mode==null) return;
    int minLength = Math.min(this.mode.length, mode.length());
    for(int i=0; i<minLength; i++)
    {
      this.mode[i] = mode.charAt(i);
    }
  }

  public void setMode(char [] mode) {
    for(int i=0; i<mode.length && i<this.mode.length; i++)
    {
      this.mode[i] = mode[i];
    }
  }

  public void setMode(String mode) {
    for(int i=0; i<mode.length() && i<this.mode.length; i++)
    {
      this.mode[i] = mode.charAt(i);
    }
  }

  public char [] getMode() {
    return Arrays.copyOf(mode, MODE_LENGTH);
  }

  public Mode sumModes(Mode parentMode)
  {
    Mode newMode = new Mode();
    for(int i=0; i<newMode.mode.length; i++)
    {
      newMode.mode[i] = sumValues(this.mode[i], parentMode.mode[i]);
    }
    return newMode;
  }

  public Mode mergeModes(Mode parentMode)
  {
    Mode newMode = new Mode();
    for(int i=0; i<newMode.mode.length; i++)
    {
      newMode.mode[i] = mergeValues(this.mode[i], parentMode.mode[i]);
    }
    return newMode;
  }

  public char sumValues(char val1, char val2)
  {
    if (val1 == INDETERMINAT) return val2;
    return val1;
  }

  public char mergeValues(char val1, char val2)
  {
    //SI ESTAN DENEGATS RETORNO EL VALOR DE L'ALTRE
    if (val1 == DENEGAT) return val2;
    if (val2 == DENEGAT) return val1;

    //SI NO ESTAN DENEGATS
    if (val1 == INDETERMINAT) return val2;
    return val1;
  }

  public boolean canDoAction(int index)
  {
    return (mode[index]!=DENEGAT);
  }

  public boolean canDoAction(Action accio)
  {
    if (accio==null) return false;
    return (mode[accio.getIndex()]!=DENEGAT);
  }


  public char valueOf(int index)
  {
    return mode[index];
  }

  public List<String> getActionsList()
  {
    List<String> list = new ArrayList<String>();

    for(Action action : Action.values())
    {
      if (canDoAction(action)) list.add(action.getActionName());
    }
 
    return list;
  }

  @Override
  public String toString()
  {
    return String.valueOf(mode);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (!(obj instanceof Mode)) return false;

    char [] other = ((Mode)obj).mode;
    int minlength = Math.min(mode.length, other.length);
    for(int i=0; i<minlength; i++)
    {
      if (mode[i]!=other[i]) return false;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 11 * hash + Arrays.hashCode(this.mode);
    return hash;
  }


}
