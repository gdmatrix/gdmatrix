function paintFace(id, compliance, punctuality, veracity, presence)
{
  var manipulation = 100 - veracity;  
  
  var size = 300;
  var elem = document.getElementById(id);
  if (!elem) return;
  var canvas = document.createElement("canvas");
  elem.appendChild(canvas);
  canvas.width = size;
  canvas.height = size;
  var cx = size / 2;
  var cy = size / 2;
  var radius = (size / 2) - 10;
  var ctx = canvas.getContext("2d");
  // fill color (compliance)
  if (compliance >= 100)
  {
    var gradient = ctx.createLinearGradient(0, 0, 0, 2 * radius);
    gradient.addColorStop(0, "#F0F080");
    gradient.addColorStop(1, "#E0E080");
    ctx.fillStyle = gradient;
  }
  else
  {
    var i = Math.round(Math.min(2 * compliance, 180));
    var redColor = "rgb(255," + i + "," + i + ")";
    var gradient = ctx.createLinearGradient(0, 0, 0, 2 * radius);
    gradient.addColorStop(0, redColor);
    gradient.addColorStop(1, "#E0E080");
    ctx.fillStyle = gradient;
  }
  ctx.beginPath();
  ctx.arc(cx, cy, radius, 0, 2 * Math.PI);
  ctx.fill();

  var eyeY = cy - radius / 4;
  var leftEyeX = cx - radius / 3;
  var rightEyeX = cx + radius / 3;
  var eyeSizeX;
  var eyeSizeY;
  
  if (punctuality == 100)
  {
    eyeSizeX = 10;
    eyeSizeY = 10;    
  }
  else if (punctuality > 99.9)
  {
    eyeSizeX = 10;
    eyeSizeY = 8;    
  }
  else
  {
    eyeSizeX = 11;
    eyeSizeY = 7;        
  }

  try
  {
    // left eye
    ctx.beginPath();
    ctx.ellipse(leftEyeX, eyeY, eyeSizeX, eyeSizeY, 0, 0, 2 * Math.PI, false);
    ctx.stroke();

    // right eye
    ctx.beginPath();
    ctx.ellipse(rightEyeX, eyeY, eyeSizeX, eyeSizeY, 0, 0, 2 * Math.PI, false);
    ctx.stroke();
  }
  catch (e)
  {
    // ellipse unsuported
    // left eye
    ctx.beginPath();
    ctx.arc(leftEyeX, eyeY, eyeSizeX, 0, 2 * Math.PI);
    ctx.stroke();

    // right eye
    ctx.beginPath();
    ctx.arc(rightEyeX, eyeY, eyeSizeX, 0, 2 * Math.PI);
    ctx.stroke();
  }
  
  var manip = 0.2 * manipulation;

  // left eyebrow (manipulation)
  ctx.beginPath();
  ctx.moveTo(leftEyeX + eyeSizeX, eyeY - 2.2 * eyeSizeY);
  ctx.lineTo(leftEyeX - eyeSizeX, eyeY - 2.2 * eyeSizeY - manip);
  ctx.stroke();  

  // right eyebrow (manipulation)
  ctx.beginPath();
  ctx.moveTo(rightEyeX - eyeSizeX, eyeY - 2.2 * eyeSizeY);
  ctx.lineTo(rightEyeX + eyeSizeX, eyeY - 2.2 * eyeSizeY - manip);
  ctx.stroke();
  
  // mouth (presence)
  var mouthSize = Math.min(0.3 * Math.abs(presence - 100), 0.05 * radius);
  var mouthY = cy + (0.4 / (0.003 * presence + 1)) * radius;
  var sep = 0.1 * radius;
  var mouthValue = presence - 80;
  if (mouthValue < -10) mouthValue = -10;
  else if (mouthValue > 40) mouthValue = 40;
  var cp1x = leftEyeX + sep;
  var cp1y = mouthY + mouthValue;
  var cp2x = rightEyeX - sep;
  var cp2y = mouthY + mouthValue;
  ctx.beginPath();
  ctx.moveTo(leftEyeX - mouthSize, mouthY);
  ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, rightEyeX + mouthSize, mouthY);
  ctx.stroke();
}
