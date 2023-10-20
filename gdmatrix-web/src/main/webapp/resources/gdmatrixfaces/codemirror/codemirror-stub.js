/* codemirror-stub.js */

import "./codemirror.js";

function codemirrorInit(clientId, readonly)
{
  console.info("INIT " + clientId);
  
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
  const { defaultHighlightStyle } = CM["@codemirror/highlight"];
  const { searchKeymap, highlightSelectionMatches } = CM["@codemirror/search"];
  const { indentOnInput } = CM["@codemirror/language"];
  const { EditorState } = CM["@codemirror/state"];

  let theme = EditorView.theme({
    "&.cm-focused .cm-cursor" : {
      borderLeftColor: "#000",
      borderLeftWidth: "2px"
    },
    "&.cm-focused .cm-matchingBracket" : {
      "backgroundColor" : "yellow",
      "color" : "black"
    },
    "& .ͼa" : {
      "color" : "#444",
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

  const extensions = [
    lineNumbers(),
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    foldGutter(),
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
    javascript(),
    updateListenerExtension,
    theme];
  
  let editorState = EditorState.create(
  {
    doc: inputElem.value,
    extensions : extensions
  });

  editorView.setState(editorState);
}

window.codemirrorInit = codemirrorInit;

