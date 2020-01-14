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
package org.santfeliu.elections.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;
import org.santfeliu.elections.Party;
import org.santfeliu.elections.Results;
import org.santfeliu.elections.Results.PartyStats;

/**
 *
 * @author realor
 */
public class HemicyclePanel extends JPanel
{
  private static final int TOP_MARGIN = 50;
  private static final int SIDE_MARGIN = 50;
  private static final int FOOTER_HEIGHT = 50;
  private Results results;
  private Results prevResults;
  private int totalVotes;
  private boolean doPaint;
  private Font bigFont = new Font("Arial", Font.BOLD, 20);
  private Font smallFont = new Font("Arial", Font.BOLD, 12);

  public HemicyclePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
  }

  public void updateData(Results results, Results prevResults)
  {
    if ("1".equals(results.getCallId()))
    {
      doPaint = true;
      this.results = results;
      this.prevResults = prevResults;
      this.totalVotes = results.getTotalVotes();
      invalidate();
    }
    else
    {
      doPaint = false;
      this.results = null;
      this.prevResults = null;
      invalidate();
      repaint();
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.black);
    if (!doPaint) return;

    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth();
    int height = getHeight();
    int dim = Math.min(width, 2 * height - FOOTER_HEIGHT - TOP_MARGIN);

    double radius = (dim / 2) - SIDE_MARGIN;
    double radius2 = radius * 0.8;
    double radius3 = radius * 0.7;
    double radius4 = radius * 0.5;

    int cx = (int)Math.round(getWidth() / 2);
    int cy = (int)Math.round(getHeight() - FOOTER_HEIGHT);
    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
      BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
    g2d.setColor(Color.BLACK);
    g2d.drawLine(cx, cy, cx, 0);
    g2d.setStroke(new BasicStroke(1));

    Collection<Party> parties = results.getPartiesSortByVotes();
    paintHemicycle(g2d, results, radius, radius2, parties, false);

    if (prevResults != null)
    {
      Collection<Party> prevParties =
        prevResults.getPartiesSortByVotes();

//      Collection<Party> prevParties =
//        results.getScrutinizedBoardsCount() == 0 ?
//        prevResults.getPartiesSortByVotes() :
//        prevResults.getPartiesMatchedWith(parties);
      paintHemicycle(g2d, prevResults, radius3, radius4, prevParties, true);
    }

    // paint unassigned councillors
    if (totalVotes > 0)
    {
      int councillorsCount = results.getCouncillorsCount();
      int electedCouncillorsCount = results.getElectedCouncillors().size();
      int unassignedCouncillors = councillorsCount - electedCouncillorsCount;
      if (unassignedCouncillors > 0)
      {
        String text = String.valueOf(unassignedCouncillors);
        if (unassignedCouncillors == 1) text += " REGIDOR PER SORTEIG";
        else text += " REGIDORS PER SORTEIG";
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        double textWidth = g.getFontMetrics().getStringBounds(text, g).getWidth();
        int x = (int)(getWidth() - (int)textWidth - 6);
        int y = 18;
        g.setColor(Color.WHITE);
        g.fillRect(x - 4, 0, getWidth(), y + 4);
        g.setColor(Color.RED);
        g.drawString(text, x, y);
      }
    }
  }

  private void paintHemicycle(Graphics2D g, Results results,
    double radius, double holeRadius, Collection<Party> parties,
    boolean innerTexts)
  {
    double cx = getWidth() / 2;
    double cy = getHeight() - FOOTER_HEIGHT;

    Ellipse2D hole = new Ellipse2D.Double();
    hole.setFrameFromCenter(cx, cy, cx + holeRadius, cy + holeRadius);
    Area holeArea = new Area(hole);

    Area area = new Area();
    Arc2D pie = new Arc2D.Double();
    pie.setArcByCenter(cx, cy, radius,
      0, 180, Arc2D.PIE);
    area.add(new Area(pie));
    area.subtract(holeArea);

    g.setColor(new Color(230, 230, 230));
    g.fill(area);
    g.setColor(new Color(100, 100, 100));
    g.draw(area);

    if (results.getScrutinizedBoardsCount() > 0)
    {
      double sector = 0;
      double regAngle = Math.PI / results.getCouncillorsCount();

      Iterator<Party> iter = parties.iterator();
      Map partyStats = results.getPartyStats();
      while (iter.hasNext())
      {
        Party party = iter.next();
        Results.PartyStats stats = (PartyStats)partyStats.get(party.getId());
        if (stats != null)
        {
          int numreg = stats.getNumElectedCouncillors();
          if (numreg > 0)
          {
            double startAngle = Math.PI - sector * regAngle;
            double midAngle = Math.PI - (sector + numreg / 2.0) * regAngle;
            double endAngle = Math.PI - (sector + numreg) * regAngle;

            area = new Area();
            pie = new Arc2D.Double();
            pie.setArcByCenter(cx, cy, radius,
              (180 / Math.PI) * startAngle,
              (180 / Math.PI) * (endAngle - startAngle), Arc2D.PIE);
            area.add(new Area(pie));
            area.subtract(holeArea);

            g.setColor(party.getColor());
            g.fill(area);
            g.setColor(new Color(100, 100, 100));
            g.draw(area);

            double ix = cx + 0.5 * (radius + holeRadius) * Math.cos(midAngle);
            double iy = cy - 0.5 * (radius + holeRadius) * Math.sin(midAngle);
            ix -= 4;
            iy += 6;
            if (numreg > 9) ix -= 5;

            String snum = String.valueOf(numreg);
            g.setFont(bigFont);
            g.setColor(party.getColor().brighter().brighter().brighter());

            g.drawString(snum, (int)ix - 1, (int)iy - 1);
            g.drawString(snum, (int)ix + 1, (int)iy + 1);
            g.drawString(snum, (int)ix - 1, (int)iy + 1);
            g.drawString(snum, (int)ix + 1, (int)iy - 1);

            g.setColor(Color.BLACK);
            g.drawString(snum, (int)ix, (int)iy);

            double r1, r2;
            if (innerTexts)
            {
              r1 = holeRadius - 16;
              r2 = holeRadius;
            }
            else
            {
              r1 = radius + 4;
              r2 = radius;
            }

            ix = cx + (r1 + 4) * Math.cos(midAngle);
            iy = cy - (r1 + 4) * Math.sin(midAngle);
            double ix2 = cx + r2 * Math.cos(midAngle);
            double iy2 = cy - r2 * Math.sin(midAngle);
            g.setColor(Color.GRAY);
            g.drawLine((int)ix, (int)iy, (int)ix2, (int)iy2);

            g.setFont(smallFont);
            g.setColor(Color.BLACK);
            Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(
              party.getAbbreviation(), g);
            if (midAngle > 4 * Math.PI / 10 && midAngle < 6 * Math.PI / 10)
            {
              ix -= stringBounds.getWidth() / 2;
              if (innerTexts) iy += 8;
            }
            else if (midAngle > Math.PI / 2)
            {
              if (!innerTexts) ix -= stringBounds.getWidth();
            }
            else if (midAngle < Math.PI / 2)
            {
              if (innerTexts) ix -= stringBounds.getWidth();
            }
            if (ix < 2) ix = 2;
            g.drawString(party.getAbbreviation(), (int)ix, (int)iy);

            sector += numreg;
          }
        }
      }
    }
    String year = String.valueOf(results.getDate().get(Calendar.YEAR));
    g.setFont(bigFont);
    Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(year, g);
    g.setColor(Color.BLACK);
    int offset = (int)(0.5 * stringBounds.getWidth());
    double textRadius = (radius + holeRadius) / 2;
    g.drawString(year, (int)(cx - textRadius - offset), (int)(cy + FOOTER_HEIGHT / 2));
    g.drawString(year, (int)(cx + textRadius - offset), (int)(cy + FOOTER_HEIGHT / 2));
  }
}

