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
require_js(dirname(__FILE__).'defs/ws-lib.js');
$css_tag = '<link href="defs/ws-defaults.css" rel="stylesheet" type="text/css" />';


print_header_simple(
format_string($webscheme->name),
    '', 
$navigation,
    '', 
$css_tag,
true,
update_module_button($cm->id, $course->id, get_string('modulename', 'webscheme')),
navmenu($course, $cm)
);



echo "<pre>";print_r($webscheme);echo"</pre>";break;


////// page part defintions

$scheme_handler_start = <<<EEOOTT
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

// loadurls
$loadurl_parameters = "";
$loadUrlCount = 0;
$loadurls = json_decode($defaults['ws_loadurls'], true);
if (json_last_error() != JSON_ERROR_NONE) {
	print_error(get_string('badjsondecode','webscheme')	);
}
foreach ($loadurls as $loadurl) {
	$loadurl = trim($loadurl);   // needed?
	$loadurl_parameters .= "<param value=\"{$loadurl}\" name=\"loadurl-{$loadUrlCount} /> ";
	$loadUrlCount++;
}


//....


//Initial Expressions
$initExpr = trim(htmlentities($wsml->{'ws-initExpr'}));
echo "<param value=\"$initExpr\" name=\"init-expr\" />\n";
 
//Events
$eCount = 0;
foreach ($wsml->{'ws-event'} as $event){
	$name = trim(htmlentities($event->{'ws-event-name'}));
	$assertions = trim(htmlentities($event->{'ws-event-assertions'}));
	$template = trim(htmlentities($event->{'ws-event-template'}));
	echo "<param value=\"$name\" name=\"event-name-$eCount\" />\n";
	echo "<param value=\"$assertions\" name=\"event-assertions-$eCount\" />\n";
	echo "<param value=\"$template\" name=\"event-template-$eCount\" />\n";
	$eCount++;
}

// -----------------------------------------------------------------------------
// Predefined values -----------------------------------------------------------


// -----------------------------------------------------------------------------

// User defined section using WSML/HTML (from the <ws-html> data in WSML)
// changed to take into account html-entities pass
$html = $wsml->{'ws-html'}->asXML();
echo html_entity_decode($html);
//TODO -- fix this
echo $wsml->{'ws-html'}->asXML();
echo "\n";
// Predefined value -----------------------------------------------------------
echo "</form>\n";
// ----------------------------------------------------------------------------


/// Print the main part of the page
echo "<br/>";

echo $scheme_handler_start . "\n";


echo "</applet><div>\n";
echo "<form onsubmit=\"return false;\" id=\"wsfields\">\n";
echo "<!-- End of WSML -->\n";

/// Finish the page
print_footer($course);



?>
