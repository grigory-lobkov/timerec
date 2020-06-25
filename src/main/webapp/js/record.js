var API_URL = "/timerec/api/" + "record/";
var SERVICES = [];
var SERVICE_ID = 0;
var SCHEDULE = [];
var SCHEDULE_TIME = "";
var DAYS = [];

const DAY_DATE_FORMAT = new Intl.DateTimeFormat(window.navigator.language, { weekday: 'long', month: 'long', day: 'numeric' });
var TIME_CLASS_BUSY = "btn btn-secondary";
var TIME_CLASS_FREE = "btn btn-outline-secondary";
var TIME_CLASS_CHOOSEN = "btn btn-primary";

function getServiceHtml( s ) {
  return '   <div class="row"> ' +
    '     <div class="col-md-4">' +
    '       <img width="256" height="256" src="img/service.png" class="avatar img-circle" alt="' + s.name + '">' +
    '     </div>' +
    '     <div class="col-md-8">' +
    '       <h3>' + s.name + '</h3>' +
    '       <br>' +
    '       <div>' + s.description + '</div>' +
    '       <div><small>' + s.duration + ' min</small></div>' +
    '       <br>' +
    '       <input type="button" class="btn btn-primary" value="Check In" onclick="getSchedule(' + s.service_id + '); return false;">' +
    '     </div>' +
    '  </div>';
}
function showServices() {
    var html = '';
    if( SERVICES.length > 0 ) {
        SERVICES.forEach( function( s ){
            html += getServiceHtml( s );
        });
    }
    $( "#serviceList" ).html( html );
    $( "#schedulePage" ).hide();
    $( "#servicePage" ).show();
}
function getServices() {
    $( "#schedulePage" ).hide();
    $( "#serviceList" ).html( '...' );
    $( "#servicePage" ).show();
    var url = API_URL + "?data=services";
	getAjaxJson({
		url: url,
		done: function( data ) {
            SERVICE_ID = 0;
            SERVICES = [];
            data.forEach( function( s ){
                SERVICES[s.service_id] = s;
            });
			showServices();
		}
	});
}

function getScheduleDayStr(day) {
    dt = new Date(day);
    dtStr = DAY_DATE_FORMAT.format(dt);   // date to string
    dtStr = dtStr[0].toUpperCase() + dtStr.substring(1); // first letter to upper
    return dtStr;
}

function getScheduleDayHtml(day, scheduleHtml) {
    var html = "";
    if(day) {
        dtStr = getScheduleDayStr( day );
        html += '<h4>' + dtStr + '</h4>' +
            '<div>' + scheduleHtml + '</div>' +
            '<br>';
    }
    return html;
}

function getScheduleTime(t) {
    var time = t.substring(11).substring(0,8);
    if((time.length==8) && (time.substring(6)=='00'))
        time = time.substring(0,5);
    return time;
}

function getScheduleTimeHtml(s) {
    var time = getScheduleTime(s.start);
    var btnClass = "btn";
    var btnAttrib = "";
    if(s.type=="SCH_BUSY") {
        btnClass = TIME_CLASS_BUSY;
        btnAttrib = 'disabled';
    } else if(s.type=="SCH_FREE") {
        btnClass = TIME_CLASS_FREE;
        interval = getScheduleInterval(s);
        btnAttrib = 'onclick="clickTime(this,\'' +interval+ '\'); return false;"';
    }
    return '<input type="button" class="' + btnClass + '" ' +
        'id="' + s.start + '" ' +
        'value="' + time + '" ' +
        btnAttrib + '> ';
}

function getScheduleInterval(s) {
    var fr = getScheduleTime(s.start);
    var to = timeFromSeconds( timeToSeconds( fr ) + (s.duration * 60) );
    return fr + '—' + to;
}

