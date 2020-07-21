package integration.impl;

import integration.IIntegrator;
import model.ScheduleRow;
import model.TzRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * MOODLE implementation of integration
 *
 * https://moodle.org/
 *
 * Class is singleton implemented
 * Constructor class: integration.Integrator
 */
public class MoodleIntegrator implements IIntegrator {

    HashMap<String, Integer> moodleTzs;
    private ITable<TzRow> storageTz = StorageFactory.getTzInstance();

    /**
     * Initialization block
     */
    {
        // from https://download.moodle.org/timezone/
        // Excel "MoodleIntegrator-java-init.xlsx" will help to refresh it
        moodleTzs = new HashMap<String, Integer>(1103);
        moodleTzs.put("Africa/Algiers", 60);
        moodleTzs.put("Atlantic/Cape_Verde", -60);
        moodleTzs.put("Africa/Ndjamena", 60);
        moodleTzs.put("Africa/Abidjan", 0);
        moodleTzs.put("Africa/Cairo", 120);
        moodleTzs.put("Africa/Accra", 0);
        moodleTzs.put("Africa/Bissau", 0);
        moodleTzs.put("Africa/Nairobi", 180);
        moodleTzs.put("Africa/Monrovia", 0);
        moodleTzs.put("Africa/Tripoli", 120);
        moodleTzs.put("Indian/Mauritius", 240);
        moodleTzs.put("Africa/Casablanca", 0);
        moodleTzs.put("Africa/El_Aaiun", 0);
        moodleTzs.put("Africa/Maputo", 120);
        moodleTzs.put("Africa/Windhoek", 60);
        moodleTzs.put("Africa/Lagos", 60);
        moodleTzs.put("Indian/Reunion", 240);
        moodleTzs.put("Indian/Mahe", 240);
        moodleTzs.put("Africa/Johannesburg", 120);
        moodleTzs.put("Africa/Khartoum", 180);
        moodleTzs.put("Africa/Tunis", 60);
        moodleTzs.put("Antarctica/Casey", 660);
        moodleTzs.put("Antarctica/Davis", 420);
        moodleTzs.put("Antarctica/Mawson", 300);
        moodleTzs.put("Indian/Kerguelen", 300);
        moodleTzs.put("Antarctica/DumontDUrville", 600);
        moodleTzs.put("Antarctica/Syowa", 180);
        moodleTzs.put("Antarctica/Troll", 0);
        moodleTzs.put("Antarctica/Vostok", 360);
        moodleTzs.put("Antarctica/Rothera", -180);
        moodleTzs.put("Asia/Kabul", 270);
        moodleTzs.put("Asia/Yerevan", 240);
        moodleTzs.put("Asia/Baku", 240);
        moodleTzs.put("Asia/Dhaka", 360);
        moodleTzs.put("Asia/Thimphu", 360);
        moodleTzs.put("Indian/Chagos", 360);
        moodleTzs.put("Asia/Brunei", 480);
        moodleTzs.put("Asia/Yangon", 390);
        moodleTzs.put("Asia/Shanghai", 480);
        moodleTzs.put("Asia/Urumqi", 360);
        moodleTzs.put("Asia/Hong_Kong", 480);
        moodleTzs.put("Asia/Taipei", 480);
        moodleTzs.put("Asia/Macau", 480);
        moodleTzs.put("Asia/Nicosia", 120);
        moodleTzs.put("Asia/Famagusta", 180);
        moodleTzs.put("Asia/Tbilisi", 240);
        moodleTzs.put("Asia/Dili", 540);
        moodleTzs.put("Asia/Kolkata", 330);
        moodleTzs.put("Asia/Jakarta", 420);
        moodleTzs.put("Asia/Pontianak", 420);
        moodleTzs.put("Asia/Makassar", 480);
        moodleTzs.put("Asia/Jayapura", 540);
        moodleTzs.put("Asia/Tehran", 210);
        moodleTzs.put("Asia/Baghdad", 180);
        moodleTzs.put("Asia/Jerusalem", 120);
        moodleTzs.put("Asia/Tokyo", 540);
        moodleTzs.put("Asia/Amman", 120);
        moodleTzs.put("Asia/Almaty", 360);
        moodleTzs.put("Asia/Qyzylorda", 360);
        moodleTzs.put("Asia/Aqtobe", 300);
        moodleTzs.put("Asia/Aqtau", 300);
        moodleTzs.put("Asia/Atyrau", 300);
        moodleTzs.put("Asia/Oral", 300);
        moodleTzs.put("Asia/Bishkek", 360);
        moodleTzs.put("Asia/Seoul", 540);
        moodleTzs.put("Asia/Pyongyang", 510);
        moodleTzs.put("Asia/Beirut", 120);
        moodleTzs.put("Asia/Kuala_Lumpur", 480);
        moodleTzs.put("Asia/Kuching", 480);
        moodleTzs.put("Indian/Maldives", 300);
        moodleTzs.put("Asia/Hovd", 420);
        moodleTzs.put("Asia/Ulaanbaatar", 480);
        moodleTzs.put("Asia/Choibalsan", 480);
        moodleTzs.put("Asia/Kathmandu", 345);
        moodleTzs.put("Asia/Karachi", 300);
        moodleTzs.put("Asia/Gaza", 120);
        moodleTzs.put("Asia/Hebron", 120);
        moodleTzs.put("Asia/Manila", 480);
        moodleTzs.put("Asia/Qatar", 180);
        moodleTzs.put("Asia/Riyadh", 180);
        moodleTzs.put("Asia/Singapore", 480);
        moodleTzs.put("Asia/Colombo", 330);
        moodleTzs.put("Asia/Damascus", 120);
        moodleTzs.put("Asia/Dushanbe", 300);
        moodleTzs.put("Asia/Bangkok", 420);
        moodleTzs.put("Asia/Ashgabat", 300);
        moodleTzs.put("Asia/Dubai", 240);
        moodleTzs.put("Asia/Samarkand", 300);
        moodleTzs.put("Asia/Tashkent", 300);
        moodleTzs.put("Asia/Ho_Chi_Minh", 420);
        moodleTzs.put("Australia/Darwin", 570);
        moodleTzs.put("Australia/Perth", 480);
        moodleTzs.put("Australia/Eucla", 525);
        moodleTzs.put("Australia/Brisbane", 600);
        moodleTzs.put("Australia/Lindeman", 600);
        moodleTzs.put("Australia/Adelaide", 570);
        moodleTzs.put("Australia/Hobart", 600);
        moodleTzs.put("Australia/Currie", 600);
        moodleTzs.put("Australia/Melbourne", 600);
        moodleTzs.put("Australia/Sydney", 600);
        moodleTzs.put("Australia/Broken_Hill", 570);
        moodleTzs.put("Australia/Lord_Howe", 630);
        moodleTzs.put("Antarctica/Macquarie", 660);
        moodleTzs.put("Indian/Christmas", 420);
        moodleTzs.put("Indian/Cocos", 390);
        moodleTzs.put("Pacific/Fiji", 720);
        moodleTzs.put("Pacific/Gambier", -540);
        moodleTzs.put("Pacific/Marquesas", -570);
        moodleTzs.put("Pacific/Tahiti", -600);
        moodleTzs.put("Pacific/Guam", 600);
        moodleTzs.put("Pacific/Tarawa", 720);
        moodleTzs.put("Pacific/Enderbury", 780);
        moodleTzs.put("Pacific/Kiritimati", 840);
        moodleTzs.put("Pacific/Majuro", 720);
        moodleTzs.put("Pacific/Kwajalein", 720);
        moodleTzs.put("Pacific/Chuuk", 600);
        moodleTzs.put("Pacific/Pohnpei", 660);
        moodleTzs.put("Pacific/Kosrae", 660);
        moodleTzs.put("Pacific/Nauru", 720);
        moodleTzs.put("Pacific/Noumea", 660);
        moodleTzs.put("Pacific/Auckland", 720);
        moodleTzs.put("Pacific/Chatham", 765);
        moodleTzs.put("Pacific/Rarotonga", -600);
        moodleTzs.put("Pacific/Niue", -660);
        moodleTzs.put("Pacific/Norfolk", 660);
        moodleTzs.put("Pacific/Palau", 540);
        moodleTzs.put("Pacific/Port_Moresby", 600);
        moodleTzs.put("Pacific/Bougainville", 660);
        moodleTzs.put("Pacific/Pitcairn", -480);
        moodleTzs.put("Pacific/Pago_Pago", -660);
        moodleTzs.put("Pacific/Apia", 780);
        moodleTzs.put("Pacific/Guadalcanal", 660);
        moodleTzs.put("Pacific/Fakaofo", 780);
        moodleTzs.put("Pacific/Tongatapu", 780);
        moodleTzs.put("Pacific/Funafuti", 720);
        moodleTzs.put("Pacific/Wake", 720);
        moodleTzs.put("Pacific/Efate", 660);
        moodleTzs.put("Pacific/Wallis", 720);
        moodleTzs.put("Europe/London", 0);
        moodleTzs.put("Europe/Dublin", 0);
        moodleTzs.put("WET", 0);
        moodleTzs.put("CET", 60);
        moodleTzs.put("MET", 60);
        moodleTzs.put("EET", 120);
        moodleTzs.put("Europe/Tirane", 60);
        moodleTzs.put("Europe/Andorra", 60);
        moodleTzs.put("Europe/Vienna", 60);
        moodleTzs.put("Europe/Minsk", 180);
        moodleTzs.put("Europe/Brussels", 60);
        moodleTzs.put("Europe/Sofia", 120);
        moodleTzs.put("Europe/Prague", 60);
        moodleTzs.put("Europe/Copenhagen", 60);
        moodleTzs.put("Atlantic/Faroe", 0);
        moodleTzs.put("America/Danmarkshavn", 0);
        moodleTzs.put("America/Scoresbysund", -60);
        moodleTzs.put("America/Godthab", -180);
        moodleTzs.put("America/Thule", -240);
        moodleTzs.put("Europe/Tallinn", 120);
        moodleTzs.put("Europe/Helsinki", 120);
        moodleTzs.put("Europe/Paris", 60);
        moodleTzs.put("Europe/Berlin", 60);
        moodleTzs.put("Europe/Gibraltar", 60);
        moodleTzs.put("Europe/Athens", 120);
        moodleTzs.put("Europe/Budapest", 60);
        moodleTzs.put("Atlantic/Reykjavik", 0);
        moodleTzs.put("Europe/Rome", 60);
        moodleTzs.put("Europe/Riga", 120);
        moodleTzs.put("Europe/Vilnius", 120);
        moodleTzs.put("Europe/Luxembourg", 60);
        moodleTzs.put("Europe/Malta", 60);
        moodleTzs.put("Europe/Chisinau", 120);
        moodleTzs.put("Europe/Monaco", 60);
        moodleTzs.put("Europe/Amsterdam", 60);
        moodleTzs.put("Europe/Oslo", 60);
        moodleTzs.put("Europe/Warsaw", 60);
        moodleTzs.put("Europe/Lisbon", 0);
        moodleTzs.put("Atlantic/Azores", -60);
        moodleTzs.put("Atlantic/Madeira", 0);
        moodleTzs.put("Europe/Bucharest", 120);
        moodleTzs.put("Europe/Kaliningrad", 120);
        moodleTzs.put("Europe/Moscow", 180);
        moodleTzs.put("Europe/Simferopol", 180);
        moodleTzs.put("Europe/Astrakhan", 240);
        moodleTzs.put("Europe/Volgograd", 180);
        moodleTzs.put("Europe/Saratov", 240);
        moodleTzs.put("Europe/Kirov", 180);
        moodleTzs.put("Europe/Samara", 240);
        moodleTzs.put("Europe/Ulyanovsk", 240);
        moodleTzs.put("Asia/Yekaterinburg", 300);
        moodleTzs.put("Asia/Omsk", 360);
        moodleTzs.put("Asia/Barnaul", 420);
        moodleTzs.put("Asia/Novosibirsk", 420);
        moodleTzs.put("Asia/Tomsk", 420);
        moodleTzs.put("Asia/Novokuznetsk", 420);
        moodleTzs.put("Asia/Krasnoyarsk", 420);
        moodleTzs.put("Asia/Irkutsk", 480);
        moodleTzs.put("Asia/Chita", 540);
        moodleTzs.put("Asia/Yakutsk", 540);
        moodleTzs.put("Asia/Vladivostok", 600);
        moodleTzs.put("Asia/Khandyga", 540);
        moodleTzs.put("Asia/Sakhalin", 660);
        moodleTzs.put("Asia/Magadan", 660);
        moodleTzs.put("Asia/Srednekolymsk", 660);
        moodleTzs.put("Asia/Ust-Nera", 600);
        moodleTzs.put("Asia/Kamchatka", 720);
        moodleTzs.put("Asia/Anadyr", 720);
        moodleTzs.put("Europe/Belgrade", 60);
        moodleTzs.put("Europe/Madrid", 60);
        moodleTzs.put("Africa/Ceuta", 60);
        moodleTzs.put("Atlantic/Canary", 0);
        moodleTzs.put("Europe/Stockholm", 60);
        moodleTzs.put("Europe/Zurich", 60);
        moodleTzs.put("Europe/Istanbul", 180);
        moodleTzs.put("Europe/Kiev", 120);
        moodleTzs.put("Europe/Uzhgorod", 120);
        moodleTzs.put("Europe/Zaporozhye", 120);
        moodleTzs.put("EST", -300);
        moodleTzs.put("MST", -420);
        moodleTzs.put("HST", -600);
        moodleTzs.put("EST5EDT", -300);
        moodleTzs.put("CST6CDT", -360);
        moodleTzs.put("MST7MDT", -420);
        moodleTzs.put("PST8PDT", -480);
        moodleTzs.put("America/New_York", -300);
        moodleTzs.put("America/Chicago", -360);
        moodleTzs.put("America/North_Dakota/Center", -360);
        moodleTzs.put("America/North_Dakota/New_Salem", -360);
        moodleTzs.put("America/North_Dakota/Beulah", -360);
        moodleTzs.put("America/Denver", -420);
        moodleTzs.put("America/Los_Angeles", -480);
        moodleTzs.put("America/Juneau", -540);
        moodleTzs.put("America/Sitka", -540);
        moodleTzs.put("America/Metlakatla", -540);
        moodleTzs.put("America/Yakutat", -540);
        moodleTzs.put("America/Anchorage", -540);
        moodleTzs.put("America/Nome", -540);
        moodleTzs.put("America/Adak", -600);
        moodleTzs.put("Pacific/Honolulu", -600);
        moodleTzs.put("America/Phoenix", -420);
        moodleTzs.put("America/Boise", -420);
        moodleTzs.put("America/Indiana/Indianapolis", -300);
        moodleTzs.put("America/Indiana/Marengo", -300);
        moodleTzs.put("America/Indiana/Vincennes", -300);
        moodleTzs.put("America/Indiana/Tell_City", -360);
        moodleTzs.put("America/Indiana/Petersburg", -300);
        moodleTzs.put("America/Indiana/Knox", -360);
        moodleTzs.put("America/Indiana/Winamac", -300);
        moodleTzs.put("America/Indiana/Vevay", -300);
        moodleTzs.put("America/Kentucky/Louisville", -300);
        moodleTzs.put("America/Kentucky/Monticello", -300);
        moodleTzs.put("America/Detroit", -300);
        moodleTzs.put("America/Menominee", -360);
        moodleTzs.put("America/St_Johns", -210);
        moodleTzs.put("America/Goose_Bay", -240);
        moodleTzs.put("America/Halifax", -240);
        moodleTzs.put("America/Glace_Bay", -240);
        moodleTzs.put("America/Moncton", -240);
        moodleTzs.put("America/Blanc-Sablon", -240);
        moodleTzs.put("America/Toronto", -300);
        moodleTzs.put("America/Thunder_Bay", -300);
        moodleTzs.put("America/Nipigon", -300);
        moodleTzs.put("America/Rainy_River", -360);
        moodleTzs.put("America/Atikokan", -300);
        moodleTzs.put("America/Winnipeg", -360);
        moodleTzs.put("America/Regina", -360);
        moodleTzs.put("America/Swift_Current", -360);
        moodleTzs.put("America/Edmonton", -420);
        moodleTzs.put("America/Vancouver", -480);
        moodleTzs.put("America/Dawson_Creek", -420);
        moodleTzs.put("America/Fort_Nelson", -420);
        moodleTzs.put("America/Creston", -420);
        moodleTzs.put("America/Pangnirtung", -300);
        moodleTzs.put("America/Iqaluit", -300);
        moodleTzs.put("America/Resolute", -360);
        moodleTzs.put("America/Rankin_Inlet", -360);
        moodleTzs.put("America/Cambridge_Bay", -420);
        moodleTzs.put("America/Yellowknife", -420);
        moodleTzs.put("America/Inuvik", -420);
        moodleTzs.put("America/Whitehorse", -480);
        moodleTzs.put("America/Dawson", -480);
        moodleTzs.put("America/Cancun", -300);
        moodleTzs.put("America/Merida", -360);
        moodleTzs.put("America/Matamoros", -360);
        moodleTzs.put("America/Monterrey", -360);
        moodleTzs.put("America/Mexico_City", -360);
        moodleTzs.put("America/Ojinaga", -420);
        moodleTzs.put("America/Chihuahua", -420);
        moodleTzs.put("America/Hermosillo", -420);
        moodleTzs.put("America/Mazatlan", -420);
        moodleTzs.put("America/Bahia_Banderas", -360);
        moodleTzs.put("America/Tijuana", -480);
        moodleTzs.put("America/Nassau", -300);
        moodleTzs.put("America/Barbados", -240);
        moodleTzs.put("America/Belize", -360);
        moodleTzs.put("Atlantic/Bermuda", -240);
        moodleTzs.put("America/Costa_Rica", -360);
        moodleTzs.put("America/Havana", -300);
        moodleTzs.put("America/Santo_Domingo", -240);
        moodleTzs.put("America/El_Salvador", -360);
        moodleTzs.put("America/Guatemala", -360);
        moodleTzs.put("America/Port-au-Prince", -300);
        moodleTzs.put("America/Tegucigalpa", -360);
        moodleTzs.put("America/Jamaica", -300);
        moodleTzs.put("America/Martinique", -240);
        moodleTzs.put("America/Managua", -360);
        moodleTzs.put("America/Panama", -300);
        moodleTzs.put("America/Puerto_Rico", -240);
        moodleTzs.put("America/Miquelon", -180);
        moodleTzs.put("America/Grand_Turk", -240);
        moodleTzs.put("America/Argentina/Buenos_Aires", -180);
        moodleTzs.put("America/Argentina/Cordoba", -180);
        moodleTzs.put("America/Argentina/Salta", -180);
        moodleTzs.put("America/Argentina/Tucuman", -180);
        moodleTzs.put("America/Argentina/La_Rioja", -180);
        moodleTzs.put("America/Argentina/San_Juan", -180);
        moodleTzs.put("America/Argentina/Jujuy", -180);
        moodleTzs.put("America/Argentina/Catamarca", -180);
        moodleTzs.put("America/Argentina/Mendoza", -180);
        moodleTzs.put("America/Argentina/San_Luis", -180);
        moodleTzs.put("America/Argentina/Rio_Gallegos", -180);
        moodleTzs.put("America/Argentina/Ushuaia", -180);
        moodleTzs.put("America/La_Paz", -240);
        moodleTzs.put("America/Noronha", -120);
        moodleTzs.put("America/Belem", -180);
        moodleTzs.put("America/Santarem", -180);
        moodleTzs.put("America/Fortaleza", -180);
        moodleTzs.put("America/Recife", -180);
        moodleTzs.put("America/Araguaina", -180);
        moodleTzs.put("America/Maceio", -180);
        moodleTzs.put("America/Bahia", -180);
        moodleTzs.put("America/Sao_Paulo", -180);
        moodleTzs.put("America/Campo_Grande", -240);
        moodleTzs.put("America/Cuiaba", -240);
        moodleTzs.put("America/Porto_Velho", -240);
        moodleTzs.put("America/Boa_Vista", -240);
        moodleTzs.put("America/Manaus", -240);
        moodleTzs.put("America/Eirunepe", -300);
        moodleTzs.put("America/Rio_Branco", -300);
        moodleTzs.put("America/Santiago", -240);
        moodleTzs.put("America/Punta_Arenas", -180);
        moodleTzs.put("Pacific/Easter", -360);
        moodleTzs.put("Antarctica/Palmer", -180);
        moodleTzs.put("America/Bogota", -300);
        moodleTzs.put("America/Curacao", -240);
        moodleTzs.put("America/Guayaquil", -300);
        moodleTzs.put("Pacific/Galapagos", -360);
        moodleTzs.put("Atlantic/Stanley", -180);
        moodleTzs.put("America/Cayenne", -180);
        moodleTzs.put("America/Guyana", -240);
        moodleTzs.put("America/Asuncion", -240);
        moodleTzs.put("America/Lima", -300);
        moodleTzs.put("Atlantic/South_Georgia", -120);
        moodleTzs.put("America/Paramaribo", -180);
        moodleTzs.put("America/Port_of_Spain", -240);
        moodleTzs.put("America/Montevideo", -180);
        moodleTzs.put("America/Caracas", -240);
        moodleTzs.put("Etc/GMT", 0);
        moodleTzs.put("Etc/UTC", 0);
        moodleTzs.put("Etc/UCT", 0);
        moodleTzs.put("Etc/GMT-14", -840);
        moodleTzs.put("Etc/GMT-13", -780);
        moodleTzs.put("Etc/GMT-12", -720);
        moodleTzs.put("Etc/GMT-11", -660);
        moodleTzs.put("Etc/GMT-10", -600);
        moodleTzs.put("Etc/GMT-9", -540);
        moodleTzs.put("Etc/GMT-8", -480);
        moodleTzs.put("Etc/GMT-7", -420);
        moodleTzs.put("Etc/GMT-6", -360);
        moodleTzs.put("Etc/GMT-5", -300);
        moodleTzs.put("Etc/GMT-4", -240);
        moodleTzs.put("Etc/GMT-3", -180);
        moodleTzs.put("Etc/GMT-2", -120);
        moodleTzs.put("Etc/GMT-1", -60);
        moodleTzs.put("Etc/GMT+1", 60);
        moodleTzs.put("Etc/GMT+2", 120);
        moodleTzs.put("Etc/GMT+3", 180);
        moodleTzs.put("Etc/GMT+4", 240);
        moodleTzs.put("Etc/GMT+5", 300);
        moodleTzs.put("Etc/GMT+6", 360);
        moodleTzs.put("Etc/GMT+7", 420);
        moodleTzs.put("Etc/GMT+8", 480);
        moodleTzs.put("Etc/GMT+9", 540);
        moodleTzs.put("Etc/GMT+10", 600);
        moodleTzs.put("Etc/GMT+11", 660);
        moodleTzs.put("Etc/GMT+12", 720);
    }


