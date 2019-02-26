package com.github.revadyndavion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.Page.PageBuilder;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.CommandAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.github.revadyndavion.enums.EnumBadgeItem;
import com.github.revadyndavion.enums.EnumTextColor;

import net.minecraft.item.Item;

final class SortByQuantity implements Comparator<ItemStack> {

	@Override
	public int compare(ItemStack item1, ItemStack item2) {
		return item1.getQuantity() - item2.getQuantity();
	}
	
}

public class Gyms implements CommandExecutor {
	
	private void sendHelp(CommandSource src) {
		src.sendMessage(Text.of(TextColors.GREEN, "Gyms help"));
    	src.sendMessage(Text.of(TextColors.AQUA, "Usage: /gyms [mode] [args]"));
    	src.sendMessage(Text.of(""));
    	src.sendMessage(Text.of(TextColors.GREEN, "Modes"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "none: /gyms"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Shows gym GUI"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "add: /gyms add type badge color [lvlcap] [priority]"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Adds a gym of given type, badge, and badge color"));
    	src.sendMessage(Text.of(TextColors.WHITE, "    User can optionally supply a level cap or priority,"));
    	src.sendMessage(Text.of(TextColors.WHITE, "    A lower priority means the gym will appear first."));
    	src.sendMessage(Text.of(TextColors.AQUA,  "leader add: /gyms leader add gym gymleader"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Adds gymleader as a leader to the gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "leader remove: /gyms leader remove gym gymleader"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Adds gymleader as a leader to the gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "reward add: /gyms reward add gym"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Adds items in hand as reward for beating gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "reward clear: /gyms reward clear gym"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Clears all rewards from gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "givebadge: /gyms givebadge gym player"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Gives badge and rewards to player."));
	}
	
	private void saveJson(String filename, JSONObject json, CommandSource src) {
		try (FileWriter file = new FileWriter(filename)) {
    		file.write(json.toString(4));
    		file.close();
    	} catch (IOException e) {
    		src.sendMessage(Text.of(TextColors.RED, "Error while saving gym file"));
			e.printStackTrace();
		}
	}
	
	private JSONObject readJson(String filename, CommandSource src) {
    	File f = new File(filename);

    	String content = "{}";
      	if(f.exists()){
      		try {
				content = FileUtils.readFileToString(f, Charset.defaultCharset());
			} catch (IOException e) {
				src.sendMessage(Text.of(TextColors.RED, "Error while reading gym file"));
				e.printStackTrace();
			}
      	}
      	return new JSONObject(content);
	}
	
	private ArrayList<ItemStack> parseGymFile(String filename, Player plr) {
		ArrayList<ItemStack> badgeItemStacks = new ArrayList<ItemStack>();
		File f = new File(filename);
		
		ItemStack defaultItemStack = ItemStack.builder()
   				.itemType(ItemTypes.BARRIER)
   				.build();
		
		if (f.exists()) {
       		String content = "{}";
			try {
				content = FileUtils.readFileToString(f, Charset.defaultCharset());
			} catch (IOException e) {
				plr.sendMessage(Text.of(TextColors.RED, "Error occurred while reading the gyms file"));
				e.printStackTrace();
				badgeItemStacks.add(defaultItemStack);
				return badgeItemStacks;
			}
       		
			JSONObject gymsJson = new JSONObject(content);
       		Iterator<String> keys = gymsJson.keys();
       		
       		while (keys.hasNext()) {
       			String key = keys.next();
       			JSONObject metadata = (JSONObject) gymsJson.get(key);
       			String badgeType = metadata.getString("badge");
       			try {
       				Item badge = EnumBadgeItem.valueOf(badgeType.toUpperCase()).get();
       				ItemStack badgeStack = ItemStack.builder()
           					.itemType((ItemType) badge)
           					.quantity(metadata.getInt("priority"))
           					.build();
       				badgeStack.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.BOLD, TextStyles.ITALIC, EnumTextColor.valueOf(metadata.getString("color").toUpperCase()).get(), key.toUpperCase()));
       				
       				ArrayList<Boolean> isOnline = new ArrayList<Boolean>();
       				
       				ArrayList<Text> itemLore = new ArrayList<Text>();
       				itemLore.add(Text.of(TextColors.WHITE, "Max lvl: " + metadata.get("level")));
       				metadata.getJSONArray("leaders").forEach((leader) -> {
       					if(Sponge.getServer().getPlayer((String) leader).isPresent()) {
       						itemLore.add(Text.of(TextStyles.RESET, TextColors.GREEN, " - " + leader));
       						isOnline.add(true);
       					} else {
       						itemLore.add(Text.of(TextStyles.RESET, TextColors.DARK_RED, " - " + leader));
       					}
       				});
       				if (itemLore.size() == 1) {
       					itemLore.add(Text.of(TextColors.RED, "No leaders for this gym have been set"));
       				}
       				if (isOnline.size() > 0) {
       					itemLore.add(Text.of(TextColors.GREEN, "Leaders for this gym are online"));
       				} else {
       					itemLore.add(Text.of(TextColors.RED, "No leaders for this gym are online"));
       				}
       				badgeStack.offer(Keys.ITEM_LORE, itemLore);
       				
       				
       				badgeItemStacks.add(badgeStack);
       			} catch (Exception e) {
       				e.printStackTrace();
       				plr.sendMessage(Text.of(TextColors.RED, badgeType + " is an invalid badge"));
       			}
       			
       		}
       		
       		Collections.sort(badgeItemStacks, new SortByQuantity());
       		
       		ArrayList<ItemStack> sortedStacks = new ArrayList<ItemStack>();
       		for (ItemStack stack: badgeItemStacks) {
       			stack.setQuantity(1);
       			sortedStacks.add(stack);
       		}
       		
       		return sortedStacks;
       	} else {
       		badgeItemStacks.add(defaultItemStack);
       		return badgeItemStacks;
       	}
	}
	
