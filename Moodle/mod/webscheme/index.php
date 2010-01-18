<?php 
/**
 * This page lists all the instances of webscheme in a particular course
 *
 * @author  
 * @package webscheme
 **/


require_once(dirname(dirname(dirname(__FILE__))).'/config.php');
require_once(dirname(__FILE__).'/lib.php');

$id = required_param('id', PARAM_INT);   // course

if (! $course = get_record('course', 'id', $id)) {
    error('Course ID is incorrect');
}

require_course_login($course);

add_to_log($course->id, 'webscheme', 'view all', "index.php?id=$course->id", '');


/// Get all required stringswebscheme

$strwebschemes = get_string('modulenameplural', 'webscheme');
$strwebscheme  = get_string("modulename", 'webscheme');


/// Print the header

$navlinks = array();
$navlinks[] = array('name' => $strwebschemes, 'link' => '', 'type' => 'activity');
$navigation = build_navigation($navlinks);

print_header_simple("$strwebschemes", "", $navigation, "", "", true, "", navmenu($course));

     

/// Get all the appropriate data

if (! $webschemes = get_all_instances_in_course('webscheme', $course)) {
    notice(get_string('noinstances','webscheme'), "../../course/view.php?id=$course->id");
    die;
}

/// Print the list of instances (your module will probably extend this)

$timenow = time();
$strname  = get_string("name");
$strweek  = get_string("week");
$strtopic  = get_string("topic");

if ($course->format == 'weeks') {
    $table->head  = array ($strweek, $strname);
    $table->align = array ('center', 'left');
} else if ($course->format == 'topics') {
    $table->head  = array ($strtopic, $strname);
    $table->align = array ('center', 'left', 'left', 'left');
} else {
    $table->head  = array ($strname);
    $table->align = array ('left', 'left', 'left');
}


foreach ($webschemes as $webscheme) {
    if (!$webscheme->visible) {
        //Show dimmed if the mod is hidden
        $link = '<a class="dimmed" href="view.php?id='.$webscheme->coursemodule.'">'.format_string($webscheme->name).'</a>';
    } else {
        //Show normal if the mod is visible
       $link = '<a href="view.php?id='.$webscheme->coursemodule.'">'.format_string($webscheme->name).'</a>';
    }

    if ($course->format == 'weeks' or $course->format == 'topics') {
        $table->data[] = array ($webscheme->section, $link);
    } else {
        $table->data[] = array ($link);
    }
}

print_heading($strwebschemes);
print_table($table);

/// Finish the page
    
print_footer($course);
		
?>
