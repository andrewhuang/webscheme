<html>
<h1>Convert HTML to WSML</h1>
<b>Enter HTML Code here:</b><br /><br />

<?php

$inputHtmlTest = <<<HTMLCODE


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<!--WISEHEAD-->
<!-- <base href="http://wise-dev.berkeley.edu/modules/webscheme/student.php"/> -->
<script type="text/javascript">
wise_groupID = 236130;
wise_pageID = 1086;
</script>
<!--/WISEHEAD-->
<title>Define an acronym procedure</title> <link href="defs/ws-defaults.css" rel="stylesheet" type="text/css"/> <script type="text/javascript" src="defs/ws-lib.js"></script></head> <body><div>
<!-- Sorry, this is not an XHTML element but Safari ne comprends pas l'objet -->
<applet
 code="webscheme.SchemeHandler.class"
 scriptable="true"
 mayscript="true"
 classid="java:webscheme.SchemeHandler.class" width="120" height="36" archive="lib/webscheme.jar" name="SchemeHandler" id="SchemeHandler" standby="Loading WebScheme daemon"><param name="mayscript" value="true"/> <param name="scriptable" value="true"/> <param name="name" value="SchemeHandler"/> <param name="progressbar" value="true"/> <param name="progresscolor" value="#FFCC33"/> <param name="boxfgcolor" value="#AAAACC"/> <param name="boxbgcolor" value="#FFCC33"/> <param name="boxmessage" value="Loading WebScheme..."/> 
 
 <param name="loadurl-0" value="http://inst.eecs.berkeley.edu/~ryanc/treejunk.scm"/> <param name="loadurl-1" value="http://inst.eecs.berkeley.edu/~ryanc/ws-berkeley.scm"/>
 <param name="loadurl-2" value="http://inst.eecs.berkeley.edu/~cs3/programs/romanv1.scm"/> 
 
 <param name="init-expr" value=""/> <param name="event-name-0" value="test-a"/> 
 
 <param name="event-assertions-0" value="(ws-assert-minlength &#39;a1 1)(ws-assert-balanced &#39;a1)"/> <param name="event-template-0" value="(define (acronym sent) (every {a1} sent))
       (ws-set-status &#39;a1-status &quot;unknown&quot;)
       (if (equal? (acronym &#39;(michael j clancy)) &#39;(m j c))
         (ws-set-status &#39;a1-status &quot;passed&quot;)
         (ws-set-status &#39;a1-status &quot;failed&quot;) )"/>
		 
		 <param name="event-name-0" value="test-b"/> 
 
 <param name="event-assertions-0" value="B (ws-assert-minlength &#39;a1 1)(ws-assert-balanced &#39;a1)"/> <param name="event-template-0" value="B (define (acronym sent) (every {a1} sent))
       (ws-set-status &#39;a1-status &quot;unknown&quot;)
       (if (equal? (acronym &#39;(michael j clancy)) &#39;(m j c))
         (ws-set-status &#39;a1-status &quot;passed&quot;)
         (ws-set-status &#39;a1-status &quot;failed&quot;) )"/></applet></div> <form id="wsfields" onsubmit="return false" action="">
		 
		 <div>
		 <h3>Define an acronym procedure</h3> <p>The operation of applying a procedure to each word in a sentence or to each character in a word (a mapping pattern) is so common that a procedure equivalent to <tt>applied-to-all</tt>  is builtin to our version of Scheme. It&#39;s called  <tt>every.</tt></p> <p>Fill in the blank below with an argument for  <tt>every</tt> that results in the  <tt>acronym</tt> procedure returning the first initials of the words in the argument sentence. For example,  <tt>(acronym &#39;(royal air force))</tt> should return <tt>(r a f).</tt> To see if you are right, press the pointer. If you see a green check, you got the right answer, otherwise you got it wrong.</p> <p>You will need to wait until you see the word &quot;SchemeHandler&quot; in the upper left corner of the page before you start.</p> <table width="100%" border="1"><tr><td>Scheme Expression</td> <td> </td> <td>Correct?</td></tr> <tr><td><tt>(define (acronym sent)</tt> <br/> <tt> (every </tt> <input type="text" id="a1" title="a1" class="inputField" value="" size="20" maxlength="20"/> <tt>sent) )</tt></td> <td><img id="a1-status" title="a1-status" alt="Status icon" src="ws-icons/status_notrun.gif"/></td> <td><button onclick="runSchemeEvent(&#39;test-a&#39;);"><img alt="point right" src="ws-icons/handpoint_right.gif"/></button></td></tr></table></div>
		 
		 </form></body></html>



HTMLCODE;



function htmlToWsml($htmlCode){
  $html = new SimpleXMLElement($htmlCode);
  $wsmlCode = "<wsml>\n";

  foreach($html->xpath('//param') as $param){
    $name = (string) $param[name];
  
    if(stristr($name, "loadurl") ){	
	  $value = $param[value];
      $wsmlCode .= "\t<ws-loadurl>$value</ws-loadurl>\n";
    }elseif(stristr($name, "init-expr") ){  
   	  $value = $param[value];
	  $wsmlCode .="\n";
	  $wsmlCode .= "\t<ws-initExpr>$value</ws-initExpr>\n";
    }elseif(stristr($name, "event-name") ){  
	  $value = $param[value];
	  $wsmlCode .="\n";
	  $wsmlCode .= "\t<ws-event>\n";
	  $wsmlCode .= "\t\t<ws-event-name>$value</ws-event-name>\n";
    }elseif(stristr($name, "event-assertions")){
      $value = $param[value];
	  $wsmlCode .="\n";
	  $wsmlCode .= "\t\t<ws-event-assertions>\n";
	  $wsmlCode .= "\t\t\t$value\n";
	  $wsmlCode .= "\t\t</ws-event-assertions>\n";
    }elseif(stristr($name, "event-template")){
      $value = $param[value];
	  $wsmlCode .="\n";
	  $wsmlCode .= "\t\t<ws-event-template>\n";
	  $wsmlCode .= "\t\t\t$value\n";
	  $wsmlCode .= "\t\t</ws-event-template>\n";
	  $wsmlCode .= "\t</ws-event>\n";
    }
  }

  //ASSUMING:
  //-Only one form
  //-html form is in the format:
  //  <form><div>...</div></form>
  $form = $html->xpath('//form');
  $formData = $form[0]->div->asXML();
  $wsmlCode .="\n\t<ws-html>\n";
  $wsmlCode .="\t\t$formData\n";
  $wsmlCode .="\t</ws-html>\n";

  $wsmlCode .="</wsml>";

  return $wsmlCode;
}

if($_POST && $_POST["submit"]){
  $inputHtml = stripslashes($_POST["inputHtml"]);
  $wsmlCode = htmlToWsml($inputHtml); 
}

echo "<form name=\"htmlCode\" action=\"htmlToWsml.php\" method=\"post\">";
echo "  <textarea name=\"inputHtml\" rows=\"30\" cols=\"100\">$inputHtml</textarea><br />";
echo "  <input type=\"submit\" name=\"submit\" value=\"Convert\" />";
echo "</form>";

if($wsmlCode){
  echo "<b>WSML Code:</b><br />";
  echo "<textarea rows = 30 cols = 100>$wsmlCode</textarea>";
}


 
//Useful for creating SimpleXMLElement holding wsml info using html file
//$wsml = new SimpleXMLElement("<wsml></wsml>");
//$loadUrl = $wsml->addChild('ws-loadurl', $param[value]);
//$wsml = new SimpleXMLElement($wsmlCode);
//$wsmlPrint = $wsml->asXML();


?>

</html>