 <?php  // $Id: view.php,v 1.6 2007/09/03 12:23:36 jamiesensei Exp $
/**
 * This page prints a particular instance of webscheme
 *
 * @author
 * @version $Id: view.php,v 1.6 2007/09/03 12:23:36 jamiesensei Exp $
 * @package webscheme
 **/

/// (Replace newmodule with the name of your module)

    require_once("../../config.php");
    require_once("lib.php");

    $id = optional_param('id', 22, PARAM_INT); // Course Module ID, or
    $a  = optional_param('a', 22, PARAM_INT);  // webscheme ID

    if ($id) {
        if (! $cm = get_record("course_modules", "id", $id)) {
            error("Course Module ID was incorrect");
        }

        if (! $course = get_record("course", "id", $cm->course)) {
            error("Course is misconfigured");
        }

        if (! $webscheme = get_record("webscheme", "id", $cm->instance)) {
            error("Course module is incorrect");
        }

    } else {
        if (! $webscheme = get_record("webscheme", "id", $a)) {
            error("Course module is incorrect");
        }
        if (! $course = get_record("course", "id", $webscheme->course)) {
            error("Course is misconfigured");
        }
        if (! $cm = get_coursemodule_from_instance("webscheme", $webscheme->id, $course->id)) {
            error("Course Module ID was incorrect");
        }
    }
    
    require_login($course->id);
    add_to_log($course->id, "webscheme", "view", "view.php?id=$cm->id", "$webscheme->id");

/// Print the page header
    $strwebschemes = get_string("modulenameplural", "webscheme");
    $strwebscheme  = get_string("modulename", "webscheme");

    // It seems like build_navigation currently does not work, so I built my own $navigation variable below
    // to use in print_header_simple
    
    //$navlinks = array();
    //$navlinks[] = array('name' => $strwebschemes, 'link' => "index.php?id=$course->id", 'type' => 'activity');
    //$navlinks[] = array('name' => format_string($webscheme->name), 'link' => '', 'type' => 'activityinstance');
    //$navigation = build_navigation($navlinks);
    //print($navigation);
	//$navigation = $navlinks;
  
    $navigation = "<a href=\"index.php?id=$course->id\">$strwebschemes</a> -> ".format_string($webscheme->name);
    
    print_header_simple(format_string($webscheme->name), "", $navigation, "", "", true,
                  update_module_button($cm->id, $course->id, $strwebscheme), navmenu($course, $cm));

/// Print the main part of the page

    echo "<br/>";
    $wsmlInput = $webscheme->wsml;
    
    //Predefined values for the scheme handler -----------------------------------
    echo "\n<!-- Start of WSML  -->\n";
    echo "<script src=\"defs/ws-lib.js\" type=\"text/javascript\"></script>\n";
	echo "<div>\n<!-- Sorry, this is not an XHTML element but Safari ne comprends pas";
	echo "l'objet -->\n<applet width=\"120\" height=\"36\"";
	echo "standby=\"Loading WebScheme daemon\" id=\"SchemeHandler\"";
	echo "name=\"SchemeHandler\" archive=\"lib/webscheme.jar\"";
	echo "classid=\"java:webscheme.SchemeHandler.class\" mayscript=\"true\"";
	echo "scriptable=\"true\" code=\"webscheme.SchemeHandler.class\">\n";
	echo "<param value=\"true\" name=\"mayscript\" />\n";
	echo "<param value=\"true\" name=\"scriptable\" />\n";
	echo "<param value=\"SchemeHandler\" name=\"name\" />\n";
	echo "<param value=\"true\" name=\"progressbar\" />\n";
	echo "<param value=\"#FFCC33\" name=\"progresscolor\" />\n";
	echo "<param value=\"#AAAACC\" name=\"boxfgcolor\" />\n";
	echo "<param value=\"#FFCC33\" name=\"boxbgcolor\" />\n";
	echo "<param value=\"Loading WebScheme...\" name=\"boxmessage\" />\n";
	
    // ---------------------------------------------------------------------------
	// User defined params for scheme handler from WSML 
    $wsml = new SimpleXMLElement($wsmlInput);
	
    //Load Urls
    $loadUrlCount = 0;
    foreach ($wsml->{'ws-loadurl'} as $lu){
    	$lu = trim($lu);
        echo "<param value=\"$lu\" name=\"loadurl-$loadUrlCount\" />\n";
        $loadUrlCount++;
    }
    
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
	echo "</applet>\n";
	echo "</div>\n";
	echo "<form onsubmit=\"return false;\" id=\"wsfields\">\n";
    // -----------------------------------------------------------------------------
    
	// User defined section using WSML/HTML (from the <ws-html> data in WSML)
    echo $wsml->{'ws-html'}->asXML();
    echo "\n";
    // Predefined value -----------------------------------------------------------
   	echo "</form>\n";
   	// ----------------------------------------------------------------------------
   	
   	echo "<!-- End of WSML -->\n";
/// Finish the page
    print_footer($course);
    
?>
