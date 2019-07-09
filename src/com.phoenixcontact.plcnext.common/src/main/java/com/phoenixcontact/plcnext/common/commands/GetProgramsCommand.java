/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;


	/**
	 * The command line tool command for getting programs of a project
	 *
	 */
public class GetProgramsCommand extends Command {
	
		private static final String GET_PROGRAMS_VERB = Messages.GetProgramsCommand_verb;
		
		/**
		 * @param options
		 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
		 */
		public GetProgramsCommand(Map<String, String> options) {
			super(options, GET_PROGRAMS_VERB);
		}
		
		/**
		 * option path
		 */
		public final static String OPTION_PATH = Messages.GetProgramsCommand_optionpath;
		
		/**
		 * option project source directories
		 */
		public final static String OPTION_SOURCES = Messages.GetProgramsCommand_optionsources;
		
		
		@Override
		protected String[] formatOption(String key, String value) {
			String formattedValue = value;
			
			if(key.equals(OPTION_PATH) || key.equals(OPTION_SOURCES)) {
				formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return super.formatOption(key, formattedValue);
		}
		
		/**
		 * option component
		 */
		public final static String OPTION_COMPONENT = Messages.GetProgramsCommand_optioncomponent;
}
