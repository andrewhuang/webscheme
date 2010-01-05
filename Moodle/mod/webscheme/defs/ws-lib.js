/**
 * Document events
 * 
 * must be called from Javascript rather than the applet's stop() or
 * destroy() methods to ensure that the Javascript environment is
 * available when the applet runs its queries
 * 
 * there is no loadWebscheme() here for a similar reason: the applet
 * isn't done loading yet.  So just have it restore the state when
 * it's ready.
 */


// TODO -- delay runSchemeEvent until page is fully loaded.


function getSchemeHandler() {
  // get by slot instead of getElementById() for Safari
  // in Safari, the latter returns you the DOM node "APPLET" rather than
  // the JavaScript object you need for LiveConnect
  applet = document.SchemeHandler;
  return applet;
}

/**
 * Bound to body's onUnload event
 */
function unloadWebscheme () {
  // must be called after page load
  stateStore = getSchemeHandler().getStateStore();
  stateStore.save();
}

function runSchemeEvent (eventName) {
  try {
    // must be called after page load
    getSchemeHandler().runEvent(eventName);
  } catch (pe) { // outer PrivilegedActionException
    var pe2 = pe.getException(); // inner PrivilegedActionException
    var ie = pe2.getException(); // InvocationTargetException
    var re = ie.getTargetException();  // the root cause
    alert("(Java exception) " + re);
  }
}

function setErrorMessage (errorMsg) {
  try {
    // must be called after page load
    getSchemeHandler().setJSError(errorMsg);
  } catch (pe) { // outer PrivilegedActionException
    var pe2 = pe.getException(); // inner PrivilegedActionException
    var ie = pe2.getException(); // InvocationTargetException
    var re = ie.getTargetException();  // the root cause
    alert("(Java exception) " + re);
  }
}

/*
  Internal
*/

function setCellValue (cell, value) {
  cell.normalize();
  if (cell.hasChildNodes()) {
    // try modifying the child
    if (cell.firstChild.nodeType == 3)
      cell.firstChild.nodeValue = value;
  } else {
    // add a new child
    var textNode = document.createTextNode(value);
    cell.appendChild(textNode);
  }
}

function getRowElement (tableID, rowNum) {
  return document.getElementById(tableID).rows[rowNum];
}

function getTableElement (tableID) {
//  alert("getTableElement( "+tableID+" )");
  return document.getElementById(tableID);
}

function rowElementToArray (rowElement) {
  var cells = rowElement.cells;
  var cellVals = new Array(cells.length);
  for (var i = 0; i < cells.length; i += 1)
    cellVals[i] = getCellValue(cells[i]);
  return cellVals;
}

function rowElementToJavaArray (rowElement) {
  var cells = rowElement.cells;
  var cellVals = java.lang.reflect.Array.newInstance(java.lang.String, cells.length);
  for (var i = 0; i < cells.length; i += 1)
    cellVals[i] = getCellValue(cells[i]);
  return cellVals;
}

function fillRowElement (rowElement, vals) {
  for (var i = 0; i < vals.length; i += 1) {
    var cell = rowElement.insertCell(i);
    setCellValue(cell, vals[i]);
  }
}

function getRowElementAsArray (tableID, rowNum) {
  return rowElementToArray(getRowElement(tableID, rowNum));
}

// called by DataModel
function getRowElementAsJavaArray (tableID, rowNum) {
  return rowElementToJavaArray(getRowElement(tableID, rowNum));
}

function getTableAsMatrix (tableID) {
  var rows = getTableElement(tableID).rows;
  var newRows = new Array(rows.length);
  for (var i = 0; i < rows.length; i += 1)
    newRows[i] = rowElementToArray(rows[i]);
  return newRows;
}

function getRowElementAsString (tableID, rowNum) {
  var cells = getRowElement(tableID, rowNum).cells;
  var str = "";
  for (var i = 0; i < cells.length; i += 1)
    str += getCellValue(cells[i]) + "\t";
  return str;
}

function insertRowValues (tableID, index, vals) {
  var tableElement = getTableElement(tableID);
  var rowElement = tableElement.insertRow(index);
  fillRowElement(rowElement, vals);
}


/*
  DataModel
*/

// called by DataModel
function addRowValues (tableID, vals) {
  /*
  try {
    var noTable = new org.ucwise.toolkit.NoSuchTableException(tableID);
  } catch (ex) {
    alert("couldn't construct Java exception");
  }
  */

  var table;
  try {
    table = getTableElement(tableID);
  } catch (ex2) {
    alert("js caught");
    try {
      setErrorMessage("no such table id '"+tableID+"'");
    } catch (ex3) {
      alert("can't even set error message");
    }
  }
  var rowElement = table.insertRow(table.rows.length);
  fillRowElement(rowElement, vals);
}


// called by DataModel
function getCellValue (cell) {
  cell.normalize();
  if (cell.firstChild.nodeType == 3)
    return cell.firstChild.nodeValue;
  else {
    alert("cell.firstChild has nodeType: " + cell.firstChild.nodeType);
    return '';
  }
}

// called by DataModel
function getCellElement (tableID, rowNum, cellNum) {
  return document.getElementById(tableID).rows[rowNum].cells[cellNum];
}

