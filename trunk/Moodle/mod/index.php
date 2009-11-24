<?php // $Id: index.php,v 1.7 2007/09/03 12:23:36 jamiesensei Exp $
/**
 * This page lists all the instances of webscheme in a particular course
 *
 * @author
 * @version $Id: index.php,v 1.7 2007/09/03 12:23:36 jamiesensei Exp $
 * @package webscheme
 **/

/// Replace newmodule with the name of your module

    require_once("../../config.php");
    require_once("lib.php");

    $id = required_param('id', PARAM_INT);   // course

       
    
    if (! $course = get_record("course", "id", $id)) {
        error("Course ID is incorrect");
    }

    require_login($course->id);

    add_to_log($course->id, "webscheme", "view all", "index.php?id=$course->id", "");


/// Get all required stringswebscheme

    $strwebschemes = get_string("modulenameplural", "webscheme");
    $strwebscheme  = get_string("modulename", "webscheme");


/// Print the header

    $navlinks = array();
    $navlinks[] = array('name' => $strwebschemes, 'link' => '', 'type' => 'activity');
    //$navigation = build_navigation($navlinks);

    $navigation = "webschemes";
    print_header_simple("$strwebschemes", "", $navigation, "", "", true, "", navmenu($course));

     

/// Get all the appropriate data

    if (! $webschemes = get_all_instances_in_course("webscheme", $course)) {
        notice("There are no webschemes", "../../course/view.php?id=$course->id");
        die;
    }

/// Print the list of instances (your module will probably extend this)

    $timenow = time();
    $strname  = get_string("name");
    $strweek  = get_string("week");
    $strtopic  = get_string("topic");

    if ($course->format == "weeks") {
        $table->head  = array ($strweek, $strname);
        $table->align = array ("center", "left");
    } else if ($course->format == "topics") {
        $table->head  = array ($strtopic, $strname);
        $table->align = array ("center", "left", "left", "left");
    } else {
        $table->head  = array ($strname);
        $table->align = array ("left", "left", "left");
    }

    foreach ($webschemes as $webscheme) {
        if (!$webscheme->visible) {
            //Show dimmed if the mod is hidden
            $link = "<a class=\"dimmed\" href=\"view.php?id=$webscheme->coursemodule\">$webscheme->name</a>";
        } else {
            //Show normal if the mod is visible
            $link = "<a href=\"view.php?id=$webscheme->coursemodule\">$webscheme->name</a>";
        }

        if ($course->format == "weeks" or $course->format == "topics") {
            $table->data[] = array ($webscheme->section, $link);
        } else {
            $table->data[] = array ($link);
        }
    }

    echo "<br />";

    print_table($table);

/// Finish the page
    
    print_footer($course);
		
?>
