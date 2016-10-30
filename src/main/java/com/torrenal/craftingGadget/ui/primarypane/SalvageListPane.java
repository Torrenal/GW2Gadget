package com.torrenal.craftingGadget.ui.primarypane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;

import com.torrenal.craftingGadget.ui.ItemList;
import com.torrenal.craftingGadget.ui.SalvageList;
import com.torrenal.craftingGadget.ui.ItemList.ColumnType;
import com.torrenal.craftingGadget.ui.components.ColumnToggleMenuItem;

public class SalvageListPane extends JPanel implements PrimaryPaneInterface
{
   private static final long serialVersionUID = 1L;
   
   SalvageList salvageList;
   JMenu menu[] = null;
   private Object syncObject = new Object();
   
   public SalvageListPane()
   {
      salvageList = new SalvageList();
      setLayout(new GridBagLayout());
      GridBagConstraints gbc = new  GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = gbc.weighty = 1;
      add(salvageList, gbc);
   }

   @Override
   public String getTabName()
   {
      return "Salvage";
   }

   @Override
   public JMenu[] getPaneMenues()
   {
      if(menu != null)
      {
         return menu;
      }
      synchronized(syncObject)
      {
         if(menu != null)
         {
            return menu;
         }

         JMenu menu[] = new JMenu[1];
         menu[0] = new JMenu("Table");
         menu[0].setMnemonic(KeyEvent.VK_T);
         menu[0].getAccessibleContext().setAccessibleDescription(
               "Table column selection");


         Vector<ColumnType> activeColumns = new Vector<>();
         {
            for(ColumnType column : ItemList.getDefaultColumns())
            {
               activeColumns.add(column);
            }
         }

         for(ColumnType column : ColumnType.values())
         {
            boolean defaultChecked = activeColumns.contains(column); 
            JCheckBoxMenuItem menuItem = new ColumnToggleMenuItem<ColumnType>(column, defaultChecked);
            menuItem.addActionListener(salvageList);
            menu[0].add(menuItem);
         }
         this.menu = menu;
      }
      return menu;
   }

   @Override
   public void performOnDemandRefresh()
   {
      salvageList.updateList();
   }

   @Override
   public String getToolTip()
   {
      return "View Loot Bags";
   }

}
