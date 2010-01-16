<?php //$Id: mod_form.php,v 1.2.2.3 2009/03/19 12:23:11 mudrd8mz Exp $

/**
 * This file defines the main newmodule configuration form
 * It uses the standard core Moodle (>1.8) formslib. For
 * more info about them, please visit:
 *
 * http://docs.moodle.org/en/Development:lib/formslib.php
 *
 * The form must provide support for, at least these fields:
 *   - name: text element of 64cc max
 *
 * Also, it's usual to use these fields:
 *   - intro: one htmlarea element to describe the activity
 *            (will be showed in the list of activities of
 *             newmodule type (index.php) and in the header
 *             of the newmodule main page (view.php).
 *   - introformat: The format used to write the contents
 *             of the intro field. It automatically defaults
 *             to HTML when the htmleditor is used and can be
 *             manually selected if the htmleditor is not used
 *             (standard formats are: MOODLE, HTML, PLAIN, MARKDOWN)
 *             See lib/weblib.php Constants and the format_text()
 *             function for more info
 */

require_once($CFG->dirroot.'/course/moodleform_mod.php');


class mod_webscheme_mod_form extends moodleform_mod {

	function definition() {

		//echo"<pre>";print_r($this);echo"<pre>";die();

		global $COURSE;
		$mform =& $this->_form;

		//-------------------------------------------------------------------------------
		/// Adding the "general" fieldset, where all the common settings are showed
		$mform->addElement('header', 'general', get_string('general', 'form'));

		/// Adding the standard "name" field
		$mform->addElement('text', 'name', get_string('webschemename', 'webscheme'), array('size'=>'64'));
		$mform->setType('name', PARAM_TEXT);
		$mform->addRule('name', null, 'required', null, 'client');
		$mform->addRule('name', get_string('maximumchars', '', 255), 'maxlength', 255, 'client');

		/// Adding the required "intro" field to hold the description of the instance
		$mform->addElement('htmleditor', 'intro', get_string('webschemeintro', 'webscheme'));
		$mform->setType('intro', PARAM_RAW);
		$mform->addRule('intro', get_string('required'), 'required', null, 'client');
		$mform->setHelpButton('intro', array('writing', 'richtext'), false, 'editorhelpbutton');

		/// Adding "introformat" field
		$mform->addElement('format', 'introformat', get_string('format'));

		//--------page definitions-----------------------------------------------------------------------

		// little stuff
		$mform->addElement('header', 'pagedefsheader', get_string('pagedefsheader', 'webscheme'));

		$mform->addElement('textarea', 'ws_loadurls', get_string('loadurls', 'webscheme'),
						   'rows="2" cols="80"');
		$mform->setDefault('ws_loadurls', get_string('loadurlsdefault','webscheme'));

		$mform->addElement('textarea', 'ws_initexpr', get_string('initexpr', 'webscheme'),
        				   'rows="5" cols="80"');

		// events, ug ------

		$mform->addElement('header', 'event_header', get_string('event_header','webscheme'));

		$one_event_set=array();
		$one_event_set[]= &MoodleQuickForm::createElement('text', 'eventname', get_string('eventname','webscheme'),'size="64"');
		$one_event_set[]= &MoodleQuickForm::createElement('textarea', 'eventasserts', get_string('eventasserts','webscheme'),'rows="2" cols="80"');
		$one_event_set[]= &MoodleQuickForm::createElement('textarea', 'eventtemplate', get_string('eventtemplate','webscheme'),'rows="4" cols="80"');
		$one_event_set[]= &MoodleQuickForm::createElement('hidden', 'eventid', 0);
		$one_event_set[]= &MoodleQuickForm::createElement('static','eventspacer',"","");
		//looks bad     $one_event_set[]= &MoodleQuickForm::createElement('html', '<div><hr width="60%"></div>');

		if ($this->_instance) {
			$wsobj = get_record('webscheme','id', $this->_instance);
			$repeatno = count(json_decode($wsobj->ws_events));
			$repeatno++;  // give an extra blank one after
		} else {
			$repeatno=1;
		}
		$event_repeat_opts = array();
		$this->repeat_elements($one_event_set, $repeatno, $event_repeat_opts,
							   'event_id', 'event_add_fields', 1,
		get_string('event_add_fields_button','webscheme'),
		false
		);


		/// Main content area. ----
		$mform->addElement('header', 'content_header', get_string('content_header', 'webscheme'));
		$htmleditorsettings = array('canUseHtmlEditor'=>'detect', 'rows' => 30,
								'cols'  => 65, 'width' => 0,'height'=> 0);
		//$mform->addElement('htmleditor', 'ws_html', get_string('ws_html', 'webscheme'), $htmleditorsettings);
		$mform->addElement('textarea', 'ws_html', get_string('ws_html', 'webscheme'), 'rows="30" cols="80"');
		$mform->setType('ws_html', PARAM_RAW);
		$mform->addRule('ws_html', null, 'required', null, 'client');
		// hmmm, where is the help file supposed to be? taint workin
		$mform->setHelpButton('ws_html', array('ws_html', get_string('ws_html_help', 'webscheme'), 'webscheme'));



		//-------------------------------------------------------------------------------
		// add standard elements, common to all modules
		$this->standard_coursemodule_elements();
		//-------------------------------------------------------------------------------
		// add standard buttons, common to all modules
		$this->add_action_buttons();

	}

	function data_preprocessing(&$defaults) {
		if (!empty($this->_instance)) {

			// settings needs to be dealt with, if authors ever get to it...

			// loadurls...
			$loadurlsarray = json_decode($defaults['ws_loadurls'], true);
			if (function_exists("json_last_error")) {
				if (json_last_error() != JSON_ERROR_NONE) {
					print_error(get_string('badjsondecode_auth','webscheme').
				            "(cmid={$this->_instance}, field=loadurls");
				}
			}
			$defaults['ws_loadurls'] = implode(" \n", $loadurlsarray);

			//events
			$eventsarray = json_decode($defaults['ws_events'], true);
			if (function_exists("json_last_error")) {
				if (json_last_error() != JSON_ERROR_NONE) {
					print_error(get_string('badjsondecode_auth','webscheme').
				            "(cmid={$this->_instance}, field=events");
				}
			}
			//echo"<hr><pre>";print_r($eventsarray);echo"</pre>";die();
			// sniff, wherefore art thou anonymous lambdas?  php 5.3?
			//  sheesh, can't do callbacks with object methods anyway?  whatever, heathens.
			$defaults['eventname'] = $this->webscheme_grabkey("name", $eventsarray);
			$defaults['eventasserts'] = $this->webscheme_grabkey("assertion", $eventsarray);
			$defaults['eventtemplate'] = $this->webscheme_grabkey("template", $eventsarray);

			//echo"<pre>";print_r($defaults);echo"</pre>";die();
		}
	}

	//returns array of values from subarrays of $array for key=selector
	function webscheme_grabkey($selector, $events) {
		$out = array();
		foreach($events as $event) {
			$out[] = $event[$selector];
		}
		return $out;
	}




}  // close object

?>
