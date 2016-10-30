package com.torrenal.craftingGadget.db.recipes;

import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.db.guildupgrades.GuildUpgradeDB;

public class UpgradeDataPair extends CraftingDataPair
{
   private static final long serialVersionUID = 1L;

   long upgradeID;

   public UpgradeDataPair(long upgradeID, int quantity)
   {
      super(-1L, quantity);
      this.upgradeID = upgradeID;
   }
   
   public Item getItem()
   {
      return GuildUpgradeDB.findItemByUpgradeID(upgradeID);
   }

}
