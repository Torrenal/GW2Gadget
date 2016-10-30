package com.torrenal.craftingGadget.transactions.sources;

import java.util.Arrays;
import java.util.Vector;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateEngine;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.apiInterface.recipies.RecipeDetailRequest;
import com.torrenal.craftingGadget.db.guildupgrades.GuildUpgradeDB;
import com.torrenal.craftingGadget.db.recipes.RecipeRecord;
import com.torrenal.craftingGadget.db.recipes.UpgradeDataPair;

public class APIRecipe extends Recipe
{
   RecipeRecord backingRecipe;
   long lastUnresolvedSequenceID = -1;
   public APIRecipe(RecipeRecord recipe)
   {
      if(recipe == null)
      {
         throw new NullPointerException("recipe may not be null");
      }

      backingRecipe = recipe;
      updateFromRecipe(true);
   }

   public void updateFromRecipe(boolean forceStructureUpdate)
   {
      if(backingRecipe == null)
      {
         throw new NullPointerException("Backing Recipe may not be null");
      }
      if(backingRecipe.getLastUpdateTimestamp() < 10)
      {
         return;
      }
      
      {// scope
         Long guildUpgradeID = backingRecipe.getProductUpgradeID();
         if(guildUpgradeID != null)
         {
            GuildUpgradeDB.registerUpgrade(backingRecipe, backingRecipe.getProductItemId(), backingRecipe.getProductUpgradeID());      
         }
      }
      
      boolean isStructureUpdate = forceStructureUpdate; 

      ItemQuantitySet[] oldOutputs = getOutputs();
      ItemQuantitySet[] newOutputs = backingRecipe.getOutputs();
      
      

      if(!Arrays.equals(oldOutputs, newOutputs))
      {
         if(oldOutputs != null)
         {
            for(ItemQuantitySet output : oldOutputs)
            {
               output.getItem().removeSource(this);   
            }
         }
         setOutputs(newOutputs);
         for(ItemQuantitySet output : newOutputs)
         {
            output.getItem().addSource(this);   
         }
         isStructureUpdate = true;
      }

      // Update ingredients
      {
         ItemQuantitySet[] oldInputs = getInputs();

         ItemQuantitySet[] basicInputs = backingRecipe.getInputs();
         Vector<UpgradeDataPair> upgradeInputs = backingRecipe.getUpgradeComponents();
         Vector<ItemQuantitySet> combinedInputs = new Vector<>(basicInputs.length + upgradeInputs.size());
         for(ItemQuantitySet input : basicInputs)
         {
            combinedInputs.add(input);
         }
         for(UpgradeDataPair input : upgradeInputs)
         {
            combinedInputs.add(new ItemQuantitySet(input));
         }

         ItemQuantitySet[] newInputs = combinedInputs.toArray(new ItemQuantitySet[0]);

         if(!Arrays.equals(oldInputs, newInputs))
         {
            setInputs(newInputs);
            isStructureUpdate = true;
         }
      }

      //Difficulty...
      {
         String oldDifficulty = getDifficulty();
         setDifficulty(""+backingRecipe.getDifficulty());
         String newDifficulty = getDifficulty();
         if(!newDifficulty.equals(oldDifficulty))
         {
            isStructureUpdate = true;
         }
      }
      //Discipline
      {
         updateDisciplneFromRecipe();
      }
      for(ItemQuantitySet output : newOutputs)
      {
         EvaluateEngine.evaluate(output.getItem());
      }
      if(isStructureUpdate)
      {
         ContextUpdateNotifier.notifyLazyStructureUpdates();
      } else
      {
         ContextUpdateNotifier.notifyContentUpdates();
      }
   }

   public void updateFromRecipe()
   {
      updateFromRecipe(false);
   }

   public void updateRecipeFromAPI()
   {
      RecipeDetailRequest.requestRecipeDetailFor(backingRecipe.getRecipeID(), backingRecipe.getLastUpdateTimestamp());
   }

   public long getLastUpdateTimestamp()
   {
      return backingRecipe.getLastUpdateTimestamp();
   }

   public void updateDisciplneFromRecipe()
   {
      setDisciplines(backingRecipe.getDisciplines());
   }
}
