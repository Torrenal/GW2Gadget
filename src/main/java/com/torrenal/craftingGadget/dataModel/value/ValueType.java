package com.torrenal.craftingGadget.dataModel.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.torrenal.craftingGadget.ItemCurrency;

public enum ValueType
{
	COIN_TOKEN("Coins", "Bronze", ValueCoin.class),
	KARMA_TOKEN("Karma", "Karma", ValueKarma.class),
	GEM_TOKEN ("Gems", "Gem", ValueGem.class),
	SKILL_POINTS("Spirit Shards", "Spirit Shard", ValueSkillPoint.class),
	DOLLAR_TOKEN("Dollars", "Dollar", ValueDollars.class),
	LAURELS_TOKEN("Laurels", "Laurel", ValueLaurel.class),
	FRACTAL_TOKENS("Fractal Relics", "Fractal Relic", ValueRelics.class), 
	PRISTINE_FRACTAL_TOKENS("Pristine Fractal Relics", "Pristine Fractal Relic", ValuePristineFractalRelics.class),
	GEODE_TOKENS("Geodes", "Geode", ValueGeodes.class),
	WvW_TOKENS("Badges of Honor", "Badge of Honor", ValueHonorBadges.class),
	AC_TOKENS("Ascalonian Tears", "Ascalonian Tear", ValueTears.class),
	CM_TOKENS("Seals of Beetletun", "Seal of Beetletun", ValueSeals.class),
	TA_TOKENS("Deadly Blooms", "Deadly Bloom", ValueBlooms.class),
	SE_TOKENS("Manifestos of the Moletariate", "Manifesto", ValueManifestos.class),
	CF_TOKENS("Flame Legion Charr Carvings", "Charr Carving", ValueCarvings.class),
	HotW_TOKENS("Symbols of Koda", "Symbol of Koda", ValueKoda.class),
	CoE_TOKENS("Knowledge Crystals", "Knowledge Crystal", ValueKnowledgeCrystals.class),
	Arah_TOKENS("Shards of Zhaitan", "Shard of Zhaitan", ValueShardsOfZhaitan.class);
	
	
	private String name;
	private String singularName;
	private Class<?> reppingClass;
	private ItemCurrency currency = null;
	
	ValueType(String pluralName, String singularName, Class<?> reppingClass)
	{
		this.name = pluralName;
		this.singularName = singularName;
		this.reppingClass = reppingClass;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public String getID()
	{
	   return super.toString();
	}
	public Class<?> getRepresentativeClass()
	{
		return reppingClass;
	}

	public String getSingularName()
   {
	   return singularName;
   }
	
	public ItemCurrency getCurrency()
	{
	   if(currency == null)
	   {
	      this.currency = new ItemCurrency(this);
	   }
	   return currency;
	}

   @SuppressWarnings("rawtypes")
   public Value getValue()
   {
      try
      {
         Class<?> repClass = getRepresentativeClass();
         Constructor<?> construct = repClass.getConstructor(double.class);
         Object[] args = {1d};
         ValueElement instance = (ValueElement)construct.newInstance(args);
         return new Value(instance);
      } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
      {
         e.printStackTrace();
      }
      return null;
   }
}
