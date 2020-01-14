package org.santfeliu.agenda.web.view;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.custom.schedule.DefaultScheduleEntryRenderer;
import org.apache.myfaces.custom.schedule.HtmlSchedule;
import org.apache.myfaces.custom.schedule.UIScheduleBase;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;

/**
 *
 * @author realor
 */
public class ScheduleEntryRenderer extends DefaultScheduleEntryRenderer
{
  @Override
  protected void renderCompactContent(FacesContext context, 
    ResponseWriter writer, HtmlSchedule schedule, ScheduleDay day,
    ScheduleEntry entry, boolean selected) throws IOException
  {
    StringBuilder text = new StringBuilder();
    Date startTime = entry.getStartTime();

    if (day.getDayStart().after(entry.getStartTime()))
    {
      startTime = day.getDayStart();
    }

    Date endTime = entry.getEndTime();

    if (day.getDayEnd().before(entry.getEndTime()))
    {
      endTime = day.getDayEnd();
    }

    if (!entry.isAllDay())
    {
      DateFormat format = getDateFormat(context, schedule, "HH:mm");
      text.append(format.format(startTime));
      if (!startTime.equals(endTime))
      {
        text.append("-");
        text.append(format.format(endTime));
      }
      text.append(": ");
    }
    if (entry.getTitle() != null)
      text.append(entry.getTitle());
    if (selected)
    {
      writer.startElement(HTML.ANCHOR_ELEM, schedule);
      writer.writeAttribute("href",
        "javascript:document.getElementById('selectEvent').click()", null);
    }
    writer.writeText(text.toString(), null);
    if (selected) writer.endElement(HTML.ANCHOR_ELEM);
  }

  @Override
  protected void renderDetailedContentText(FacesContext context,
     ResponseWriter writer, HtmlSchedule schedule, ScheduleDay day,
     ScheduleEntry entry, boolean selected) throws IOException
  {
    // write the title of the entry
    if (selected)
    {
      writer.startElement(HTML.ANCHOR_ELEM, schedule);
      writer.writeAttribute("href",
        "javascript:document.getElementById('selectEvent').click()", null);
    }
    if (entry.getTitle() != null)
    {
      writer.startElement(HTML.SPAN_ELEM, schedule);
      writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(
              schedule, "title"), null);
      writer.writeText(entry.getTitle(), null);
      writer.endElement(HTML.SPAN_ELEM);      
    }
    if (entry.getSubtitle() != null)
    {
      writer.startElement("br", schedule);
      writer.endElement("br");
      writer.startElement(HTML.SPAN_ELEM, schedule);
      writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(
              schedule, "subtitle"), null);
      writer.writeText(entry.getSubtitle(), null);
      writer.endElement(HTML.SPAN_ELEM);
    }
    if (entry.getDescription() != null)
    {
      writer.startElement("br", schedule);
      writer.endElement("br");
      writer.startElement(HTML.SPAN_ELEM, schedule);
      writer.writeAttribute(HTML.CLASS_ATTR, getStyleClass(
              schedule, "description"), null);
      writer.writeText(entry.getDescription(), null);
      writer.endElement(HTML.SPAN_ELEM);
    }
    if (selected) writer.endElement(HTML.ANCHOR_ELEM);
  }

  protected DateFormat getDateFormat(FacesContext context,
    UIScheduleBase schedule, String pattern)
  {
    Locale viewLocale = context.getViewRoot().getLocale();
    DateFormat format = (pattern != null && pattern.length() > 0)
            ? new SimpleDateFormat(pattern, viewLocale)
            : DateFormat.getDateInstance(DateFormat.MEDIUM, viewLocale);

    format.setTimeZone(schedule.getModel().getTimeZone());
    return format;
  }
}
