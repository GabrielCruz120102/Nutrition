package ca.wescook.nutrition.events;

import ca.wescook.nutrition.configs.Config;
import ca.wescook.nutrition.gui.ModGuiHandler;
import ca.wescook.nutrition.network.ModPacketHandler;
import ca.wescook.nutrition.network.PacketNutritionRequest;
import ca.wescook.nutrition.nutrition.Nutrient;
import ca.wescook.nutrition.nutrition.NutrientList;
import ca.wescook.nutrition.nutrition.NutritionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventNutritionDecay {

    private int fauxTick = 0;// This integer helps limit how often the payload fires

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent event) {

        // Only run on server
        EntityPlayer player = event.player;
        if (player.getEntityWorld().isRemote) { return; }

        // Set config values to variables
        int nutritionDecay = Config.nutritionDecay;
        int nutritionHunger = Config.nutritionHunger;

        if (fauxTick>=nutritionDecay) { // When the elapsed tick count reaches the configured value, trigger payload
            if (player.getFoodStats().getFoodLevel()<=nutritionHunger) { // When the food level of the player is below the threshold
                for (Nutrient nutrient : NutrientList.get()) { // Cycle through nutrient list
                    player.getCapability(NutritionProvider.NUTRITION_CAPABILITY, null).subtract(nutrient, 0.1F); // And update player nutrition

                    GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                    // Ensure correct GUI is open
                    if (currentScreen == null || !currentScreen.equals(ModGuiHandler.nutritionGui))
                        return;

                    ModPacketHandler.NETWORK_CHANNEL.sendToServer(new PacketNutritionRequest.Message()); // Request GUI update from server
                }
                fauxTick = 0; // If the payload has been triggered, reset tick count
            }
        }
        fauxTick++; // Gotta keep count...
    }
}
