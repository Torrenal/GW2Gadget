package com.torrenal.craftingGadget.transactions.sources;

import com.torrenal.craftingGadget.ItemQuantitySet;

public class ComplexConsume extends Recipe
{
   public ComplexConsume(ItemQuantitySet[] outputs, ItemQuantitySet[] inputs)
   {
      super();
      setOutputs(outputs);
      setInputs(inputs);
      setDifficulty("");
      setDiscipline("Consume");
   }
   public String getCreateVerb()
   {
      return "consume";
   }

}
