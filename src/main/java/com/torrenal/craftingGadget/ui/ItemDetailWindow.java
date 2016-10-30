package com.torrenal.craftingGadget.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;
import com.torrenal.craftingGadget.ui.components.CTextField;
import com.torrenal.craftingGadget.ui.recipeDetails.ProductsPanel;
import com.torrenal.craftingGadget.ui.recipeDetails.RecipePanel;
import com.torrenal.craftingGadget.ui.recipeDetails.SourcesPanel;

public class ItemDetailWindow extends CFrame implements SelectionListener
{
   private static final long serialVersionUID = 1L;
   private static ItemDetailWindow instance = null; 
	private CTextField recipeNameField;
	private CTextField valueField;
	private ProductsPanel productsPanel;
	private SourcesPanel sourcesPanelA;
	private SourcesPanel sourcesPanelB;
	private RecipesListWindow listWindow;
	private RecipePanel recipePanel;
	private Item item = null;
	private EvaluateListener evalListener;
	private String nameText = "??";
	private String valueText = "??";
	
   public ItemDetailWindow(RecipesListWindow listWindow)
   {
   	super("Item Detail Window");
   	instance = this;
   	this.listWindow = listWindow;
   	
   	initContent();
   	pack();
   	this.listWindow.addSelectionListener(this);
   	
   	evalListener = new EvaluateListener() {
			@Override
			public void contentUpdateEvent()
			{
				if(item != null)
				{
					String oldName = nameText;
					String oldValue = valueText;
					String newName = item.getName();
					String newValue = item.getSaleValueBest().toString();
					if(newName.equals(oldName) && newValue.equals(oldValue))
					{
						return;
					}
					nameText = newName;
					valueText = newValue;
					recipeNameField.setText(nameText);
					valueField.setText(valueText);
				}
				repaint();

			}

			@Override
         public void structureUpdateEvent()
         {
         }
		};
   	ContextUpdateNotifier.addContentUpdateListener(evalListener);
   }
   
	private void initContent()
   {
	   Container cp = getContentPane();
	   cp.setLayout(new GridBagLayout());
	   
	   GridBagConstraints gbc = new GridBagConstraints();

	   gbc.gridx = 1;
	   gbc.gridy = 1;
	   gbc.weightx = 0;
	   gbc.weighty = 0;
	   gbc.anchor = GridBagConstraints.CENTER;
	   gbc.fill=GridBagConstraints.NONE;

	   cp.add(new CLabel("Item:"), gbc);

	   gbc.gridy++;
	   cp.add(new CLabel("Value:"), gbc);

	   gbc.fill=GridBagConstraints.BOTH;
	   gbc.gridy--;
	   gbc.gridx++;
	   gbc.weightx = 1;
	   recipeNameField = new CTextField();
	   recipeNameField.setEditable(false);
	   cp.add(recipeNameField,gbc);
			
	   gbc.gridy++;
	   valueField = new CTextField();
	   valueField.setEditable(false);
	   cp.add(valueField,gbc);
	   
		JTabbedPane tabPane = new JTabbedPane();
		gbc.weightx =1;
		gbc.weighty = 1;
		gbc.gridx = 1;
		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		cp.add(tabPane, gbc);
		
		tabPane.add("Products", getProductsPane());
		tabPane.add("Components", getComponentsPane());
		
   }

	private Component getComponentsPane()
   {
	   JPanel panel = new JPanel(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   gc.fill = GridBagConstraints.BOTH;
	   gc.gridx = 1;
	   gc.gridy = 1;
	   gc.weightx = 1;
	   gc.weighty = 1;
	   
	   sourcesPanelA = new SourcesPanel();
	   panel.add(sourcesPanelA, gc);

	   gc.gridx++;
	   recipePanel = new RecipePanel(sourcesPanelA);
	   panel.add(recipePanel, gc);

	   return panel;
   }

	private Component getProductsPane()
   {
	   JPanel panel = new JPanel(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   gc.fill = GridBagConstraints.BOTH;
	   gc.gridx = 1;
	   gc.gridy = 1;
	   gc.weightx = 1;
	   gc.weighty = 1;

	   productsPanel = new ProductsPanel();
	   panel.add(productsPanel, gc);

	   gc.gridx++;
	   sourcesPanelB = new SourcesPanel();
	   panel.add(sourcesPanelB, gc);
	   return panel;
   }

	@Override
   public void selectionChanged(Object newSelection)
   {
		showDetailsForImpl(newSelection);
   }
	public void showDetailsForImpl(Object object)
   {
	   if(object == null)
	   {
	   	return;
	   }
	   if(object instanceof Source)
	   {
	      ItemQuantitySet[] outputs = ((Source) object).getOutputs();
	      if(outputs == null)
	      {
	         return;
	      }
	      object = outputs[0];
	   }
	   if(object instanceof Item)
	   {
	   	item = (Item) object;

	   	sourcesPanelA.setSources(item);
	   	sourcesPanelB.setSources(item);
	   	nameText = item.getName();
	   	recipeNameField.setText(nameText);
	   	valueText = item.getSaleValueBest().toString();
	   	valueField.setText(valueText);
	   	productsPanel.setProductAndSources(item, getDerivitivesFor(item));
	   }
   }

	private Collection<Source> getDerivitivesFor(Item product)
   {
		Collection<Item> derivitives = product.getDerivitives();
		Vector<Source> derivitiveMethods = new Vector<Source>();  
		for(Item item : derivitives)
		{
			for(Source source : item.getSources())
			{
				if(source.derivesFrom(product))
				{
					derivitiveMethods.add(source);
				}
			}
		}
	   return derivitiveMethods;
   }

	public static void showDetailsFor(Object object)
   {
	   instance.showDetailsForImpl(object);
   }

	public static void bringToFront()
   {
	   if(!instance.isShowing())
	   {
	   	instance.setVisible(true);
	   }
	   instance.toFront();
   }
}
