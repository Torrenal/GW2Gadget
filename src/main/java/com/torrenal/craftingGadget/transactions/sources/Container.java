package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public class Container extends Source
{
	private Item container;
	private Item contents; 
	private double qty;
	private ItemQuantitySet[] outputs;
	
	public Container(Item container, Item contents, double qty)
   {
	   super();
	   this.container = container;
	   this.contents = contents;
	   this.qty = qty;
	   outputs = new ItemQuantitySet[1];
	   outputs[0] = new ItemQuantitySet(contents, qty);
	   
	   container.addProduct(contents);
   }

	public Item getContainer()
   {
   	return container;
   }

	public String toString()
	{
		return container.getName() + " contains " + CookingCore.doubleToString(qty) + " " + contents.getName() + "\n  Extended cost: " + container.getBestObtainCost();
	}


	@Override
   public String getMethodName()
   {
	   return "Package";
   }

	@Override
   public String getSourceName()
   {
	   return container.getName();
   }

	@Override
   public boolean derivesFrom(Item item)
   {
	   return container.equals(item);
   }

    @Override
    public ItemQuantitySet[] getOutputs()
    {
       return outputs;
    }
    
    @Override
    public double getOutputQty(Item product)
    {
       return qty;
    }


	@Override
   public String getIngredientDescriptionFor(double quantity)
   {
		StringBuilder ret = new StringBuilder();
		Value unitValue = getObtainOneCost(contents);
		Value lineTotal = unitValue.multiply(quantity);
		
		double neededOutput = quantity * qty;
		
	   ret.append(neededOutput);
	   ret.append(" ").append(contents.getName());
	   ret.append("\n   - Line Total: ").append(lineTotal);
	   ret.append("\n   - Found in ").append(quantity).append(" ").append(container.getName());
	   
	   Source source = getContainer().getSource();
	   if(source == null)
	   {
	   	ret.append("\n   - Not Obtainable");
	   } else if(source instanceof BuyFromMerchant)
	   {
	   	ret.append("\n   - Bought from ").append(((BuyFromMerchant) source).getMerchantName()).append(" for ").append(source.getObtainOneCost(container)).append(" per each");
	   } else if(source instanceof BuyOnMarket)
	   {
	   	ret.append("\n   - Bought from Market for ").append(source.getObtainOneCost(container)).append(" per each");
	   } else
	   {
	   	ret.append("\n   - Err - source type " + source.getClass().getSimpleName() + " not coded for");
	   }
	   return ret.toString();
   }

	@Override
   public String getObtainString()
   {
	   return getObtainString(contents, 1);
   }


	@Override
   public String getObtainString(Item product, double obtainQuantity)
	{
		StringBuilder ret = new StringBuilder();
		String qty = CookingCore.doubleToString(obtainQuantity / this.qty); 
		Item container = getContainer();
		String name = container.getName();
		if(container.getClass() == Item.class)
		{
			name = "\"" + name + "\""; 
		}
		ret.append("   ").append(qty).append("x - Open ").append(name);
		return ret.toString();
   }

	@Override
	public Vector<ItemQtyWrapper> getNestedSources(Item product)
	{
	   if(!product.equals(contents))
	   {
	      throw new IllegalStateException("Cannot get " + product.getName() +  " from " + this);
	   }
		boolean isRoot = false;
		Vector<ItemQtyWrapper> result;
		
		Source containerSource = getContainer().getSource();
		if(containerSource == null)
		{
			containerSource = new Unobtainium(getContainer());
		} 
		result = containerSource.getNestedSources(container);
		adjustQuantitiesByMultiplier(1 / qty, result);
		if(result.size() == 1)
		{
			result.get(0).setRootStep(false);
			result.get(0).setPhantomRootStep(isRoot);
			isRoot = true;
		}
		
		ItemQtyWrapper wrapper = new ItemQtyWrapper(this, 1/qty);
		wrapper.setRootStep(isRoot);
		
		result.add(wrapper);
	   return result;
   }

	@Override
   public boolean isRootStep()
   {
		/* Technically false, but this has the product show in the ingredients */
	   return false;
   }

	public String getSourceType()
	{
		return "Container";
	}
	
	@Override
	public void linkToTradingPost()
	{ 
	   if(contents.isLinkedToAPI() && contents.isTradeable())
	   {
	      PriceTool.queueItemForStandardUpdates(Long.parseLong(contents.getItemID()));
	   }
       if(container.isLinkedToAPI() && container.isTradeable())
       {
          PriceTool.queueItemForStandardUpdates(Long.parseLong(container.getItemID()));
       }
	}

	   public synchronized void convertItem(Item staticItem, APIItem dynamicItem)
	   {
	      if(contents == staticItem)
	      {
	         contents = dynamicItem;
	         outputs[0] = new ItemQuantitySet(contents, qty);
	         discardCalculatedValues();
	      }
	      if(container == staticItem)
	      {
	         container = dynamicItem;
	      }
	      linkToTradingPost();
	   }

      @Override
      public Value getSourceUseCost()
      {
         return container.getBestObtainCost();
      }

      @Override
      public Value getSalePriceLessCostBest()
      {
         if(outputs.length == 1)
         {
            return outputs[0].getSalePriceLessSaleCostBest();
         }
         
         Value value = new Value(false); 
      
         for(ItemQuantitySet output: outputs)
         {
            value = value.add(output.getSalePriceLessSaleCostBest());
         }
         return value;
      }

      @Override
      public Value getSalePriceLessCostFast()
      {
         if(outputs.length == 1)
         {
            return outputs[0].getSalePriceLessSaleCostFast();
         }
         
         Value value = new Value(false); 
      
         for(ItemQuantitySet output: outputs)
         {
            value = value.add(output.getSalePriceLessSaleCostFast());
         }
         return value;
      }

      @Override
      public Value getSalePriceBest()
      {
         if(outputs.length == 1)
         {
            return outputs[0].getSalePriceBest();
         }
         
         Value value = new Value(false); 
      
         for(ItemQuantitySet output: outputs)
         {
            value = value.add(output.getSalePriceBest());
         }
         return value;
      }

      @Override
      public Value getSalePriceFast()
      {
         if(outputs.length == 1)
         {
            return outputs[0].getSalePriceFast();
         }
         
         Value value = new Value(false); 
      
         for(ItemQuantitySet output: outputs)
         {
            value = value.add(output.getSalePriceFast());
         }
         return value;
      }

      @Override
      public String getFullMethodName()
      {
         return "Open";
      }

}
