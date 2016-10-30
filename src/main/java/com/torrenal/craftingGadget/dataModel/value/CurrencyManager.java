package com.torrenal.craftingGadget.dataModel.value;

public class CurrencyManager
{
   private static volatile ValueType inspectedCurrency = ValueType.SKILL_POINTS;

   public static ValueType getInspectedCurrency()
   {
    return inspectedCurrency;
   }

   public static void setInspectedCurrency(ValueType newCurrency)
   {
      inspectedCurrency = newCurrency;
   }

}
