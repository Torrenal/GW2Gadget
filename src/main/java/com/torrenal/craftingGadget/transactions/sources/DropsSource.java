package com.torrenal.craftingGadget.transactions.sources;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public abstract class DropsSource extends Source
{
   /**
    * Returns the verb for this source.
    * Eg: rare-salvage, drops, 
    * 
    * @return
    */
   public abstract String getSourceVerb();
   public abstract ItemQuantitySet[] getInputs();
   public abstract ItemQuantitySet[] getOutputs();
   public abstract ItemQuantitySet[] getOutputErrors();
   public abstract ItemQuantitySet[] getOutputsMinimums();

   @Override
   public String getFullMethodName()
   {
      return getMethodName() + getInputsString();
   }

   @Override
   public String toString()
   {
      StringBuilder ret = new StringBuilder();

      ret.append(getSourceVerb() + " from ").append(getInputsString()).append('\n');
      ret.append("Products Sum Value: ").append(getSalePriceBest()).append('\n');
      ret.append(getProductsString());

      return ret.toString();
   }

   private String getProductsString()
   {
      ItemQuantitySet[] products = getOutputs();
      ItemQuantitySet[] productErrors = getOutputErrors();
      Arrays.sort(products, new InverseValueComparator());

      StringBuilder ret = new StringBuilder();
      for(ItemQuantitySet product : products)
      {
         ItemQuantitySet err = null;
         if(productErrors != null)
         {
            for(ItemQuantitySet prodErr : productErrors)
            {
               if(prodErr.getItem().equals(product.getItem()))
               {
                  err = prodErr;
                  break;
               }
            }
         }
         ret.append("\n   ").append(CookingCore.doubleToPrecisionString(product.getQuantity()));
         if(err != null)
         {
            ret.append('\u00B1').append(CookingCore.doubleToPrecisionString(err.getQuantity()));
         }
         ret.append(' ').append(product.getItemName());
         
         if(err == null)
         {
            ret.append("\n      Average Return: ~").append(product.getSalePriceLessSaleCostBest());
         } else
         {
            ret.append("\n      Average Return: ").append(product.getSalePriceLessSaleCostBest());
            ret.append(" \u00B1").append(err.getSalePriceLessSaleCostBest());
         }
      }
      return ret.substring(1);

   }

   public String getInputsString()
   {
      ItemQuantitySet[] inputs = getInputs();

      StringBuilder ret = new StringBuilder();
      for(ItemQuantitySet input : inputs)
      {

         ret.append(", ");
         ret.append(input);
      }
      return ret.substring(2);
   }

   @Override
   public String getSourceName()
   {
      return getInputsString();
   }

   @Override
   public boolean derivesFrom(Item item)
   {
      ItemQuantitySet[] inputs = getInputs();
      for(ItemQuantitySet input : inputs)
      {
         if(input.getItem().equals(item))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public double getOutputQty(Item output)
   {

      for(ItemQuantitySet candidate : getOutputs())
      {
         if(candidate.getItem().equals(output))
         {
            return candidate.getQuantity();
         }
      }
      //TODO put real numbers here
      return 0.000000001;
   }

   @Override
   public String getObtainString()
   {
      throw new UnsupportedOperationException("Cannot provide obtain string without a product");
   }

   @Override
   public String getObtainString(Item desiredProduct, double obtainQty)
   {
      StringBuilder recipeString = new StringBuilder();

      recipeString.append("   ");
      double productQuantity = getOutputQty(desiredProduct);
      recipeString.append(CookingCore.doubleToString(obtainQty / productQuantity)).append("x - " + getSourceVerb() + " ");
      recipeString.append(getSourceName()).append(" for ");
      recipeString.append(CookingCore.doubleToString(obtainQty)).append(" ").append(desiredProduct.getName());
      return recipeString.toString();
   }

   @Override
   public Vector<ItemQtyWrapper> getNestedSources(Item product)
   {
      Vector<ItemQtyWrapper> result = new Vector<ItemQtyWrapper>();
      
      ItemQuantitySet[] inputs = getInputs();
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
         
         double productQuantity = getOutputQty(product);

         adjustQuantitiesByMultiplier(qtyMult / productQuantity, nestedSubSet);
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

      result.add(new ItemQtyWrapper(product, this, 1));
      return result;
   }

   @Override
   public String getIngredientDescriptionFor(double quantity)
   {
      ItemQuantitySet[] inputs = getInputs();

      StringBuilder ret = new StringBuilder();
      for(ItemQuantitySet input : inputs)
      {
         ret.append(", ").append(input.toStringWithMult(quantity));
      }
      return ret.substring(2);   
   }


   @Override
   public boolean isRootStep()
   {
      return false;
   }

   public static class InverseValueComparator implements Comparator<ItemQuantitySet>
   {
      @Override
      public int compare(ItemQuantitySet o1, ItemQuantitySet o2)
      {
         int result = (o2.getSalePriceBest().compareTo(o1.getSalePriceBest()));
         if(result != 0)
         {
            return result;
         }
         if(o1.getQuantity() < o2.getQuantity())
         {
            return 1;
         }
         if(o1.getQuantity() > o2.getQuantity())
         {
            return -1;
         }
         return 0;
      }
   }

   @Override
   public Value getSalePriceBest()
   {
      Value cost = new Value(false);

      ItemQuantitySet[] outputs = getOutputs();
      for(ItemQuantitySet output : outputs)
      {
         cost = cost.add(output.getSalePriceBest());
      }
      return cost;
   }

   @Override
   public Value getSalePriceFast()
   {
      Value cost = new Value(false);

      ItemQuantitySet[] outputs = getOutputs();
      for(ItemQuantitySet output : outputs)
      {
         cost = cost.add(output.getSalePriceFast());
      }
      return cost;
   }
   
   @Override
   public Value getSourceUseCost()
   {
      ItemQuantitySet[] inputs = getInputs();
      if(inputs.length == 1)
      {
         return inputs[0].getObtainCost();
      }
      Value cost = new Value(false);
      for(ItemQuantitySet input : inputs)
      {
         cost = cost.add(input.getObtainCost());
      }
      return cost;
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      ItemQuantitySet[] outputs = getOutputs();
      if(outputs.length == 1)
      {
         return outputs[0].getSalePriceLessSaleCostBest();
      }
      Value price = new Value(false);
      for(ItemQuantitySet input : outputs)
      {
         price = price.add(input.getSalePriceLessSaleCostBest());
      }
      return price;
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      ItemQuantitySet[] outputs = getOutputs();
      if(outputs.length == 1)
      {
         return outputs[0].getSalePriceLessSaleCostFast();
      }
      Value price = new Value(false);
      for(ItemQuantitySet input : outputs)
      {
         price = price.add(input.getSalePriceLessSaleCostFast());
      }
      return price;
   }

   public Value getSalePriceBestError()
   {
      ItemQuantitySet[] outputErrors = getOutputErrors();
      if(outputErrors == null)
      {
         return null;
      }
      Value error = new Value(false);

      for(ItemQuantitySet outputError : outputErrors)
      {
         error = error.add(outputError.getSalePriceBest());
      }
      return error;
   }

   public Value getSalePriceLessSaleCostBestError()
   {
      ItemQuantitySet[] outputErrors = getOutputErrors();
      if(outputErrors == null)
      {
         return null;
      }
      Value error = new Value(false);

      for(ItemQuantitySet outputError : outputErrors)
      {
         error = error.add(outputError.getSalePriceLessSaleCostBest());
      }
      return error;
   }

   public Value getSalePriceLessSaleCostFastError()
   {
      ItemQuantitySet[] outputErrors = getOutputErrors();
      if(outputErrors == null)
      {
         return null;
      }
      Value error = new Value(false);

      for(ItemQuantitySet outputError : outputErrors)
      {
         error = error.add(outputError.getSalePriceLessSaleCostFast());
      }
      return error;
   }

   public Value getSalePriceLessSaleCostLowBound()
   {
      ItemQuantitySet[] outputs = getOutputsMinimums();
      if(outputs == null)
      {
         return null;
      }
      Value value = new Value(false);
      for(ItemQuantitySet output : outputs)
      {
         value = value.add(output.getSalePriceLessSaleCostBest());
      }
      return value;   
   }

   @Override
   public void convertItem(Item staticItem, APIItem dynamicItem)
   {
      ItemQuantitySet[] inputs = getInputs();
      for(ItemQuantitySet input : inputs)
      {
         if(input.getItem() == dynamicItem)
         {
            discardCalculatedValues();
            linkToTradingPost();
            return;
         }
      }
      ItemQuantitySet[] outputs = getOutputs();
      for(ItemQuantitySet output : outputs)
      {
         if(output.getItem() == dynamicItem)
         {
            discardCalculatedValues();
            linkToTradingPost();
            return;
         }
      }
   }
   
   @Override
   public void linkToTradingPost()
   {
      ItemQuantitySet[] inputs = getInputs();
      for(ItemQuantitySet input : inputs)
      {
         Item item = input.getItem();
         if(item.isLinkedToAPI() && item.isTradeable())
         {
            PriceTool.queueItemForStandardUpdates(Long.parseLong(item.getItemID()));
         }
      }

      ItemQuantitySet[] outputs = getOutputs();
      for(ItemQuantitySet output : outputs)
      {
         Item item = output.getItem();
         if(item.isLinkedToAPI() && item.isTradeable())
         {
            PriceTool.queueItemForStandardUpdates(Long.parseLong(item.getItemID()));
         }
      }
   }
   
   /** To be invoked by the constructor after the backing data
    * for this item is in place.
    * This registers the item as a source for its products and
    * the producer as the proper source for all the items it produces.
    */
   protected void registerInputsAndOutputs()
   {
      ItemQuantitySet[] outputs = getOutputs();
      for(ItemQuantitySet output : outputs)
      {
         output.getItem().addSource(this);
      }
      ItemQuantitySet[] inputs = getInputs();
      for(ItemQuantitySet input : inputs)
      {
         for(ItemQuantitySet output : outputs)
         {
            input.getItem().addProduct(output.getItem());
         }
      }
   }
}
