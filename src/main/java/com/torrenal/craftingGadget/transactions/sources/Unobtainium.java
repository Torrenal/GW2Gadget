package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;

public class Unobtainium extends Source
{
	private Item item;
	private ItemQuantitySet[] outputs;
	private Value cost = new Value(true);
	
	public Unobtainium(Item item)
   {
	   this.item = item;
	   outputs = new ItemQuantitySet[1];
	   outputs[0] = new ItemQuantitySet(item, 0);
   }

	@Override
	public String toString()
	{
		return "Cannot be obtained";
	}

	@Override
   public String getMethodName()
   {
	   return "Unobtainable";
   }

	@Override
   public String getSourceName()
   {
	   return "Unobtainable";
   }
	
	@Override
   public boolean derivesFrom(Item item)
   {
	   return false;
   }

	@Override
   public String getObtainString()
   {
		return "Cannot be obtained";
   }

	@Override
   public String getObtainString(Item product, double quantity)
   {
		StringBuilder ret = new StringBuilder();
		
		
		ret.append("   ");
		String obtainQty = CookingCore.doubleToString(quantity);
		ret.append(obtainQty).append(" - ").append(item.getName());
		ret.append("\n      - Cannot be obtained");
		
	   return ret.toString();
   }

	@Override
   public String getIngredientDescriptionFor(double quantity)
   {
		StringBuilder ret = new StringBuilder();
		
		String qty = CookingCore.doubleToString(quantity);

	   ret.append(qty);
	   String productName = item.getName();
	   if(item.getClass() == Item.class)
	   {
	   	productName = "\"" + productName + "\"";
	   }
		ret.append(" ").append(productName);
	   ret.append("\n   - Not Available");
	   return ret.toString();
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
		return "No-Details";
	}

    public void linkIngredientsToAPI()
    { 
       // This is the no-source source.  No linky to API just because we used this.
    }
    
    public void convertItem(Item staticItem, APIItem dynamicItem)
    {
       // No Impl, these are created as needed when no sources exist.
    }

   @Override
   public Value getSourceUseCost()
   {
      return cost;
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      return item.getSalePriceLessSaleCostBest();
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      return item.getSalePriceLessSaleCostFast();
   }

   @Override
   public Value getSalePriceBest()
   {
      return item.getSaleValueBest();
   }

   @Override
   public Value getSalePriceFast()
   {
      return item.getSaleValueFast();
   }

   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return outputs;
   }

   @Override
   public double getOutputQty(Item output)
   {
      return 0;
   }

   @Override
   public void linkToTradingPost()
   {
   }

   @Override
   public String getFullMethodName()
   {
      return "Unobtainable";
   }

}
