/* codemirror-stub.js */

import "./codemirror.js";

function codemirrorInit(clientId, readonly, language, showLineNumbers, completion)
{
  const editorId = clientId + "_editor";
  const inputId = clientId + "_input";

  const editorElem = document.getElementById(editorId);
  editorElem.className = "cm-editor-holder";

  const inputElem = document.getElementById(inputId);

  const { keymap, highlightSpecialChars, drawSelection, 
          highlightActiveLine, dropCursor,
          rectangularSelection, crosshairCursor, EditorView,
          lineNumbers, highlightActiveLineGutter } = CM["@codemirror/view"];
  const { Extension, EditorState } = CM["@codemirror/state"];
  const { defaultHighlightStyle, syntaxHighlighting, indentOnInput, 
          bracketMatching, foldGutter, foldKeymap} = CM["@codemirror/language"]
  const { defaultKeymap, history, historyKeymap } = CM["@codemirror/commands"];
  const { searchKeymap, highlightSelectionMatches } = CM["@codemirror/search"];
  const { autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap } =
          CM["@codemirror/autocomplete"];
  const { lintKeymap } = CM["@codemirror/lint"];

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
      "backgroundColor" : "rgba(128, 128, 128, 0.1)"
    },
    "&.ͼ2 .cm-activeLineGutter" : {
      "backgroundColor" : "rgba(128, 128, 128, 0.1)"
    },
    "&.ͼ2 .cm-tooltip" : {
      "backgroundColor" : "var(--surface-overlay)"
    },
    "&.ͼ2 .cm-gutters" : {
      "backgroundColor" : "var(--surface-overlay)"
    },
    "& .ͼa" : {
      "color" : "#666",
      "fontWeight" : "bold"
    },
    "& .ͼl" : {
      "color" : "#808080"
    },
    "& .ͼd" : {
      "color" : "#2020ff"
    },
    "& .cm-line" : {
      "color" : "var(--text-color)"
    },
    "& .cm-wrap" : {
      "height" : "100%"
    },
    "& .cm-scroller" : {
      "overflow" : "auto"
    },
    "& .ͼb" : {
      "color" : "#444",
      "fontWeight" : "bold"
    },
    "& .ͼe" : {
      "color" : "#2020ff"
    },
    "& .ͼf" : {
      "color" : "#8080e0"
    },
    "& .ͼg" : {
      "color" : "#444"
    },
    "& .ͼm" : {
      "color" : "#808080"
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
    highlightActiveLineGutter(),
    highlightSpecialChars(),
    history(),
    drawSelection(),
    dropCursor(),
    EditorState.allowMultipleSelections.of(true),
    indentOnInput(),
    syntaxHighlighting(defaultHighlightStyle, {fallback: true}),
    bracketMatching(),
    closeBrackets(),
    autocompletion(),
    rectangularSelection(),
    crosshairCursor(),
    highlightActiveLine(),
    highlightSelectionMatches(),
    keymap.of([
      ...closeBracketsKeymap,
      ...defaultKeymap,
      ...searchKeymap,
      ...historyKeymap,
      ...foldKeymap,
      ...completionKeymap,
      ...lintKeymap
    ]),
    updateListenerExtension,
    theme];

  switch (language)
  {
    case "html":
      const { html } = CM["@codemirror/lang-html"];
      extensions.push(html());
      break;

    case "json":
      const { json } = CM["@codemirror/lang-json"];
      extensions.push(json());
      break;

    case "xml":
      const { xml } = CM["@codemirror/lang-xml"];
      extensions.push(xml({ autoCloseTags : true }));
      break;

    case "sql":
      const { sql } = CM["@codemirror/lang-sql"];
      extensions.push(sql());
      break;

    case "css":
      const { css } = CM["@codemirror/lang-css"];
      extensions.push(css());
      break;    
    
    default:
      const { javascript } = CM["@codemirror/lang-javascript"];
      extensions.push(javascript());
      break;      
  }

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

