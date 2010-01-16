<?php

    function webscheme_backup_mods($bf,$preferences) {

        global $CFG;

        $status = true;

        //Iterate over choice table
        $webschemes = get_records("webscheme","course",$preferences->backup_course, "id");
        if ($webschemes) {
            foreach ($webschemes as $ws) {
                if (backup_mod_selected($preferences,'webscheme',$choice->id)) {
                    $status = webscheme_backup_one_mod($bf,$preferences,$ws);
                }
            }
        }
        // really?  return the status of the last one?  this is borrowed from choice...
        return $status;
    }
    
    
   function webscheme_backup_one_mod($bf,$preferences,$ws) {

        global $CFG;

        if (is_numeric($ws)) {
            $ws = get_record('webscheme', 'id', $ws);
        }

        $status = true;

        //Start mod
        fwrite ($bf,start_tag("MOD",3,true));
        //Print webscheme data
        fwrite ($bf,full_tag("ID",4,false,$ws->id));
        fwrite ($bf,full_tag("MODTYPE",4,false,"webscheme"));
        fwrite ($bf,full_tag("NAME",4,false,$ws->name));
        fwrite ($bf,full_tag("INTRO",4,false,$ws->intro));
        fwrite ($bf,full_tag("INTROFORMAT",4,false,$ws->introformat));
        fwrite ($bf,full_tag("TIMECREATED",4,false,$ws->timecreated));
        fwrite ($bf,full_tag("TIMEMODIFIED",4,false,$ws->timemodified));

        fwrite ($bf,full_tag("WS_SETTINGS",4,false,htmlentities($ws->ws_settings)));
        fwrite ($bf,full_tag("WS_EVENTS",4,false,htmlentities($ws->ws_events)));
        fwrite ($bf,full_tag("WS_INITEXPR",4,false,htmlentities($ws->ws_initexpr)));
        fwrite ($bf,full_tag("WS_LOADURLS",4,false,htmlentities($ws->ws_loadurls)));
        fwrite ($bf,full_tag("WS_HTML",4,false,htmlentities($ws->ws_html)));
                        
       //End mod
        $status = fwrite ($bf,end_tag("MOD",3,true));

        return $status;
      }
      
      
     ////?? no real idea what this is doing. Copied mostly from mod/label
     ////Return an array of info (name,value)
     function webscheme_check_backup_mods($course,$user_data=false,$backup_unique_code,$instances=null) {
        if (!empty($instances) && is_array($instances) && count($instances)) {
            $info = array();
            foreach ($instances as $id => $instance) {
                $info += webscheme_check_backup_mods_instances($instance,$backup_unique_code);
            }
            return $info;
        }

         //First the course data
         $info[0][0] = get_string("modulenameplural","webscheme");
         $info[0][1] = count_records("webscheme", "course", "$course");
         return $info;
         
         //No user data, so nothing more...
    }
    
    
    ////?? no real idea what this is doing. Copied mostly from mod/label
    ////Return an array of info (name,value)
    function webscheme_check_backup_mods_instances($instance,$backup_unique_code) {
         //First the course data
        $info[$instance->id.'0'][0] = '<b>'.$instance->name.'</b>';
        $info[$instance->id.'0'][1] = '';
        return $info;
    }
    
    
/*
    // no real idea here.  Supposed to munge $content links somehow? 
    //   (it is in mod/choice)
    function webscheme_encode_content_links($content,$preferences) {

        global $CFG;
        
        return $content;  // un-munged
    }
*/
    
?>