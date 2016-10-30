package com.torrenal.craftingGadget.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.ui.BagsList.ColumnType;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.StatusBar;

public class BagsListWindow extends CFrame implements ActionListener
{
   private static final long serialVersionUID = 1L;
   private Vector<SelectionListener> listeners = new Vector<SelectionListener>();
   private EvaluateListener evalListener;
   private BagsList bagsList;

   protected BagsListWindow()
   {
      //FIXME delete me
      super("Scraps and Bags Listing");

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


//      gc.gridx=6;
//      gc.gridy=1;
//      gc.gridheight = 4;
//      add(getCurrencyPanel(), gc);

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

      bagsList = new BagsList();
//      bagsList.addRecipeFilter(textFilter);
      bagsList.addSelectionListener(new SelectionListener() {

         @Override
         public void selectionChanged(Object newSelection)
         {
            notifyListeners(newSelection);
         }
      });
      add(bagsList, gc);

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

   private JMenuBar createMenuBar()
   {
      JMenuBar menuBar = new JMenuBar();

      JMenu menu = new JMenu("File");
      {
         menu.setMnemonic(KeyEvent.VK_F);
         //     menu.getAccessibleContext().setAccessibleDescription(
         //             "The only menu in this program that has menu items");
         menuBar.add(menu);

         //a group of JMenuItems
         JMenuItem menuItem = new JMenuItem("Close Bags List",
               KeyEvent.VK_L);
         menuItem.addActionListener(this);
         menu.add(menuItem);
      }

//      menu = new JMenu("Table");
//      {
//         menu.setMnemonic(KeyEvent.VK_T);
//         menu.getAccessibleContext().setAccessibleDescription(
//               "Table column selection");
//         menuBar.add(menu);
//
//         Vector<ColumnType> activeColumns = new Vector<>();
//         {
//            for(ColumnType column : RecipeList.getDefaultColumns())
//            {
//               activeColumns.add(column);
//            }
//         }
//
//         for(ColumnType column : ColumnType.values())
//         {
//            boolean defaultChecked = activeColumns.contains(column); 
//            JCheckBoxMenuItem menuItem = new ColumnToggleMenuItem(column, defaultChecked);
//            menuItem.addActionListener(this);
//            menu.add(menuItem);
//         }
//      }

//      menu = new JMenu("Windows");
//      {
//         menu.setMnemonic(KeyEvent.VK_W);
//         //     menu.getAccessibleContext().setAccessibleDescription(
//         //             "The only menu in this program that has menu items");
//         menuBar.add(menu);
//
//         //a group of JMenuItems
//         JMenuItem menuItem = new JMenuItem("Item Detail Window",
//               KeyEvent.VK_D);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(
//               KeyEvent.VK_D, ActionEvent.ALT_MASK));
//         menuItem.getAccessibleContext().setAccessibleDescription(
//               "Bring the Item Detail Window to front.");
//         menuItem.addActionListener(this);
//         menu.add(menuItem);
//
//         menuItem = new JMenuItem("Currencies...",
//               KeyEvent.VK_C);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(
//               KeyEvent.VK_C, ActionEvent.ALT_MASK));
//         menuItem.getAccessibleContext().setAccessibleDescription(
//               "Open the Currencies Editor.");
//         menuItem.addActionListener(this);
//         menu.add(menuItem);
//      }


      menu = new JMenu("Help");
      {
         menu.setMnemonic(KeyEvent.VK_H);
         //     menu.getAccessibleContext().setAccessibleDescription(
         //             "The only menu in this program that has menu items");
         menuBar.add(menu);

         //a group of JMenuItems
         JMenuItem menuItem = new JMenuItem("About",
               KeyEvent.VK_A);
         //      menuItem.setAccelerator(KeyStroke.getKeyStroke(
         //              KeyEvent.VK_F4, ActionEvent.ALT_MASK));
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
//      Object source = event.getSource();
//      if(source instanceof ColumnToggleMenuItem)
//      {
//         recipeList.toggleColumn(((ColumnToggleMenuItem) source ));
//      }
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
