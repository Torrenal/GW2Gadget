package com.torrenal.craftingGadget.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.dataModel.value.CurrencyManager;
import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.ui.RecipeList.ColumnType;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.CLabel;
import com.torrenal.craftingGadget.ui.components.StatusBar;

public class RecipesListWindow extends CFrame implements ActionListener
{

	private static final long serialVersionUID = 1L;
	private Vector<SelectionListener> listeners = new Vector<SelectionListener>();
	private EvaluateListener evalListener;
	private CurrencySlider currencySlider;
	private RecipeList recipeList;

	protected RecipesListWindow()
	{
		super(ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
		
   	evalListener = new EvaluateListener() {
			@Override
			public void contentUpdateEvent()
			{
				repaint();
			}

			@Override
         public void structureUpdateEvent()
         {
         }
		};
		ContextUpdateNotifier.addContentUpdateListener(evalListener);
   	setupExitListener();
	}
	
	private void setupExitListener()
   {
	   addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0)
			{}
			
			@Override
			public void windowIconified(WindowEvent arg0)
			{}
			
			@Override
			public void windowDeiconified(WindowEvent arg0)
			{}
			
			@Override
			public void windowDeactivated(WindowEvent arg0)
			{}
			
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent arg0)
			{}
			
			@Override
			public void windowActivated(WindowEvent arg0)
			{}
		});
   }

	void initContent()
   {
		
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
		
	   Container cp = getContentPane();
	   cp.setLayout(new GridBagLayout());
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   gc.fill = GridBagConstraints.NONE;
	   gc.anchor = GridBagConstraints.NORTHWEST;
	   gc.weightx = 0;
	   gc.weighty = 0;
	  
	   gc.gridx = 1;
	   gc.gridy = 1;
	   
		DisciplineFilter disciplineFilter = new DisciplineFilter();
	   add(disciplineFilter, gc);
	   
	   gc.gridx++;
	   add(Box.createHorizontalStrut(10), gc);
	   gc.gridx++;
	   DifficultyFilter difficultyFilter = new DifficultyFilter();
	   add(difficultyFilter, gc);

	   
	   gc.gridx=6;
	   gc.gridy=1;
	   gc.gridheight = 4;
	   add(getCurrencyPanel(), gc);
	   
	   gc.gridwidth = 5;
	   gc.gridheight = 1;
	   gc.fill = GridBagConstraints.NONE;
	   gc.weightx = 1;
	   gc.weighty = 0;
	   gc.gridy++;
	   gc.gridy++;
	   gc.gridx=1;
	   
	   CheckBoxFilters checkBoxFilters = new CheckBoxFilters();
	   add(checkBoxFilters, gc);

	   gc.gridy++;
	   TextFilter textFilter = new TextFilter();
	   gc.gridwidth = GridBagConstraints.REMAINDER;
	   gc.fill = GridBagConstraints.HORIZONTAL;
	   add(textFilter, gc);

	   gc.fill = GridBagConstraints.BOTH;
	   gc.weighty = 1;
	   gc.gridy++;
	   
	   recipeList = new RecipeList();
	   recipeList.addRecipeFilter(difficultyFilter);
	   recipeList.addRecipeFilter(disciplineFilter);
	   recipeList.addRecipeFilter(textFilter);
	   recipeList.addRecipeFilter(checkBoxFilters);
	   recipeList.addSelectionListener(new SelectionListener() {
			
			@Override
			public void selectionChanged(Object newSelection)
			{
				notifyListeners(newSelection);
			}
		});
	   add(recipeList, gc);
	   
	   gc.gridx=1;
	   gc.gridy=9999;
	   gc.gridwidth = GridBagConstraints.REMAINDER;
	   gc.weightx = 0;
	   gc.weighty = 0;
	   gc.fill = GridBagConstraints.BOTH;
	   {
	   	add(getStatusBar(),gc);
	   }
	   
	   pack();
   }

	private Component getCurrencyPanel()
   {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Currency"));
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.NORTHWEST;
		
	   panel.add(new CLabel("Examine:"), gc);
	   gc.gridx++;
	   JComboBox<ValueType> focusCurrencyBox = getCurrencySelectorComponent();
	   panel.add(focusCurrencyBox, gc);

	   gc.gridx--;
	   gc.gridy++;
	   gc.gridwidth=2;
	   gc.gridheight = GridBagConstraints.REMAINDER;
	   {
	   	JPanel innerPanel = new JPanel(new GridBagLayout());
	   	GridBagConstraints igc = new GridBagConstraints();
	   	igc.weightx = 0;
	   	igc.gridx=1;
	   	igc.gridy=1;
	   	
	   	innerPanel.add(new CLabel("Conserve"), igc);
	   	
	   	igc.gridx++;
	   	igc.weightx = 1;
	   	innerPanel.add(Box.createHorizontalGlue(), igc);
	   	
	   	igc.gridx++;
	   	igc.weightx = 0;
	   	CLabel spendLabel = new CLabel("Spend");
	   	spendLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
	   	innerPanel.add(spendLabel, igc);
	   	
		   igc.gridy++;
		   igc.gridx=1;
		   igc.gridwidth=3;
		   igc.weightx = 1;
		   currencySlider = new CurrencySlider(CurrencyManager.getInspectedCurrency());
			innerPanel.add(currencySlider, igc);

	   	panel.add(innerPanel,gc);
	   }
	   return panel;
   }

	private JMenuBar createMenuBar()
   {
	   JMenuBar menuBar = new JMenuBar();
	   
	   JMenu menu = new JMenu("File");
	   {
	   	menu.setMnemonic(KeyEvent.VK_F);
	   	//	   menu.getAccessibleContext().setAccessibleDescription(
	   	//	           "The only menu in this program that has menu items");
	   	menuBar.add(menu);

	   	//a group of JMenuItems
	   	JMenuItem menuItem = new JMenuItem("Exit",
	   			KeyEvent.VK_X);
	   	menuItem.setAccelerator(KeyStroke.getKeyStroke(
	   			KeyEvent.VK_F4, ActionEvent.ALT_MASK));
	   	menuItem.getAccessibleContext().setAccessibleDescription(
	   			"Close the program.");
	   	menuItem.addActionListener(this);
	   	menu.add(menuItem);
	   }

	   menu = new JMenu("Table");
	   {
	   	menu.setMnemonic(KeyEvent.VK_T);
	   	menu.getAccessibleContext().setAccessibleDescription(
	   			"Table column selection");
	   	menuBar.add(menu);
	   	
	   	Vector<ColumnType> activeColumns = new Vector<>();
	   	{
	   		for(ColumnType column : RecipeList.getDefaultColumns())
	   		{
	   			activeColumns.add(column);
	   		}
	   	}

	   	for(ColumnType column : ColumnType.values())
	   	{
	   		boolean defaultChecked = activeColumns.contains(column); 
		   	JCheckBoxMenuItem menuItem = new ColumnToggleMenuItem(column, defaultChecked);
		   	menuItem.addActionListener(this);
		   	menu.add(menuItem);
	   	}
	   }
	   
	   menu = new JMenu("Windows");
	   {
	   	menu.setMnemonic(KeyEvent.VK_W);
	   	//	   menu.getAccessibleContext().setAccessibleDescription(
	   	//	           "The only menu in this program that has menu items");
	   	menuBar.add(menu);

	   	//a group of JMenuItems
	   	JMenuItem menuItem = new JMenuItem("Item Detail Window",
	   			KeyEvent.VK_D);
	   	menuItem.setAccelerator(KeyStroke.getKeyStroke(
	   			KeyEvent.VK_D, ActionEvent.ALT_MASK));
	   	menuItem.getAccessibleContext().setAccessibleDescription(
	   			"Bring the Item Detail Window to front.");
	   	menuItem.addActionListener(this);
	   	menu.add(menuItem);
	   	
	   	menuItem = new JMenuItem("Currencies...",
	   			KeyEvent.VK_C);
	   	menuItem.setAccelerator(KeyStroke.getKeyStroke(
	   			KeyEvent.VK_C, ActionEvent.ALT_MASK));
	   	menuItem.getAccessibleContext().setAccessibleDescription(
	   			"Open the Currencies Editor.");
	   	menuItem.addActionListener(this);
	   	menu.add(menuItem);
	   }

	   
	   menu = new JMenu("Help");
	   {
	   	menu.setMnemonic(KeyEvent.VK_H);
	   	//	   menu.getAccessibleContext().setAccessibleDescription(
	   	//	           "The only menu in this program that has menu items");
	   	menuBar.add(menu);

	   	//a group of JMenuItems
	   	JMenuItem menuItem = new JMenuItem("About",
	   			KeyEvent.VK_A);
//	   	menuItem.setAccelerator(KeyStroke.getKeyStroke(
//	   			KeyEvent.VK_F4, ActionEvent.ALT_MASK));
	   	menuItem.getAccessibleContext().setAccessibleDescription(
	   			"About this program.");
	   	menuItem.addActionListener(this);
	   	menu.add(menuItem);
	   }

	   return menuBar;
   }

	private Component getStatusBar()
   {
		return new StatusBar();
   }

	private JComboBox<ValueType> getCurrencySelectorComponent()
   {
	   ValueType[] alternateCurrencies;
	   {
	   	Vector<ValueType> resultSet = new Vector<>();
	   	
	   	for(ValueType type : ValueType.values())
	   	{
//	   		if(type != ValueType.COIN)
//	   		{
	   			resultSet.add(type);
//	   		}
	   	}
	   	alternateCurrencies = resultSet.toArray(new ValueType[0]);
	   }
	   final JComboBox<ValueType> focusCurrencyBox = new JComboBox<>(alternateCurrencies);
	   focusCurrencyBox.setSelectedItem(CurrencyManager.getInspectedCurrency());
	   focusCurrencyBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event)
			{
			   CurrencyManager.setInspectedCurrency((ValueType) focusCurrencyBox.getModel().getSelectedItem());
				currencySlider.setCurrency(CurrencyManager.getInspectedCurrency());
				Runnable doRun = new Runnable() {

					@Override
               public void run()
               {
						recipeList.updateList();
               }
					
				};
				new Thread(doRun, "UpdateTableAuxWorker").start();
				repaint();
			}
		});
	   return focusCurrencyBox;
   }
	
	public void addSelectionListener(SelectionListener listener)
	{
		listeners.add(listener);
	}

	private void notifyListeners(Object newSelection)
   {
      for(SelectionListener listener : listeners.toArray(new SelectionListener[0]))
      {
      	listener.selectionChanged(newSelection);	
      }
   }

	@Override
   public void actionPerformed(ActionEvent event)
   {
		Object source = event.getSource();
		if(source instanceof ColumnToggleMenuItem)
		{
			recipeList.toggleColumn(((ColumnToggleMenuItem) source ));
		}
		String command = event.getActionCommand();
		switch(command)
		{
			case "Exit":
				System.exit(0);
			case "About":
				AboutDialog.presentDialog();
				break;
			case "Currencies...":
				CurrenciesDialog.presentDialog();
				break;
			case "Item Detail Window":
				ItemDetailWindow.bringToFront();
				break;
		}
		
		
   }

   public class ColumnToggleMenuItem extends JCheckBoxMenuItem
   {

      private static final long serialVersionUID = 1L;
      
		private ColumnType columnType;

		public ColumnToggleMenuItem(ColumnType type, boolean b)
	   {
		   super(type.name(), b);
		   this.columnType = type;
	   }
		
		public ColumnType getColumnType()
		{
			return columnType;
		}
   }

	
}
