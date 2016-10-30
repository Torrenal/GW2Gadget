package com.torrenal.craftingGadget.dataModel.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.torrenal.craftingGadget.ItemCurrency;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.transactions.sources.CurrencyConversion;

public class Value implements Comparable<Value>
{
	private boolean unobtainium;
	private Vector<ValueElement<?>> elements;

	public Value(boolean unobtainium)
   {
		elements = new Vector<>(1);
		this.unobtainium = unobtainium;
   }
	
	public List<ItemQuantitySet> explodeCurrencies()
	{
	   Vector<ItemQuantitySet> ret = new Vector<>();
	   for(ValueElement<?> element : elements)
	   {
	      element = element.clone();
	      double amount = element.getQuantity();
	      ItemCurrency currency = element.getType().getCurrency();
	      ret.add(new ItemQuantitySet(currency, amount));
	   }
	   return ret;
	}
	
	public Value(ValueElement<?> element)
   {
		elements = new Vector<>(1);
		elements.add(element);
   }

	public Value(ValueElement<?> element, boolean unobtainium)
   {
		elements = new Vector<>(1);
		elements.add(element);
		this.unobtainium = unobtainium;
   }

	public Value(Collection<ValueElement<?>> values, boolean unobtainium)
   {
		elements = new Vector<>(values.size());
		elements.addAll(values);
		this.unobtainium = unobtainium;
   }

	public double getScore()
	{
		double score = 0;
		for(ValueElement<?> element : elements)
		{
			score += element.getScore();
		}
		return score;
	}

	public String toString()
	{

		StringBuilder ret = new StringBuilder();
		ValueElement<?>[] sortedElements = elements.toArray(new ValueElement<?>[0]);
		Arrays.sort(sortedElements);

		for(ValueElement<?> element : elements)
		{
			if(element.getQuantity() != 0)
			{
				if(element instanceof ValueCoin)
				{
					ret.insert(0, ", " + element.toString());
				}else
				{
					ret.append(", ").append(element.toString());
				}
			}
		}
		if(ret.length() == 0)
		{
			if(unobtainium)
			{
				return "(unobtainium)";
			}
			return "nothing";
		}
		if(unobtainium)
		{
			ret.append(" (unobtainium)");
		}
		
		return ret.substring(2);
	}
	
	@Override
   public int compareTo(Value that)
   {
	   Double myValue = getScore();  
	   Double yourValue = that.getScore();
	   if(this.isUnobtanium() && !that.isUnobtanium())
	   {
	   	return 1;
	   }
	   if(!this.isUnobtanium() && that.isUnobtanium())
	   {
	   	return -1;
	   }
	   if(myValue > yourValue)
	   {
	   	return 1;
	   }
	   if(myValue < yourValue)
	   {
	   	return -1;
	   }
	   return 0;
   }
	public boolean isUnobtanium()
   {
	   return unobtainium;
   }

