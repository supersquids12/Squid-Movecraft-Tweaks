package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.api.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CruiseCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("status")){
            return false;
        }
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("You must be a player to call status");
            return true;
        }
        Player player = (Player) commandSender;



if (!player.hasPermission("movecraft.commands") || !player.hasPermission("movecraft.commands.status")) {
            player.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;

final Craft craft = CraftManager.getInstance().getCraftByPlayerName(player.getName());

if (craft == null) {
                player.sendMessage(I18nSupport.getInternationalisedString("You must be piloting a craft"));
                return true;
}


// grabbed and edited the status algorithms and such from blockTranslationCommand
// class. replaced all “signText” variables with “statusText” and discarded
// the sign column and line statements
int fuel = 0;
            int totalBlocks = 0;
            HashMap<Integer, Integer> foundBlocks = new HashMap<>();
            for (MovecraftLocation ml : craft.getBlockList()) {
                Integer blockID = craft.getW().getBlockAt(ml.getX(), ml.getY(), ml.getZ()).getTypeId();

                if (foundBlocks.containsKey(blockID)) {
                    Integer count = foundBlocks.get(blockID);
                    if (count == null) {
                        foundBlocks.put(blockID, 1);
                    } else {
                        foundBlocks.put(blockID, count + 1);
                    }
                } else {
                    foundBlocks.put(blockID, 1);
                }

                if (blockID == 61) {
                    InventoryHolder inventoryHolder = (InventoryHolder) ml.toBukkit(craft.getW()).getBlock().getState();
                    if (inventoryHolder.getInventory().contains(263)
                            || inventoryHolder.getInventory().contains(173)) {
                        ItemStack[] istack = inventoryHolder.getInventory().getContents();
                        for (ItemStack i : istack) {
                            if (i != null) {
                                if (i.getTypeId() == 263) {
                                    fuel += i.getAmount() * 8;
                                }
                                if (i.getTypeId() == 173) {
                                    fuel += i.getAmount() * 80;
                                }
                            }
                        }
                    }
                }
                if (blockID != 0) {
                    totalBlocks++;
                }
            }

            for (ArrayList<Integer> alFlyBlockID : craft.getType().getFlyBlocks().keySet()) {
                int flyBlockID = alFlyBlockID.get(0);
                Double minimum = craft.getType().getFlyBlocks().get(alFlyBlockID).get(0);
                if (foundBlocks.containsKey(flyBlockID) && minimum > 0) { // if it has a minimum, it should be considered for sinking consideration
                    int amount = foundBlocks.get(flyBlockID);
                    Double percentPresent = (double) (amount * 100 / totalBlocks);
                    int deshiftedID = flyBlockID;
                    if (deshiftedID > 10000) {
                        deshiftedID = (deshiftedID - 10000) >> 4;
                    }
                    String statusText = "";
                    if (percentPresent > minimum * 1.04) {
                        statusText += ChatColor.GREEN;
                    } else if (percentPresent > minimum * 1.02) {
                        statusText += ChatColor.YELLOW;
                    } else {
                        statusText += ChatColor.RED;
                    }
                    if (deshiftedID == 152) {
                        statusText += "R";
                    } else if (deshiftedID == 42) {
                        statusText += "I";
                    } else {
                        statusText += CraftMagicNumbers.getBlock(deshiftedID).getName().substring(0, 1);
                    }

                    statusText += " ";
                    statusText += percentPresent.intValue();
                    statusText += "/";
                    statusText += minimum.intValue();
                    statusText += "  ";



// also borrowed this from the blockTranslationCommand class with sign
// related bits removed
String fuelText = "";
            Integer fuelRange = (int) ((fuel * (1 + craft.getType().getCruiseSkipBlocks())) / craft.getType().getFuelBurnRate());
            if (fuelRange > 1000) {
                fuelText += ChatColor.GREEN;
            } else if (fuelRange > 100) {
                fuelText += ChatColor.YELLOW;
            } else {
                fuelText += ChatColor.RED;
            }
            fuelText += "Fuel range:";
            fuelText += fuelRange.toString()

// final chat print statement for the status information! :D
// I have no idea if it is formatted properly though. expect misplaced text
    commandSender.sendMessage(fuelText);
    commandSender.sendMessage(statusText);

