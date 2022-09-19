/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */

function initSlick()
{
  var w = $(window).width();   
  destroyCarousel();
  if (w <= 991)
  {  
    $('.slick-columns').slick({
      dots: true,
      arrows: false,
      slidesToShow: 1,
      slidesToScroll: 1,
      variableWidth: false,
      initialSlide: 1        
    });
    clickSlickDot(2);
  }
}

function destroyCarousel() {
  if ($('.slick-columns').hasClass('slick-initialized')) {
    $('.slick-columns').slick('destroy');
  }      
} 

function clickSlickDot(index)
{
 $('.slick-dots li:nth-child(' + index + ') button').click();        
}

initSlick();

if (window.addEventListener)
{
  window.addEventListener("load", initSlick, false);
  window.addEventListener("resize", initSlick, false);
}
else if (window.attachEvent)
{
  window.attachEvent("onload", initSlick);
  window.attachEvent("onresize", initSlick);
}
