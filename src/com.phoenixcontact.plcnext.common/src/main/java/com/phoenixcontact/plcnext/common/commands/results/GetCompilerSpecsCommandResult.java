/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.commands.results.GetCompilerSpecsCommandResult.Compiler.Macro;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult.IncludePath;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetCompilerSpecsCommandResult extends CommandResult
{

	public GetCompilerSpecsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetCompilerSpecsCommandResult(Compiler[] compiler) 
	{
		super((JsonObject)null, null);
		this.compilerSpecifications = compiler;
	}
	
	private Compiler[] compilerSpecifications;

	public Compiler[] getCompiler()
	{
		return compilerSpecifications;
	}
	
	public void setCompiler()
	{
		
	}
	
	public static class Compiler
	{
		public Compiler(IncludePath[] includePaths, Macro[] macros)
		{
			this.includePaths = includePaths;
			this.compilerMacros = macros;
		}
		
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
			
			public Macro(String name, String value)
			{
				this.name = name;
				this.value = value;
			}
		}
	}
	
	
	
	
	public static GetCompilerSpecsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			// remove empty entry at start of list
			int index = stdout.indexOf("");
			stdout = stdout.subList(index + 1, stdout.size());
			
			List<String> includes = new ArrayList<String>();
			List<Macro> macros = new ArrayList<Macro>();

			while (!stdout.isEmpty())
			{
				int includesStart = stdout.indexOf("Include paths:") + 1;
				int macroStart = stdout.indexOf("Compiler Macros:") + 1;
				int macroEnd = stdout.indexOf("");
				if (includesStart > 0 && macroStart > 0)
				{
					if (macroEnd == -1)
					{
						macroEnd = stdout.size();
					}

					includes.addAll(stdout.subList(includesStart, macroStart - 1));

					List<String> macroSubResults = stdout.subList(macroStart, macroEnd);

					for (String result : macroSubResults)
					{
						String[] resultParts = result.split("=");
						String name = resultParts[0];
						String value = "";
						if (resultParts.length > 1)
						{
							value = resultParts[1];
						}
						if(!macros.stream().anyMatch(m -> m.getName().equals(name)))
							macros.add(new Macro(name, value));
						
					}
					if (macroEnd == stdout.size())
					{
						stdout.clear();
					} else
					{
						stdout = stdout.subList(macroEnd + 1, stdout.size());
					}

				} else
				{
					return null;
				}
			}
			
			return new GetCompilerSpecsCommandResult( new Compiler[] {new Compiler(includes.toArray(new IncludePath[0]), macros.toArray(new Macro[0]))} );
		}
		return null;
	}
}
