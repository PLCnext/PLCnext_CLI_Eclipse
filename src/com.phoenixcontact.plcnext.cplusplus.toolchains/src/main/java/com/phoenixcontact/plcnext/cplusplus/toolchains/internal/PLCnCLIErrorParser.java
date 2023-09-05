/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;

public class PLCnCLIErrorParser extends RegexErrorParser
{

	private String getSeverityByInt(int severity)
	{
		switch (severity)
		{
		case IMarkerGenerator.SEVERITY_WARNING:
			return "Warning";

		case IMarkerGenerator.SEVERITY_INFO:
			return "Info";

		default:
			return "Error";
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		PLCnCLIErrorParser parser = new PLCnCLIErrorParser();
		for(RegexErrorPattern p : getPatterns())
		{
			parser.addPattern((RegexErrorPattern) p.clone());
		}
		
		
		IErrorParserNamed ep = ErrorParserManager.getErrorParserCopy("org.eclipse.cdt.core.GCCErrorParser");
		if (ep != null && ep instanceof RegexErrorParser)
		{
			RegexErrorPattern[] patterns = ((RegexErrorParser) ep).getPatterns();
			clonePatterns(patterns, parser);
		}
		
		IErrorParserNamed ep2 = ErrorParserManager.getErrorParserCopy("org.eclipse.cdt.core.GLDErrorParser");
		if (ep2 != null && ep2 instanceof RegexErrorParser)
		{
			RegexErrorPattern[] patterns = ((RegexErrorParser) ep2).getPatterns();
			clonePatterns(patterns, parser);
		}	
		
		return parser;
	}
	
	private void clonePatterns(RegexErrorPattern[] patterns, PLCnCLIErrorParser parser) throws CloneNotSupportedException
	{
		for (RegexErrorPattern origPattern : patterns)
		{
			RegexErrorPattern pattern = (RegexErrorPattern) origPattern.clone();
			String patternAsString = pattern.getPattern();
			patternAsString = "\\[cmake\\]:\\s*" + patternAsString;
			pattern.setPattern(patternAsString);

			String oldExpression = pattern.getDescriptionExpression();
			String severity = getSeverityByInt(pattern.getSeverity());
			String descriptionExpr = String.format("PLCnCLI Build %s: %s", severity, oldExpression);
			pattern.setDescriptionExpression(descriptionExpr);

			parser.addPattern(pattern);
		}
	}
}
