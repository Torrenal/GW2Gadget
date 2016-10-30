package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public class BuyFromMerchant extends Source
{
	private String merchantName;
	private Item item;
	private Value cost;
	private ItemQuantitySet[] outputs;
	
	public BuyFromMerchant(Item item, String merchantName, Value cost)
   {
	   this.item = item;
	   this.merchantName = merchantName;
	   this.cost = cost;
	   outputs = new ItemQuantitySet[1];
       outputs[0] = new ItemQuantitySet(item, 1);
   }

	public String getMerchantName()
   {
   	return merchantName;
   }

	public Value getValue()
   {
   	return cost;
   }
	
	public Value getValueFor(Item product)
	{
	   return cost;
	}

	public String toString()
	{
		return "Buy from " + merchantName + ": " + cost;
	}

	@Override
   public String getMethodName()
   {
	   return "Merchant";
   }

	@Override
   public String getSourceName()
   {
	   return merchantName;
   }
	
	@Override
	public ItemQuantitySet[] getOutputs()
	{
	   return outputs;
	}
	
	@Override
	public double getOutputQty(Item product)
	{
	   return 1;
	}


	@Override
   public boolean derivesFrom(Item item)
   {
	   return false;
   }

	@Override
   public String getObtainString()
   {
		return getObtainString(item, 1);
   }

	@Override
   public String getObtainString(Item product, double quantity)
   {
		StringBuilder ret = new StringBuilder();
		
		Value cost = this.cost;
		Value extendedCost = cost.multiply(quantity);
		
		ret.append("   ");
		String obtainQty = CookingCore.doubleToString(quantity);
		ret.append(obtainQty).append(" - ").append(item.getName());
		ret.append("\n      - Extended Cost: ").append(extendedCost);
		ret.append("\n      - ").append(toString()).append(" for ").append(cost).append("/each");
		
	   return ret.toString();
   }

	@Override
   public String getIngredientDescriptionFor(double quantity)
   {
		StringBuilder ret = new StringBuilder();
		Value unitValue = getValue();
		Value lineTotal = unitValue.multiply(quantity);
		
		String qty = CookingCore.doubleToString(quantity);

	   ret.append(qty);
	   ret.append(" ").append(item.getName());
	   ret.append("\n   - Line Total: ").append(lineTotal);
	   ret.append("\n   - Buy from ").append(merchantName).append(" for ").append(unitValue);
	   return ret.toString();
   }

	@Override
	
	public Vector<ItemQtyWrapper> getNestedSources(Item product)
	{
		Vector<ItemQtyWrapper> result = new Vector<ItemQtyWrapper>();
		
		result.add(new ItemQtyWrapper(this, 1));
		ItemQtyWrapper[] conversions = getValue().getConversions();
		if(conversions != null)
		{
			for (ItemQtyWrapper conversion : conversions)
			{
				result.add(conversion);		
			}
		}
		
	   return result;
   }

	@Override
   public boolean isRootStep()
   {
	   return true;
   }
	
	public String getSourceType()
	{
		return "Merchant";
	}

	public void convertItem(Item staticItem, APIItem dynamicItem)
	{
	   if(item == staticItem)
	   {
	      item = dynamicItem;
	      outputs[0] = new ItemQuantitySet(item, 1);
	      linkToTradingPost();
	   }
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
   public void linkToTradingPost()
   {
      if(item.isLinkedToAPI() && item.isTradeable())
      {
         PriceTool.queueItemForStandardUpdates(Long.parseLong(item.getItemID()));
      }
   }

   @Override
   public String getFullMethodName()
   {
      return "Buy";
   }

}
