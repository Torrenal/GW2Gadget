package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public class TaskSource extends Source
{
   private String source;
   private String directions;
   private Value cost;
   private Item product;
   private String type;
   private ItemQuantitySet[] outputs;

   public TaskSource(Item product, String type, String source, String directions)
   {
      super();
      this.type = type;
      this.source = source;
      this.directions = directions;
      this.cost = new Value(false);
      this.product = product;
      outputs = new ItemQuantitySet[1];
      outputs[0] = new ItemQuantitySet(product, 1);
   }

   public String toString()
   {
      return type+" " + source + ": " + directions;
   }

   @Override
   public String getMethodName()
   {
      return type;
   }

   @Override
   public String getSourceName()
   {
      return source;
   }

   @Override
   public boolean derivesFrom(Item item)
   {
      return false;
   }

   @Override
   public String getObtainString()
   {
      return getObtainString(product, 1);
   }

   @Override
   public String getIngredientDescriptionFor(double quantity)
   {
      StringBuilder ret = new StringBuilder();

      String qty = CookingCore.doubleToString(quantity);

      ret.append(qty);
      ret.append(" ").append(product.getName());
      ret.append("\n   - Line Total: "+getSourceType()+" Only");
      ret.append("\n   - ").append(toString());
      return ret.toString();
   }

   @Override
   public String getObtainString(Item product, double obtainQuantity)
   {
      String qty = CookingCore.doubleToString(obtainQuantity * 1);
      return "   " + qty + "x - " + toString();
   }

   @Override
   public Vector<ItemQtyWrapper> getNestedSources(Item product)
   {
      Vector<ItemQtyWrapper> result = new Vector<ItemQtyWrapper>();

      result.add(new ItemQtyWrapper(this, 1));
      return result;
   }

   @Override
   public boolean isRootStep()
   {
      return true;
   }

   public String getSourceType()
   {
      return type;
   }

   public void linkIngredientsToAPI()
   { 
      if(product.isLinkedToAPI() && product.isTradeable())
      {
         PriceTool.queueItemForStandardUpdates(Long.parseLong(product.getItemID()));
      }
   }

   public synchronized void convertItem(Item staticItem, APIItem dynamicItem)
   {
      if(product == staticItem)
      {
         product = dynamicItem;
         linkIngredientsToAPI();
      }
   }

   @Override
   public Value getSourceUseCost()
   {
      return cost; // Returns 0 cost
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      return product.getSalePriceLessSaleCostBest();
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      return product.getSalePriceLessSaleCostFast();
   }

   @Override
   public Value getSalePriceBest()
   {
      return product.getSaleValueBest();
   }

   @Override
   public Value getSalePriceFast()
   {
      return product.getSaleValueFast();
   }

   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return outputs;
   }

   @Override
   public double getOutputQty(Item output)
   {
      return 1;
   }

   @Override
   public void linkToTradingPost()
   {
   }

   @Override
   public String getFullMethodName()
   {
      return "Task";
   }
}
