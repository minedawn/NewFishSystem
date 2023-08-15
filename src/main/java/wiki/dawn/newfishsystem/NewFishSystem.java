package wiki.dawn.newfishsystem;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class NewFishSystem extends JavaPlugin implements Listener {

    public Map<Player, Fishing> fishingState = new HashMap<>();
    public Map<Player, Entity> fishingLoot = new HashMap<>();
    public final ConfigReader config = new ConfigReader(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("nfs").setExecutor(new Commands(this));
        getCommand("newfishsystem").setExecutor(new Commands(this));
        Metrics metrics = new Metrics(this, 19491);
        config.initialization();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        removeAllFishingPlayer();
    }

    public void removeAllFishingPlayer() {
        if (!fishingState.isEmpty()) {
            for (Map.Entry<Player, Fishing> entry : fishingState.entrySet()) {
                Fishing fishing = entry.getValue();
                fishing.stopFishingState();
            }
            fishingState.clear();
        }
        if (!fishingLoot.isEmpty()) fishingLoot.clear();
    }

    @EventHandler
    public void onCaughtFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (!checkWorld(event.getPlayer().getWorld())) {
                return;
            }
            Player player = event.getPlayer();
            if (!fishingState.containsKey(player)) {
                int size = config.fish_caught_messages.size();
                if (size > 0)
                    if (!config.fish_caught_messages.get(0).equals(""))

                        player.sendTitle(" ", config.fish_caught_messages.get(generateRandomInt(0, size - 1)), 10, 80, 10);
                Fishing fishing = new Fishing(player, this);
                fishing.enterFishingState();
                fishingState.put(player, fishing);
                Entity caughtEntity = event.getCaught();
                if (fishingLoot.containsKey(player)) {
                    fishingLoot.remove(player);
                }
                fishingLoot.put(player, caughtEntity);

                FishHook fishHook = event.getHook();
                fishHook.setApplyLure(false);
                fishHook.setMaxWaitTime(12020); //1.16+版本可用
                fishHook.setMinWaitTime(12000);
            } else {
                Fishing fishing = fishingState.get(player);
                fishing.setCs(2);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTryToReelIn(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.REEL_IN) {
            Player player = event.getPlayer();
            if (fishingState.containsKey(player)) {
                Fishing fishing = fishingState.get(player);
                fishing.setCs(2);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (fishingState.containsKey(player)) {
            Fishing fishing = fishingState.get(player);
            fishing.stopFishingState();
            fishingState.remove(player);
        }
    }

    private boolean checkWorld(World world) {
        if (config.getIsEnabledInAllWorlds()) return true;
        for (String enableWorld : config.getWorlds())
            if (enableWorld.equalsIgnoreCase(world.getName()))
                return true;
        return false;
    }

    public static int generateRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

}


