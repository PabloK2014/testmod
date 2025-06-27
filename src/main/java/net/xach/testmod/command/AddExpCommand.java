package net.xach.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xach.testmod.TestMod;

import java.util.logging.Logger;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID)
public class AddExpCommand {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("testmod")
                        .then(Commands.literal("addexp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();
                                            player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                                                cap.addExperience(amount);
                                                cap.sync(player);
                                                LOGGER.info("Added " + amount + " experience to player " + player.getName().getString() + ", new level: " + cap.getLevel() + ", new exp: " + cap.getExperience());
                                                source.sendSuccess(() -> Component.literal("Добавлено " + amount + " опыта. Уровень: " + cap.getLevel() + ", опыт: " + cap.getExperience()), false);
                                            });
                                            return 1;
                                        })
                                )
                        )
                        .requires(source -> source.hasPermission(2)) // Требуются права оператора
        );
    }
}