package com.torrenal.craftingGadget.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.components.CLabel;

public class DisciplineFilter extends JPanel implements RecipeFilter
{
	private static final long serialVersionUID = 1L;
	private JComboBox<String> comboBox;
	private Vector<FilterListener> listeners;
	private EvaluateListener listener;
	private String[] disciplineSet = null;

   public DisciplineFilter()
   {
	   super( new GridBagLayout() );
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
	   gc.weightx = 0;
	   gc.weighty = 0;
	   
	   gc.gridx = 1;
	   gc.gridy = 1;
	   add(new CLabel("Discipline:"),gc);
	   
	   gc.gridx++;
	   
	   String[] disciplines = getDisciplines();
	   disciplineSet = disciplines;
	   comboBox = new JComboBox<String>( new DefaultComboBoxModel<String>(disciplines));
	   add(comboBox, gc);
	   comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				notifyListeners();
			}
		});
	   listener = new EvaluateListener() {

			@Override
         public void contentUpdateEvent()
         {
         }

			@Override
         public void structureUpdateEvent()
         {
				String[] disciplines = getDisciplines();
				if(Arrays.equals(disciplines,disciplineSet))
				{
					return;
				}
				disciplineSet = disciplines;
				final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(disciplines);
				Runnable doRun = new Runnable() {

					@Override
               public void run()
               {
	               comboBox.setModel(model);
               }
				};
				SwingUtilities.invokeLater(doRun);
         }
	   };
	   ContextUpdateNotifier.addContentUpdateListener(listener);
	   
   }

	private String[] getDisciplines()
   {
		HashSet<String> disciplines = new HashSet<>();
		
		disciplines.add("All");
		
	   HashSet<Item> items = new HashSet<>();
	   items.addAll(CookingCore.getStaticItems().values());
	   items.addAll(CookingCore.getAPIItems().values());
	   for(Item item : items)
	   {
	   	Collection<Source> sources = item.getSources();
	   	for(Source source : sources)
	   	{
	   		if(source instanceof Recipe)
	   		{
	   			String[] recipeDisciplines = ((Recipe) source).getDisciplines();
	   			if(recipeDisciplines == null)
	   			{
	   				continue;
	   			}
	   			for(String discipline : recipeDisciplines )
	   			{
	   				disciplines.add(discipline);
	   			}
	   		}
	   	}
	   }
	   String[] result = disciplines.toArray(new String[0]);
	   Arrays.sort(result);
	   return result;
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

		for(Recipe recipe : dataToFilter)
		{
			String[] recipeDisciplines = recipe.getDisciplines();
			if(recipeDisciplines == null)
			{
				return dataToFilter;
			}
			for(String discipline : recipeDisciplines)
			{
				if(filterSetting.equals(discipline))
				{
					result.add(recipe);
					break;
				}
			}
		}
		return result;
   }
}
