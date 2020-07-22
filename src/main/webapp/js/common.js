
//API_SERVER_URI = "http://gri.myqnapcloud.com:8081/timerec/api/";
//API_SERVER_URI = "proxy.php?url=http://gri.myqnapcloud.com:8081/timerec/";
//API_SERVER_URI = "/timerec/api/";

_GET = null;
SERVICE_ID = null;
/*
AJAX_ERRSTATUS_CODE_PRC =
	{
		401: function() {
			alert("Sorry, you have no access for this action");
			//window.location.replace("login.html");
		}
	};
AJAX_FAIL_PRC =
	function(jqXHR, textStatus) {
		alertMessage( "Request failed: " + textStatus );
	};
*/

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

function getMenu(cache) {
/*	$.ajax({
		method: "GET",
		url: API_SERVER_URI + "menu",
		cache: cache,
		dataType: "json"
	}).done(function( data ) {
		fillMenu(data);
	}).fail(function( jqXHR, textStatus ) {
		alert( "Menu Get failed: " + textStatus );
	});*/
	if(!( cache )) cache = true;
	var serviceId = getUrlVars()["service_id"];
	var url = "/timerec/api/" + "menu" + (serviceId ? "?service_id=" + serviceId : "");
	getAjaxJson({
		url: url,
		cache: cache,
		done: function( data ) {
			fillMenu(data);
		},
		fail: function( jqXHR, textStatus ) {
			alert( "Menu Get failed: " + textStatus );
		}
	});
}

function fillMenu(data) {
	// for service menu
	function addMenuElement( name, page, curPage ) {
		if( curPage == page ) {
			return '<li class="nav-item active"><a class="nav-link" href="'+page+'">'+name+' <span class="sr-only">(current)</span></a></li>';
		} else {
			return '<li class="nav-item"><a class="nav-link" href="'+page+'">'+name+'</a></li>';
		}
	}

	// https://getbootstrap.com/docs/4.5/components/navbar/
	// user menu
	var isUser = data.user.user_id > 0;
	var uMenu =
		'<li class="nav-item dropdown">'+
			'<a class="nav-link dropdown-toggle" href="#" id="navbarDropdownU" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
			data.user.name+
			'</a>'+
			'<div class="dropdown-menu" aria-labelledby="navbarDropdownU">'+
			'<a class="dropdown-item'+(isUser?' disabled':'')+'" href="login.html">Log in</a>'+
			'<a class="dropdown-item'+(!isUser?' disabled':'')+'" href="profile.html">Profile</a>'+
			'<div class="dropdown-divider"></div>'+
			'<a class="dropdown-item'+(!isUser?' disabled':'')+'" href="logout.html">Log out</a>'+
			'</div>'+
		'</li>';

    // pages menu
    var mMenu = "";
	var path = window.location.href;
    var page = path.split("/").pop();
	var ps = data.pages;
	var isService = false;
    for (var i = 0, len = ps.length; i < len; i++) {
        if(isUser && ps[i].item == 'login') {
            //skip
        } else {
		    mMenu += addMenuElement(ps[i].name, ps[i].item+'.html'+( ps[i].param ? '?' + ps[i].param : ''), page);
		}
		if( ps[i].item == 'service' ) {
		    isService = true;
		}
    }

	// services list
	var sMenu = '';
    var page = page.split("?").shift();
	if( isService ) {
        var serviceId = getUrlVars()["service_id"];
        var serviceName = '(service list)';
        if( page != 'service.html' && page != 'repeat.html' && page != 'schedule.html' )
            page = 'service.html';
        var ss = data.services;
        for ( var i = 0, len = ss.length; i < len; i++ ) {
            if( serviceId == ss[i].service_id ) {
                serviceName = ss[i].name;
                sMenu += '<a class="dropdown-item active" href="'+page+'?service_id='+ss[i].service_id+'">'+ss[i].name+' <span class="sr-only">(current)</span></a>';
            } else {
                sMenu += '<a class="dropdown-item" href="'+page+'?service_id='+ss[i].service_id+'">'+ss[i].name+'</a>';
                if( serviceId == null ) serviceId = ss[i].service_id;
            }
        }
	}

	// services menu
	/*var urlParams = serviceId==null?"":"?service_id=" + serviceId;
    var mMenu = addMenuElement('Service', 'service.html', urlParams) +
        addMenuElement('Repeat', 'repeat.html', urlParams) +
        addMenuElement('Schedule', 'schedule.html', urlParams) +
        '<li class="nav-item dropdown">'+
            '<a class="nav-link dropdown-toggle" href="#" id="navbarDropdownS" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
                serviceName+
            '</a>'+
            '<div class="dropdown-menu" aria-labelledby="navbarDropdownS">'+sMenu+'</div>'+
        '</li>';*/

    if(sMenu && isService) {
		mMenu += '<li class="nav-item dropdown">'+
    			'<a class="nav-link dropdown-toggle" href="#" id="navbarDropdownS" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+
    				serviceName+
    			'</a>'+
    			'<div class="dropdown-menu" aria-labelledby="navbarDropdownS">'+sMenu+'</div>'+
    		'</li>';
    }

	// menu
	var menu =
		'<a class="navbar-brand" href="index.html">TimeRec</a>'+
		'<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContentS" aria-controls="navbarSupportedContentS" aria-expanded="false" aria-label="Toggle navigation">'+
		'<span class="navbar-toggler-icon"></span>'+
		'</button>'+
		'<div class="collapse navbar-collapse" id="navbarSupportedContentS">'+
		'<!-- SERVICE MENU -->'+
		'<ul class="navbar-nav mr-auto">'+mMenu+'</ul>'+
		'<!-- /SERVICE MENU -->'+
		'<!-- USER MENU -->'+
		'<ul class="navbar-nav mr-right" style="min-width: 150px;">'+uMenu+'</ul>'+
		'<!-- /USER MENU -->'+
		'</div>';
	
	$('#navMenuContainer').html(menu)
		.addClass('navbar navbar-expand-lg navbar-light')
		.attr('style','background-color: #e3f2fd;');

}

