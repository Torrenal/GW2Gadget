package com.torrenal.craftingGadget;

public class ItemKey
{
	
	protected String name = null;
	protected Integer itemLevel = null;

	
	public ItemKey(String name)
	{
		if(name.contains("["))
		{
		   try
		   {
		      // Some things don't have numbers, eg: A "trait guide [Phalanx Strength]"
		      itemLevel = Integer.parseInt(name.split("\\[")[1].split("\\]")[0]);
		      //name = name.split("\\[")[0].trim();
		   } catch (NumberFormatException err)
		   {
		      itemLevel = 0;
		   }
		   
		}
	   this.name = name;
	}
	
	public Integer getItemLevel()
   {
   	return itemLevel;
   }

	@Override
   public int hashCode()
   {
	   final int prime = 31;
	   int result = 1;
	   result = prime * result
	         + ((itemLevel == null) ? 0 : itemLevel.hashCode());
	   result = prime * result + ((name == null) ? 0 : name.hashCode());
	   return result;
   }


	@Override
   public boolean equals(Object obj)
   {
	   if (this == obj)
		   return true;
	   if (obj == null)
		   return false;
	   ItemKey other = (ItemKey) obj;
	   if (itemLevel == null)
	   {
		   if (other.itemLevel != null)
			   return false;
	   } else if (!itemLevel.equals(other.itemLevel))
		   return false;
	   if (name == null)
	   {
		   if (other.name != null)
			   return false;
	   } else if (!name.equals(other.name))
		   return false;
	   return true;
   }
}
