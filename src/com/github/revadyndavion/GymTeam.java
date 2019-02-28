package com.github.revadyndavion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jpaste.pastebin.Pastebin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GymTeam implements CommandExecutor {
	
	private void sendHelp(CommandSource src) {
		src.sendMessage(Text.of(TextColors.GREEN, "GymTeam help"));
    	src.sendMessage(Text.of(TextColors.AQUA, "Usage: /gymteam mode [args]"));
    	src.sendMessage(Text.of(""));
    	src.sendMessage(Text.of(TextColors.GREEN, "Modes"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "import: /gymteam import gym pastecode teamname"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Imports a team from the pastecode into the gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "delete: /gymteam delete gym teamname"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Deletes a team from the gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "list: /gymteam list [gym]"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Provides GUI for choosing teams. Also lists either all gyms or all teams in a gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "load: /gymteam load gym teamname"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Loads a team from a gym"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "clear:  /gymteam clear"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - DANGER: CLEARS ALL POKEMON FROM PARTY"));
    	src.sendMessage(Text.of(TextColors.AQUA,  "upload:  /gymteam upload gym teamname"));
    	src.sendMessage(Text.of(TextColors.WHITE, "  - Uploads the user's party team to the gym"));
	}
	
	private Map<String, String> parseFirstLine(String firstLine) {
		
		String item = "";
		String nameAndGender = firstLine.trim();
		if (firstLine.contains(" @ ")) {
			String[] atExploded = firstLine.split("@");
			item = atExploded[atExploded.length - 1].trim();
			nameAndGender = String.join("@", Arrays.copyOfRange(atExploded, 0, atExploded.length - 1)).trim();
		}
		
		String gender = "";
		String nameAndNick = nameAndGender;
		if (nameAndGender.contains("(M)")) {
			gender = "M";
			nameAndNick = nameAndGender.replace("(M)", "").trim();
		} else if (nameAndGender.contains("(F)")) {
			gender = "F";
			nameAndNick = nameAndGender.replace("(F)", "").trim();
		}
		
		String species = "";
		String nickname = "";
		if (nameAndNick.contains("(")) {
			species = nameAndNick.substring(nameAndNick.indexOf("(")+1, nameAndNick.indexOf(")"));
			String[] nameAndNickExploded = nameAndNick.split(" ");
			nickname = nameAndNickExploded[0];
		} else {
			species = nameAndNick.trim();
		}
		
		if (species.contains("-Mega")) {
			species = nickname.replace("-Mega", "");
		}
		
		Map<String, String> outMap = new HashMap<String, String>();
		outMap.put("nickname", nickname);
		outMap.put("species", species);
		outMap.put("gender", gender);
		outMap.put("item", item);
		
		return outMap;
		
	}
	
	//Takes regex and matches it against source string. Returns only one matched string
	private String findOne(String source, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}
	
	private List<String> findAll(String source, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		List<String> matches = new ArrayList<String>();
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches;
	}
	
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<String> cmdMode = args.<String>getOne("mode");
        Optional<String> initCmdArgs = args.<String>getOne("args");
        
        String mode = "";
        if (cmdMode.isPresent()) {
        	mode = cmdMode.get();
        }
        
        String[] cmdArgs;
        if (initCmdArgs.isPresent()) {
        	cmdArgs = initCmdArgs.get().split(" ");
        } else {
        	cmdArgs = new String[0];
        }
        
        if (mode.toLowerCase().equals("import")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.import"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	
        	String gym = "", pasteCode = "", name = "";
        	try {
        		gym = cmdArgs[0];
            	pasteCode = cmdArgs[1];
            	name = String.join(" ", Arrays.copyOfRange(cmdArgs, 2, cmdArgs.length));
        	} catch (Exception e) {
        		sendHelp(src);
        		return CommandResult.success();
        	}
        	String contents = "";
        	
        	//gets text from pastebin
        	try {
        		contents = Pastebin.getContents(pasteCode);
        	} catch (Exception e) {
        		src.sendMessage(Text.of(Color.RED, "Pastebin Invalid"));
        		return CommandResult.success();
        	}
        	
        	try {
	        	//Splits up pastebin into pokemon by doublespace
	        	final String pokemonRegex = "((?:[^\\n][\\n]?)+)";
	        	final Matcher m = Pattern.compile(pokemonRegex).matcher(contents);
	        	
	        	List<String> matches = new ArrayList<>();
	        	while (m.find()) {
	        		matches.add(m.group(0));
	        	}
	        	
	        	
	        	JSONArray teamJson = new JSONArray();
	        	
	        	//Iterates over each pokemon and stores attributes in JSON
	        	matches.forEach((s) -> {
	        		
	        		//Grabs ability from line 2
	        		String abilityLine = findOne(s, "[\\n\\r].*Ability:\\s*([^\\n\\r]*)");
	        		String[] abilityExploded = abilityLine.split(":");
	        		String ability = abilityExploded[1].trim();
	        		
	        		//Grabs nature from line 4
	        		String natureLine = findOne(s, "[\\n\\r].* Nature\\s*([^\\n\\r]*)");
	        		String nature;
	        		if (natureLine.equals("")) {
	        			nature = "Serious";
	        		} else {
	        			String[] natureExploded = natureLine.split(" ");
	        			nature = natureExploded[0].trim();
	        		}
	        		
	        		//Grabs level, if not present sets default as 100
	        		String levelLine = findOne(s, "[\\n\\r].*Level:\\s*([^\\n\\r]*)");
	        		Integer level;
	        		if (levelLine.equals("")) {
	        			level = 100;
	        		} else {
	        			String[] levelExploded = levelLine.split(":");
	        			level = Integer.parseInt(levelExploded[1].trim());
	        		}
	        		
	        		//Grabs happiness, if not present sets default as 255
	        		String happinessLine = findOne(s, "[\\n\\r].*Happiness:\\s*([^\\n\\r]*)");
	        		Integer happiness;
	        		if (happinessLine.equals("")) {
	        			happiness = 255;
	        		} else {
	        			String[] happinessExploded = happinessLine.split(":");
	        			happiness = Integer.parseInt(happinessExploded[1].trim());
	        		}
	        		
	        		//If shiny line exists set to shiny
	        		String shinyLine = findOne(s, "[\\n\\r].*Shiny:\\s*([^\\n\\r]*)");
	        		Boolean shiny = !(shinyLine.equals(""));
	        		
	        		//Grabs moves from lines that contain a hyphen followed by a space
	        		List<String> moveLines = findAll(s, "[\\n\\r].*- \\s*([^\\n\\r]*)");
	        		List<String> moves = new ArrayList<String>();
	        		for (String moveLine : moveLines) {
	        			String moveStr = moveLine.substring(3);
	        			String[] moveStrExploded = moveStr.split(" ");
	        			if (moveStrExploded.length >= 2) {
	        				if ((moveStrExploded[0].toLowerCase() == "hidden") && (moveStrExploded[0].toLowerCase() == "power")) {
	        					moves.add("Hidden Power");
	        					continue;
	        				}
	        			}
	        			moves.add(moveStr.trim());
	        		}
	        		
	        		
	        		//Initializes evs to jsonObject
	        		JSONObject evJson = new JSONObject();
	        		String[] possibleEvs = {"HP", "Atk", "Def", "SpA", "SpD", "Spe"};
	        		for (String ev : possibleEvs) {
	        			evJson.put(ev, 0);
	        		}
	        		
	        		//Gets list of evs from input
	        		String evsLines = findOne(s, "[\\n\\r].*EVs: \\s*([^\\n\\r]*)");
		        	if (!(evsLines.equals(""))) {
		        		
	        			String allEvs = evsLines.split(":")[1].trim();
		        		String[] evsList = allEvs.split(" / ");
		        		
		        		//Puts evs in jsonObject
		        		for (String evStr : evsList) {
		        			String[] ev = evStr.trim().split(" ");
		        			evJson.put(ev[1], Integer.parseInt(ev[0]));
		        		}
	        		
		        	}
		        	
		        	//Initializes ivs to jsonObject
	        		JSONObject ivJson = new JSONObject();
	        		for (String iv : possibleEvs) {
	        			ivJson.put(iv, 31);
	        		}
		        	
	        		//Gets list of ivs from input
	        		String ivsLines = findOne(s, "[\\n\\r].*IVs: \\s*([^\\n\\r]*)");
		        	if(!(ivsLines.equals(""))) {
	        			String allIvs = ivsLines.split(":")[1].trim();
		        		String[] ivsList = allIvs.split(" / ");
		        		
		        		//Puts ivs in jsonObject
		        		for (String ivStr : ivsList) {
		        			String[] iv = ivStr.trim().split(" ");
		        			ivJson.put(iv[1], Integer.parseInt(iv[0]));
		        		}
		        	}
		        		
	        		Map<String, String> firstLineMap = parseFirstLine(s.split("\n")[0]);
	        		
	        		JSONObject pokemonJson = new JSONObject();
	        		pokemonJson.put("species", firstLineMap.get("species"));
	        		pokemonJson.put("nickname", firstLineMap.get("nickname"));
	        		pokemonJson.put("gender", firstLineMap.get("gender"));
	        		pokemonJson.put("item", firstLineMap.get("item"));
	        		
	        		pokemonJson.put("ability", ability);
	        		pokemonJson.put("nature", nature);
	        		pokemonJson.put("level", level);
	        		pokemonJson.put("shiny", shiny);
	        		pokemonJson.put("happiness", happiness);
	        		
	        		pokemonJson.put("moves", new JSONArray(moves));
	        		pokemonJson.put("EVs", evJson);
	        		pokemonJson.put("IVs", ivJson);
	        		
	        		teamJson.put(pokemonJson);
	        		
	        	});
	        	
	        	File file = new File("./config/easygym/" + gym.toUpperCase() + "/" + name.toUpperCase() + ".json");
	        	file.getParentFile().mkdirs();
	        	try {
	                 file.createNewFile();
	                 FileWriter writer = new FileWriter(file);
	                 writer.write(teamJson.toString(4));
	                 writer.close();
	                 src.sendMessage(Text.of(TextColors.GREEN, "GymTeam successfully imported"));
	             } catch (IOException e) {
	            	 src.sendMessage(Text.of(TextColors.RED, "Error while saving GymTeam file"));
	            	 return CommandResult.success();
	             }
        	} catch (Exception e) {
        		src.sendMessage(Text.of(TextColors.RED, "Error occurred while importing GymTeam"));
           	 	return CommandResult.success();
        	}
        
        } else if (mode.toLowerCase().equals("upload")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.upload"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	
        	if (!(src instanceof Player)) {
        		src.sendMessage(Text.of(TextColors.RED, "You must be a player to execute this command"));
        		return CommandResult.success();
        	}
        	Player player = (Player) src;
        	
        	String gym = "", name = "";
        	try {
        		gym = cmdArgs[0];
            	name = String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length));
        	} catch (Exception e) {
        		sendHelp(src);
        		return CommandResult.success();
        	}
        	
        	
        	Pokemon[] partyPokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) player).getAll();
        	JSONArray teamJson = new JSONArray();
        	
        	for (Pokemon pokemon : partyPokemon) {
        		if (pokemon == null) {
        			continue;
        		}
        		
        		JSONObject pokeJson = new JSONObject();
        		
        		pokeJson.put("species", pokemon.getSpecies().name);
        		pokeJson.put("happiness", pokemon.getFriendship());
        		pokeJson.put("level", pokemon.getLevel());
        		pokeJson.put("nickname", pokemon.getNickname());
        		pokeJson.put("ability", pokemon.getAbility().getName());
        		pokeJson.put("shiny", pokemon.isShiny());
        		pokeJson.put("nature", pokemon.getNature().name());
        		pokeJson.put("item", pokemon.getHeldItemAsItemHeld().getLocalizedName());

        		pokeJson.put("gender", "");
        		if (pokemon.getGender().equals(Gender.Female)) {
        			pokeJson.put("gender", "F");
        		} else if (pokemon.getGender().equals(Gender.Male)) {
        			pokeJson.put("gender", "M");
        		}
        		
        		Attack[] atks = pokemon.getMoveset().attacks;
        		JSONArray moveset = new JSONArray();
        		for (Attack atk : atks) {
        			moveset.put(atk.toString());
        		}
        		pokeJson.put("moves", moveset);
        		
        		JSONObject evsJson = new JSONObject();
        		EVStore evs = pokemon.getStats().evs;
        		evsJson.put("Def", evs.defence);
        		evsJson.put("SpA", evs.specialAttack);
        		evsJson.put("SpD", evs.specialDefence);
        		evsJson.put("HP", evs.hp);
        		evsJson.put("Atk", evs.attack);
        		evsJson.put("Spe", evs.speed);
        		pokeJson.put("EVs", evsJson);
        		
        		JSONObject ivsJson = new JSONObject();
        		IVStore ivs = pokemon.getStats().ivs;
        		ivsJson.put("Def", ivs.defence);
        		ivsJson.put("SpA", ivs.specialAttack);
        		ivsJson.put("SpD", ivs.specialDefence);
        		ivsJson.put("HP", ivs.hp);
        		ivsJson.put("Atk", ivs.attack);
        		ivsJson.put("Spe", ivs.speed);
        		pokeJson.put("IVs", ivsJson);
        		
        		teamJson.put(pokeJson);
        		
        	}
        	
        	File file = new File("./config/easygym/" + gym.toUpperCase() + "/" + name.toUpperCase() + ".json");
        	file.getParentFile().mkdirs();
        	try {
                 file.createNewFile();
                 FileWriter writer = new FileWriter(file);
                 writer.write(teamJson.toString(4));
                 writer.close();
                 src.sendMessage(Text.of(TextColors.GREEN, "GymTeam successfully uploaded"));
             } catch (IOException e) {
            	 src.sendMessage(Text.of(TextColors.RED, "Error while saving GymTeam file"));
            	 return CommandResult.success();
             }
        	
        } else if (mode.toLowerCase().equals("delete")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.delete"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	
        	String gym = "";
        	try {
        		gym = cmdArgs[0];
        	} catch (Exception e) {
        		sendHelp(src);
        		return CommandResult.success();
        	}
        	
        	String name = String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length));
        	String filename = "./config/easygym/" + gym.toUpperCase() + "/" + name.toUpperCase() + ".json";
        	try {
				Files.deleteIfExists(Paths.get(filename));
			} catch (IOException e) {
				src.sendMessage(Text.of(TextColors.RED, "Error occurred while deleting GymTeam"));
           	 	return CommandResult.success();
			}
        	
        	src.sendMessage(Text.of(TextColors.GREEN, "GymTeam deleted successfully"));
        	return CommandResult.success();
        	
        } else if (mode.toLowerCase().equals("list")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.list"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	
        	if (cmdArgs.length == 0) {
        		File[] directories = new File("./config/easygym").listFiles(File::isDirectory);
            	List<String> gymNames = new ArrayList<String>();
            	
            	if (directories.length > 0) {
	            	for (File file : directories) {
	            		gymNames.add(file.getName());
	            	}
            	}
            	
            	src.sendMessage(Text.of(TextColors.GREEN, "----------------- Gym Teams -----------------"));
            	if (gymNames.size() > 0) {
            		for (String gymName : gymNames) {
                		src.sendMessage(
                				Text.builder(gymName).color(TextColors.AQUA)
                				.onClick(TextActions.runCommand("/gymteam list " + gymName))
                				.onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to view teams for this gym")))
                				.build()
                			);
                	}
            	} else {
            		src.sendMessage(Text.of(TextColors.RED, "No teams for any gyms have been imported"));
            	}
        	} else {
        		String gym = cmdArgs[0];
        		File[] teams = new File("./config/easygym/"+gym).listFiles();
        		List<String> teamNames = new ArrayList<String>();
        		
        		for (File file : teams) {
            		teamNames.add(file.getName().substring(0, file.getName().lastIndexOf('.')));
            	}
        		
        		src.sendMessage(Text.of(TextColors.GREEN, "----------------- " + gym.toUpperCase() + " Teams -----------------"));
        		if (teamNames.size() > 0) {
            		for (String teamName : teamNames) {
                		src.sendMessage(
                				Text.builder(teamName).color(TextColors.AQUA)
                				.onClick(TextActions.runCommand("/gymteam load " + gym + " " + teamName))
                				.onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to load this team")))
                				.build()
                			);
                	}
            	} else {
            		src.sendMessage(Text.of(TextColors.RED, "No teams for this gym have been imported"));
            	}
        	}
        	
        	
        } else if (mode.toLowerCase().equals("load")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.load"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	if (src instanceof Player) {
        		Player player = (Player) src;
	        	String gym = "",
	        		   name = "";
	        	try {
	        		gym = cmdArgs[0];
	        		name = String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length));
	        	} catch (Exception e) {
	        		sendHelp(src);
	        		return CommandResult.success();
	        	}
	        	String filename = "./config/easygym/" + gym.toUpperCase() + "/" + name.toUpperCase() + ".json";
	        	
	        	String content = "";
	        	try {
	        		content = new String(Files.readAllBytes(Paths.get(filename)));
	        	} catch (Exception e) {
	        		src.sendMessage(Text.of(TextColors.RED, "Error occurred while loading gymteam file"));
	        		return CommandResult.success();
	        	}
	        	
	        	
	        	PlayerPartyStorage party = Pixelmon.storageManager.getParty((EntityPlayerMP) player);
	        	
	        	Pokemon[] partyPokemon = party.getAll();
	        	for (Pokemon pokemon : partyPokemon) {
	        		if (!(pokemon == null)) {
	        			Pixelmon.storageManager.getPCForPlayer((EntityPlayerMP) player).add(pokemon);
	        			src.sendMessage(Text.of(TextColors.GRAY, pokemon.getSpecies().name + " has been moved to your PC"));
	        		}
		        }
	        	
	        	int[] slots = {0, 1, 2, 3, 4, 5};
        		for (int slot : slots) {
        			party.set(slot, null);
        		}
	        	
	        	
	        	JSONArray teamJson = new JSONArray(content);
	        	
	        	for (Integer i = 0; i < teamJson.length(); i++) {
	        		JSONObject pokemonJson = (JSONObject) teamJson.get(i);
	        		EnumSpecies species = EnumSpecies.getFromNameAnyCase((String) pokemonJson.get("species"));
	        		Pokemon pokemon = null;
	        		try {
	        			pokemon = Pixelmon.pokemonFactory.create(species);
	        		} catch (Exception e) {
	        			src.sendMessage(Text.of(TextColors.RED, pokemonJson.getString("species") + " is an invalid species"));
	        			continue;
	        		}
	        		
	        		pokemon.setNature(EnumNature.natureFromString((String) pokemonJson.get("nature")));
	        		pokemon.setLevel((int) pokemonJson.get("level"));
	        		pokemon.setFriendship((int) pokemonJson.get("happiness"));
	        		pokemon.setShiny((boolean) pokemonJson.get("shiny"));
	        		pokemon.setNickname((String) pokemonJson.get("nickname"));
	        		pokemon.setAbility((String) pokemonJson.get("ability"));
	        		
	        		Item item = (Item) PixelmonItemsHeld.getHeldItem(pokemonJson.getString("item").toLowerCase());
	        		pokemon.setHeldItem(new ItemStack(item));
	        		
	        		JSONObject evJson = (JSONObject) pokemonJson.get("EVs");
	        		JSONObject ivJson = (JSONObject) pokemonJson.get("IVs");
	        		
	        		pokemon.setGrowth(EnumGrowth.Ordinary);
	        		
	        		String jsonGender = (String) pokemonJson.get("gender");
	        		Gender gender;
	        		if (jsonGender.equals("M")) {
	        			gender = Gender.Male;
	        		} else if (jsonGender.equals("F")) {
	        			gender = Gender.Female;
	        		} else {
	        			gender = Gender.getRandomGender(species.getBaseStats());
	        		}
	        		pokemon.setGender(gender);

	        		final Moveset moveset = pokemon.getMoveset();
	        		
	        		JSONArray moveJson = (JSONArray) pokemonJson.get("moves");

	        		for (int j = 0; j < moveJson.length(); j++) {
	        			String atkStr = moveJson.getString(j).trim();
	        			if (Attack.hasAttack(atkStr)) {
	        				Attack atk = null;
	        				atk = new Attack(atkStr);
		        			moveset.set(j, atk);
	        			} else {
	        				src.sendMessage(Text.of(TextColors.RED, atkStr + " is not a valid move name"));
	        			}
	        		}
	        		
	        		pokemon.setTemporaryMoveset(moveset);
	        		pokemon.setDoesLevel(false);
	        		
	        		
	        		final EVStore evStore = pokemon.getStats().evs;
	        		evStore.set(StatsType.HP, (int) evJson.get("HP"));
	        		evStore.set(StatsType.Attack, (int) evJson.get("Atk"));
	        		evStore.set(StatsType.Defence, (int) evJson.get("Def"));
	        		evStore.set(StatsType.SpecialAttack, (int) evJson.get("SpA"));
	        		evStore.set(StatsType.SpecialDefence, (int) evJson.get("SpD"));
	        		evStore.set(StatsType.Speed, (int) evJson.get("Spe"));
	        		evStore.markDirty();
	        		
	        		final IVStore ivStore = pokemon.getStats().ivs;
	        		ivStore.set(StatsType.HP, (int) ivJson.get("HP"));
	        		ivStore.set(StatsType.Attack, (int) ivJson.get("Atk"));
	        		ivStore.set(StatsType.Defence, (int) ivJson.get("Def"));
	        		ivStore.set(StatsType.SpecialAttack, (int) ivJson.get("SpA"));
	        		ivStore.set(StatsType.SpecialDefence, (int) ivJson.get("SpD"));
	        		ivStore.set(StatsType.Speed, (int) ivJson.get("Spe"));
	        		ivStore.markDirty();
	        		
	        		pokemon.getStats().setLevelStats(pokemon.getNature(), pokemon.getBaseStats(), 100);
	        		pokemon.setHealthPercentage(100);
	        		party.set(i, pokemon);
	        	}
	        	
        	} else {
        		src.sendMessage(Text.of(TextColors.RED, "This can only be used by players."));
        	}
        } else if (mode.toLowerCase().equals("clear")) {
        	if (!(src.hasPermission("easygym.actions.gymteam.clear"))) {
        		src.sendMessage(Text.of(TextColors.RED, "You do not have permission for this"));
        		return CommandResult.success();
        	}
        	if (src instanceof Player) {
        		EntityPlayerMP player = (EntityPlayerMP) src;
        		int[] slots = {0, 1, 2, 3, 4, 5};
        		for (int slot : slots) {
        			Pixelmon.storageManager.getParty(player).set(slot, null);
        		}
        	} else {
        		src.sendMessage(Text.of(TextColors.RED, "This can only be used by players."));
        	}
        } else {
        	sendHelp(src);
        }
        
        return CommandResult.success();
    }

}
