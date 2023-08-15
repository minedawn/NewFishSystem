package wiki.dawn.newfishsystem;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;

import java.util.Random;
import java.util.Map;

public class Fishing {

    private final Player player;
    private BukkitRunnable expChangeTask;
    private BukkitRunnable targetChangeTask;
    private final NewFishSystem plugin;
    private double exp = 0.5d;
    private int cs = 0;
    private BossBar bossBar;
    private int targetPosition = 18;
    private int currentPosition = 18;
    private int targetWeight = 9;
    private int currentWeight = 9;
    private int lastBarColor = 0;
    private int progress = 20;

    public Fishing(Player player, NewFishSystem plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public int getCs() {
        return cs;
    }

    public void setCs(int cs) {
        this.cs = cs;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public double getExp() {
        return exp;
    }


    public void setTargetPosition(int targetPosition) {
        this.targetPosition = targetPosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setTargetWeight(int targetWeight) {
        this.targetWeight = targetWeight;
    }

    public int getTargetWeight() {
        return targetWeight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getLastBarColor() {
        return lastBarColor;
    }

    public void setLastBarColor(int lastBarColor) {
        this.lastBarColor = lastBarColor;
    }

    public void enterFishingState() {
        bossBar = Bukkit.createBossBar("                  ┌──00%──┐                  ", BarColor.RED, BarStyle.SOLID);
        bossBar.setProgress(0.5d);//§
        bossBar.addPlayer(player);
        startExpChange();
        startTargetChange();
    }

    public void stopFishingState() {
        stopExpChange();
        stopTargetChange();
        bossBar.removeAll();
    }

    public void startExpChange() {

        expChangeTask = new BukkitRunnable() {
            @Override
            public void run() {
                changeExpPeriodically();
            }
        };
        expChangeTask.runTaskTimerAsynchronously(plugin, 0L, 2L); // 每0.1s执行一次
    }

    public void stopExpChange() {
        if (expChangeTask != null) {
            expChangeTask.cancel();
        }
    }

    private synchronized void changeExpPeriodically() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int times = getCs();
                double exp = getExp();
                if (times > 0) {
                    if (exp <= 0.96d) {
                        setExp(exp + 0.03d);
                        bossBar.setProgress(exp + 0.03d);
                    } else {
                        setExp(1);
                        bossBar.setProgress(1);
                    }
                    setCs(times - 1);
                } else {
                    if (exp >= 0.04d) {
                        setExp(exp - 0.03d);
                        bossBar.setProgress(exp - 0.03d);
                    } else {
                        setExp(0);
                        bossBar.setProgress(0);
                    }
                }

                // 将蓄力条与目标条对比
                int position1 = getCurrentPosition();
                int position2 = getTargetPosition();
                int weight1 = getCurrentWeight();
                int weight2 = getTargetWeight();
                double exp2 = getExp();
                double left = (position1 - 1) / 45d;
                double right = (position1 + weight1 + 1) / 45d;
                int lastBarColor = getLastBarColor();
                int progress = getProgress();
                int newProgress;
                if (exp2 >= left && exp2 <= right) {
                    if (progress + 1 >= 120) {
                        // 成功钓到鱼

                        if (plugin.config.getIsCustomItemsEnabled()) {

                            int randomValue = generateRandomInt2(1, plugin.config.getTotalWeight());
                            int totalWeight = 0;
                            for (Map.Entry<ItemStack, Integer> entry : plugin.config.items.entrySet()) {
                                int weight = entry.getValue();
                                totalWeight += weight;
                                if (totalWeight >= randomValue) {
                                    ItemStack itemStack = entry.getKey();
                                    player.getInventory().addItem(itemStack);
                                    break;
                                }
                            }
                        } else {
//                            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
//                            Bukkit.dispatchCommand(console, "execute as " + player.getName() + " at " + player.getName() + " run loot spawn ~ ~1 ~ fish minecraft:gameplay/fishing ~ ~1 ~ mainhand");
                            Entity caughtEntity = plugin.fishingLoot.get(player);
                            caughtEntity.teleport(player.getLocation().add(0, 1, 0));

                        }

                        //产生1~6点经验球
                        Location location = player.getLocation();
                        ExperienceOrb expOrb = location.getWorld().spawn(location, ExperienceOrb.class);
                        expOrb.setExperience(generateRandomInt2(1, 6));

                        //扣除耐久
                        ItemStack fishHook = player.getInventory().getItemInMainHand();
                        ItemMeta fishHookMeta = fishHook.getItemMeta();
                        if (fishHookMeta instanceof Damageable) {
                            Damageable fishHookDamageable = (Damageable) fishHookMeta;
                            int damage = fishHookDamageable.getDamage();
                            int enchantmentLevel = fishHook.getEnchantmentLevel(Enchantment.DURABILITY);
                            if (enchantmentLevel == 0) {
                                fishHookDamageable.setDamage(damage + 1);
                                fishHook.setItemMeta((ItemMeta) fishHookDamageable);
                            } else {
                                int random = generateRandomInt2(0, enchantmentLevel);
                                if (random == 0) {
                                    fishHookDamageable.setDamage(damage + 1);
                                    fishHook.setItemMeta((ItemMeta) fishHookDamageable);
                                }
                            }
                        }
                        int size = plugin.config.fish_success_messages.size();
                        if (size > 0)
                            if (!plugin.config.fish_success_messages.get(0).equals("")) {
                                player.sendTitle(" ", plugin.config.fish_success_messages.get(generateRandomInt2(0, size - 1)), 10, 40, 10);
                            }
                        plugin.fishingState.remove(player);
                        plugin.fishingLoot.remove(player);
                        stopFishingState();
                        return;
                    } else {
                        if (lastBarColor == 0) setBossBarColor(BarColor.GREEN);
                        newProgress = progress + 1 <= 20 ? 21 : progress + 1;
                        setProgress(newProgress);
                        setLastBarColor(1);
                    }
                } else {
                    if (progress - 1 <= 0) {
                        //进度到0，钓鱼失败
                        plugin.fishingState.remove(player);
                        int size = plugin.config.fish_failure_messages.size();
                        if (size > 0)
                            if (!plugin.config.fish_failure_messages.get(0).equals(""))
                                player.sendTitle(" ", plugin.config.fish_failure_messages.get(generateRandomInt2(0, size - 1)), 10, 40, 10);
                        plugin.fishingLoot.remove(player);
                        stopFishingState();
                        return;
                    } else {
                        if (lastBarColor == 1) setBossBarColor(BarColor.RED);
                        newProgress = progress - 1;
                        setProgress(newProgress);
                        setLastBarColor(0);
                    }
                }


                // 移动目标条
                int newPosition;
                if (position1 != position2) {
                    if (position1 > position2) newPosition = position1 - 1;
                    else newPosition = position1 + 1;
                    setCurrentPosition(newPosition);
                } else newPosition = position1;

                // 改变目标条长度
                int newWeight;
                if (weight1 != weight2) {
                    if (weight1 > weight2) newWeight = weight1 - 1;
                    else newWeight = weight1 + 1;
                    setCurrentWeight(newWeight);
                } else newWeight = weight1;
                setBossBarTitleSpace(newPosition, newWeight, Math.max(newProgress - 20, 0));
            }
        }.runTask(plugin); // 在主线程中执行
    }

    private String generateSpace(int number) {
        StringBuilder space = new StringBuilder();
        for (int i = 1; i <= number; i++) {
            space.append(" ");
        }
        return space.toString();
    }

    private String generateLine(int number, int progress) {
        StringBuilder line = new StringBuilder();
        line.append("┌");
        for (int i = 1; i <= (number - 5) / 2; i++) {
            line.append("─");
        }
        line.append(progress <= 9 ? String.format("0%d", progress) : String.valueOf(progress)).append("%");
        for (int i = 1; i <= (number - 5) / 2; i++) {
            line.append("─");
        }
        line.append("┐");
        return line.toString();
    }

    public static int generateRandomInt(int min, int max) { // 产生一个指定范围随机数
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static int generateRandomInt2(int min, int max) { // 产生一个指定范围随机数
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static int generateRandomOddInt(int min, int max) {
        Random random = new Random();
        int randomValue = random.nextInt((max - min + 2) / 2) * 2 + min;
        if (randomValue % 2 == 0) {
            randomValue--; // 如果生成的数是偶数，减一使其成为奇数
        }
        return randomValue;
    }


    private void setBossBarTitleSpace(int space, int weight, int progress) {
        bossBar.setTitle(generateSpace(space) + generateLine(weight, progress) + generateSpace(45 - weight - space));
    }

    private void setBossBarColor(BarColor barColor) {
        bossBar.setColor(barColor);
    }

    public void startTargetChange() {
        targetChangeTask = new BukkitRunnable() {
            @Override
            public void run() {
                changeTargetPeriodically();
            }
        };
        targetChangeTask.runTaskTimerAsynchronously(plugin, getRandomTicks(1, 2), getRandomTicks(2, 3)); // 每2~3s执行一次
    }

    public void stopTargetChange() {
        if (targetChangeTask != null) {
            targetChangeTask.cancel();
        }
    }

    private synchronized void changeTargetPeriodically() {
        new BukkitRunnable() {
            @Override
            public void run() {
                setTargetPosition(generateRandomInt(1, 32));
                setTargetWeight(generateRandomOddInt(7, 13));
            }
        }.runTask(plugin); // 在主线程中执行
    }


    private long getRandomTicks(int left, int right) {
        // 生成一个随机时间间隔（以tick为单位，20 tick = 1秒）
        return 20L * generateRandomInt(left, right);
    }


}
