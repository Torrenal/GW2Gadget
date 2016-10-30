package com.torrenal.craftingGadget.ui;

import java.util.AbstractCollection;

import com.torrenal.craftingGadget.transactions.sources.Recipe;

public interface RecipeFilter
{
	public AbstractCollection<Recipe> filterRecipes(AbstractCollection<Recipe> dataToFilter);
	
	public void addFilterListener(FilterListener listener);
}