    /**
     * Allow user to be registered while logon
     * If user not found, it can be registered automatically
     * Procedure will get only {@code email} and {@code password}
     * and can fill other fields to create new user (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * Integrator expects {@code email} is equals to "MoodleSession" and
     * {@code password} have Moodle session cookie
     *
     * Событие возникает на странице "login". Определяет, разрешено ли
     * пользователю зарегистрироваться при авторизации.
     * {@code true} - не разрешено
     * {@code false} - разрешено
     * Инверсия "deny" делана для того, чтобы "не вникая" все методы
     * интегратора по-умолчанию возвращали "true"
     *
     * @param user who is doing action
     * @return {@code true} to deny
     */
    public boolean login_denyAutoRegister(UserRow user) {
        //return true;
        if (user.email != null && user.email.equals("MoodleSession") &&
                user.password != null && user.password.length() > 10) {
            try {
                UserRow moodleUser = getUserBySessionCookie(user.password);
                moodleUser.copyTo(user);
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public UserRow getUserBySessionCookie(String moodleSession) throws IOException {
        URL url = new URL("http://lms.progwards.ru/moodle/user/edit.php");

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Cookie", "MoodleSession=" + moodleSession);
        con.connect();

        UserRow user = new UserRow();
        String id = "";
        String firstName = "";
        String lastName = "";
        user.email = "";

        if (con.getResponseCode() == 200) {
            String inKey = "input type=\"text\"";
            int fromInLine = 0; // how far from input
            String idKey = "name=\"id\" value=\"";
            String unKey = "value=\"";
            String opKey = "option value=\"";
            String selKey = "\" selected";
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strCurrentLine;
            String strPrevLine = null;
            while ((strCurrentLine = br.readLine()) != null) {
                fromInLine++;
                if (strCurrentLine.contains(idKey)){
                    id = strCurrentLine.substring(strCurrentLine.indexOf(idKey) + idKey.length());
                    id = id.substring(0, id.indexOf("\""));
                }
                else if (strCurrentLine.contains(inKey)) {
                    fromInLine = 0;
                }
                else if (fromInLine < 10 && strCurrentLine.contains(unKey)) {
                    // get user email, name
                    String value = strCurrentLine.substring(strCurrentLine.indexOf(unKey) + unKey.length());
                    value = value.substring(0, value.indexOf("\""));
                    if(strPrevLine.contains("id=\"id_email\"")) {
                        user.email = value;
                    } else if(strPrevLine.contains("id=\"id_firstname\"")) {
                        firstName = value;
                    } else if(strPrevLine.contains("id=\"id_lastname\"")) {
                        lastName = value;
                    }
                }
                else if (strCurrentLine.contains(selKey) && strCurrentLine.contains(opKey)) {
                    // get user Timezone
                    String value = strCurrentLine.substring(strCurrentLine.indexOf(opKey) + opKey.length());
                    value = value.substring(0, value.indexOf("\""));
                    Integer offset = moodleTzs.get(value);
                    if(offset == null) {
                        offset = (03)*60; // default is Moscow TZ (UTC+03)
                    }
                    try {
                        TzRow tz = storageTz.select(offset.toString());
                        user.tz_id = tz.tz_id;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                strPrevLine = strCurrentLine;
            }
        }

        if(id.isEmpty())
            throw new RuntimeException("Cannot find USER_ID");

        if(user.email.isEmpty())
            throw new RuntimeException("Cannot find user ID="+id+" email!");

        user.name = firstName + ' ' + lastName;
        user.password = moodleSession;

        return user;
    }

    /**
     * Allow registered user to be logged in
     * It can be denied even if user is registered
     *
     * {@code user.password} is encrypted
     *
     * Событие возникает на странице "login". Определяет, разрешено ли
     * зарегистрированному пользователю выполнить авторизацию.
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean login_allowRegistered(UserRow user){
        return true;
    }


    /**
     * Allow new user to be registered
     * Procedure can change any field to store in database (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * Событие возникает на странице "register". Определяет, разрешено ли
     * данному пользователю пройти регистрацию (можно разрешить регистрацию
     * только e-mail из списка).
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean register_allowRegistration(UserRow user){
        return true;
    }


    /**
     * Allow user to make session
     * User can log-in several months ago. This method can prevent it.
     *
     * {@code user.password} is encrypted
     *
     * Определяет, может ли пользователь по сохраненным в кукис логину/паролю
     * создать новую сессию и начать работать с системой
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean session_allowUser(UserRow user){
        return true;
    }


    /**
     * Allow user to make a record
     *
     * {@code user.password} is encrypted
     *
     * Событие возникает на странице "record", где пользователь делает запись
     * в расписание. Метод определяет, может ли данный пользователь {@code user}
     * сделать запись {@code schedule}.
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user who is doing action
     * @param schedule chosen time and other data inside
     * @return {@code true} to accept
     */
    public boolean record_allowRecord(UserRow user, ScheduleRow schedule){
        return true;
    }


    /**
     * Allow user to modify own profile
     * If {@code oldUser.password} not equals to {@code newUser.password}, then
     * {@code newUser.password} is not encrypted and contains a new password
     *
     * If {@code newUser.password==""}, then password is not changed
     *
     * Событие возникает на странице "profile". Определяет, может ли пользователь
     * внести данную правку в свой профиль.
     * {@code true} - может
     * {@code false} - не может
     *
     * Если {@code newUser.password} пустой, значит пользователь пароль не меняет.
     * Если {@code oldUser.password} не равен {@code newUser.password}, значит
     * {@code newUser.password} не зашифрован и хранит новый пароль пользователя.
     *
     * @param oldUser old user data
     * @param newUser new user data
     * @return {@code true} to accept
     */
    public boolean profile_allowModification(UserRow oldUser, UserRow newUser){
        return true;
    }

}
