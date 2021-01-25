package net.cjsah.csmod;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CsMod implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("cs");
	private static final File PATH = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cs.json");
	private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
	private static final JsonObject CONFIG = new JsonObject();

	@Override
	public void onInitialize() {
		this.initWhitelist();
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				CommandManager.literal("c").requires(source -> source.hasPermissionLevel(2) || this.getWhiteList().contains(source.getName()))
						.executes(commandContext -> {
							this.changeMode(commandContext, GameMode.SPECTATOR, true);
							return  Command.SINGLE_SUCCESS;
						})
		));

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				CommandManager.literal("s").requires(source -> source.hasPermissionLevel(2) || this.getWhiteList().contains(source.getName()))
						.executes(context -> {
							this.changeMode(context, GameMode.SURVIVAL, false);
							return  Command.SINGLE_SUCCESS;
						})
		));
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("cs").requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("add").then(CommandManager.argument("target", EntityArgumentType.player()).executes(commandContext -> {
					this.changeWhiteList(EntityArgumentType.getPlayer(commandContext, "target"), true);
					return Command.SINGLE_SUCCESS;
				}))).then(CommandManager.literal("remove").then(CommandManager.argument("target", EntityArgumentType.player()).executes(commandContext -> {
					this.changeWhiteList(EntityArgumentType.getPlayer(commandContext, "target"), false);
					return Command.SINGLE_SUCCESS;
				}))).then(CommandManager.literal("list").executes(commandContext -> {
					this.sendList(commandContext);
					return Command.SINGLE_SUCCESS;
				}))
		));
	}

	private void changeMode(CommandContext<ServerCommandSource> context, GameMode mode, boolean effect) {
		try {
			ServerPlayerEntity player = context.getSource().getPlayer();
			if (player.interactionManager.getGameMode() != mode) {
				player.changeGameMode(mode);
				if (effect) {
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 2147483647, 0, true, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 2147483647, 0, true, false));
				}else {
					player.removeStatusEffect(StatusEffects.NIGHT_VISION);
					player.removeStatusEffect(StatusEffects.CONDUIT_POWER);
				}
			}
		} catch (CommandSyntaxException ignore) {}
	}

	private List<String> getWhiteList() {
		List<String> list = new ArrayList<>();
		JsonArray array = CONFIG.get("whitelist").getAsJsonArray();
		for (JsonElement index : array) {
			list.add(index.getAsString());
		}
		return list;
	}

	private void sendList(CommandContext<ServerCommandSource> context) {
		try {
			ServerPlayerEntity player = context.getSource().getPlayer();
			List<String> list = getWhiteList();
			if (!list.isEmpty()) {
				this.sendMessage(player, "Whitelist :");
				for (String index : list) {
					player.sendMessage(new LiteralText("- " + index), false);
				}
			}else {
				this.sendMessage(player, "Empty list");
			}
		} catch (CommandSyntaxException ignore) {}
	}

	private void initWhitelist() {
		if (!PATH.exists() || !PATH.isFile()) {
			try (FileWriter writer = new FileWriter(PATH)) {
				CONFIG.add("whitelist", new JsonArray());
				writer.write(GSON.toJson(CONFIG));
			}catch (IOException e) {
				LOGGER.error("Failed to initialization whitelist");
				e.printStackTrace();
			}
		}else {
			try (BufferedReader reader = new BufferedReader(new FileReader(PATH))) {
				CONFIG.add("whitelist", GSON.fromJson(reader, JsonObject.class).get("whitelist"));
			}catch (IOException e) {
				LOGGER.error("Failed to load whitelist");
				e.printStackTrace();
			}
		}
	}

	private void changeWhiteList(ServerPlayerEntity player, boolean add) {
		String name = player.getEntityName();
		try (FileWriter writer = new FileWriter(PATH)) {
			List<String> list = this.getWhiteList();
			if (add) {
				if (!list.contains(name)) {
					list.add(name);
					this.sendMessage(player, "Add %s to whitelist completed");
				}else {
					this.sendMessage(player, "%s is already on the whitelist");
				}
			}else {
				if (list.contains(name)) {
					list.remove(name);
					this.sendMessage(player, "Remove %s from whitelist completed");
				}else {
					this.sendMessage(player, "%s is not on the whitelist");
				}
			}
			JsonArray jsonArray = new JsonArray();
			for (String index : list) {
				jsonArray.add(index);
			}
			CONFIG.add("whitelist", jsonArray);
			writer.write(GSON.toJson(CONFIG));
		}catch (IOException e) {
			LOGGER.error("Failed to change whitelist");
			e.printStackTrace();
		}
	}

	private void sendMessage(ServerPlayerEntity player, String str) {
		player.sendMessage(new TranslatableText("translation.test.args", new LiteralText("[cs]").formatted(Formatting.GOLD), new LiteralText(String.format(str, player.getEntityName()))), false);
	}
}
