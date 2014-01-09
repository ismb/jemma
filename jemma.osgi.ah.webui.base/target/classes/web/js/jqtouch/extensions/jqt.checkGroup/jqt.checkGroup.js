/*

            _/    _/_/    _/_/_/_/_/                              _/       
               _/    _/      _/      _/_/    _/    _/    _/_/_/  _/_/_/    
          _/  _/  _/_/      _/    _/    _/  _/    _/  _/        _/    _/   
         _/  _/    _/      _/    _/    _/  _/    _/  _/        _/    _/    
        _/    _/_/  _/    _/      _/_/      _/_/_/    _/_/_/  _/    _/     
       _/                                                                  
    _/

    Created by David Kaneda <http://www.davidkaneda.com>
    checkGroup extension by Daniel J. Pinter <Nimbus.Software@gmail.com>
    Documentation and issue tracking on Google Code <http://code.google.com/p/jqtouch/>
    
    Special thanks to Jonathan Stark <http://jonathanstark.com/>
    and pinch/zoom <http://www.pinchzoom.com/>
    
    (c) 2009 by jQTouch project members.
    See LICENSE.txt for license.

*/

(function ($) {
  if ($.jQTouch) {
    $.jQTouch.addExtension(function checkGroup(jQT) {
      var defaultColor, cgName, selectedColor, undefined, cgValue;
      var cgUseLocalStorage = new Boolean();
      $(function () {
        $(".checkGroup").children("li").click(function () {
          $(this).siblings().each(function () {
            if (defaultColor === undefined && $("ul.checkGroup li").css("color") !== selectedColor) {
              defaultColor = $("ul.checkGroup li").css("color");
            }
            $(this).css("color", defaultColor);
          })
          $($(this).children("input")).each(function () {
            this.checked = true;
              cgName = $(this).attr("name");
              cgValue = $(this).attr("value");
            if (cgUseLocalStorage) {
              localStorage.setItem(cgName, cgValue);
              getCGValue();
            }
          });
          if (selectedColor === undefined) {
            selectedColor = $("ul.checkGroup li input:radio").css("color");
          }
          $(this).css("color", selectedColor);
        });  
      });
      function getCGValue(){
        return {
          cgName:cgName,
          cgValue:cgValue
        };
      }
      return {
        getCGValue: getCGValue
      };
    });
  }
})(jQuery);