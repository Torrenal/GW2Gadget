package com.torrenal.craftingGadget.transactions.sources;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;
import com.torrenal.craftingGadget.ui.recipeDetails.SourceDetails;

import java.util.Vector;

public class Recipe extends Source
{
   private String difficulty;
   private String[] disciplines;
   private Object priceCalcLock = new Object();
   private Value inputsObtainCost = null;
   private Value outputsSalePriceBest = null;
   private Value outputsSalePriceFast = null;
   private Value outputsSalePriceLessSaleCostBest = null;
   private Value outputsSalePriceLessSaleCostFast = null;

   private ItemQuantitySet[] inputs;
   private ItemQuantitySet[] outputs;


   public String getFullMethodName()
   {
      ItemQuantitySet[] outputs = getOutputs();
      if(outputs == null)
      {
         return "Recipe(null)";
      }
      if(outputs.length == 1)
      {
         return outputs[0].toString();
      } 
      StringBuilder sb = new StringBuilder();
      for(ItemQuantitySet output : outputs)
      {
         sb.append(", ").append(output);
      }
      return sb.substring(2);
   }

   public String toString()
   {
      StringBuilder ret = new StringBuilder();

      ItemQuantitySet[] outputs = getOutputs();
      ret.append("makes ");
      if(outputs == null)
      {
         ret.append("\"Null\"");
      } else
      {
         ret.append("makes ").append(CookingCore.doubleToString(getOutputQty(outputs[0].getItem()))).append(' ').append(outputs[0].getItem().getName());
      }
      if(difficulty != null && !difficulty.isEmpty())
      {
         ret.append(" at difficulty ").append(difficulty);
      }
      
      int outputsCount = 0;
      if(outputs != null)
      {
         outputsCount = outputs.length;
      }
      switch(outputsCount)
      {
         case 0:
            ret.append("\nRecipe cost = ").append(getSourceUseCost());
            break;
         case 1:
            if(outputs[0].getQuantity() != 1)
            {
               ret.append("\nUnit Cost = ").append(getSourceUseCost().multiply(1/outputs[0].getQuantity()));
               ret.append("\nRecipe Cost = ").append(getSourceUseCost());
            } else
            {
               ret.append("\nRecipe Cost = ").append(getSourceUseCost());
            }
            break;
         default:
            ret.append("\nRecipe Cost = ").append(getSourceUseCost());
            break;
      }

      ret.append( "\nIngredients: \n");
      ret.append(getRootIngredientsString());
      ret.append("\n\nRecipe:\n");
      ret.append( getObtainString());

      return ret.toString();
   }
   
   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return outputs;
   }

   public ItemQuantitySet[] getInputs()
   {
      return inputs;
   }

   @Override
   public double getOutputQty(Item item)
   {
      if(outputs != null)
      {
         for(ItemQuantitySet output:outputs)
         {
            if(item.equals(output.getItem()))
            {
               return output.getQuantity();
            }
         }
      }
      return 0;
   }

   private String getRootIngredientsString()
   {
      StringBuilder result = new StringBuilder();

      
      Vector<ItemQtyWrapper> dataSet = getNestedSources(null);

      // Filter out non-root steps
      {
         @SuppressWarnings("unchecked")
         Vector<ItemQtyWrapper> editCopy = (Vector<ItemQtyWrapper>) dataSet.clone();
         for(ItemQtyWrapper item : dataSet)
         {
            if(!item.isRootStep())
            {
               editCopy.remove(item);
               continue;
            } 
         }
         dataSet = editCopy;
      }

      // Sort the remainder
      ItemQtyWrapper[] rows = dataSet.toArray(new ItemQtyWrapper[0]);
      Arrays.sort(rows, ItemQtyWrapper.getReverseObtainValueSort());

      for(ItemQtyWrapper item : rows)
      {
         result.append("\n");
         result.append(item.getSource().getIngredientDescriptionFor(item.getQty()));
      }
      if(result.length() == 0)
      {
         return "";
      }
      return result.substring(1);
   }

   public String getObtainString()
   {
      StringBuilder result = new StringBuilder();

      Vector<ItemQtyWrapper> sources = getNestedSources(outputs[0].getItem());
      for(ItemQtyWrapper source : sources)
      {
         Source sourceSource = source.getSource();
         if(!source.isRootStep() && !sourceSource.isRootStep())
         {
            if(sourceSource instanceof CurrencyConversion)
            {
               result.append("\n").append(sourceSource.getObtainString(source.getProduct(), source.getQty()));
            }
         }
      }
      for(ItemQtyWrapper source : sources)
      {
         if(!source.isRootStep() && !source.getSource().isRootStep())
         {
            if(source.getSource() instanceof Container)
            {
               continue;
            }
            if(source.isPhantomRootStep())
            {
               continue;
            }
            if(source.getSource() instanceof CurrencyConversion)
            {
               continue;
            }
            Item intermediateProduct = source.getProduct();
            result.append("\n").append(source.getSource().getObtainString(intermediateProduct, source.getSource().getOutputQty(intermediateProduct) * source.getQty()));
         }
      }
      if(result.length() == 0)
      {
         return "Undefined.";
      }
      return result.substring(1);
   }

   @Override
   public String getObtainString(Item product, double obtainQty)
   {
      StringBuilder recipeString = new StringBuilder();

      recipeString.append("   ");
      ItemQuantitySet[] outputs = getOutputs();
      if(outputs.length == 1)
      {
         recipeString.append(CookingCore.doubleToString(obtainQty / outputs[0].getQuantity())).append("x - " + getCreateVerb() + " ");
         recipeString.append(outputs[0].getItem().getName()).append(" - ").append(getDisciplineString());
      } else
      {
         if(outputs == null)
         {
            recipeString.append("Outputs = null\n");
         } else
         {
            recipeString.append("Multiple Outputs:\n");
            for(ItemQuantitySet output:outputs)
            {
               recipeString.append("   ");
               recipeString.append(CookingCore.doubleToString(obtainQty / output.getQuantity())).append("x - " + getCreateVerb() + " ");
               recipeString.append(output.getItem().getName()).append("\n");
            }
            recipeString.append(" - ").append(getDisciplineString());
         }
      }
      if(getDifficulty() != null && getDifficulty().trim().length() > 0)
      {
         recipeString.append(" (").append(getDifficulty()).append(")");
      }
      return recipeString.toString();
   }

   public String getDisciplineString()
   {
      if(disciplines == null)
      {
         return "??";
      }
      StringBuilder ret = new StringBuilder();
      boolean notFirst = false;
      for(String discipline : disciplines)
      {
         if(notFirst)
         {
            ret.append(", ");
         }
         notFirst = true;
         ret.append(discipline);
      }
      if(ret.length() == 0)
      {
         return "None";
      }
      return ret.toString();
   }

   public void discardCalculatedValues()
   {
      synchronized(priceCalcLock)
      {
         inputsObtainCost = null;
         outputsSalePriceBest = null;
         outputsSalePriceFast = null;
         outputsSalePriceLessSaleCostBest = null;
         outputsSalePriceLessSaleCostFast = null;
      }
   }

   @Override
   public Value getSourceUseCost()
   {
      Value value = inputsObtainCost;
      while(value == null)
      {
         evaluatePricing();
         value = inputsObtainCost;
      }
      return value;
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      Value value = outputsSalePriceLessSaleCostBest;
      while(value == null)
      {
         evaluatePricing();
         value = outputsSalePriceLessSaleCostBest;
      }
      return value;
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      Value value = outputsSalePriceLessSaleCostFast;
      while(value == null)
      {
         evaluatePricing();
         value = outputsSalePriceLessSaleCostFast;
      }
      return value;
   }

   @Override
   public Value getSalePriceBest()
   {
      Value value = outputsSalePriceBest;
      while(value == null)
      {
         evaluatePricing();
         value = outputsSalePriceBest;
      }
      return value;
   }

   @Override
   public Value getSalePriceFast()
   {
      Value value = outputsSalePriceFast;
      while(value == null)
      {
         evaluatePricing();
         value = outputsSalePriceFast;
      }
      return value;
   }


   private void evaluatePricing()
   {
      // As the same item can be used both as an ingredient 
      // and an output in a single recipe (some Mystic Forge recipes do this)
      // we need to be careful with how we do this so that we don't need our
      // own price just to calculate our own price.
      //
      
      synchronized(priceCalcLock)
      {
         if(outputsSalePriceFast != null)
         {
            return;
         }
         // Handle scenarios with null inputs or outputs first.
         if(inputs == null)
         {
            Value unobtainium = new Value(true);
            inputsObtainCost = unobtainium;
            if(outputs == null)
            {
               outputsSalePriceBest = unobtainium;
               outputsSalePriceFast = unobtainium;
               outputsSalePriceLessSaleCostBest = unobtainium;
               outputsSalePriceLessSaleCostFast = unobtainium;
            } else
            {
               Value outputSalePriceBest = new Value(false);
               Value outputSalePriceFast = new Value(false);
               Value outputSalePriceLessSaleCostBest = new Value(false);
               Value outputSalePriceLessSaleCostFast = new Value(false);
               for(ItemQuantitySet output : outputs)
               {
                  outputSalePriceBest = outputSalePriceBest.add(output.getSalePriceBest());
                  outputSalePriceFast = outputSalePriceFast.add(output.getSalePriceFast());
                  outputSalePriceLessSaleCostBest = outputSalePriceLessSaleCostBest.add(output.getSalePriceLessSaleCostBest());
                  outputSalePriceLessSaleCostFast = outputSalePriceLessSaleCostFast.add(output.getSalePriceLessSaleCostFast());
               }
               this.outputsSalePriceBest = outputSalePriceBest;
               this.outputsSalePriceFast = outputSalePriceFast;
               this.outputsSalePriceLessSaleCostBest = outputSalePriceLessSaleCostBest;
               this.outputsSalePriceLessSaleCostFast = outputSalePriceLessSaleCostFast;
            }
            return;
         }
         if(outputs == null)
         {
            Value unobtainium = new Value(true);
            
            outputsSalePriceBest = unobtainium;
            outputsSalePriceFast = unobtainium;
            outputsSalePriceLessSaleCostBest = unobtainium;
            outputsSalePriceLessSaleCostFast = unobtainium;

            Value useOnceCost = new Value(false);
            for(ItemQuantitySet input : inputs)
            {
               useOnceCost = useOnceCost.add(input.getObtainCost());
            }
            inputsObtainCost = useOnceCost;
            return;
         }
         // We have inputs AND outputs both, normal pricing/costing applies:

         // Count all inputs that appear as outputs as 'free'.
         // Count all outputs that were inputs as 'zero profit'.
         //
         // To accomplish this:
         // Lump all the inputs and outputs into one collection.
         // This lets items that appear on both sides of the equation to cancel
         // themselves out.
         // Outputs are positive.
         // Inputs are negative
         Hashtable<Item, Double> itemQuantities = new Hashtable<>();
         for(ItemQuantitySet input : inputs)
         {
            Item item = input.getItem();
            Double qty = itemQuantities.get(item);
            if(qty == null)
            {
               qty = -input.getQuantity();
            } else
            {
               qty -= input.getQuantity();
            }
            itemQuantities.put(item, qty);
         }
         for(ItemQuantitySet output : outputs)
         {
            Item item = output.getItem();
            Double qty = itemQuantities.get(item);
            if(qty == null)
            {
               qty = output.getQuantity();
            } else
            {
               qty += output.getQuantity();
            }
            itemQuantities.put(item, qty);
         }
         
         Value useOnceCost = new Value(false);
         Value outputSalePriceBest = new Value(false);
         Value outputSalePriceFast = new Value(false);
         Value outputSalePriceLessSaleCostBest = new Value(false);
         Value outputSalePriceLessSaleCostFast = new Value(false);
         for(Entry<Item, Double> entry : itemQuantities.entrySet())
         {
            Item item = entry.getKey();
            Double quantity = entry.getValue();
            if(quantity > 0)
            {
               outputSalePriceBest = outputSalePriceBest.add(item.getSaleValueBest().multiply(quantity));
               outputSalePriceFast = outputSalePriceFast.add(item.getSaleValueFast().multiply(quantity));
               outputSalePriceLessSaleCostBest = outputSalePriceLessSaleCostBest.add(item.getSalePriceLessSaleCostBest().multiply(quantity));
               outputSalePriceLessSaleCostFast = outputSalePriceLessSaleCostFast.add(item.getSalePriceLessSaleCostFast().multiply(quantity));
            } else if(quantity < 0)
            {
               useOnceCost = useOnceCost.add(item.getBestObtainCost().multiply(-quantity));
            }
            
         }
         this.inputsObtainCost = useOnceCost;
         this.outputsSalePriceBest = outputSalePriceBest;
         this.outputsSalePriceFast = outputSalePriceFast;
         this.outputsSalePriceLessSaleCostBest = outputSalePriceLessSaleCostBest;
         this.outputsSalePriceLessSaleCostFast = outputSalePriceLessSaleCostFast;

         
      }
   }

   public String getDifficulty()
   {
      return difficulty;
   }

   public String[] getDisciplines()
   {
      return disciplines;
   }

   @Override
   public String getMethodName()
   {
      return "Recipe";
   }

   @Override
   public String getSourceName()
   {
      if(difficulty.isEmpty())
      {
         return getDisciplineString();
      }
      return getDisciplineString() + "("+difficulty+")";
   }

   @Override
   public boolean derivesFrom(Item item)
   {
      
      for(ItemQuantitySet input : inputs)
      {
         if(item.equals(input.getItem()))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public String getIngredientDescriptionFor(double quantity)
   {
      String qty = CookingCore.doubleToString(quantity);

      Item output = outputs[0].getItem();
      String productName = output.getName();
      if(output.getClass() == Item.class)
      {
         productName = "\"" + productName + "\"";
      }
      return ""+qty+" " + productName + "\n - Using resulting product";
   }

   @Override
   public Vector<ItemQtyWrapper> getNestedSources(Item product)
   {
      Vector<ItemQtyWrapper> result = new Vector<ItemQtyWrapper>();

      if(inputs == null)
      {
         return result;
      }
      
      for(ItemQuantitySet input : inputs)
      {
         Item ingredient = input.getItem();
         double qtyMult = input.getQuantity();
         Source source = ingredient.getSource();
         if(source == null)
         {
            source = new Unobtainium(ingredient);
         }
         Vector<ItemQtyWrapper> nestedSubSet;
         if(source != this)
         {
            nestedSubSet = source.getNestedSources(ingredient);
         }
         else
         {
            nestedSubSet = new Vector<ItemQtyWrapper>();
            nestedSubSet.add(new ItemQtyWrapper(source, qtyMult));
         }
         if(!(source instanceof Unobtainium))
         {
            adjustQuantitiesByMultiplier(qtyMult / source.getOutputQty(ingredient), nestedSubSet);
         }
         for(ItemQtyWrapper nestedItem : nestedSubSet)
         {
            if(result.contains(nestedItem))
            {
               ItemQtyWrapper actual = result.get(result.indexOf(nestedItem));
               actual.addQty(nestedItem.getQty());
            } else
            {
               result.add(nestedItem);
            }
         }
      }

      if(getOutputs() == null)
      {
         return result;
      }
         
      result.add(new ItemQtyWrapper(this, 1));
      return result;
   }


   @Override
   public boolean isRootStep()
   {
      return false;
   }

   protected void onDoubleClick(Item item)
   {
      Source source = item.getSource();
      SourceDetails.showDetailsFor(source);
   }


   /** Returns true if the item is made using all known components */
   public boolean isRecipeValid()
   {
      for(ItemQuantitySet input : getInputs())
      {
         if(input.getItem() instanceof APIItem)
         {
            continue;
         }
         return false;
      }
      return true;
   }

   protected synchronized void setInputs(ItemQuantitySet[] newInputs)
   {
      ItemQuantitySet[] inputs = this.inputs;
      ItemQuantitySet[] outputs = this.outputs;
      if(outputs != null && inputs != null)
      {
         Item[] outputItems = new Item[outputs.length];
         for(int i = 0; i < outputs.length; i++)
         {
            outputItems[i] = outputs[i].getItem();
         }
         for(ItemQuantitySet input : this.inputs)
         {
            Item inputItem = input.getItem();
         
            for(Item outputItem : outputItems)
            {
               inputItem.removeProduct(outputItem);
            }
         }
      }
      this.inputs = newInputs;

      if(newInputs != null && outputs != null)
      {
         Item[] outputItems = new Item[outputs.length];
         for(int i = 0; i < outputs.length; i++)
         {
            outputItems[i] = outputs[i].getItem();
         }
         for(ItemQuantitySet input : newInputs)
         {
            Item inputItem = input.getItem();
         
            for(Item outputItem : outputItems)
            {
               inputItem.addProduct(outputItem);
            }
         }
      }
      
      discardCalculatedValues();
      linkToTradingPost();
   }

   protected void setOutputs(ItemQuantitySet[] newOutputs)
   {
      ItemQuantitySet[] inputs = this.inputs;
      ItemQuantitySet[] outputs = this.outputs;
      if(outputs != null && inputs != null)
      {
         Item[] outputItems = new Item[outputs.length];
         for(int i = 0; i < outputs.length; i++)
         {
            outputItems[i] = outputs[i].getItem();
         }
         for(ItemQuantitySet input : this.inputs)
         {
            Item inputItem = input.getItem();
         
            for(Item outputItem : outputItems)
            {
               inputItem.removeProduct(outputItem);
            }
         }
      }
      this.outputs = newOutputs;

      if(inputs != null && newOutputs != null)
      {
         Item[] outputItems = new Item[newOutputs.length];
         for(int i = 0; i < newOutputs.length; i++)
         {
            outputItems[i] = newOutputs[i].getItem();
         }
         for(ItemQuantitySet input : inputs)
         {
            Item inputItem = input.getItem();
         
            for(Item outputItem : outputItems)
            {
               inputItem.addProduct(outputItem);
            }
         }
      }
      
      discardCalculatedValues();
      linkToTradingPost();

   }

     protected void setDifficulty(String difficulty)
   {
      this.difficulty = difficulty;
   }

   protected void setDiscipline(String discipline)
   {
      String[] array = new String[1];
      array[0] = discipline;
      setDisciplines(array);
   }

   protected void setDisciplines(String[] disciplines)
   {
      String[] oldDisciplines = this.disciplines;
      this.disciplines = disciplines;
      boolean updateStructure = false;
      updateStructure |= oldDisciplines == null;
      if(!updateStructure)
      {
         updateStructure = oldDisciplines.length != this.disciplines.length;
      }
      if(!updateStructure)
      {
         Arrays.sort(oldDisciplines);
         Arrays.sort(this.disciplines);
         updateStructure = !oldDisciplines.equals(this.disciplines);
      }
      if(updateStructure)
      {
         ContextUpdateNotifier.notifyLazyStructureUpdates();
      }

      if(oldDisciplines == null )
         ContextUpdateNotifier.notifyContentUpdates();
   }

   public synchronized void convertItem(Item staticItem, APIItem dynamicItem)
   {
      boolean republishOutputs = false;
      ItemQuantitySet[] outputs = getOutputs();
      ItemQuantitySet[] inputs = getInputs();

      if(inputs == null || outputs == null)
      {
         return;
      }
      
      for(ItemQuantitySet output : outputs)
      {
         if(output.getItem().equals(dynamicItem))
         {
            republishOutputs = true;
            break;
         }
      }
      if(republishOutputs)
      {
         Item[] outputItems = new Item[outputs.length];
         for(int i = 0; i < outputs.length; i++)
         {
            outputItems[i] = outputs[i].getItem();
         }
         for(ItemQuantitySet input : inputs)
         {
            for(Item outputItem : outputItems)
            {
               input.getItem().addProduct(outputItem);
            }
         }
         discardCalculatedValues();
         linkToTradingPost();
      }
   }

   public String getSourceType()
   {
      if("Mystic Forge".equals(getDisciplineString()))
      {
         return "Mystic Forging";
      }
      return "Crafting";
   }

   @Override
   public void linkToTradingPost()
   { 
      ItemQuantitySet[] inputs = this.inputs;
      ItemQuantitySet[] outputs = this.outputs;
      
      if(inputs != null)
      {
         for(ItemQuantitySet input : inputs)
         {
            Item item = input.getItem();
            String itemId = item.getItemID();
            if(itemId == null || itemId.isEmpty())
            {
               continue;
            }
            PriceTool.queueItemForStandardUpdates(Long.parseLong(itemId));
         }
      }
      if(outputs!= null)
      {
         for(ItemQuantitySet output : outputs)
         {
            Item item = output.getItem();
            String itemId = item.getItemID();
            if(itemId == null || itemId.isEmpty())
            {
               continue;
            }
            PriceTool.queueItemForStandardUpdates(Long.parseLong(itemId));
         }
      }

   }

   public String getCreateVerb()
   {
      return "Craft";
   }
}
