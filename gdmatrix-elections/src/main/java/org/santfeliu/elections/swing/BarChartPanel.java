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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import org.santfeliu.elections.Party;
import org.santfeliu.elections.Results;

/**
 *
 * @author realor
 */
public class BarChartPanel extends JPanel
{

  public static final String DEFAULTPARTYABBR = "ALTRES";
  public static final String BLANKSLABEL = "BLANCS";
  private boolean groupWithoutColor = true;
  private Results results;

  private ChartPanel chartPanel;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JCheckBox jCheckBox1 = new JCheckBox();

  public BarChartPanel()
  {
    try
    {
      jbInit();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(505, 422));
    this.setLayout(borderLayout1);
    jCheckBox1.setText("Mostrar agrupats");
    jCheckBox1.setSelected(true);
    jCheckBox1.setToolTipText("null");
    groupWithoutColor = jCheckBox1.isSelected();
    jCheckBox1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jCheckBox1_actionPerformed(e);
      }
    });
    chartPanel = new ChartPanel(createChart(new DefaultCategoryDataset()));
    chartPanel.add(jPanel1, null);
    this.add(chartPanel, BorderLayout.CENTER);
    this.add(jCheckBox1, BorderLayout.NORTH);
  }

  public void updateData(Results results)
  {
    JFreeChart chart = chartPanel.getChart();
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    setPlotSettings(plot, results);
    this.results = results;
  }

  private DefaultCategoryDataset createDataset(Results results)
  {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    if (results != null)
    {
      Collection parties = results.getParties();
      Iterator iter = parties.iterator();
      int defaultPartyVotes = 0;
      while (iter.hasNext())
      {
        Party party = (Party) iter.next();
        int partyVotes = results.getVotes(party.getId());
        Color color = party.getColor();
        if (color == null && groupWithoutColor)
        {
          defaultPartyVotes = defaultPartyVotes + partyVotes;
        } else
        {
          if (partyVotes > 0)
          {
            String abbr = party.getAbbreviation();
            dataset.addValue(partyVotes, "", abbr);
          }
        }
      }
      //Altres (No color defined)
      if (defaultPartyVotes > 0)
      {
        dataset.addValue(defaultPartyVotes, "", DEFAULTPARTYABBR);
      }
      //Blank
      setBlankVotes(dataset, results);
    }

    return dataset;
  }

  private JFreeChart createChart(DefaultCategoryDataset dataset)
  {
    JFreeChart chart = createBarChart3D(
      "", // chart title
      "", // domain axis label
      "Vots", // range axis label
      dataset, // data
      PlotOrientation.HORIZONTAL, // orientation
      false, // include legend
      true, // tooltips?
      true // URLs?
    );
    //Plot settings
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    plot.setNoDataMessage("No hi ha dades");

    ValueAxis rangeAxis = plot.getRangeAxis();
    rangeAxis.setUpperMargin(0.5); //Set Margin 50%

    CustomBarRenderer3D renderer = (CustomBarRenderer3D) plot.getRenderer();

    renderer.setItemLabelGenerator(new CategoryItemLabelGenerator()
    {
      DecimalFormat df1 = new DecimalFormat("#,###");
      DecimalFormat df2 = new DecimalFormat("0.00");

      public String generateRowLabel(CategoryDataset cd, int i)
      {
        return "";
      }

      public String generateColumnLabel(CategoryDataset cd, int i)
      {
        return "";
      }

      public String generateLabel(CategoryDataset cd, int i, int j)
      {
        int totalVotes = results.getValidVotes();
        int partyVotes = ((Number)cd.getValue(i, j)).intValue();
        double perc = partyVotes * 100.0 / totalVotes;
        return df1.format(partyVotes) + " (" + df2.format(perc) + "%)";
      }
    });
    renderer.setPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT));
    renderer.setItemLabelAnchorOffset(15.0);
    renderer.setItemLabelFont(new Font(null, Font.BOLD, 16));
    renderer.setItemLabelsVisible(true);

    return chart;
  }

  private void setPlotSettings(CategoryPlot plot, Results results)
  {
    DefaultCategoryDataset dataset = createDataset(results);
    plot.setDataset(dataset);
    CustomBarRenderer3D renderer = (CustomBarRenderer3D) plot.getRenderer();
    Vector colors = getColors(results, dataset);
    renderer.setPaints(colors);
  }

  private Vector getColors(Results results, DefaultCategoryDataset dataset)
  {
    Vector colors = new Vector();
    int index = -1;
    Collection parties = results.getParties();
    Iterator iter = parties.iterator();
    while (iter.hasNext())
    {
      Party party = (Party) iter.next();
      index = dataset.getColumnIndex(party.getAbbreviation());
      if (index >= 0)
      {
        colors.add(party.getColor());
      }
    }
    index = dataset.getColumnIndex(DEFAULTPARTYABBR);
    if (index >= 0)
    {
      colors.add(null);
    }
    index = dataset.getColumnIndex(BLANKSLABEL);
    if (index >= 0)
    {
      colors.add(Color.LIGHT_GRAY);
    }

    return colors;
  }

  private void setBlankVotes(DefaultCategoryDataset dataset, Results results)
  {
    int blankVotes = results.getBlankVotes();
    if (blankVotes > 0) // Blank votes
    {
      dataset.addValue(blankVotes, "", BLANKSLABEL);
    }
  }

  private JFreeChart createBarChart3D(String title,
    String categoryAxisLabel,
    String valueAxisLabel,
    CategoryDataset dataset,
    PlotOrientation orientation,
    boolean legend,
    boolean tooltips,
    boolean urls)
  {
    if (orientation == null)
    {
      throw new IllegalArgumentException("Null 'orientation' argument.");
    }

    CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
    ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);

    CustomBarRenderer3D renderer = new CustomBarRenderer3D();
    if (tooltips)
    {
      renderer.setBaseToolTipGenerator(
        new StandardCategoryToolTipGenerator());
    }
    if (urls)
    {
      renderer.setBaseItemURLGenerator(
        new StandardCategoryURLGenerator());
    }

    CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
      renderer);
    plot.setOrientation(orientation);
    if (orientation == PlotOrientation.HORIZONTAL)
    {
        // change rendering order to ensure that bar overlapping is the
      // right way around
      plot.setRowRenderingOrder(SortOrder.DESCENDING);
      plot.setColumnRenderingOrder(SortOrder.DESCENDING);
    }
    plot.setForegroundAlpha(0.75f);

    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
      plot, legend);

    return chart;
  }

  public void setGroupWithoutColor(boolean groupWithoutColor)
  {
    this.groupWithoutColor = groupWithoutColor;
  }

  public boolean isGroupWithoutColor()
  {
    return groupWithoutColor;
  }

  private void jCheckBox1_actionPerformed(ActionEvent e)
  {
    groupWithoutColor = jCheckBox1.isSelected();
    updateData(results);
  }
}