function clickTime(b, interval) {
    if(SCHEDULE_TIME) {
        var old = document.getElementById( SCHEDULE_TIME );
        old.className = TIME_CLASS_FREE;
    }
    b.className = TIME_CLASS_CHOOSEN;
    SCHEDULE_TIME = b.id;

    var dtStr = getScheduleDayStr( b.id.substring(0,10) );
    var timeObj = document.getElementById( 'serviceTime' );
    timeObj.innerHTML = dtStr + ', ' + interval;

	$( "#serviceBtn" ).show();
}

function showSchedule() {
    var html = "";
    var htmlDay = "";
    var currDay = "";
    SCHEDULE.forEach( function( s ){
        var nowDay = s.start.substring( 0, 10 );
        if(nowDay!=currDay) {
            html += getScheduleDayHtml( currDay, htmlDay );
            htmlDay = "";
            currDay = nowDay;
        }
        htmlDay += getScheduleTimeHtml( s );
    });
    html += getScheduleDayHtml( currDay, htmlDay );
    $( "#scheduleList" ).html( html );
}

function getSchedule(s_id) {
	SERVICE_ID = s_id;
	SCHEDULE = [];
    $( "#servicePage" ).hide();
    $( "#scheduleList" ).html( '...' );
    $( "#serviceName" ).text( SERVICES[s_id].name );
    $( "#serviceTime" ).text( '' );
    $( "#schedulePage" ).show();
	$( "#serviceBtn" ).hide();

	var url = API_URL + "?data=schedule&service_id=" + SERVICE_ID;
	getAjaxJson({
		url: url,
		done: function( data ) {
	        SCHEDULE_TIME = "";
	        $( "#serviceBtn" ).hide();
            data.sort(function (a, b) {
              if (a.start > b.start) {
                return 1;
              }
              if (a.start < b.start) {
                return -1;
              }
              return 0;
            });
            SCHEDULE = data;
			showSchedule();
		}
	});
}

function checkData() {
	alertMessageHide();

	return true;
}

function timeToSeconds(strTime, description) {
    if( !( strTime ) ) return null;
	var regex = /(\d{1,2}):(\d{1,2})/;
	var match = regex.exec(strTime);
    var h = +match[1];
    var m = +match[2];
    var seconds = h * 60 * 60 + m * 60;
    return seconds;
}

function timeFromSeconds(seconds) {
	var h = Math.trunc(seconds / 60 / 60);
    var m = Math.trunc(seconds / 60) - h * 60;
    hh = ( h < 10 ? '0' : '' ) + h;
    mm = ( m < 10 ? '0' : '' ) + m;
    return hh + ':' + mm;
}

function backToServices() {
    $( "#servicePage" ).show();
    $( "#schedulePage" ).hide();
}

function postData() {
	if( !checkData() ) return false;
	getAjaxJson({
		method: "POST",
		url: API_URL,
		data: JSON.stringify({
		    service_id: SERVICE_ID,
		    start: SCHEDULE_TIME, //new Date( SCHEDULE_TIME ),//.toJSON(),
		    title: $( "#uTitle" ).val(),
		    description: $( "#uDescription" ).val(),
		}),
		done: function( data ) {
		    if( data.success ) {
                alertMessage( "You have recorded successfully" );
                $( "#servicePage" ).hide();
                $( "#schedulePage" ).hide();
                setTimeout( function(){
                    window.location.href = "index.html";
                }, 10000 );
			} else if( data.busy ) {
			    busy_msg = "This time is already busy, sorry";
                alertMessage( busy_msg );
                setTimeout( function(){
                    getSchedule(SERVICE_ID);
                    if( $( '#alertMessage' ).text() == "" ) {
                        alertMessage( busy_msg );
                    }
                }, 3000 );
                setTimeout( function(){
                    alertMessageHide();
                }, 10000 );
			} else {
			    alertMessage( "Record status is unknown. Please, try again later", JSON.stringify( data ) );
			}
		}
	})
}

document.addEventListener( 'DOMContentLoaded', function(){ getServices() } );