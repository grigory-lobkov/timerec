
//API_SERVER_URI = "http://gri.myqnapcloud.com:8081/timerec/";
API_SERVER_URI = "proxy.php?url=http://gri.myqnapcloud.com:8081/timerec/";

_GET = null;
SERVICE_ID = null;

function getUrlVars() {
	if(_GET == null) {
		_GET = {};
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			_GET[hash[0]] = hash[1];
		}
	}
	if(SERVICE_ID!=null) _GET["service_id"]=SERVICE_ID;
    return _GET;
}

function getMenu(cache=true) {
	$.ajax({
		method: "GET",
		url: API_SERVER_URI + "menu",
		cache: cache,
		dataType: "json"
	}).done(function( data ) {
		fillMenu(data);
	}).fail(function( jqXHR, textStatus ) {
		alert( "Menu Get failed: " + textStatus );
	});
}

function fillMenu(data) {
	// for service menu
	function addMenuElement(name, page, params) {
		if(window.location.pathname == page) {
			return '<li class="nav-item active"><a class="nav-link" href="'+page+params+'">'+name+' <span class="sr-only">(current)</span></a></li>';
		} else {
			return '<li class="nav-item"><a class="nav-link" href="'+page+params+'">'+name+'</a></li>';
		}
	}
	// https://getbootstrap.com/docs/4.5/components/navbar/
	// user menu
	var uMenu =
		'<li class="nav-item dropdown">'+
			'<a class="nav-link dropdown-toggle" href="#" id="navbarDropdownU" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
			data.user.name+
			'</a>'+
			'<div class="dropdown-menu" aria-labelledby="navbarDropdownU">'+
			'<a class="dropdown-item disabled" href="#">Log in</a>'+
			'<a class="dropdown-item" href="profile.html">Profile</a>'+
			'<a class="dropdown-item" href="settings.html">Settings</a>'+
			'<div class="dropdown-divider"></div>'+
			'<a class="dropdown-item disabled" href="#">Log out</a>'+
			'</div>'+
		'</li>';
	// services list
	var sMenu = '';
	var serviceId = getUrlVars()["service_id"];
	var serviceName = '(service list)'
	var ss = data.services
    for (var i = 0, len = ss.length; i < len; i++) {
		if(serviceId==ss[i].service_id) {
			serviceName = ss[i].name;
			sMenu += '<a class="dropdown-item active" href="'+window.location.pathname+'?service_id='+ss[i].service_id+'">'+ss[i].name+' <span class="sr-only">(current)</span></a>';
		} else {
			sMenu += '<a class="dropdown-item" href="'+window.location.pathname+'?service_id='+ss[i].service_id+'">'+ss[i].name+'</a>';
			if(serviceId==null) serviceId = ss[i].service_id;
		}
    }
	var urlParams = "?service_id=" + serviceId;
	// services menu
	var mMenu = addMenuElement('Service', 'service.html', urlParams) +
		addMenuElement('Repeat', 'repeat.html', urlParams) +
		addMenuElement('Schedule', 'schedule.html', urlParams) +
		'<li class="nav-item dropdown">'+
			'<a class="nav-link dropdown-toggle" href="#" id="navbarDropdownS" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
				serviceName+
			'</a>'+
			'<div class="dropdown-menu" aria-labelledby="navbarDropdownS">'+sMenu+'</div>'+
		'</li>';
	// menu
	var menu =
		'<a class="navbar-brand" href="#">TimeRec</a>'+
		'<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">'+
		'<span class="navbar-toggler-icon"></span>'+
		'</button>'+
		'<div class="collapse navbar-collapse" id="navbarSupportedContentS">'+
		'<!-- SERVICE MENU -->'+
		'<ul class="navbar-nav mr-auto">'+mMenu+'</ul>'+
		'<!-- /SERVICE MENU -->'+
		'<!-- USER MENU -->'+
		'<ul class="navbar-nav mr-right">'+uMenu+'</ul>'+
		'<!-- /USER MENU -->'+
		'</div>';
	
	$('#navMenuContainer').html(menu)
		.addClass('navbar navbar-expand-lg navbar-light')
		.attr('style','background-color: #e3f2fd;');

}

function alertMessage(htmlText, htmlTitle="") {
	$('#alertMessage').html(
		'<div class="alert alert-info alert-dismissable">'+
		'<a class="panel-close close" data-dismiss="alert">×</a>'+
		(htmlTitle==""?"":'<i class="fa fa-coffee">'+htmlTitle+'<br></i>')+
		htmlText+
		'</div>'
	);
}
function alertMessageHide() {
	$('#alertMessage').text("");
}

document.addEventListener('DOMContentLoaded', function(){ getMenu() });