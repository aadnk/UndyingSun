package com.comphenix.undyingsun;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;

import com.comphenix.undyingsun.CommandTimeParser.TimeOfDay;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Represenst the undying sun command.
 * @author Kristian
 */
public class CommandUndying implements TabExecutor {
	public static final String NAME = "undying";
	
	// The configuration
	private UndyingConfiguration config;
	
	// The sub commands
	private enum SubCommand {
		RELOAD("reload"),
		SERVER_TIME("servertime"),
		CLIENT_TIME("clienttime"),
		SERVER_SPEED("serverspeed"),
		CLIENT_SPEED("clientspeed");
		
		private final String commandName;
	
		private SubCommand(String commandName) {
			this.commandName = commandName;
		}
		
		/**
		 * Retrieve the command name of this subcommand.
		 * @return The command name.
		 */
		public String getCommandName() {
			return commandName;
		}
		
		@Override
		public String toString() {
			return commandName;
		}
		
		/**
		 * Attempt to parse a given subcommand.
		 * @param text - the text to parse.
		 * @return The command, or NULL if not found.
		 */
		public static SubCommand findExact(String text) {
			for (SubCommand cmd : values()) {
				if (cmd.getCommandName().equalsIgnoreCase(text)) {
					return cmd;
				}
			}
			return null;
		}
	}
	
	/**
	 * Construct a new command handler.
	 * @param config - the configuration.
	 */
	public CommandUndying(UndyingConfiguration config) {
		this.config = config;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Ensure we are dealing with our root command
		if (NAME.equals(command.getName())) { 
			if (args.length > 0) {
				handleCommand(sender, args[0], subList(args, 1));
			} else {
				// Print every valid sub command
				sender.sendMessage(ChatColor.GOLD + "Possible sub-commands: " + 
					Strings.join(onTabComplete(sender, command, label, args), ", "));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Handle a sub command.
	 * @param sender - the sender.
	 * @param commandName - the sub command name.
	 * @param args - the arguments.
	 */
	private void handleCommand(CommandSender sender, String commandName, List<String> args) {
		final SubCommand subCommand = SubCommand.findExact(commandName);
		
		switch (subCommand) {
			case RELOAD:
				config.reloadConfig();
				break;
			case CLIENT_TIME:
			case SERVER_TIME:
				handleTime(sender, subCommand, args);
				break;
			case CLIENT_SPEED:
			case SERVER_SPEED:
				handleSpeed(sender, subCommand, args);
				break;
			default:
				sender.sendMessage(ChatColor.RED + "No sub-command with the name " + commandName);
		}
	}	
	
	/**
	 * Handle the server time or the client time subcommand.
	 * @param sender - the sender.
	 * @param command - the sub-command.
	 * @param args - the arguments.
	 */
	private void handleTime(CommandSender sender, SubCommand command, List<String> args) {
		boolean server = command == SubCommand.SERVER_TIME;
		
		if (args.size() == 0) {
			sender.sendMessage("Fixed time: " + TimeOfDay.toTimeString(
					server ? config.getServerTime() : config.getClientTime() 
			));
			
		} else if (args.size() == 1) {
			TimeOfDay time = CommandTimeParser.parse(args.get(0));
			
			// Update configuration
			if (server)
				config.setServerTime(time);
			else
				config.setClientTime(time);
			config.saveConfig();
			
			// Notify sender
			sender.sendMessage(ChatColor.GOLD + "New fixed " + 
					(server ? "server" : "client") + " time: " + time.getAlias());
			
		} else {
			sender.sendMessage(ChatColor.RED + "Too many arguments.");
		}
	}
	
	/**
	 * Handle the server speed or the client speed subcommand.
	 * @param sender - the sender.
	 * @param command - the sub-command.
	 * @param args - the arguments.
	 */
	private void handleSpeed(CommandSender sender, SubCommand command, List<String> args) {
		boolean server = command == SubCommand.SERVER_SPEED;
		
		if (args.size() == 0) {
			sender.sendMessage("Current speed: " + 
				(server ? config.getServerSpeed() : config.getClientSpeed()) 
			);
			
		} else if (args.size() == 1) {
			try {
				double speed = Double.parseDouble(args.get(0));
				
				if (server)
					config.setServerSpeed(speed);
				else
					config.setClientSpeed(speed);
				config.saveConfig();
				
				// Notify sender
				sender.sendMessage(ChatColor.GOLD + "New " + 
						(server ? "server" : "client") + " speed: " + speed);
				
			} catch (NumberFormatException e) {
				// Incorrect input
				sender.sendMessage(ChatColor.RED + args.get(0) + " is not a number.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Too many arguments.");
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// Only respond to the expected command
		if (NAME.equals(command.getName()) && args.length > 0) {
			String text = args[0];
			SubCommand exact = SubCommand.findExact(text);
			
			if (exact == SubCommand.SERVER_TIME || exact == SubCommand.CLIENT_TIME) {
				// Note that this doesn't include numbers
				return handleTimeComplete(subList(args, 1));
			} else if (exact == null) {
				return findByPrefix(asStrings(SubCommand.values()), text);
			}
		}
		return null;
	}
	
	/**
	 * Determine every possible time input at argument 0.
	 * @param args - the current arguments.
	 * @return Every possible time input, or NULL if none are possible.
	 */
	private List<String> handleTimeComplete(List<String> args) {
		if (args.size() == 1) {
			String text = args.get(0);
			TimeOfDay exact = TimeOfDay.fromAlias(text);

			if (exact == null) {
				return findByPrefix(asStrings(
					Iterables.concat(TimeOfDay.VALUES, CommandTimeParser.DISABLED)),
					text
				);
			}
		}
		return null;
	}
		
	/**
	 * Filter the given input iterable by elements matching a specific prefix, and collect them into a list.
	 * @param prefix - the prefix.
	 * @return A list of every matching element, or NULL.
	 */
	public static List<String> findByPrefix(Iterable<String> input, String prefix) {
		List<String> result = null;
		
		for (String entry : input) {
			if (entry.startsWith(prefix)) {
				if (result == null)
					result = Lists.newArrayList();
				result.add(entry);
			}
		}
		return result;
	}
	
	/**
	 * Retrieve the string value of all the entries in the array.
	 * @param array - the array to convert.
	 * @return The string value of each entry.
	 */
	private Iterable<String> asStrings(Object[] array) {
		return asStrings(Arrays.asList(array));
	}
	
	/**
	 * Retrieve the string value of all the entries in the iterable.
	 * @param iterable - the iterable to convert.
	 * @return The string value of each entry.
	 */
	private Iterable<String> asStrings(Iterable<? extends Object> iterable) {
		return Iterables.transform(iterable, Functions.toStringFunction());
	}
	
	/**
	 * Retrieve a view of the given array as a list.
	 * @param input - the input array.
	 * @param beginIndex - the starting index (inclusive) or the list.
	 * @return The resulting view.
	 */
	private List<String> subList(String[] input, int beginIndex) {
		if (input.length < beginIndex)
			return Collections.emptyList();
		return Arrays.asList(input).subList(beginIndex, input.length);
	}
}
