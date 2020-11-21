package net.cjsah;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;


public class CsMod implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				CommandManager.literal("c").requires(source -> source.hasPermissionLevel(1))
						.executes(c -> {
							changeMode(c, GameMode.SPECTATOR);
							return  Command.SINGLE_SUCCESS;
						})
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				CommandManager.literal("s").requires(source -> source.hasPermissionLevel(1))
						.executes(s -> {
							changeMode(s, GameMode.SURVIVAL);
							return  Command.SINGLE_SUCCESS;
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
