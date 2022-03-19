/** install.js **/

function install()
{
  output.println("List package content:");
  var files = packageDir.listFiles();
  for (var i = 0; i < files.length; i++)
  {
    var file = files[i];
    output.println(file.name);
  }
}
