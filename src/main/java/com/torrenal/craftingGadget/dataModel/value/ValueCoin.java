package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueCoin extends ValueElement<ValueCoin>
{
	double coins;
	
	static
	{
		valueModifier.put(ValueType.COIN_TOKEN,  0.03F);
	}

	public ValueCoin(double coins)
	{
		setQuantity(coins);
	}

	@Override
	public ValueType getType()
	{
		return ValueType.COIN_TOKEN;
	}

	@Override
   void setQuantity(double coins)
   {
		if(Double.isInfinite(coins) || Double.isNaN(coins))
		{
			System.err.println("invalid coin quantity encountered!  Eeeep!");
		}
	   this.coins = coins;
   }

	@Override
	public double getQuantity()
	{
		return coins;
	}

	public String toString()
	{
		return getValueStringFor(coins);
	}

	/**
	 * Returns a new element that is valued at this+that
	 */
	@Override
	public ValueElement<ValueCoin> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueCoin))
		{
			throw new Error("Invalid Argument, expected ValueCoin");
		}
		return new ValueCoin(coins + that.getQuantity());
	}

	/**
	 * Returns a new element that is valued at this-that
	 */
	@Override
   public ValueElement<ValueCoin> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueCoin))
		{
			throw new Error("Invalid Argument, expected ValueCoin");
		}
		return new ValueCoin(coins - that.getQuantity());
   }

	public static String getValueStringFor(double coins)
   {
		StringBuilder ret = new StringBuilder();
		boolean printAnyway = false;
		int sign = coins >= 0 ? 1 : -1;
		coins *= sign;
		if(coins >= 10000)
		{
			double remainder = coins % 10000;
			long gold = Math.round((coins - remainder)/10000) * sign;
			ret.append(" ").append(gold).append(" gold");
			coins = remainder;
			printAnyway = true;
		}
		if(coins >= 100 || printAnyway)
		{
			double remainder = coins % 100;
			long silver = Math.round((coins - remainder)/100) * sign;
			ret.append(" ").append(silver).append(" silver");
			coins = remainder;
			printAnyway = true;
		}
		String bronzeString = CookingCore.doubleToString(coins * sign);
		ret.append(" ").append(bronzeString).append(" copper");

		if(ret.length() == 0)
		{
			return "";
		}
		return ret.substring(1);
   }
}
