package com.torrenal.craftingGadget.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractCollection;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueKarma;
import com.torrenal.craftingGadget.dataModel.value.ValueSkillPoint;
import com.torrenal.craftingGadget.transactions.sources.Recipe;

public class CheckBoxFilters extends JPanel implements RecipeFilter
{
	private static final long serialVersionUID = 1L;
	private JCheckBox sellNowCheckbox;
	private JCheckBox sellLaterCheckbox;
	private JCheckBox karmaRequiresCheckbox;
	private JCheckBox karmaFreeCheckbox;
	private JCheckBox skillPointRequiresCheckbox;
	private JCheckBox skillPointFreeCheckbox;
	private Vector<FilterListener> listeners;

   public CheckBoxFilters()
   {
	   super( new GridBagLayout() );

	   ActionListener boxListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource() == karmaRequiresCheckbox)
				{
					karmaFreeCheckbox.setSelected(false);
				}
				if(e.getSource() == karmaFreeCheckbox)
				{
					karmaRequiresCheckbox.setSelected(false);
				}
				if(e.getSource() == skillPointRequiresCheckbox)
				{
					skillPointFreeCheckbox.setSelected(false);
				}
				if(e.getSource() == skillPointFreeCheckbox)
				{
					skillPointRequiresCheckbox.setSelected(false);
				}
				notifyListeners();

			}
		};

	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.anchor = GridBagConstraints.WEST;
	   gc.fill = GridBagConstraints.HORIZONTAL;
	   gc.weightx = 0;
	   gc.weighty = 1;
	   
	   gc.gridx = 1;
	   gc.gridy = 1;

	   sellNowCheckbox = new JCheckBox("Profitable", false );
	   sellNowCheckbox.addActionListener(boxListener);
	   add(sellNowCheckbox, gc);

	   gc.gridy++;
	   sellLaterCheckbox = new JCheckBox("Potential Profit", false );
	   sellLaterCheckbox.addActionListener(boxListener);
	   add(sellLaterCheckbox, gc);
	   
	   gc.gridy = 1;
	   gc.gridy++;
	   gc.gridx++;
//	   merchantCheckbox = new JCheckBox("Merchant Profit", false );
//	   merchantCheckbox.addActionListener(boxListener);
//	   add(merchantCheckbox, gc);

	   gc.gridy=1;
	   gc.gridx++;
	   karmaRequiresCheckbox = new JCheckBox("Karma Required", false );
	   karmaRequiresCheckbox.addActionListener(boxListener);
	   add(karmaRequiresCheckbox, gc);

	   gc.gridy++;
	   karmaFreeCheckbox = new JCheckBox("Karma Free", false );
	   karmaFreeCheckbox.addActionListener(boxListener);
	   add(karmaFreeCheckbox, gc);

	   gc.gridy=1;
	   gc.gridx++;
	   skillPointRequiresCheckbox = new JCheckBox("SkillPoint Required", false );
	   skillPointRequiresCheckbox.addActionListener(boxListener);
	   add(skillPointRequiresCheckbox, gc);

	   gc.gridy++;
	   skillPointFreeCheckbox= new JCheckBox("SkillPoint Free", false );
	   skillPointFreeCheckbox.addActionListener(boxListener);
	   add(skillPointFreeCheckbox, gc);
   }

	protected void notifyListeners()
   {
	   if(listeners != null)
	   {
	   	for(FilterListener listener : listeners)
	   	{
	   		listener.filterUpdated();
	   	}
	   }
	   
   }

	@Override
   public void addFilterListener(FilterListener listener)
   {
		if(listeners == null)
		{
			listeners = new Vector<FilterListener>();
		}
		listeners.add(listener);
   }	

	@Override
   public AbstractCollection<Recipe> filterRecipes(AbstractCollection<Recipe> dataToFilter)
   {
		Vector<Recipe> result = new Vector<Recipe>();

		boolean sellNow = sellNowCheckbox.isSelected();
		boolean sellLater = sellLaterCheckbox.isSelected();
		boolean karmaUsed = karmaRequiresCheckbox.isSelected();
		boolean karmaFree = karmaFreeCheckbox.isSelected();
		boolean skillUsed = skillPointRequiresCheckbox.isSelected();
		boolean skillFree = skillPointFreeCheckbox.isSelected();
		
		for(Recipe recipe : dataToFilter)
		{
			boolean keepRecipe = true;
		
			if(sellNow)
			{
				keepRecipe &= testSellNow(recipe);
			}

			if(sellLater)
			{
				keepRecipe &= testSellLater(recipe);
			}
			
			if(karmaUsed)
			{
				keepRecipe &= testUsesKarma(recipe);
			}

			if(karmaFree)
			{
				keepRecipe &= !testUsesKarma(recipe);
			}

			if(skillUsed)
			{
				keepRecipe &= testUsesSkillPoints(recipe);
			}

			if(skillFree)
			{
				keepRecipe &= !testUsesSkillPoints(recipe);
			}

			if(keepRecipe)
			{
				result.add(recipe);
			}
		}
		return result;
   }

	private boolean testUsesSkillPoints(Recipe recipe)
   {
	   return recipe.getSourceUseCost().uses(ValueSkillPoint.class);
   }

	private boolean testUsesKarma(Recipe recipe)
   {
	   return recipe.getSourceUseCost().uses(ValueKarma.class);
   }

	/** Test whether it's profitable to use this
	 * recipe using the best sale method
	 * 
	 * @param recipe
	 * @return true if profitable
	 */
	private boolean testSellLater(Recipe recipe)
   {
       Value price = recipe.getSalePriceLessCostBest();
       Value cost = recipe.getSourceUseCost();
       Value net = price.subtract(cost);
       return net.getCoin() > 0;
   }

	/** Test whether it's profitable to use this
	 * recipe using instant-sale options
	 * 
	 * @param recipe
	 * @return true if profitable
	 */
	private boolean testSellNow(Recipe recipe)
   {
	   Value price = recipe.getSalePriceFast();
	   Value cost = recipe.getSourceUseCost();
	   Value net = price.subtract(cost);
	   return net.getCoin() > 0;
   }
}
