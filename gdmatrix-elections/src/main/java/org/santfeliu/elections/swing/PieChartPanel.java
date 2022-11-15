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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.text.DecimalFormat;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import org.santfeliu.elections.Party;
import org.santfeliu.elections.Results;


/**
 *
 * @author realor
 */
public class PieChartPanel extends JPanel
{
  public static final String DEFAULTPARTYABBR = "ALTRES";
  public static final String BLANKSLABEL = "BLANCS";
  private BorderLayout borderLayout1 = new BorderLayout();
  private ChartPanel chartPanel;

  public PieChartPanel()
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

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(482, 372));
    this.setLayout(borderLayout1);
    chartPanel = new ChartPanel(createChart(new DefaultPieDataset()));
    this.add(chartPanel, BorderLayout.CENTER);
  }

  public void updateData(Results results)
  {
    JFreeChart chart = chartPanel.getChart();
    PiePlot3D plot = (PiePlot3D)chart.getPlot();
    plot.setDataset(createDataset(results));
    setPlotSectionsSettings(plot, results);
  }

  private PieDataset createDataset(Results results)
  {
    DefaultPieDataset dataset = new DefaultPieDataset();
    if (results != null)
    {
      Collection parties = results.getParties();
      Iterator iter = parties.iterator();
      int defaultPartyVotes = 0;
      while (iter.hasNext())
      {
        Party party = (Party)iter.next();
        int partyVotes = results.getVotes(party.getId());
        Color color = party.getColor();
        if (color == null)
          defaultPartyVotes = defaultPartyVotes + partyVotes;
        else
        {
          String abbr = party.getAbbreviation();
          dataset.setValue(abbr, partyVotes);
        }
      }
      if (defaultPartyVotes > 0) // Altres
        dataset.setValue(DEFAULTPARTYABBR, defaultPartyVotes);

      setBlankVotes(dataset, results);
    }

    return dataset;
  }

  private JFreeChart createChart(PieDataset dataset)
  {
    JFreeChart chart = ChartFactory.createPieChart3D(
        null,  // chart title
        dataset,             // data
        false,               // include legend
        true,
        false
    );

    PiePlot3D plot = (PiePlot3D) chart.getPlot();
    plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    plot.setNoDataMessage("No hi ha dades");
    plot.setCircular(true);
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})",
      new DecimalFormat(), new DecimalFormat("#0.00%")));

    return chart;
  }

  private void setPlotSectionsSettings(PiePlot3D plot, Results results)
  {
    Collection parties = results.getParties();
    Iterator iter = parties.iterator();
    while (iter.hasNext())
    {
      Party party = (Party)iter.next();
      String abbr = party.getAbbreviation();
      plot.setSectionPaint(abbr, party.getColor());
    }
    plot.setSectionPaint(BLANKSLABEL, Color.LIGHT_GRAY);
  }

  private void setBlankVotes(DefaultPieDataset dataset, Results results)
  {
    int blankVotes = results.getBlankVotes();
    if (blankVotes > 0) // Blank votes
      dataset.setValue(BLANKSLABEL, blankVotes);
  }
}
