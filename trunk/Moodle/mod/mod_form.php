<?php //$Id

/**
 * This file defines de main webscheme configuration form
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
 *             webscheme type (index.php) and in the header 
 *             of the webscheme main page (view.php).
 *   - introformat: The format used to write the contents
 *             of the intro field. It automatically defaults 
 *             to HTML when the htmleditor is used and can be
 *             manually selected if the htmleditor is not used
 *             (standard formats are: MOODLE, HTML, PLAIN, MARKDOWN)
 *             See lib/weblib.php Constants and the format_text()
 *             function for more info
 */

require_once ('moodleform_mod.php');

class mod_webscheme_mod_form extends moodleform_mod {

	function definition() {

		global $COURSE;
		$mform    =& $this->_form;

//-------------------------------------------------------------------------------
    /// Adding the "general" fieldset, where all the common settings are showed
        $mform->addElement('header', 'general', get_string('general', 'form'));
    /// Adding the standard "name" field
        $mform->addElement('text', 'name', get_string('webschemename', 'webscheme'), array('size'=>'64'));
		$mform->setType('name', PARAM_TEXT);
		$mform->addRule('name', null, 'required', null, 'client');
    /// Adding the optional "intro" and "introformat" pair of fields
    	$mform->addElement('htmleditor', 'intro', get_string('webschemeintro', 'webscheme'));
		$mform->setType('intro', PARAM_RAW);
		$mform->addRule('intro', get_string('required'), 'required', null, 'client');
        $mform->setHelpButton('intro', array('writing', 'richtext'), false, 'editorhelpbutton');

       // $mform->addElement('format', 'introformat', get_string('format'));

//-------------------------------------------------------------------------------
    /// Adding the rest of webscheme settings, spreeading all them into this fieldset
    /// or adding more fieldsets ('header' elements) if needed for better logic
       // $mform->addElement('static', 'label1', 'webschemesetting1', 'Your webscheme fields go here. Replace me!');

        $mform->addElement('header', 'wsmlHeader', get_string('webschemewsml', 'webscheme'));        
        $mform->addElement('textarea', 'wsml', get_string('wsmlParams', 'webscheme'), array('rows'=>'30', 'cols' => '75'));
	  //$mform->addElement('htmleditor', 'wsml', get_string('wsmlParams', 'webscheme'));  
		$mform->setType('wsml', PARAM_RAW);
		$mform->addRule('wsml', get_string('required'), 'required', null, 'client');
        $mform->setHelpButton('wsml', array('writing', 'richtext'), false, 'editorhelpbutton');
        
//-------------------------------------------------------------------------------
        // add standard elements, common to all modules
		$this->standard_coursemodule_elements();
//-------------------------------------------------------------------------------
        // add standard buttons, common to all modules
        $this->add_action_buttons();

	}
}

?>
