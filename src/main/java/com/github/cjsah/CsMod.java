package com.github.cjsah;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;


public class CsMod implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistry.INSTANCE.register(false, (dispatcher) -> dispatcher.register(
				CommandManager.literal("c").requires(source -> source.hasPermissionLevel(2))
						.executes(c -> {
							changeMode(c, GameMode.SPECTATOR);
							return Command.SINGLE_SUCCESS;
						})
		));

		CommandRegistry.INSTANCE.register(false, (dispatcher) -> dispatcher.register(
				CommandManager.literal("s")
						.executes(s -> {
							changeMode(s, GameMode.SURVIVAL);
							return Command.SINGLE_SUCCESS;
						})
		));

	}

	private static void changeMode(CommandContext<ServerCommandSource> context, GameMode mode) {
		ServerPlayerEntity player;
		try {
			player = context.getSource().getPlayer();
			if (player.interactionManager.getGameMode() != mode) {
				player.setGameMode(mode);
			}
		} catch (CommandSyntaxException ignore) {}
	}
}
