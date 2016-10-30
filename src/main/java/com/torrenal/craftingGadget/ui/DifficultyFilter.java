package com.torrenal.craftingGadget.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractCollection;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.ui.components.CLabel;

public class DifficultyFilter extends JPanel implements RecipeFilter
{
	private static final long serialVersionUID = 1L;
	private JComboBox<String> comboBox;
	private Vector<FilterListener> listeners;

   public DifficultyFilter()
   {
	   super( new GridBagLayout() );
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
	   gc.weightx = 0;
	   gc.weighty = 0;
	   
	   gc.gridx = 1;
	   gc.gridy = 1;
	   add(new CLabel("Filter for level:"),gc);
	   
	   gc.gridx++;
	   String[] difficulties = {
	   		"All",
	   		"500",
	   		"450-499",
	   		"400-449",
	   		"375-399",
	   		"350-374",
	   		"325-349",
	   		"300-324",
	   		"275-299",
	   		"250-274",
	   		"225-249",
	   		"200-224",
	   		"175-199",
	   		"150-174",
	   		"125-149",
	   		"100-124",
	   		"75-99",
	   		"50-74",
	   		"25-49",
	   		"0-24"
	   };
	   comboBox = new JComboBox<String>( new DefaultComboBoxModel<String>(difficulties));
	   add(comboBox, gc);
	   comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				notifyListeners();
			}
		});
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
		
		String filterSetting = (String) comboBox.getSelectedItem();
		if("All".equals(filterSetting))
		{
			return dataToFilter;
		}

		if("Unknown".equals(filterSetting))
		{
			for(Recipe recipe : dataToFilter)
			{
				String recipeDifficutly = recipe.getDifficulty();
				if("".equals(recipe.getDifficulty()))
				{
					continue;
				}
				try
				{
					Integer.parseInt(recipeDifficutly);
				}
				catch(Throwable err)
				{
					result.add(recipe);
				}
			}
			return result;
		}

		int lowBound;
		int highBound;
		if("0".equals(filterSetting))
		{
			lowBound = 0;
			highBound = 0;
		} else if("500".equals(filterSetting))
		{
			lowBound = 500;
			highBound = 500;
		} else
		{
			String[] bits = filterSetting.split("-"); 
			lowBound=Integer.parseInt(bits[0]);
			highBound=Integer.parseInt(bits[1]);
		}
		
		for(Recipe recipe : dataToFilter)
		{
			String recipeDifficutly = recipe.getDifficulty();
			int diff;
			if("".equals(recipe.getDifficulty()))
			{
				diff = 0;
			} else 
			{
				try
				{
					diff = Integer.parseInt(recipeDifficutly);
				}
				catch (Throwable err)
				{
					diff = 9999;
				}
			}
			if(diff >= lowBound && diff <= highBound)
			{
				result.add(recipe);
			}

		}
		return result;
   }
}
