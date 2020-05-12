<script type="text/javascript" language="javascript">
function play()
{
  var i = document.getElementById("youtube_image");
  var v = document.getElementById("youtube_video");
  i.style.display = "none";v.style.display = "block";
}
</script>
<a id="youtube_image" href="javascript:play();"
  style="display:inline-block;width:${imageWidth};height:${imageHeight}">
  <img src="/documents/${imageDocId}" border=0 width="100%" height="100%"
    title="${imageTitle}" alt="${imageTitle}" />
</a>
<div id="youtube_video" style="text-align:center;display:none">
${code}
</div>


