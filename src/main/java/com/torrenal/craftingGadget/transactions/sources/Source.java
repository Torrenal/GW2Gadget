package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;

public abstract class Source
{
	/**
	 * String evaluation of the source 
	 * @return
	 */
	abstract public String toString();
	
	/* There are several values associated to an item.
	 * 
	 * The cost to use the source once.
	 * 
	 * The price others might value the outputs at.
	 * The price you might get for selling the outputs (price less sale costs).
	 *
	 *You have two kinds of sale price: 
	 *  The best possible price (willing to wait for sales to complete)
	 *  The best instant sale price (merchant sale or instant market sale).
	 *
	 * That gives us 5 total values:
	 * 
	 * Source Use Cost
	 * Sale Price (Best)
	 * Sale Price (Fast)
	 * Sale Price Less Cost (Best)
	 * Sale Price Less Cost (Fast)
	 * 
	 * As inputs can be included in outputs, these are not implemented here.
	 */
	
	/** Cost to use this source once 
	 */
    public abstract Value getSourceUseCost();

    /** Cost for obtaining one of the outputs, disregarding any
     * value from other outputs from the recipe.
     * @param item
     * @return
     */
    public Value getObtainOneCost(Item item)
    {
       double qty = getOutputQty(item);
       if(qty == 0)
       {
          return new Value(true);
       }
       if(qty == 1)
       {
          return getSourceUseCost();
       }
       return getSourceUseCost().multiply(1/qty);
    }

	/** Single craft Net sale price (best) 
	 * Sale Value less Obtain cost
	 */
	public abstract Value getSalePriceLessCostBest();
	/** Single craft Net sale price (Fast)
	 * Sale Value less Obtain cost.
	 */
	public abstract Value getSalePriceLessCostFast();
	/** Single craft Gross sale price (best) */
	public abstract Value getSalePriceBest();
	/** Single craft Gross sale price (fast) */
	public abstract Value getSalePriceFast();

	/**
	 * Name of the method this source represents
	 * @return
	 */
	abstract public String getMethodName();
	
	// Used for tables
	public abstract String getFullMethodName();
	
	/** 
	 * Name of the source
	 */
	abstract public String getSourceName();
	/**
	 * Test whether this source derives from the given item.
	 * @param item
	 */
	abstract public boolean derivesFrom(Item item);

	/**
	 * What item this source provides
	 */
	public abstract ItemQuantitySet[] getOutputs();
	
	/**
	 * Shorthand to get the output quantity of a specific output 
	 */
	public abstract double getOutputQty(Item output);
    
	
	/** Returns a textual description of how to obtain the item - for external queries involving a quantity of 1 action*/
	public abstract String getObtainString();

	/** Returns a textual description of how to obtain the item - for internal queries producing the indicated quantity*/
	public abstract String getObtainString(Item product, double qty);

	/** Returns a list of nested Sources for performing this step, including this source. */
	public abstract Vector<ItemQtyWrapper> getNestedSources(Item itemToSource);

	/** Returns a text description used for the 'Necessary Ingredients' list of an item 
	 * Format:  '&lt;Qty&gt; &lt;Item Name&gt;\n    - Line Total: &lt;Total Value&gt;\n   - &lt;Obtain by&gt;'
	 * &lt;Obtain by&gt; should contain price / ea
	 */
	public abstract String getIngredientDescriptionFor(double quantity);
	
	/* Returns true if this is the bottom-most step (eg: starting with initial ingredients)
	 * Returns false otherwise.  Basically, true for everything except where the source is a container or recipe
	 */
	public abstract boolean isRootStep();
	
	static public void adjustQuantitiesByMultiplier(double multipiler, Vector<ItemQtyWrapper> itemList)
	{
		for(ItemQtyWrapper item : itemList)
		{
			item.multQty(multipiler);
		}
	}

	public abstract String getSourceType();

	public void discardCalculatedValues()
   {
   }

   public abstract void linkToTradingPost();

   public abstract void convertItem(Item staticItem, APIItem dynamicItem);
   
}
