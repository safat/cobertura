/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Joakim Erdfelt <joakim@erdfelt.net>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package net.sourceforge.cobertura.ant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.cobertura.util.Copyright;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

public class InstrumentTask extends CommonMatchingTask
{

	Ignore ignoreRegex = null;

	public InstrumentTask()
	{
		super("net.sourceforge.cobertura.coverage.Main");
	}

	public Ignore createIgnore()
	{
		ignoreRegex = new Ignore();
		return ignoreRegex;
	}

	void coverage(String[] filenames)
	{
		if (filenames.length == 0)
		{
			return;
		}

		for (int i = 0; i < filenames.length; i++)
		{
			getProject().log("Adding " + filenames[i] + " to list",
					Project.MSG_VERBOSE);
			addArg(filenames[i]);
		}

		saveArgs();

		Copyright.print(System.out);
		System.out.println("instrumenting " + filenames.length + " "
				+ (filenames.length == 1 ? "class" : "classes")
				+ (toDir != null ? " to " + toDir : ""));

		/**
		 * TODO: Do something here so that we can set System.in and System.out on
		 * getJava() to the one we're using now.  So that when instrumentation calls
		 * System.out, it will show up as "[instrument] doing stuff" instead of
		 * "[java] doing stuff" in the ant output.
		 */
		if (getJava().executeJava() != 0)
		{
			throw new BuildException();
		}
	}

	public void execute() throws BuildException
	{
		initArgs();
		if (toDir != null)
		{
			addArg("-d");
			addArg(toDir.toString());
		}

		// TODO: ignoreRegex.setRegex() is never called
		if (ignoreRegex != null)
		{
			addArg("-ignore");
			addArg(ignoreRegex.getRegex());
		}

		Set filenames = new HashSet();
		Iterator i = fileSets.iterator();

		while (i.hasNext())
		{
			FileSet fileSet = (FileSet)i.next();

			addArg("-basedir");
			addArg(baseDir(fileSet));

			filenames.addAll(Arrays.asList(getFilenames(fileSet)));
		}

		coverage((String[])filenames.toArray(new String[filenames.size()]));

		unInitArgs();
	}
}
