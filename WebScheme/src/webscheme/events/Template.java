/** Template
    
    Java model of a WebScheme template

    @author Turadg
    
    Based on http://www.seapod.org/software/TemplateEngine.java
*/

package webscheme.events;

import java.util.*;

public class Template {
	static final char OPEN_TAG = '{';
	static final char CLOSE_TAG = '}';
	static final String UNDEFINED = "'undefined";

	private List templateChunks;
	private Collection symbolNames; // the .data of subset of templateChunks

	public Template(String templateText) {
		templateChunks = new Vector();
		symbolNames = new Vector();

		/* Start and end text range positions; temp range and tag index */
		int start = 0, end = 0, pos = 0;

		/* Plow through the string, scooping up content runs and
		   symbols (stripping out open and close delimiters) and
		   putting them into the cache vector, templateChunks */

		/* Find the first open tag */
		pos = templateText.indexOf(OPEN_TAG);
		int length = templateText.length();

		while (pos < length) {
			if (pos == -1) { /* No more open tags */
				templateChunks.add(
					new TemplateChunk(templateText.substring(start), false));
				break;
			}
			if (pos >= start) { /* Preceeding text run */
				end = pos;
				templateChunks.add(
					new TemplateChunk(
						templateText.substring(start, end),
						false));
				start = end + 1;
			}
			/* Find the close tag */
			pos = templateText.indexOf(CLOSE_TAG);
			if (pos == -1) { /* No close tag */
				templateChunks.add(
					new TemplateChunk(templateText.substring(start), false));
				break;
			} else { /* Has close tag */
				end = pos;
				TemplateChunk symbolChunk =
					new TemplateChunk(templateText.substring(start, end), true);
				// add symbol chunk
				templateChunks.add(symbolChunk);
				// make note of symbol name
				symbolNames.add(symbolChunk.data);
				// Shrink the string
				templateText = templateText.substring(end + 1);
				start = 0; /* Reset the start position */
			}
			pos = templateText.indexOf(OPEN_TAG);
		}

		templateChunks = Collections.unmodifiableList(templateChunks);
		symbolNames = Collections.unmodifiableCollection(symbolNames);
	}

	/**
	   Substitutes the tag symbols in the given template with
	   values obtained from the data object, or returns the
	   original template text if the data object is null.
	   @param templateName the name of the template
	   @param data the data object to substitute values from
	   @return the substituted text
	 **/
	public final String fill(Map data) {
		/* Lazy guess of minimum buffer size */
		StringBuffer resultBuffer =
			new StringBuffer(templateChunks.size() * 20);

		/* Iterate through all of the chunks in the cache */
		for (Iterator i = templateChunks.iterator(); i.hasNext();) {
			TemplateChunk chunk = (TemplateChunk) i.next();
			if (chunk.isSymbol == true) { /* Substitute it... */
				/* Perform the substitutions */
				Object obj = data.get(chunk.data);
				if (obj != null)
					resultBuffer.append(obj.toString());
				else
					resultBuffer.append(UNDEFINED);
			} else {
				resultBuffer.append(chunk.data); /* Add content to buffer */
			}
		}
		return resultBuffer.toString();
	}

	public final Collection symbols() {
		return symbolNames;
	}

	/**
	   Dumps the cache to System.out for debugging
	*/
	public void dump() {
		int cacheLength = templateChunks.size();
		System.out.println("### Cache - " + cacheLength + " chunks");
		for (int i = 0; i < cacheLength; i += 1) {
			TemplateChunk chunk = (TemplateChunk) templateChunks.get(i);
			System.out.println(
				"["
					+ i
					+ "] = \""
					+ chunk.data
					+ "\" "
					+ (chunk.isSymbol == true ? "[symbol]" : "[text]"));
		}
	}

	/**
	    Represents a chunk of a template, either a text run
	    or a symbol that needs to be substituted.
	@author Steve Klingsporn <stevek@buzzlabs.com>
	 */
	static class TemplateChunk {
		private String data = null;
		private boolean isSymbol = false;

		public TemplateChunk(String text, boolean isSym) {
			data = text;
			isSymbol = isSym;
		}
	}

}
