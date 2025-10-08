/* chips.js */

function toggleChips(chipsWidgetVar, btnWidgetVar) 
{
  const chips = PF(chipsWidgetVar);
  const btn = PF(btnWidgetVar);

  if (!chips || !btn) return;

  const input = chips.input?.[0];
  if (!input) return;

  //States
  const isEditing = chips.jq.data('editing') === true;
  const buttonChecked = btn.input.prop('checked');

  // if button turned ON then open editor
  if (buttonChecked && !isEditing) 
  {
    chips.toggleEditor();
    chips.jq.data('editing', true);

    // Focus the input after a delay
    setTimeout(() => input.focus(), 50);

    // When input loses focus, close editor and uncheck the button
    input.onblur = () => 
    {
      chips.toggleEditor();
      chips.jq.data('editing', false);
      input.onblur = null;
      btn.uncheck(); // synchronize button
    };
  }

  // If button turned OFF then close editor
  else if (!buttonChecked && isEditing) 
  {
    chips.toggleEditor();
    chips.jq.data('editing', false);
    input.onblur = null;
  }
}