function alertMessage(htmlText, htmlTitle) {
    if(!( htmlTitle )) htmlTitle="";
	$('#alertMessage').html(
		'<div class="alert alert-info alert-dismissable">'+
		'<a class="panel-close close" data-dismiss="alert">×</a>'+
		(htmlTitle==""?"":'<i class="fa fa-coffee">'+htmlTitle+'<br></i>')+
		htmlText+
		'</div>'
	);
}
function alertMessageHide() {
	$( '#alertMessage' ).text( "" );
}

function getCookie( name ) {
    var dc = document.cookie;
    var prefix = name + "=";
    var begin = dc.indexOf( "; " + prefix );
    if( begin == -1 ) {
        begin = dc.indexOf( prefix );
        if (begin != 0) return null;
    } else {
        begin += 2;
    }
    var end = document.cookie.indexOf( ";", begin );
    if( end == -1 ) {
        end = dc.length;
    }
    return decodeURI( dc.substring( begin + prefix.length, end ) );
}

function mergeObjects(obj1,obj2){
    var obj3 = {};
    for (var attrname in obj1) { obj3[attrname] = obj1[attrname]; }
    for (var attrname in obj2) { obj3[attrname] = obj2[attrname]; }
    return obj3;
}

function getAjaxJson( _ ) {
	alertMessageHide();
	return $.ajax({
		method: _.method === undefined ? 'GET' : _.method,
		url: _.url,
		cache: false,
		dataType: "json",
		data: _.data,
		statusCode: mergeObjects( {
                403: function() {
                    msg = "Sorry, you have no access for this action";
                    alertMessage( msg );
                    alert( msg );
                    if( getCookie( "password") == null )
                        window.location.href = "login.html";
                }
			}, _.statusCode )
		,
		timeout: 5000, // timeout, milliseconds
	})
		.done( _.done === undefined ? function() {} : _.done )
		.fail( _.fail === undefined ? function( jqXHR, textStatus ) {
			alertMessage( _.url, "Request failed: " + textStatus );
		} : _.fail );
}

function htmlEntities(str) {
    var buf = [];
    var s = 0;
    if(str)
        for (var i=str.length-1;i>=0;i--) {
            c = str[i].charCodeAt();
            if ( c == 32 ) {
                buf.unshift( s % 2 == 1 ? '&nbsp' : ' ' );
                s++;
            } else if ( c == 13 || c == 10 ) {
                buf.unshift( '<br>' );
                s = 0;
            } else {
                buf.unshift( [ '&#', c, ';' ].join( '' ) );
                s = 0;
            }
        }
    return buf.join( '' );
}

document.addEventListener( 'DOMContentLoaded', function() {
    if($('#navMenuContainer').length) {
        getMenu();
    }
} );