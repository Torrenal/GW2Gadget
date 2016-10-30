package com.torrenal.craftingGadget.dataModel.value;

import java.util.Hashtable;

import com.torrenal.craftingGadget.CookingCore;

public abstract class ValueElement<K extends ValueElement<?>> implements Comparable<ValueElement<?>>, Cloneable
{
	public abstract ValueType getType();
	public abstract double getQuantity();
	abstract void setQuantity(double d);
	public abstract String toString();
	public abstract ValueElement<K> add(ValueElement<?> that);
	public abstract ValueElement<K> subtract(ValueElement<?> that);
	
	protected static Hashtable<ValueType, Float> valueModifier = new Hashtable<>();
	
	public double getScore()
	{
		Float mult = valueModifier.get(getType());
		if(mult == null)
			mult = 1F;
		return  getQuantity() * ((float)CookingCore.getCurrencyValueMult(getType())) * mult;
	}
	
	@Override
   public int compareTo(ValueElement<?> that)
   {
		double thisScore = this.getScore();
		double thatScore = that.getScore();
	   if(thisScore > thatScore)
	   {
	   	return 1;
	   }
	   if(thisScore < thatScore)
	   {
	   	return -1;
	   }
	   return 0;
   }
	
	@Override
   public int hashCode()
   {
	   final int prime = 31;
	   int result = 1;
	   long temp;
	   temp = Double.doubleToLongBits(getQuantity());
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   temp = getType().hashCode();
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   return result;
   }

	@Override
   public boolean equals(Object obj)
   {
	   if (this == obj)
		   return true;
	   if (obj == null)
		   return false;
	   if (obj.getClass() != this.getClass())
		   return false;
	   ValueGem other = (ValueGem) obj;
	   if (Double.doubleToLongBits(getQuantity()) != Double.doubleToLongBits(other.getQuantity()))
		   return false;
	   if (getType() != other.getType())
		   return false;

	   return true;
   }
	
   @SuppressWarnings("unchecked")
   public ValueElement<K> clone()
	{
		ValueElement<K> result = null;
		try
      {
	      result = (ValueElement<K>) super.clone();
      } catch (CloneNotSupportedException e)
      { } /* No such err */
		return result;
	}
	
	public ValueElement<K> invert()
   {
	   ValueElement<K> ret = clone();
	   ret.setQuantity(-ret.getQuantity());
	   return ret;
   }
	
	static float getValueModifier(ValueType type)
	{
		Float score = valueModifier.get(type);
		if(score == null)
		{
			return 1F;
		}
		return score;
	}
	
	public float getValueModifier()
	{
		Float score = valueModifier.get(getType());
		if(score == null)
		{
			return 1F;
		}
		return score;
	}

}
