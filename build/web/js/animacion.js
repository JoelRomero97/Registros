$(document).ready(function(){

	$('.ir_arriba').click(function(){
		$('body, html').animate({
			scrollTop:'0px'
		}, 550);
	});
	$(window).scroll(function(){
		if($(this).scrollTop() > 0){
			$('.ir_arriba').slideDown(550);
		}else{
			$('.ir_arriba').slideUp(550);
		}
	});
			$('a.sec0').click(function(e){
			e.preventDefault();
		    enlace  = $(this).attr('href');
		    $('html, body').animate({
		        scrollTop: $(enlace).offset().top
		    }, 550);
			});


			$('a.sec1').click(function(e){
			e.preventDefault();
		    enlace  = $(this).attr('href');
		    $('html, body').animate({
		        scrollTop: $(enlace).offset().top
		    }, 550);
			});
			//vamos al principio o al final de la página

			$('a.sec2').click(function(e){
			e.preventDefault();
		    volver  = $(this).attr('href');
		    $('html, body').animate({
		        scrollTop: $(volver).offset().top
		    }, 550);
			});
			//pasando la cantidad de pixeles que queremos a scrollTop

			$('a.sec3').click(function(e){
			e.preventDefault();
		    sect  = $(this).attr('href');
		    $('html, body').animate({
		        scrollTop: $(sect).offset().top
		    }, 550);
			});
});