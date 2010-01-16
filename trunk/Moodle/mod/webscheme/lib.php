<?php  // $Id: lib.php,v 1.7.2.5 2009/04/22 21:30:57 skodak Exp $

/**
 * Library of functions and constants for module webscheme
 * This file contains all the core Moodle functions,
 *     neeeded to allow the module to work integrated in Moodle.
 * All the webscheme specific functions are in locallib.php
 */



/**
 * Given an object containing all the necessary data,
 * (defined by the form in mod_form.php) this function
 * will create a new instance and return the id number
 * of the new instance.
 *
 * @param object $webscheme An object from the form in mod_form.php
 * @return int The id of the newly inserted webscheme record
 */
function webscheme_add_instance($webscheme) {
	//echo"<pre>";print_r($webscheme);echo"</pre>";break;
	$webscheme->timecreated = time();
	$webscheme->timemodified = time();
	// ws_html is good to go
	// ws_initexpr is  good		
	webscheme_fixloadurls($webscheme);
	webscheme_fixevents($webscheme);

	// insert it.
	return insert_record('webscheme', $webscheme);

}

// tweaks the webscheme object
function webscheme_fixloadurls(&$webscheme) {
	$webscheme->ws_loadurls = webscheme_remslashes($webscheme->ws_loadurls);
	$loadurls = preg_split("/\s+/", $webscheme->ws_loadurls, 0, PREG_SPLIT_NO_EMPTY);	
	$webscheme->ws_loadurls = webscheme_db_json_encode($loadurls);
}

function webscheme_fixevents(&$webscheme) {
	// events need to be colated from eventname, evenasserts, and event templates.
	//  need to check for empties as well!
	$enames = $webscheme->eventname;
	$easserts = $webscheme->eventasserts;
	$etemplates = $webscheme->eventtemplate;
	$ws_events = array();
	for($i=0; $i < count($enames); $i++) {
		if (!empty($enames[$i])) {
			$ws_events[] = array("name"=> webscheme_remslashes($enames[$i]),
			                     "assertion"=> webscheme_remslashes($easserts[$i]),
								 "template"=> webscheme_remslashes($etemplates[$i]) );
		}
	}
	$webscheme->ws_events = webscheme_db_json_encode($ws_events);
}


// the arguments to this function have had addslashes() already done.
// Thank you moodle, you bastardos.
// need to reverse that
//    moodle escapes : ' singlequote  " doublequote \ backslash 
function webscheme_remslashes($str) {
	// shouldn't there be a way in mod_form.php to prevent this being done already?
	$search = array("\\'", "\\\"", "\\\\");
	$replace = array("'", "\"", "\\");
	return (str_replace($search, $replace, $str));
}

// Now, need to do json encode and then add slashes. 
// json escapes (with a backslash)
//   \/ (forward slash), 
//   \b (backspace)  -- shouldn't ever get this, right?
//   \t (tab)
//   \f (formfeed... huh)
//   \" (double quote)  -- it doesn't escape single quotes!
//   \\ (a backslash)
function webscheme_db_json_encode($str) {
	$str = json_encode($str);
	return (addslashes($str));   // right thing to do?
}

/**
 * Given an object containing all the necessary data,
 * (defined by the form in mod_form.php) this function
 * will update an existing instance with new data.
 *
 * @param object $webscheme An object from the form in mod_form.php
 * @return boolean Success/Fail
 */
function webscheme_update_instance($webscheme) {
	$webscheme->timemodified = time();
	$webscheme->id = $webscheme->instance;
	// ws_html is good to go
	// ws_initexpr is  good
	webscheme_fixloadurls($webscheme);
	webscheme_fixevents($webscheme);

	return update_record('webscheme', $webscheme);
}


/**
 * Given an ID of an instance of this module,
 * this function will permanently delete the instance
 * and any data that depends on it.
 *
 * @param int $id Id of the module instance
 * @return boolean Success/Failure
 */
function webscheme_delete_instance($id) {

	if (! $webscheme = get_record('webscheme', 'id', $id)) {
		return false;
	}

	$result = true;

	// nothing extra to delete

	if (! delete_records('webscheme', 'id', $webscheme->id)) {
		$result = false;
	}

	return $result;
}


/**
 * Return a small object with summary information about what a
 * user has done with a given particular instance of this module
 * Used for user activity reports.
 * $return->time = the time they did it
 * $return->info = a short text description
 *
 * @return null
 * @todo Finish documenting this function
 */
function webscheme_user_outline($course, $user, $mod, $webscheme) {
	echo "You want it?  You write it! -- in /mod/webscheme/lib.php";
	return $return;
}


/**
 * Print a detailed representation of what a user has done with
 * a given particular instance of this module, for user activity reports.
 *
 * @return boolean
 * @todo Finish documenting this function
 */
function webscheme_user_complete($course, $user, $mod, $webscheme) {
	echo "You really want it?  You write it! -- in /mod/webscheme/lib.php";
	return true;
}


/**
 * Given a course and a time, this module should find recent activity
 * that has occurred in webscheme activities and print it out.
 * Return true if there was output, or false is there was none.
 *
 * @return boolean
 * @todo Finish documenting this function
 */
function webscheme_print_recent_activity($course, $isteacher, $timestart) {
	return false;  //  True if anything was printed, otherwise false
}


/**
 * Function to be run periodically according to the moodle cron
 * This function searches for things that need to be done, such
 * as sending out mail, toggling flags etc ...
 *
 * @return boolean
 * @todo Finish documenting this function
 **/
function webscheme_cron () {
	return true;
}


/**
 * Must return an array of user records (all data) who are participants
 * for a given instance of webscheme. Must include every user involved
 * in the instance, independient of his role (student, teacher, admin...)
 * See other modules as example.
 *
 * @param int $webschemeid ID of an instance of this module
 * @return mixed boolean/array of students
 */
function webscheme_get_participants($webschemeid) {
	// hmm, we don't return anything here, right?
	return false;
}


/**
 * This function returns if a scale is being used by one webscheme
 * if it has support for grading and scales. Commented code should be
 * modified if necessary. See forum, glossary or journal modules
 * as reference.
 *
 * @param int $webschemeid ID of an instance of this module
 * @return mixed
 * @todo Finish documenting this function
 */
function webscheme_scale_used($webschemeid, $scaleid) {
	$return = false;

	//$rec = get_record("webscheme","id","$webschemeid","scale","-$scaleid");
	//
	//if (!empty($rec) && !empty($scaleid)) {
	//    $return = true;
	//}

	return $return;
}


/**
 * Checks if scale is being used by any instance of webscheme.
 * This function was added in 1.9
 *
 * This is used to find out if scale used anywhere
 * @param $scaleid int
 * @return boolean True if the scale is used by any webscheme
 */
function webscheme_scale_used_anywhere($scaleid) {
	if ($scaleid and record_exists('webscheme', 'grade', -$scaleid)) {
		return true;
	} else {
		return false;
	}
}


/**
 * Execute post-install custom actions for the module
 * This function was added in 1.9
 *
 * @return boolean true if success, false on error
 */
function webscheme_install() {
	return true;
}


/**
 * Execute post-uninstall custom actions for the module
 * This function was added in 1.9
 *
 * @return boolean true if success, false on error
 */
function webscheme_uninstall() {
	return true;
}



// additional functions in locallib.php

?>
