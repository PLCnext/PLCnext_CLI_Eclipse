/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult.IncludePath;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetCompilerSpecsCommandResult extends CommandResult
{

	public GetCompilerSpecsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}

	private Compiler[] compilerSpecifications;

	public Compiler[] getCompiler()
	{
		return compilerSpecifications;
	}

	public static class Compiler
	{
		private String compilerPath;

		private String language;

		private String compilerSysroot;

		private String compilerFlags;

		private IncludePath[] includePaths;

		private Macro[] compilerMacros;

		public String getCompilerPath()
		{
			return compilerPath;
		}

		public String getLanguage()
		{
			return language;
		}

		public String getSysroot()
		{
			return compilerSysroot;
		}

		public String getFlags()
		{
			return compilerFlags;
		}

		public IncludePath[] getIncludePaths()
		{
			return includePaths;
		}

		public Macro[] getMacros()
		{
			return compilerMacros;
		}

		public static class Macro
		{
			private String name;
			private String value;

			public String getName()
			{
				return name;
			}

			public String getValue()
			{
				return value;
			}
		}
	}

	public static GetCompilerSpecsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			return new Gson().fromJson(stdout.stream().collect(Collectors.joining("")),
					GetCompilerSpecsCommandResult.class);
		}
		return null;
	}
}
