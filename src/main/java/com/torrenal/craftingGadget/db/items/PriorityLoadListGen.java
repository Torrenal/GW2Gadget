package com.torrenal.craftingGadget.db.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.transactions.destinations.SellToMerchant;
import com.torrenal.craftingGadget.transactions.sources.BuyOnMarket;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.transactions.sources.Unobtainium;

public class PriorityLoadListGen
{
	static int maxReferenceCount = 0;
	
	
	public static void printPriorityLoadList()
   {
		Map<Long, APIItem> itemSet = CookingCore.getAPIItems();
		
		HashSet<PriorityLoadEntry> resultSet = new HashSet<>();
		
		for(APIItem item : itemSet.values())
		{
			int referenceCount = 0;
			referenceCount += item.getDerivitives().size();
			for(Source source : item.getSources())
			{
				if(source instanceof BuyOnMarket)
					continue;
				if(source instanceof Unobtainium)
					continue;
				referenceCount++;
			}
			if(referenceCount > 0)
			{
				maxReferenceCount = maxReferenceCount > referenceCount ? maxReferenceCount : referenceCount;
				resultSet.add(new PriorityLoadEntry(referenceCount, item));
			}
		}
		PriorityLoadEntry[] results = resultSet.toArray(new PriorityLoadEntry[0]);
		Arrays.sort(results);
		for(PriorityLoadEntry entry: results)
		{
			System.out.println(entry.toString());
		}
			
		
   }
	
	private static class PriorityLoadEntry implements Comparable<PriorityLoadEntry>
	{
		int priority;
		APIItem item;

		public PriorityLoadEntry(int priority, APIItem item)
      {
	      super();
	      this.priority = priority;
	      this.item = item;
      }
		public int getPriority()
      {
      	return priority;
      }
		
		@Override
      public int compareTo(PriorityLoadEntry that)
      {
	      return that.getPriority() - this.getPriority();
      }
		
		public String toString()
		{
			return "" + (maxReferenceCount - priority) + "," + item.getItemID(); 
		}
	}


}
