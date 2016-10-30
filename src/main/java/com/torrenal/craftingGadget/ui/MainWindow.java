package com.torrenal.craftingGadget.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.ui.components.CFrame;
import com.torrenal.craftingGadget.ui.components.StatusBar;
import com.torrenal.craftingGadget.ui.primarypane.BagsListPane;
import com.torrenal.craftingGadget.ui.primarypane.ItemListPane;
import com.torrenal.craftingGadget.ui.primarypane.PrimaryPaneInterface;
import com.torrenal.craftingGadget.ui.primarypane.SalvageListPane;

public class MainWindow extends CFrame implements ActionListener 
{

   private static final long serialVersionUID = 1L;
   protected ValueType inspectedCurrency = ValueType.SKILL_POINTS;
   JTabbedPane tabbedPane;
   
   Hashtable<PrimaryPaneInterface, JMenuBar> menuBars = new Hashtable<>();

   protected MainWindow()
   {
      super(ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);

      setupExitListener();
      initContent();
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

   protected void initContent()
   {
      Container cp = getContentPane();
      tabbedPane = buildTabs();
      
      setJMenuBar(getMenusFor((PrimaryPaneInterface)tabbedPane.getSelectedComponent()));
      cp.add(tabbedPane, BorderLayout.CENTER);

      tabbedPane.addChangeListener(new ChangeListener() {

         @Override
         public void stateChanged(ChangeEvent e)
         {
            setJMenuBar(getMenusFor((PrimaryPaneInterface)tabbedPane.getSelectedComponent()));
         }

      });

      cp.add(new StatusBar(), BorderLayout.SOUTH);
      pack();
   }

   private JMenuBar getMenusFor(PrimaryPaneInterface primaryPane)
   {
      JMenuBar menus = menuBars.get(primaryPane);
      if(menus != null)
      {
         return menus;
      }
      menuBars.put(primaryPane, createMenusFor(primaryPane));
      return menuBars.get(primaryPane);
   }

   private JMenuBar createMenusFor(PrimaryPaneInterface primaryPane)
   {
      JMenu[] customMenus = primaryPane.getPaneMenues();
      JMenuBar menuBar = new JMenuBar();
      
      JMenu menu = new JMenu("File");
      {
       menu.setMnemonic(KeyEvent.VK_F);
       //     menu.getAccessibleContext().setAccessibleDescription(
       //             "The only menu in this program that has menu items");
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

      for(JMenu customMenu : customMenus)
      {
         menuBar.add(customMenu);
      }
      
      menu = new JMenu("Windows");
      {
       menu.setMnemonic(KeyEvent.VK_W);
       //     menu.getAccessibleContext().setAccessibleDescription(
       //             "The only menu in this program that has menu items");
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
       //     menu.getAccessibleContext().setAccessibleDescription(
       //             "The only menu in this program that has menu items");
       menuBar.add(menu);

       //a group of JMenuItems
       JMenuItem menuItem = new JMenuItem("About",
               KeyEvent.VK_A);
//     menuItem.setAccelerator(KeyStroke.getKeyStroke(
//             KeyEvent.VK_F4, ActionEvent.ALT_MASK));
       menuItem.getAccessibleContext().setAccessibleDescription(
               "About this program.");
       menuItem.addActionListener(this);
       menu.add(menuItem);
      }

      return menuBar;
   }

   private JTabbedPane buildTabs()
   {
      final JTabbedPane tabby = new JTabbedPane();
      {
         ItemListPane itemList = new ItemListPane();
         tabby.addTab(itemList.getTabName(), null, itemList, itemList.getToolTipText());
      }
      {
//                   RecipeListPane recipeList = new RecipeListPane();
//                   tabby.addTab(recipeList.getName(), recipeList, recipeList.getToolTip());
      }
      {
         BagsListPane bagsList = new BagsListPane();
         tabby.addTab(bagsList.getTabName(), null, bagsList, bagsList.getToolTipText());
      }
      {
         SalvageListPane salvageList = new SalvageListPane();
         tabby.addTab(salvageList.getTabName(), null, salvageList, salvageList.getToolTipText());
      }

      ChangeListener listener = new ChangeListener() {
         
         @Override
         public void stateChanged(ChangeEvent e)
         {
            Object source = e.getSource();
            if(source == tabby)
            {
               performOnDemandContentRefresh();
            }
            
         }

      };
      tabby.addChangeListener(listener);
      return tabby;
   }

   private void performOnDemandContentRefresh()
   {
      Component selected = tabbedPane.getSelectedComponent();
      if(selected instanceof PrimaryPaneInterface)
      {
         ((PrimaryPaneInterface) selected).performOnDemandRefresh();
      }
      
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      // TODO Auto-generated method stub
      
   }
}
