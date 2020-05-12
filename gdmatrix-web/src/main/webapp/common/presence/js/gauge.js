function paintGauges(compliance, punctuality, veracity, presence)
{
  paintGauge("compliance_gauge", 
    [[0.0, "#ff0000"], [0.40, "#ffff00"], [0.50, "#00ff00"], [1.0, "#00c000"]], 
    0, 200, compliance);

  paintGauge("punctuality_gauge", 
    [[0.0, "#ff0000"], [0.90, "#ffff00"], [0.95, "#00ff00"], [1.0, "#00c000"]], 
    0, 100, punctuality);

  paintGauge("veracity_gauge", 
    [[0.0, "#ff0000"], [0.70, "#ffff00"], [0.90, "#00ff00"], [1.0, "#00c000"]], 
    0, 100, veracity);

  paintGauge("presence_gauge", 
    [[0.0, "#ff0000" ], [0.20, "#ffff00"], [0.40, "#00ff00"], [1.0, "#00c000"]], 
    0, 200, presence);
}

function paintGauge(id, colors, minValue, maxValue, value)
{
  var opts = {
    lines: 12,
    angle: 0.05,
    lineWidth: 0.44,
    pointer: {
      length: 0.9,
      strokeWidth: 0.035,
      color: '#000000'
    },
    limitMax: 'true', 
    percentColors: colors,
    strokeColor: '#E0E0E0',
    generateGradient: true
  };
  var target = document.getElementById(id);
  var canvas = document.createElement("canvas");
  canvas.width = 250;
  canvas.height = 100;  
  target.appendChild(canvas);
  var gauge = new Gauge(canvas).setOptions(opts);
  gauge.animationSpeed = 50;
  gauge.minValue = minValue;
  gauge.maxValue = maxValue;
  gauge.set(value == 0 ? 1 : value);    
  return gauge;
}
