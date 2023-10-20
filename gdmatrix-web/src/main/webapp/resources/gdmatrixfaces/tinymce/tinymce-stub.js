/* tinymce-stub.js */

function tinymceInit(clientId, readonly = false, language)
{
  const textareaId = clientId + "_textarea";
  const textareaElem = document.getElementById(textareaId);

  const updateTextarea = function(editor)
  {
    var html = editor.getContent();
    textareaElem.innerHTML = html;    
  };

  tinymce.init({
    target: textareaElem,
    menubar: false,
    statusbar: false,
    language: language || 'en',
    readonly: readonly,
    toolbar_mode: 'scrolling',
    content_css: [ "/resources/gdmatrixfaces/tinymce/content.css" ],
    plugins: 'preview importcss searchreplace autoresize autolink autosave directionality visualblocks visualchars fullscreen image link media codesample table charmap pagebreak nonbreaking anchor insertdatetime advlist lists wordcount help charmap emoticons code',
    toolbar : 'undo redo | bold italic underline strikethrough | fontselect fontsizeselect formatselect | alignleft aligncenter alignright | link image media codesample | h1 h2 h3 | outdent indent | numlist bullist | forecolor backcolor casechange permanentpen formatpainter removeformat | table | pagebreak | charmap emoticons | fullscreen preview | a11ycheck ltr rtl | code | showcomments addcomment',
    setup: function(editor) 
    {
      editor.on('input', function() 
      {
        updateTextarea(editor);
      });
      editor.on('change', function() 
      {
        updateTextarea(editor);
      });
    }
  });
}


