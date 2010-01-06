<?php

// copied from mod/labels without particularly deep understanding, yo.

    //This function executes all the restore procedure about this mod
    function webscheme_restore_mods($mod,$restore) {
        global $CFG, $DB;

        $status = true;

        //Get record from backup_ids
        $data = backup_getid($restore->backup_unique_code,$mod->modtype,$mod->id);

        if ($data) {
            //Now get completed xmlized object
            $info = $data->info;
            //traverse_xmlize($info);                                                                     //Debug
            //print_object ($GLOBALS['traverse_array']);                                                  //Debug
            //$GLOBALS['traverse_array']="";                                                              //Debug

            //Now, build the WEBSCHEME record structure
            $webscheme->course = $restore->course_id;
            $webscheme->name = backup_todb($info['MOD']['#']['NAME']['0']['#']);
            $webscheme->intro = backup_todb($info['MOD']['#']['INTRO']['0']['#']);
            $webscheme->introformat = backup_todb($info['MOD']['#']['INTROFORMAT']['0']['#']);
            $webscheme->timecreated = $info['MOD']['#']['TIMECREATED']['0']['#'];
            $webscheme->timemodified = $info['MOD']['#']['TIMEMODIFIED']['0']['#'];
            
            $wsml = $info['MOD']['#']['WSML']['0']['#'];
            $wsml = backup_todb(html_entity_decode($wsml));
            $webscheme->wsml = $wsml;
            

            //The structure is equal to the db, so insert the webscheme
            $newid = $DB->insert_record ("webscheme",$webscheme);

            //Do some output
            if (!defined('RESTORE_SILENTLY')) {
                echo "<li>".get_string("modulename","webscheme")." \"".format_string($webscheme->name,true)."\"</li>";
            }
            backup_flush(300);

            if ($newid) {
                //We have the newid, update backup_ids
                backup_putid($restore->backup_unique_code,$mod->modtype,
                             $mod->id, $newid);

            } else {
                $status = false;
            }
        } else {
            $status = false;
        }

        return $status;
    }

//no idea...    
//    function webscheme_decode_content_links_caller($restore) {
//    }


//scre-wit    
//    //This function returns a log record with all the necessay transformations
//    //done. It's used by restore_log_module() to restore modules log.
//    function webscheme_restore_logs($restore,$log) {
//    }


?>