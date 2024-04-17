/* codemirror-stub.js */

import "./codemirror.js";

function codemirrorInit(clientId, readonly, language, showLineNumbers, completion)
{
  const editorId = clientId + "_editor";
  const inputId = clientId + "_input";

  const editorElem = document.getElementById(editorId);
  editorElem.className = "cm-editor-holder";

  const inputElem = document.getElementById(inputId);

  const { basicSetup } = CM["@codemirror/basic-setup"];
  const { keymap, highlightSpecialChars, highlightActiveLine,
    drawSelection, EditorView } = CM["@codemirror/view"];
  const { lineNumbers, highlightActiveLineGutter} = CM["@codemirror/gutter"];
  const { history, historyKeymap } = CM["@codemirror/history"];
  const { defaultKeymap } = CM["@codemirror/commands"];
  const { bracketMatching } = CM["@codemirror/matchbrackets"];
  const { foldGutter, foldKeymap } = CM["@codemirror/fold"];
  const { javascript, javascriptLanguage } = CM["@codemirror/lang-javascript"];
  const { json, jsonLanguage } = CM["@codemirror/lang-json"];
  const { xml, xmlLanguage } = CM["@codemirror/lang-xml"];
  const { sql, sqlLanguage } = CM["@codemirror/lang-sql"];
  const { html, htmlLanguage } = CM["@codemirror/lang-html"];  
  const { defaultHighlightStyle } = CM["@codemirror/highlight"];
  const { searchKeymap, highlightSelectionMatches } = CM["@codemirror/search"];
  const { indentOnInput } = CM["@codemirror/language"];
  const { EditorState } = CM["@codemirror/state"];
  const { autocompletion } = CM["@codemirror/autocomplete"];

  let theme = EditorView.theme({
    "&.cm-focused .cm-cursor" : {
      borderLeftColor: "var(--text-color)",
      borderLeftWidth: "2px"
    },
    "&.cm-focused .cm-matchingBracket" : {
      "backgroundColor" : "#C0C000",
      "color" : "black"
    },
    "&.ͼ2 .cm-activeLine" : {
      "backgroundColor" : "var(--surface-hover)"
    },
    "&.ͼ2 .cm-tooltip" : {
      "backgroundColor" : "var(--surface-overlay)"
    },
    "&.ͼ2 .cm-gutters" : {
      "backgroundColor" : "transparent"      
    },
    "&.ͼ2 .cm-activeLineGutter" : {
      "backgroundColor" : "var(--surface-hover)"      
    },
    "& .ͼa" : {
      "color" : "#666",
      "fontWeight" : "bold"
    },
    "& .ͼl" : {
      "color" : "#808080"
    },
    "& .ͼf" : {
      "color" : "#8080e0"
    },
    "& .ͼd" : {
      "color" : "#2020ff"
    },
    "& .ͼb" : {
      "color" : "#008000"
    },
    "& .cm-wrap" : {
      "height" : "100%"
    },
    "& .cm-scroller" : {
      "overflow" : "auto"
    }
  });

  let editorView = new EditorView(
  {
    parent: editorElem
  });

  let updateListenerExtension = EditorView.updateListener.of((update) => 
  {
    if (update.docChanged)
    {
      inputElem.value = update.state.doc.toString();
    }
  });

  let langExtension;
  if (language === "json") langExtension = json();
  else if (language === "sql") langExtension = sql();
  else if (language === "html") langExtension = html();
  else if (language === "xml") langExtension = xml();
  else langExtension = javascript();
  
  const extensions = [
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    drawSelection(),
    EditorState.allowMultipleSelections.of(true),
    indentOnInput(),
    defaultHighlightStyle.fallback,
    bracketMatching(),
    highlightActiveLine(),
    highlightSelectionMatches(),
    keymap.of([
      ...defaultKeymap,
      ...searchKeymap,
      ...historyKeymap,
      ...foldKeymap
    ]),
    langExtension,
    updateListenerExtension,
    theme];

  if (showLineNumbers)
  {
    extensions.push(lineNumbers());
    extensions.push(foldGutter());
  }
  
  if (completion)
  {
    extensions.push(autocompletion({override: [completion]}));
  }
  
  let editorState = EditorState.create(
  {
    doc: inputElem.value,
    extensions : extensions
  });

  editorView.setState(editorState);
}

window.codemirrorInit = codemirrorInit;

