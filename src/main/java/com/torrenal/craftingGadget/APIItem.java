package com.torrenal.craftingGadget;

import java.util.Collection;

import com.torrenal.craftingGadget.apiInterface.items.ItemDetailRequest;
import com.torrenal.craftingGadget.db.items.ItemRecord;
import com.torrenal.craftingGadget.transactions.sources.APIRecipe;
import com.torrenal.craftingGadget.transactions.sources.Source;

public class APIItem extends Item
{
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((itemIDString == null) ? 0 : itemIDString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		APIItem other = (APIItem) obj;
		if (itemIDString == null)
		{
			if (other.itemIDString != null)
				return false;
		} else if (!itemIDString.equals(other.itemIDString))
			return false;
		return true;
	}

	private ItemRecord record;
	private String itemIDString = "";

	public APIItem(ItemRecord record)
	{
		super("??");
		this.record = record;
		if(record.getItemID() == 75690)
		{
		   new Throwable("Instantiating item 75690").printStackTrace();
		}
		updateFromRecord();
	}

	public void updateFromRecord()
	{
		synchronized(nameSync)
		{
			itemIDString = ""+record.getItemID();
			String name  = record.getItemName();
			if(name == null || name.isEmpty())
			{
				name = "??";
				this.name = name;
			} else
			{
				int level = record.getItemLevel();
				String rarity = record.getItemRarity();
				Long merchPrice = record.getMerchValue();
				boolean isTradeable = record.isTradeable();
				if(merchPrice == null)
				{
					merchPrice = 0L;
				}
				name = record.getFullName(name, level, rarity);
				boolean changes = false;
				changes |= isTradeable() != isTradeable;
					
				setTradeable(isTradeable);
				
				changes |= getMerchValue() != null || getMerchValue().getCoin() != merchPrice;
					
				updateMerchPrice(merchPrice);
				changes |= !(name.equals(this.name));
				this.name = name;
				if(changes)
				{
					ContextUpdateNotifier.notifyLazyStructureUpdates();
				}
			}
		}
		updateDisciplineOnSources();
		Collection<Item> derivitives = getDerivitives();
		for(Item derivitive : derivitives)
		{
			if(derivitive instanceof APIItem)
			{
				((APIItem) derivitive).updateDisciplineOnSources();
			}
		}

		EvaluateEngine.evaluate(this);
	}

	private void updateDisciplineOnSources()
	{
		/* Update discipline detail */
		Collection<Source> sources = getSources();
		for(Source source : sources)
		{
			if(source instanceof APIRecipe)
			{
				((APIRecipe) source).updateDisciplneFromRecipe();
			}
		}
	}

	@Override
	public boolean isConsumable()
	{
		return record.isConsumable();
	}


	@Override
	public String getItemID()
	{
		return itemIDString;
	}

	@Override
	public boolean isLinkedToAPI()
	{
		return (record.getLastUpdateTimestamp() > 10);
	}

	public long getLastUpdateTimestamp()
   {
	   return record.getLastUpdateTimestamp();
   }

	public void updateItemFromAPI()
   {
	   ItemDetailRequest.requestItemDetailFor(record.getItemID(), record.getLastUpdateTimestamp(), false, false);
   }
}