	/**
	 * Returns the result of this - that.
	 * Does not modify this or that.
	 * @param that
	 * @return
	 */
	public Value subtract(Value that)
   {
	   Hashtable<Class<ValueElement<?>>,ValueElement<?>> resultSet = new Hashtable<>();
	   for(ValueElement<?> element : elements)
	   {
	   	@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) element.getClass();
	   	resultSet.put(valueClass, element);
	   }
	   nextReductor:
	   for(ValueElement<?> reductElement : that.elements)
	   {
	   	for(ValueElement<?> baseElement : this.elements)
	   	{
	   		if(reductElement.getClass() == baseElement.getClass())
	   		{
	   			@SuppressWarnings("unchecked")
               Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) reductElement.getClass();
	   			resultSet.put(valueClass, baseElement.subtract(reductElement));

	   			continue nextReductor;
	   		}
	   	}
			@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) reductElement.getClass();
	   	resultSet.put(valueClass, reductElement.invert());
	   }
	   boolean unobtainable = this.isUnobtanium() || that.isUnobtanium();
	   return new Value(resultSet.values(), unobtainable);
   }

	public Value add(Value that)
   {
	   Hashtable<Class<ValueElement<?>>,ValueElement<?>> resultSet = new Hashtable<>();
	   for(ValueElement<?> element : elements)
	   {
	   	@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) element.getClass();
	   	resultSet.put(valueClass, element);
	   }
	   
	   nextReductor:
	   for(ValueElement<?> addElement : that.elements)
	   {
	   	for(ValueElement<?> baseElement : this.elements)
	   	{
	   		if(addElement.getClass() == baseElement.getClass())
	   		{
	   			@SuppressWarnings("unchecked")
               Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) addElement.getClass();
	   			resultSet.put(valueClass, baseElement.add(addElement));
	   			continue nextReductor;
	   		}
	   	}
			@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) addElement.getClass();
	   	resultSet.put(valueClass, addElement.clone());
	   }
	   boolean unobtainable = this.isUnobtanium() || that.isUnobtanium();
	   return new Value(resultSet.values(), unobtainable);
   }

	/** Returns a result such that result = this + (that * multiplier)
	 * @param that
	 * @param multiplier
	 * @return this + (that * multiplier)
	 */
	public Value add(Value that, double multiplier)
   {
		if(multiplier == 1)
		{
			return add(that);
		}
	   Hashtable<Class<ValueElement<?>>,ValueElement<?>> resultSet = new Hashtable<>();
	   for(ValueElement<?> element : elements)
	   {
	   	@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) element.getClass();
	   	resultSet.put(valueClass, element);
	   }
	   
	   nextReductor:
	   for(ValueElement<?> addElement : that.elements)
	   {
	   	for(ValueElement<?> baseElement : this.elements)
	   	{
	   		if(addElement.getClass() == baseElement.getClass())
	   		{
	   			ValueElement<?> sumElement = baseElement.clone();
	   			sumElement.setQuantity(sumElement.getQuantity() + addElement.getQuantity() * multiplier);
	            @SuppressWarnings("unchecked")
               Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) sumElement.getClass();
	   			resultSet.put(valueClass, sumElement);
	   			continue nextReductor;
	   		}
	   	}
	   	ValueElement<?> sumElement = addElement.clone();
	   	sumElement.setQuantity(sumElement.getQuantity() * multiplier);
	   	@SuppressWarnings("unchecked")
         Class<ValueElement<?>> valueClass = (Class<ValueElement<?>>) sumElement.getClass();
	   	resultSet.put(valueClass,sumElement);
	   }
	   boolean unobtainable = this.isUnobtanium() || that.isUnobtanium();
	   return new Value(resultSet.values(), unobtainable);
   }

	public Value multiply(double multiplier)
   {
	   if(multiplier == 1)
	   {
	      return this;
	   }
		HashSet<ValueElement<?>> resultElements = new HashSet<>();
		
		for(ValueElement<?> element : elements)
		{
			ValueElement<?> copyElement = element.clone();
			copyElement.setQuantity(element.getQuantity() * multiplier);
			resultElements.add(copyElement);
		}
		
	   return new Value(resultElements, isUnobtanium());
   }
	
	public Value lessMarketCut()
   {
		HashSet<ValueElement<?>> resultElements = new HashSet<>();
		
		for(ValueElement<?> element : elements)
		{
			ValueElement<?> copyElement = element.clone();
			if(copyElement instanceof ValueCoin)
			{
				double coin = element.getQuantity();
			   double postFee = Math.floor(coin * .05 + .5);
			   double marketCut = Math.floor(coin * .1 + .5);

				copyElement.setQuantity(coin - postFee - marketCut);
			}
			resultElements.add(copyElement);
		}
		
	   return new Value(resultElements, isUnobtanium());
   }


	/* Returns the coin value only */
	public double getCoin()
   {
	   for(ValueElement<?> element : elements)
	   {
	   	if(element instanceof ValueCoin)
	   	{
	   		return element.getQuantity();
	   	}
	   }
	   return 0;
   }

	public Value costPer(ValueType valueType)
   {
		/* Locate the unit in the values */
		double matchQty = 0;
		for(ValueElement<?> element : elements)
		{
			if(element.getType() == valueType)
			{
				matchQty = element.getQuantity();
				if(matchQty < 0)
				{
					matchQty = -matchQty;
				}
			}
		}
		if(matchQty == 0)
		{
			return new Value(false);
		}
		
		Vector<ValueElement<?>> resultSet = new Vector<>();
		
		for(ValueElement<?> element : elements)
		{
			ValueElement<?> sizedElement = element.clone();
			sizedElement.setQuantity(sizedElement.getQuantity() / matchQty);
			resultSet.add(sizedElement);
		}
		return new Value(resultSet, isUnobtanium());
   }

	public boolean uses(Class<?> valueType)
   {
	   for(ValueElement<?> element : elements)
	   {
	   	if(element.getClass() == valueType && element.getQuantity() != 0)
	   	{
	   		return true;
	   	}
	   }
	   return false;
   }

	public boolean uses(ValueType currency)
   {
	   for(ValueElement<?> element : elements)
	   {
	   	if(element.getType() == currency && element.getQuantity() != 0)
	   	{
	   		return true;
	   	}
	   }
	   return false;
   }

	public Value convertCurrency()
   {
		Vector<ValueElement<?>> addBits = new Vector<>();
		Vector<ValueElement<?>> removeBits = new Vector<>();
	   for(ValueElement<?> element : elements)
	   {
	   	ValueElement<?> convertTo = ExchangeRateManager.exchange(element);
	   	if(convertTo != null)
	   	{
	   		boolean summed = false;
	   		for(ValueElement<?> bit : addBits)
	   		{
	   			if(bit.getType() == convertTo.getType() && !summed)
	   			{
	   				summed = true;
	   				bit.setQuantity(bit.getQuantity() + convertTo.getQuantity());
	   			}
	   		}
	   		if(!summed)
	   		{
	   			addBits.add(convertTo);
	   		}
	   		removeBits.add(element); 
	   	}
	   }
	   if(addBits.isEmpty())
	   {
	   	return this;
	   }
	   Value result = new Value(isUnobtanium());
	   result.elements = addBits;
	   
	   for(ValueElement<?> element : elements)
	   {
	   	if(removeBits.contains(element))
	   	{
	   		continue;
	   	}
	   	for(ValueElement<?> counterElement : addBits)
	   	{
	   		if(counterElement.getType() == element.getType())
	   		{
	   			counterElement.setQuantity(counterElement.getQuantity() + element.getQuantity());
	   			element = null;
	   			break;
	   		}
	   	}
	   	if(element != null)
	   	{
	   		addBits.add(element);
	   	}
	   }
	   return result;
   }

	public ItemQtyWrapper[] getConversions()
   {
		Vector<ItemQtyWrapper> conversions = null;
		for(ValueElement<?> element : elements)
		{
			ValueElement<?> result = ExchangeRateManager.exchange(element);
			if(result != null)
			{
				if(conversions == null)
				{
					conversions = new Vector<>();
				}
				ValueType sourceCurrency = result.getType();
				ValueType destinationCurrency = element.getType();
				switch(sourceCurrency)
				{
					case DOLLAR_TOKEN:
						if(destinationCurrency == ValueType.COIN_TOKEN)
						{
							conversions.add(new ItemQtyWrapper(new CurrencyConversion(ValueType.DOLLAR_TOKEN, ValueType.GEM_TOKEN), result.getQuantity()));
							conversions.add(new ItemQtyWrapper(new CurrencyConversion(ValueType.GEM_TOKEN, ValueType.COIN_TOKEN), result.getQuantity() * ExchangeRateManager.getConversionRateFor(ValueType.DOLLAR_TOKEN, ValueType.GEM_TOKEN)));
							continue;
						}
						conversions.add(new ItemQtyWrapper(new CurrencyConversion(sourceCurrency, destinationCurrency), result.getQuantity()));
						break;
					default:
						conversions.add(new ItemQtyWrapper(new CurrencyConversion(sourceCurrency, destinationCurrency), result.getQuantity()));
				}
			}
		}
		if(conversions == null)
		{
			return null;
		}
	   return conversions.toArray(new ItemQtyWrapper[0]);
   }
	
	public static ItemCurrency getCurrencyByName(String currencyName)
	{
	   for(ValueType type : ValueType.values())
	   {
	      if(type.getID().equals(currencyName))
	      {
	         return type.getCurrency();
	      }
	   }
	   return null;
	}
}
