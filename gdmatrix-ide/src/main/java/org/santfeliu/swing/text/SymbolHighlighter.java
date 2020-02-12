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
package org.santfeliu.swing.text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author blanquepa, realor
 */
public class SymbolHighlighter implements CaretListener
{
  private JTextComponent component;

  private String openSymbols;
  private String closeSymbols;

  private Object leftHighlighter;
  private Object rightHighlighter;
  private Object unmatchedHighlight;

  private String openTag;
  private String closeTag;

  private final List<Block> blocks = new ArrayList<Block>();

  public SymbolHighlighter(JTextComponent textComponent,
    String openSymbols, String closeSymbols)
  {
    this(textComponent, openSymbols, closeSymbols,
      Color.YELLOW, new Color(255, 160, 160));
  }

  public SymbolHighlighter(JTextComponent textComponent, String openSymbols,
    String closeSymbols, Color matchedColor, Color unmatchedColor)
  {
    if (textComponent == null)
      throw new RuntimeException("Text component is null");

    if (!isValidSymbols(openSymbols, closeSymbols))
      throw new RuntimeException("Invalid symbols");

    this.component = textComponent;
    this.openSymbols = openSymbols;
    this.closeSymbols = closeSymbols;

    component.getDocument().putProperty(
      DefaultEditorKit.EndOfLineStringProperty, "\n");

    component.addCaretListener(this);

    try
    {
      leftHighlighter = textComponent.getHighlighter().addHighlight(0, 0,
        new DefaultHighlighter.DefaultHighlightPainter(matchedColor));
      rightHighlighter = textComponent.getHighlighter().addHighlight(0, 0,
        new DefaultHighlighter.DefaultHighlightPainter(matchedColor));
      unmatchedHighlight = textComponent.getHighlighter().addHighlight(0, 0,
        new DefaultHighlighter.DefaultHighlightPainter(unmatchedColor));
    }
    catch (BadLocationException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void setScriptletTags(String openTag, String closeTag)
  {
    this.openTag = openTag;
    this.closeTag = closeTag;
  }

  private boolean hasScriptlets()
  {
    return openTag != null && closeTag != null;
  }

  private void doHighlight(int pos)
  {
    if (hasScriptlets()) findBlocks();

    String text = component.getText();

    char ch = (pos < text.length() && text.length() > 0) ? 
      text.charAt(pos) : ' ';
    
    int pairedPosition = -2;

    //Tries at caret position
    if (isOpenSymbol(ch))
    {
      pairedPosition = getClosingPair(text, pos + 1, ch);
    }
    else if (isCloseSymbol(ch))
    {
      pairedPosition = getOpeningPair(text, pos + 1, ch);
    }
    else
    {
      //Tries next to caret position
      if (pos <= text.length())
      {
        pos--;
        if (pos >= 0)
        {
          ch = text.charAt(pos);

          if (isOpenSymbol(ch))
          {
            pairedPosition = getClosingPair(text, pos + 1, ch);
          }
          else if (isCloseSymbol(ch))
          {
            pairedPosition = getOpeningPair(text, pos + 1, ch);
          }
        }
      }
    }

    if (pairedPosition >= 0)
      setMatchedHighlight(pos, pairedPosition);
    else if (pairedPosition == -1)
      setUnmatchedHighlight(pos);
    else
      reset();
  }

  private int getClosingPair(String text, int position, char ch)
  {
    int num = 0;
    int matchPosition = -1;
    int j = position;
    String t1 = getBlockType(position);
    while (j < text.length() && matchPosition == -1)
    {
      String t2 = getBlockType(j);
      if (t1.equals(t2))
      {
        char pch = text.charAt(j);
        if (ch == pch) num++;
        else if (match(ch, pch))
        {
          if (num == 0) matchPosition = j;
          else num--;
        }
      }
      j++;
    }
    return matchPosition;
  }

  private int getOpeningPair(String text, int position, char ch)
  {
    int num = 0;
    int matchPosition = -1;
    int j = position - 2;
    String t1 = getBlockType(position);
    while (j >= 0 && matchPosition == -1)
    {
      String t2 = getBlockType(j);
      if (t1.equals(t2))
      {
        char pch = text.charAt(j);
        if (ch == pch) num++;
        else if (match(pch, ch))
        {
          if (num == 0) matchPosition = j;
          else num--;
        }
      }
      j--;
    }
    return matchPosition;
  }

  private void findBlocks()
  {
    blocks.clear();

    int state = 0;

    String text = component.getText();
    String currentTag;

    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      switch (state)
      {
        case 0: // inside CODE block (html)
          buffer.append(ch);
          currentTag = buffer.toString();
          if (openTag.equals(currentTag))
          {
            addBlock(Block.SCRIPTLET, i - openTag.length() + 1);
            state = 1;
            buffer.setLength(0);
          }
          else if (!openTag.startsWith(currentTag))
          {
            buffer.setLength(0);
            if (ch == openTag.charAt(0))
            {
              buffer.append(ch);
            }
          }
          break;
        case 1: // inside SCRIPTLET block (javascript)
          buffer.append(ch);
          currentTag = buffer.toString();
          if (closeTag.equals(currentTag))
          {
            addBlock(Block.CODE, i);
            state = 0;
            buffer.setLength(0);
          }
          else if (!closeTag.startsWith(currentTag))
          {
            buffer.setLength(0);
            if (ch == closeTag.charAt(0))
            {
              buffer.append(ch);
            }
          }
          break;
      }
    }
    if (!blocks.isEmpty())
    {
      blocks.get(blocks.size() - 1).setEndPosition(text.length());
    }
  }

  private String getBlockType(int position)
  {
    for (Block block : blocks)
    {
      if (block.getStartPosition() <= position &&
          position <= block.getEndPosition())
        return block.getType();
    }
    return Block.NOBLOCK;
  }

  private void addBlock(String type, int position)
  {
    if (blocks.isEmpty())
    {
      blocks.add(new Block(Block.CODE, 0));
    }

    blocks.get(blocks.size() - 1).setEndPosition(position - 1);

    blocks.add(new Block(type, position));
  }

  private boolean isOpenSymbol(char ch)
  {
    return openSymbols.indexOf(ch) != -1;
  }

  private boolean isCloseSymbol(char ch)
  {
    return closeSymbols.indexOf(ch) != -1;
  }

  private boolean match(char open, char close)
  {
    return openSymbols.indexOf(open) == closeSymbols.indexOf(close);
  }

  private void setMatchedHighlight(int lp, int rp)
  {
    try
    {
      component.getHighlighter().changeHighlight(leftHighlighter, lp, lp + 1);
      component.getHighlighter().changeHighlight(rightHighlighter, rp, rp + 1);
      component.getHighlighter().changeHighlight(unmatchedHighlight, 0, 0);
    }
    catch (BadLocationException ex)
    {
    }
  }

  private void setUnmatchedHighlight(int p)
  {
    try
    {
      component.getHighlighter().changeHighlight(unmatchedHighlight, p, p + 1);
      component.getHighlighter().changeHighlight(leftHighlighter, 0, 0);
      component.getHighlighter().changeHighlight(rightHighlighter, 0, 0);
    }
    catch (BadLocationException ex)
    {
    }
  }

  private void reset()
  {
    try
    {
      component.getHighlighter().changeHighlight(rightHighlighter, 0, 0);
      component.getHighlighter().changeHighlight(leftHighlighter, 0, 0);
      component.getHighlighter().changeHighlight(unmatchedHighlight, 0, 0);
    }
    catch (BadLocationException ex)
    {
    }
  }

  private boolean isValidSymbols(String openSymbols, String closeSymbols)
    throws RuntimeException
  {
    boolean valid = true;
    if (openSymbols != null && closeSymbols != null)
    {
      if (openSymbols.length() == closeSymbols.length())
      {
        String symbols = openSymbols + closeSymbols;
        int len = symbols.length();
        int i = 0;
        while (i < len && valid)
        {
          int j = i + 1;
          while (j < len && valid)
          {
            valid = symbols.charAt(i) != symbols.charAt(j);
            j++;
          }
          i++;
        }
      }
    }
    return valid;
  }

  @Override
  public void caretUpdate(CaretEvent e)
  {
    doHighlight(e.getDot());
  }

  private class Block
  {
    private final static String SCRIPTLET = "SCRIPTLET";
    private final static String CODE = "CODE";
    private final static String NOBLOCK = "NOBLOCK";

    private String type; // CODE or SCRIPLET
    private int startPosition;
    private int endPosition;

    public Block(String type, int startPosition)
    {
      this.type = type;
      this.startPosition = startPosition;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public int getStartPosition()
    {
      return startPosition;
    }

    public void setStartPosition(int startPosition)
    {
      this.startPosition = startPosition;
    }

    public int getEndPosition()
    {
      return endPosition;
    }

    public void setEndPosition(int endPosition)
    {
      this.endPosition = endPosition;
    }
  }

}
