<?php  // $Id: view.php,v 1.6.2.3 2009/04/17 22:06:25 skodak Exp $

/**
 * This page prints a particular instance of webscheme
 *
 * @author William, Nate
 * @version
 * @package mod/webscheme
 */

// TODO -- get the webscheme css file in here.  Moodle?

require_once(dirname(dirname(dirname(__FILE__))).'/config.php');
require_once(dirname(__FILE__).'/locallib.php');

$id = optional_param('id', 0, PARAM_INT); // course_module ID, or
$a  = optional_param('a', 0, PARAM_INT);  // webscheme instance ID

if ($id) {
	if (! $cm = get_coursemodule_from_id('webscheme', $id)) {
		error('Course Module ID was incorrect');
	}

	if (! $course = get_record('course', 'id', $cm->course)) {
		error('Course is misconfigured');
	}

	if (! $webscheme = get_record('webscheme', 'id', $cm->instance)) {
		error('Course module is incorrect');
	}

} else if ($a) {
	if (! $webscheme = get_record('webscheme', 'id', $a)) {
		error('Course module is incorrect');
	}
	if (! $course = get_record('course', 'id', $webscheme->course)) {
		error('Course is misconfigured');
	}
	if (! $cm = get_coursemodule_from_instance('webscheme', $webscheme->id, $course->id)) {
		error('Course Module ID was incorrect');
	}

} else {
	error('You must specify a course_module ID or an instance ID');
}



require_login($course, true, $cm);

add_to_log($course->id, "webscheme", "view", "view.php?id=$cm->id", "$webscheme->id");



////// Header stuff
$navlinks = array();
$navlinks[] = array('name' => get_string('modulenameplural', 'webscheme'),
 				    'link' => "index.php?id=$course->id", 
 				    'type' => 'activity');
$navlinks[] = array('name' => format_string($webscheme->name),
				    'link' => '', 
				    'type' => 'activityinstance');
$navigation = build_navigation($navlinks);

// for webscheme
require_js($CFG->wwwroot . '/mod/webscheme/defs/ws-lib.js');
$css_tag = '<link href="defs/ws-defaults.css" rel="stylesheet" type="text/css" />';


print_header_simple(format_string($webscheme->name),
					'', 
$navigation,
					'', 
$css_tag,
true,
update_module_button($cm->id, $course->id, get_string('modulename', 'webscheme')),
navmenu($course, $cm)
);



//echo "<pre>";print_r($webscheme);echo"</pre>";break;


////// webschemey defintions

// loadurls
$ws_loadurl_params = "";
$ws_loadUrlCount = 0;
foreach (webscheme_field2json('ws_loadurls') as $loadurl) {
	$loadurl = htmlentities($loadurl, ENT_QUOTES);
	$ws_loadurl_params .= "<param value=\"{$loadurl}\" name=\"loadurl-{$ws_loadUrlCount}\" /> ";
	$ws_loadUrlCount++;
}

//Initial Expression
$ws_initexpr = htmlentities($webscheme->ws_initexpr, ENT_QUOTES);
$ws_initexpr_param = "<param value=\"" . $ws_initexpr . "\" name=\"init-expr\" /> ";

//Events
$ws_event_params = "";
$ws_eventCount = 0;
foreach (webscheme_field2json('ws_events', false) as $event){
	$name = htmlentities($event['name'], ENT_QUOTES);
	$assertions = htmlentities($event['assertion'], ENT_QUOTES);
	$template = htmlentities($event['template'], ENT_QUOTES);
	$ws_event_params .= "<param value=\"$name\" name=\"event-name-$ws_eventCount\" /> ";
	$ws_event_params .= "<param value=\"$assertions\" name=\"event-assertions-$ws_eventCount\" /> ";
	$ws_event_params .= "<param value=\"$template\" name=\"event-template-$ws_eventCount\" /> ";
	$ws_eventCount++;
}

// html
$ws_html = $webscheme->ws_html;


////  Print the webschemey parts
echo "<br/>";

echo <<<EEOOTT
	<div>
	<!-- Sorry, this is not an XHTML element but Safari ne comprends pas l'object -->
	<applet width="120" height="36"
	  standby="Loading WebScheme daemon" id="SchemeHandler"
	  name="SchemeHandler" archive="lib/webscheme.jar"
	  classid="java:webscheme.SchemeHandler.class" mayscript="true"
      scriptable="true" code="webscheme.SchemeHandler.class"
      >
	<param value="true" name="mayscript" />
	<param value="true" name="scriptable" />
	<param value="SchemeHandler" name="name" />
	<param value="true" name="progressbar" />
	<param value="#FFCC33" name="progresscolor" />
	<param value="#AAAACC" name="boxfgcolor" />
	<param value="#FFCC33" name="boxbgcolor" />
	<param value="Loading WebScheme..." name="boxmessage" />
	
EEOOTT;

echo $ws_loadurl_params . "\n";
echo $ws_initexpr_param  . "\n";
echo $ws_event_params . "\n";

echo "</applet><div>\n";
echo "<form onsubmit=\"return false;\" id=\"wsfields\">\n";

echo $ws_html . "\n";

echo "</form>\n";
echo "<!-- End of WSML -->\n";

/// Finish the page
print_footer($course);



//// utility

function webscheme_field2json($field, $htmlentity_encode = true) {
	global $webscheme;

	$json =  json_decode($webscheme->$field, true);
	if (function_exists("json_last_error")) {   // needs php 5.3+
		if (json_last_error() != JSON_ERROR_NONE) {
			print_error(get_string('badjsondecode','webscheme') . ": field={$field}");
		}
	}
	if ($htmlentity_encode) {
		if (gettype($json) == "string") {
			$json = html_entity_decode($json);
		} else if (gettype($json == "array")) {
			$json = array_map("html_entity_decode", $json);
		}
	}

	return $json;
}



?>
