package com.torrenal.craftingGadget.transactions.sources;

import com.torrenal.craftingGadget.ItemQuantitySet;

public class RawRecipe extends Recipe
{
	public RawRecipe(String discipline, String difficulty, ItemQuantitySet[] outputs, ItemQuantitySet[] inputs)

   {
	   super();
	   setOutputs(outputs);
	   setInputs(inputs);
	   setDifficulty(difficulty);
	   setDiscipline(discipline);
   }

}
