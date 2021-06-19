/*
 *     CustomConfigurationFactory
 *     Last Modified: 2021-06-18, 7:24 p.m.
 *     Copyright (C) 2021-06-18, 7:28 p.m.  CameronBarnes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.bigcattech.MediaDB.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.net.URI;

@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class CustomConfigurationFactory extends ConfigurationFactory {
	
	static Configuration createConfiguration(final String name, ConfigurationBuilder<BuiltConfiguration> builder) {
		
		builder.setConfigurationName(name);
		builder.setStatusLevel(Level.ERROR);
		builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).
																											 addAttribute("level", Level.DEBUG));
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").
																								   addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern",
				"%c{3} %M %d{dd MMM yyyy HH:mm:ss} %highlight{%-5p [%t]: %m%n}{STYLE=Logback}").addAttribute("disableAnsi", "false"));
		appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY,
				Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
		builder.add(appenderBuilder);
		builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).
																					  add(builder.newAppenderRef("Stdout")).
																																   addAttribute("additivity", false));
		builder.add(builder.newRootLogger(Level.TRACE).add(builder.newAppenderRef("Stdout")));
		return builder.build();
	}
	
	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
		
		return getConfiguration(loggerContext, source.toString(), null);
	}
	
	@Override
	public Configuration getConfiguration(final LoggerContext loggerContext, final String name, final URI configLocation) {
		
		ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
		return createConfiguration(name, builder);
	}
	
	@Override
	protected String[] getSupportedTypes() {
		
		return new String[]{"*"};
	}
	
}