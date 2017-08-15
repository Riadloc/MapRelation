$(document).ready(function() {
	var widget = $('.tabs-vertical');
	var tabs = widget.find('ul li'),
		content = widget.find('.tabs-content > div');
	tabs.on('click', function (e) {
		e.preventDefault();
		// Get the data-index attribute, and show the matching content div
		var index = $(this).data('index');
		tabs.removeClass('tab-active');
		content.removeClass('tab-content-active');
		$(this).addClass('tab-active');
		content.eq(index).addClass('tab-content-active');
	});
});