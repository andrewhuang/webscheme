<?php

// copied from mod/labels without particularly deep understanding, yo.

    //This function executes all the restore procedure about this mod
    function webscheme_restore_mods($mod,$restore) {
        global $CFG;

        $status = true;

        //Get record from backup_ids
        $data = backup_getid($restore->backup_unique_code,$mod->modtype,$mod->id);

        if ($data) {
            //Now get completed xmlized object
            $info = $data->info;
            // (STOLEN FROM CHOICE) if necessary, write to restorelog and adjust date/time fields
            if ($restore->course_startdateoffset) {
                restore_log_date_changes('Webscheme', $restore, $info['MOD']['#'], array('TIMEOPEN', 'TIMECLOSE'));
            }
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
            
            $ws_settings = $info['MOD']['#']['WS_SETTINGS']['0']['#'];
            $ws_settings = backup_todb(html_entity_decode($ws_settings));
            $webscheme->ws_settings = $ws_settings;

            $ws_events = $info['MOD']['#']['WS_EVENTS']['0']['#'];
            $ws_events = backup_todb(html_entity_decode($ws_events));
            $webscheme->ws_events = $ws_events;

            $ws_initexpr = $info['MOD']['#']['WS_INITEXPR']['0']['#'];
            $ws_initexpr = backup_todb(html_entity_decode($ws_initexpr));
            $webscheme->ws_initexpr = $ws_initexpr;
            
            $ws_loadurls = $info['MOD']['#']['WS_LOADURLS']['0']['#'];
            $ws_loadurls = backup_todb(html_entity_decode($ws_loadurls));
            $webscheme->ws_loadurls = $ws_loadurls;
            
            $ws_html = $info['MOD']['#']['WS_HTML']['0']['#'];
            $ws_html = backup_todb(html_entity_decode($ws_html));
            $webscheme->ws_html = $ws_html;
            

            //The structure is equal to the db, so insert the webscheme
            //  ah?  Do we need to addslashes or anything?
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