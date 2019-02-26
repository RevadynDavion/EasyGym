package com.github.revadyndavion;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;


@Plugin(id = "easygym", name = "Easy Gym", version = "1.0", description="Gyms for pixelmon")
public class EasyGym {
	
	@Inject
	private Logger logger;
	
	@Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Successfully running EasyGym");
        
        CommandSpec gymteamSpec = CommandSpec.builder()
        		.description(Text.of("Handles Gym Team loading and clearing"))
        		.arguments(
        				GenericArguments.optional(
        						GenericArguments.onlyOne(GenericArguments.string(Text.of("mode")))
        						),
        				GenericArguments.optional(
        						GenericArguments.remainingJoinedStrings(Text.of("args"))
        						)
        				)
        		.permission("easygym.command.gymteam")
        		.executor(new GymTeam())
        		.build();
        
        CommandSpec gymsSpec = CommandSpec.builder()
        		.description(Text.of("Show available gyms"))
        		.arguments(
        				GenericArguments.optional(
        						GenericArguments.remainingJoinedStrings(Text.of("args"))
        						)
        				)
        		.permission("easygym.command.gyms")
        		.executor(new Gyms())
        		.build();
        
        Sponge.getCommandManager().register(this, gymteamSpec, "gymteam");
        Sponge.getCommandManager().register(this, gymsSpec, "gyms");
    }
}