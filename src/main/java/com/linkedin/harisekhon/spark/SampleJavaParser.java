//
//  Author: Hari Sekhon
//  Date: 2015-06-08 00:05:08 +0100 (Mon, 08 Jun 2015)
//
//  vim:ts=4:sts=4:sw=4:noet
//
//  https://github.com/harisekhon
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/harisekhon
//

package com.linkedin.harisekhon.spark;

import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.HashMap;

public class SampleJavaParser extends AbstractParser implements Serializable {

	private static final long serialVersionUID = 102L;
	
	public FileOffsetLineDocument parse(String path, Long offset, String line) {
	
		String path_stripped = path.replaceFirst("^file:", "").replaceFirst("^hdfs:\\/\\/[\\w.-]+(?:\\d+)?", "");
		/*
		String date = null;
		HashMap<String, String> doc = new HashMap<String, String>();
		doc.put("path", path_stripped);
		if (offset > -1) {
		  doc.put("offset", offset.toString());
		}
		doc.put("line", line);
		// TODO: fix
		if (date != null) {
		  doc.put("date", date.toString());
		}
		return doc;
		*/
		return new FileOffsetLineDocument(path_stripped, offset, line);
	}
		  
	// return a list of possible return objects to pass to Kryo registration for optimization
	public FileOffsetLineDocument returns() {
		/*
		ArrayList<Object> a = new ArrayList<Object>();
		a.add(new FileOffsetLineDocument("path", 0L, "line"));
		a.add(new FileOffsetDateLineDocument("path", 0L, "line"));
		return a;
		*/
		return new FileOffsetLineDocument("path", 0L, "line");
	}

}
