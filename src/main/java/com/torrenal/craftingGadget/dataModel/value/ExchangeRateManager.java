package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public class ExchangeRateManager implements EvaluateListener
{
	
	static ExchangeRateManager instance = new ExchangeRateManager();
	float gemToGoldRate = 1;
	float goldToGemRate = 1/2F;
	final float relicToSkillPointRate = 1F/35F;
	final float dollarsToGemRate = 800F/10F;
	
	private ExchangeRateManager()
	{
		ContextUpdateNotifier.addContentUpdateListener(this);
	}

	@Override
   public void contentUpdateEvent()
   {
		Float newGoldRate = PriceTool.getGemToGoldExchangeRate();
		Float newGemRate = PriceTool.getGoldToGemExchangeRate();
		if(newGoldRate == null || newGemRate == null)
		{
			return;
		}
		newGemRate = 1/newGemRate;
		
		if(gemToGoldRate != newGoldRate || goldToGemRate != newGemRate)
		{
			gemToGoldRate = newGoldRate;
			goldToGemRate = newGemRate;
			ContextUpdateNotifier.notifyContentUpdates();
		}
   }
	
	static public ValueElement<?> exchange(ValueElement<?> inputElement)
	{
		return instance.exchange(inputElement, true);
	}


	private ValueElement<?> exchange(ValueElement<?> inputElement, boolean isInstanceInvocation)
	{
		switch(inputElement.getType())
		{
			case SKILL_POINTS:
			{
				double relicValue = CookingCore.getCurrencyValue(ValueType.FRACTAL_TOKENS) * ValueElement.getValueModifier(ValueType.FRACTAL_TOKENS);
				double skillValue = CookingCore.getCurrencyValue(ValueType.SKILL_POINTS) * ValueElement.getValueModifier(ValueType.SKILL_POINTS) * relicToSkillPointRate;
				if(skillValue/relicValue > 1.00001)
				{
					return new ValueRelics(inputElement.getQuantity() / relicToSkillPointRate);
				}
				return null;
			}
			case COIN_TOKEN:
			{
//				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN) * ValueElement.getValueModifier(ValueType.COIN);
//				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM) * ValueElement.getValueModifier(ValueType.GEM) * goldToGemRate;
//				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR) * ValueElement.getValueModifier(ValueType.DOLLAR) * goldToGemRate / dollarsToGemRate;
				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN_TOKEN);
				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM_TOKEN);
				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR_TOKEN);
				if(gemValue +300 < coinValue && gemValue +300 < dollarValue)
				{
					return new ValueGem(inputElement.getQuantity() / gemToGoldRate);
				}
				if(dollarValue +300 < coinValue)
				{
					return new ValueDollars(inputElement.getQuantity() / gemToGoldRate / dollarsToGemRate);
				}
				return null;
			}
			case GEM_TOKEN:
			{
//				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM) * ValueElement.getValueModifier(ValueType.GEM);
//				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN) * ValueElement.getValueModifier(ValueType.COIN) / goldToGemRate;
//				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR) * ValueElement.getValueModifier(ValueType.DOLLAR) / dollarsToGemRate;
				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM_TOKEN);
				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN_TOKEN);
				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR_TOKEN);
				if(coinValue + 300 < gemValue && coinValue +300 < dollarValue)
				{
					return new ValueCoin(inputElement.getQuantity() / goldToGemRate);
				}
				if(dollarValue +300 < gemValue)
				{
					return new ValueDollars(inputElement.getQuantity() / dollarsToGemRate);
				}
				return null;
			}
				
//			case DOLLAR:
//			{
//				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR) * ValueElement.getValueModifier(ValueType.DOLLAR);
//				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN) * ValueElement.getValueModifier(ValueType.COIN) * dollarsToGemRate;
//				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM) * ValueElement.getValueModifier(ValueType.GEM) * dollarsToGemRate / gemToGoldRate;
//				if(dollarValue < coinValue && dollarValue < gemValue)
//				{
//					return null;
//				} else
//				{
//					if(coinValue < gemValue)
//					{
//						return new ValueCoin(inputElement.getQuantity() * dollarsToGemRate / gemToGoldRate);
//					}
//					return new ValueGem(inputElement.getQuantity() * dollarsToGemRate);
//				}
//			}
			default:
				return null;
		}
	}
	
//	static public ValueElement<?> reverseExchange(ValueElement<?> inputElement)
//	{
//		return instance.reverseExchange(inputElement, true);
//	}
//
//
//	private ValueElement<?> reverseExchange(ValueElement<?> inputElement, boolean isInstanceInvocation)
//	{
//		switch(inputElement.getType())
//		{
//			case SKILL_POINTS:
//			{
//				double relicValue = CookingCore.getCurrencyValue(ValueType.FRACTAL_TOKENS) * ValueRelics.getValueModifier();
//				double skillValue = CookingCore.getCurrencyValue(ValueType.SKILL_POINTS) * ValueSkillPoint.getValueModifier();
//				if(skillValue * relicToSkillPointRate < relicValue)
//				{
//					return new ValueRelics(inputElement.getQuantity() / relicToSkillPointRate);
//				}
//				return null;
//			}
//			case GEM:
//			{
//				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM) * ValueGem.getValueModifier();
//				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR) * ValueDollars.getValueModifier() / dollarsToGemRate;
//				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN) * ValueCoin.getValueModifier() / goldToGemRate;
//				if(gemValue > dollarValue > coinValue)
//				{
//					return null;
//				}
//				if(gemValue * goldToGemRate < coinValue)
//				{
//					return new ValueCoin(inputElement.getQuantity() / goldToGemRate);
//				}
//				return null;
//			}
//			case DOLLAR:
//			{
//				double dollarValue = CookingCore.getCurrencyValue(ValueType.DOLLAR) * ValueDollars.getValueModifier();
//				double coinValue = CookingCore.getCurrencyValue(ValueType.COIN) * ValueCoin.getValueModifier() * dollarsToGemRate;
//				double gemValue = CookingCore.getCurrencyValue(ValueType.GEM) * ValueGem.getValueModifier() * dollarsToGemRate * gemToGoldRate;
//				if(dollarValue < coinValue && dollarValue < gemValue)
//				{
//					return null;
//				} else
//				{
//					if(coinValue < gemValue)
//					{
//						return new ValueCoin(inputElement.getQuantity() * dollarsToGemRate * gemToGoldRate);
//					}
//					return new ValueGem(inputElement.getQuantity() * dollarsToGemRate);
//				}
//			}
//			default:
//				return null;
//		}
//	}


	@Override
   public void structureUpdateEvent()
   { }

	public static float getConversionRateFor(ValueType fromCurrency, ValueType toCurrency)
   {
	   switch(fromCurrency)
	   {
	   	case FRACTAL_TOKENS:
	   		if(toCurrency == ValueType.SKILL_POINTS)
	   		{
	   			return instance.relicToSkillPointRate;
	   		}
	   		break;
	   	case DOLLAR_TOKEN:
	   		if(toCurrency == ValueType.GEM_TOKEN)
	   		{
	   			return instance.dollarsToGemRate;
	   		}
	   		break;
	   	case GEM_TOKEN:
	   		if(toCurrency == ValueType.COIN_TOKEN)
	   		{
	   			return instance.gemToGoldRate;
	   		}
	   	case COIN_TOKEN:
	   		if(toCurrency == ValueType.GEM_TOKEN)
	   		{
	   			return instance.goldToGemRate;
	   		}
	   		break;
	   }
		throw new Error("Invalid Arguments: from " + fromCurrency + " / to " + toCurrency);
   }
	

}
