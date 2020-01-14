var timePickerInputId = "timepicker";

function makeTimePickerAccessible(clientId) {
    timePickerInputId = clientId + ":time";
  $(timePickerInputId).click(function() {
    closeTimePicker();
  });
  
  $(document.getElementById(clientId + "_clock")).click(function () {
    setTimeout(function () {
      var timepicker = $(document.getElementById(timePickerInputId));
      timepicker.show();
      timepicker.focus();      
      var hourSelector = $('.ui-timepicker-select')[0];
      hourSelector.focus();
      
      timePickHandler();
      $(document).on('click', '#ui-datepicker-div .ui-datepicker-close', function () {
        closeTimePicker();
      });
    }, 0);
  });
}

function timePickHandler() {
  var activeDate;
  var container = document.getElementById('ui-datepicker-div');
  var input = document.getElementById(timePickerInputId);

  if (!container || !input) {
    return;
  }

  container.setAttribute('role', 'application');
  container.setAttribute('aria-label', 'time-picker');

  $(container).on('keydown', function calendarKeyboardListener(keyVent) {
    var which = keyVent.which;
    var target = keyVent.target;
    
//    var currentHour = getCurrentHour(container);
//
//    if (!currentHour) {
//      currentHour = $('a.ui-state-default')[0];
//      setHighlightState(currentHour, container);
//    }

    if (27 === which) {
      keyVent.stopPropagation();
      return closeTimePicker();
    } else if (which === 9 && keyVent.shiftKey) { // SHIFT + TAB
      keyVent.preventDefault();
      if ($(target).hasClass('ui-datepicker-close')) { // close button
        $('.ui-datepicker-prev')[0].focus();
      } else if ($(target).hasClass('ui-state-default')) { // a date link
        $('.ui-datepicker-close')[0].focus();
      } else if ($(target).hasClass('ui-datepicker-prev')) { // the prev link
        $('.ui-datepicker-next')[0].focus();
      } else if ($(target).hasClass('ui-datepicker-next')) { // the next link
        activeDate = $('.ui-state-highlight') ||
                    $('.ui-state-active')[0];
        if (activeDate) {
          activeDate.focus();
        }
      }
    } else if (which === 9) { // TAB
      keyVent.preventDefault();
      if ($(target).parent().hasClass('ui-datepicker-close')) { // close button
        activeDate = $('.ui-state-highlight') ||
                    $('.ui-state-active')[0];
        if (activeDate) {
          activeDate.focus();
        }
      } else if ($(target).parent().hasClass('ui_tpicker_hour_slider')) {
        $('.ui-timepicker-select')[1].focus();
      } else if ($(target).parent().hasClass('ui_tpicker_minute_slider')) {
        $('.ui-datepicker-close')[0].focus();
      }
    } else if (which === 13) { // ENTER
      if ($(target).hasClass('ui-state-default')) {
        setTimeout(function () {
          closeTimePicker();
        }, 100);
      } else if ($(target).hasClass('ui-datepicker-prev')) {
        handlePrevClicks();
      } else if ($(target).hasClass('ui-datepicker-next')) {
        handleNextClicks();
      }
    } else if (32 === which) {
      if ($(target).hasClass('ui-datepicker-prev') || $(target).hasClass('ui-datepicker-next')) {
        target.click();
      }
    }  
    $(".ui-datepicker-current").hide();
  });
}

function closeTimePicker() {
  var container = $('#ui-timepicker-div');
  $(container).off('keydown');
  var input = $(document.getElementById(datePickerInputId))[0];
  $(input).datepicker('hide');
  input.focus();
}