	@Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> initCmdArgs = args.<String>getOne("args");
		String[] cmdArgs;
        if (initCmdArgs.isPresent()) {
        	cmdArgs = initCmdArgs.get().split(" ");
        } else {
        	cmdArgs = new String[] {""};
        }
		
        if (src instanceof Player) {
        	Player plr = (Player) src;
        	String mode = "";
        	try {
        		mode = cmdArgs[0].toLowerCase();
        	} catch (Exception e) {
        		sendHelp(src);
        		return CommandResult.success();
        	}
        	
	        if (mode.equals("")) {	
		        StateContainer container = new StateContainer();
		        
		        String filename = "./config/easygym/gyms.json";
		        ArrayList<ItemStack> badgeStacks = parseGymFile(filename, plr);
		        
		       	PageBuilder builder = Page.builder()
		       			.setUpdatable(false)
		       			.setInventoryDimension(new InventoryDimension(9, Math.floorDiv(badgeStacks.size(), 9) + 1))
		       			.setTitle(Text.of(TextColors.AQUA, "EasyGym Menu - Plugin Created by Appelsin"));
		       			
		       	badgeStacks.forEach((badgeStack) -> {
		       		String cmd = "say " + badgeStack.get(Keys.DISPLAY_NAME).get().toPlain().toLowerCase();
		       		builder.addElement(new ActionableElement(new CommandAction(container, ActionType.NORMAL, "gymmenu", cmd), badgeStack));
		       	});
		       	
		       	Page page = builder.build("gymmenu");
		       	
		       	container.addState(page);
	        	container.launchFor(plr);
	        } else if (mode.equals("add")) {
	        	if (!(src.hasPermission("easygym.actions.gyms.add"))) {
	        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
	        		return CommandResult.success();
	        	}
	        	String type = "",
	        		   badge = "",
	        		   color = "";
	        	try {
		        	type = cmdArgs[1];
		        	badge = cmdArgs[2];
		        	color = cmdArgs[3];
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	
	        	int levelCap = 100;
	        	int priority = 0;
	        	try {
	        		levelCap = Integer.parseInt(cmdArgs[4]);
	        		priority = Integer.parseInt(cmdArgs[5]);
	        	} catch (Exception e) {}
	        	
	        	new File("./config/easygym/" + type.toUpperCase()).mkdirs();
	        	
	        	String filename = "./config/easygym/gyms.json";
	        	
	        	JSONObject gymsJson = readJson(filename, src);
	        	JSONObject gymMetadata = new JSONObject();
	        	JSONArray gymLeaders = new JSONArray();
	        	JSONArray rewards = new JSONArray();
	        	
	        	gymMetadata.put("badge", badge);
	        	gymMetadata.put("leaders", gymLeaders);
	        	gymMetadata.put("level", levelCap);
	        	gymMetadata.put("color", color);
	        	gymMetadata.put("rewards", rewards);
	        	gymMetadata.put("priority", priority);
	        	
	        	gymsJson.put(type, gymMetadata);
	        	
	        	saveJson(filename, gymsJson, src);
	        	
	        	src.sendMessage(Text.of(TextColors.GREEN, "Gym added"));
	        } else if (mode.equals("delete")) {
	        	if (!(src.hasPermission("easygym.actions.gyms.delete"))) {
	        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
	        		return CommandResult.success();
	        	}
	        	String type = "";
	        	try {
	        		type = cmdArgs[1];
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	try {
					FileUtils.deleteDirectory(new File("./config/easygym/" + type.toUpperCase()));
				} catch (IOException e) {
					src.sendMessage(Text.of(TextColors.RED, "Error occured while deleting the gym"));
					return CommandResult.success();
				}
	        	
	        	String filename = "./config/easygym/gyms.json";
	        	try {
	        		JSONObject gymsJson = readJson(filename, src);
	        		gymsJson.remove(type);
	        		saveJson(filename, gymsJson, src);
	        	} catch (Exception e) {
	        		src.sendMessage(Text.of(TextColors.RED, "There is no " + type + " gym to delete"));
	        		return CommandResult.success();
	        	}
	        		        	
	        	src.sendMessage(Text.of(TextColors.GREEN, "Successfully deleted gym"));
	        } else if (mode.equals("leader")) {
	        	if (!(src.hasPermission("easygym.actions.gyms.leader"))) {
	        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
	        		return CommandResult.success();
	        	}
	        	String leaderMode = "",
	        		   gym = "",
	        		   player = "";
	        	try {
	        		leaderMode = cmdArgs[1].toLowerCase();
	        		gym = cmdArgs[2];
	        		player = cmdArgs[3];
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	
	        	if (leaderMode.equals("add")) {
	        		
	        		String filename = "./config/easygym/gyms.json";
	        		JSONObject gymsJson = readJson(filename, src);
	        		try {
	        			JSONObject metadata = (JSONObject) gymsJson.get(gym);
	        			JSONArray leaders = metadata.getJSONArray("leaders");
	        			
	        			boolean found = false;
	        			for (int i = 0; i < leaders.length(); i++) {
	        			    if (leaders.getString(i).equals(player)) {
	        			        found = true;
	        			    }
	        			}
	        			
	        			if (!(found)) {
	        				leaders.put(player);
	        				metadata.put("leaders", leaders);
	        				gymsJson.put(gym, metadata);
	        				saveJson(filename, gymsJson, src);
	        				src.sendMessage(Text.of(TextColors.GREEN, "Player added as a gym leader"));
	        				return CommandResult.success();
	        			} else {
	        				src.sendMessage(Text.of(TextColors.RED, "Player already a leader of this gym"));
	        				return CommandResult.success();
	        			}
	        		} catch (Exception e) {
	        			e.printStackTrace();
	        			src.sendMessage(Text.of(TextColors.RED, "Gym does not exist"));
	        			return CommandResult.success();
	        		}
			      	
	        	} else if (leaderMode.equals("remove")) {
	        		
	        		String filename = "./config/easygym/gyms.json";
	        		JSONObject gymsJson = readJson(filename, src);
	        		JSONObject metadata = new JSONObject();
	        		try {
	        			metadata = gymsJson.getJSONObject(gym);
	        		} catch (Exception e) {
	        			src.sendMessage(Text.of(TextColors.RED, "This gym does not exist"));
	        			return CommandResult.success();
	        		}
	        		JSONArray leaders = metadata.getJSONArray("leaders");
	        		
	        		boolean found = false;
	        		int j = 0;
        			for (int i = 0; i < leaders.length(); i++) {
        			    if (leaders.getString(i).equals(player)) {
        			        found = true;
        			        j = i;
        			    }
        			}
	        		
        			if (found) {
        				leaders.remove(j);
        				metadata.put("leaders", leaders);
        				gymsJson.put(gym, metadata);
        				saveJson(filename, gymsJson, src);
        				src.sendMessage(Text.of(TextColors.GREEN, "Player removed successfully"));
        				return CommandResult.success();
        			} else {
        				src.sendMessage(Text.of(TextColors.RED, "Player is not a leader of this gym"));
        				return CommandResult.success();
        			}
        			
	        	}
	        } else if (mode.equals("reward")) {
	        	if (!(src.hasPermission("easygym.actions.gyms.reward"))) {
	        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
	        		return CommandResult.success();
	        	}
	        	String rewardMode = "",
	        		   gym = "";
	        	try {
	        		rewardMode = cmdArgs[1].toLowerCase();
	        		gym = cmdArgs[2];
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	
	        	
	        	if (rewardMode.equals("add")) {
	        		
	        		String filename = "./config/easygym/gyms.json";
	        		JSONObject gymsJson = readJson(filename, src);
	        		JSONObject metadata = new JSONObject();
	        		try {
	        			metadata = gymsJson.getJSONObject(gym);
	        		} catch (Exception e) {
	        			src.sendMessage(Text.of(TextColors.RED, "This gym does not exist"));
	        			return CommandResult.success();
	        		}
	        		JSONArray rewards = metadata.getJSONArray("rewards");
	        		
	        		DataView reward = plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer();
	        		try {
						JSONObject rewardJson = new JSONObject(DataFormats.JSON.write(reward));
						rewards.put(rewardJson);
						metadata.put("rewards", rewards);
						gymsJson.put(gym, metadata);
						saveJson(filename, gymsJson, src);
						src.sendMessage(Text.of(TextColors.GREEN, "Reward added"));
					} catch (JSONException | IOException e) {
						src.sendMessage(Text.of(TextColors.RED, "Reward could not be added"));
						e.printStackTrace();
					}
	        	} else if (rewardMode.equals("clear")) {
	        		
	        		String filename = "./config/easygym/gyms.json";
	        		JSONObject gymsJson = readJson(filename, src);
	        		JSONObject metadata = new JSONObject();
	        		try {
	        			metadata = gymsJson.getJSONObject(gym);
	        		} catch (Exception e) {
	        			src.sendMessage(Text.of(TextColors.RED, "This gym does not exist"));
	        			return CommandResult.success();
	        		}
	        		metadata.put("rewards", new JSONArray());
	        		gymsJson.put(gym, metadata);
	        		saveJson(filename, gymsJson, src);
	        		src.sendMessage(Text.of(TextColors.GREEN, "Rewards cleared"));
	        	}
	        } else if (mode.equals("givebadge")) {
	        	if (!(src.hasPermission("easygym.actions.gyms.givebadge"))) {
	        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
	        		return CommandResult.success();
	        	}
	        	String gym = "",
	        		   playerStr = "";
	        	try {
	        		gym = cmdArgs[1];
	        		playerStr = cmdArgs[2];
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	
	        	final String player = playerStr;
	        	
	        	String filename = "./config/easygym/gyms.json";
        		JSONObject gymsJson = readJson(filename, src);
        		JSONObject metadata = new JSONObject();
        		try {
        			metadata = gymsJson.getJSONObject(gym);
        		} catch (Exception e) {
        			src.sendMessage(Text.of(TextColors.RED, "This gym does not exist"));
        			return CommandResult.success();
        		}
        		JSONArray leaders = metadata.getJSONArray("leaders");
        				
        		boolean found = false;
    			for (int i = 0; i < leaders.length(); i++) {
    			    if (leaders.getString(i).equals(player)) {
    			        found = true;
    			    }
    			}
    			
    			if (!(found)) {
    				src.sendMessage(Text.of(TextColors.RED, "You are not a leader of this gym"));
    				return CommandResult.success();
    			}
    			
    			String badgeType = metadata.getString("badge");
    			Item badge = EnumBadgeItem.valueOf(badgeType.toUpperCase()).get();
    			ItemStack badgeStack = ItemStack.builder()
    					.itemType((ItemType) badge)
    					.build();
    			badgeStack.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.BOLD, TextStyles.ITALIC, EnumTextColor.valueOf(metadata.getString("color").toUpperCase()).get(), badgeType.toUpperCase() + " BADGE"));
	        
    			ArrayList<Text> itemLore = new ArrayList<Text>();
    			itemLore.add(Text.of(TextColors.DARK_PURPLE, "A badge that commemorates " + src.getName() + "'s victory"));
    			itemLore.add(Text.of(TextColors.DARK_PURPLE, "over the " + gym.toUpperCase() + " gym."));
    			badgeStack.offer(Keys.ITEM_LORE, itemLore);
    			Sponge.getServer().getPlayer(player).get().getInventory().offer(badgeStack);
    			Sponge.getServer().getBroadcastChannel().send(Text.of(TextStyles.BOLD, TextColors.WHITE, "Congratulations to ", TextColors.LIGHT_PURPLE, player.toUpperCase(),
    					TextColors.WHITE, " for beating the ", TextColors.LIGHT_PURPLE, gym.toUpperCase(), TextColors.WHITE, " gym!"));
    			
    			JSONArray rewards = metadata.getJSONArray("rewards");
    			rewards.forEach((reward) -> {
    				try {
						DataView container = DataFormats.JSON.read(((JSONObject) reward).toString());
						ItemStack rewardStack = Sponge.getDataManager().deserialize(ItemStack.class, container).get();
						Sponge.getServer().getPlayer(player).get().getInventory().offer(rewardStack);
					} catch (InvalidDataException | IOException e) {
						src.sendMessage(Text.of(TextColors.RED, "Error occurred while retrieving rewards"));
						e.printStackTrace();
					}
    			});
    			
	        } else {
	        	sendHelp(src);
	        }
        } else {
        	src.sendMessage(Text.of(TextColors.RED, "Only players can use this command"));
        }
        
		return CommandResult.success();
	}
}
