<?php

/**
 * XML-RPC API for WebScheme
 *
 * TODO
 * Convert SQL timestamp to XML-RPC date
 * 
 * @package webscheme
 * @author turadg
 * @version $Id: xmlrpc.php 6615 2006-04-14 20:50:09Z turadg $
 */

require_once 'wsdb.inc';


/**
 * @return associative array (hashtable) of datakey => data in statefile
 */
function rpc_get_state_data ($method_name, $params, $app_data) {
    $groupID = $params[0];
    $fileKey = $params[1];
    $fileID = ws_state_get_fileid($groupID, $fileKey);
    return ws_state_get_data($fileID);
}

/**
 * input is same as output of rpc_get_state_data()
 */
function rpc_set_state_data ($method_name, $params, $app_data) {
    $groupID = $params[0];
    $fileKey = $params[1];
    $dataTable = $params[2];
    $fileID = ws_state_get_fileid($groupID, $fileKey);
    return ws_state_set_data($fileID, $dataTable);
}

/**
 * @return 2D array of timestamped values for queue
 */
function rpc_get_queue_values ($method_name, $params, $app_data) {
    $groupID = $params[0];
    $fileKey = $params[1];
    $queueKey = $params[2];
    $queueID = ws_log_get_queue_id($groupID, $fileKey, $queueKey);
    return ws_log_get_queue_values($queueID);
}

/**
 * Pushes a value onto the specified queue
 */
function rpc_push_value ($method_name, $params, $app_data) {
    $groupID = $params[0];
    $fileKey = $params[1];
    $queueKey = $params[2];
    $queueID = ws_log_get_queue_id($groupID, $fileKey, $queueKey);
    $value = $params[3];
    return ws_log_push_value($queueID, $value);
}


/**
 * Return "Hello, world" for testing
 */
function rpc_hello_world ($method_name, $params, $app_data) {
    return "Hello, world";
}


function processRequest(&$request_xml) {
    // define XML-RPC server
    $server = xmlrpc_server_create();

    // log for debuggin
    $fp = fopen("/var/tmp/ws_request.log", "a");
    fwrite($fp, date("H:i")."\n");
    fwrite($fp, $request_xml);
    fwrite($fp, "\n\n");
    fclose($fp);

    // register methods
    xmlrpc_server_register_method($server, "getQueueValues", "rpc_get_queue_values");
    xmlrpc_server_register_method($server, "pushValue", "rpc_push_value");
    xmlrpc_server_register_method($server, "getStateData", "rpc_get_state_data");
    xmlrpc_server_register_method($server, "setStateData", "rpc_set_state_data");

    xmlrpc_server_register_method($server, "helloWorld", "rpc_hello_world");
    xmlrpc_server_register_method($server, "noop", "rpc_hello_world");

    // call the method
    $response =& xmlrpc_server_call_method($server, $request_xml, '');
    
    // send back the respond
    print $response;
    
    // release resources
    xmlrpc_server_destroy($server);
}

if (isset($HTTP_RAW_POST_DATA)) {
    $input =& $HTTP_RAW_POST_DATA;
} else {
    $input =& implode("\r\n", file('php://input'));
}

// only attempt to process the request if there is a real request
if (!empty($input)) {
    processRequest($input);
}


?>
