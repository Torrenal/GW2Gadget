package com.torrenal.craftingGadget.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemKey;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.CurrencyManager;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueKarma;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.ui.components.ColumnToggleMenuItem;
import com.torrenal.craftingGadget.ui.components.CraftingTable;
import com.torrenal.craftingGadget.ui.components.CraftingTableModel;

public class ItemList extends JPanel implements FilterListener, ActionListener
{
   private static final long serialVersionUID = 1L;

   //	private Vector<RecipeFilter> filters = new Vector<RecipeFilter>();
   private EvaluateListener listener = null;

   private JTable itemTable;

   private Vector<SelectionListener> listeners = new Vector<SelectionListener>();

   private Hashtable<ColumnType, TableColumn> columnReservationSet = new Hashtable<>();

   public enum ColumnType
   {
      ItemName("Product Name", 600),
      Cost("Cost to Make", 600),
      SellHighProfitPercent("High Profit %", 90),
      SellHighProfit("Sell High Profit", 200),
      SellLowProfit("Sell Low Profit", 200),
      HighProfitPerUnit("High Unit Profit", 200),
      LowProfitPerUnit("Low Unit Profit", 200),
      BuyVolume("Buy Volume", 80),
      SellVolume("Sell Volume", 80);

      private String name;
      private int defaultWidth;

      ColumnType(String name, int defaultWidth)
      {
         this.defaultWidth = defaultWidth;
         this.name = name;
      }

      public String toString()
      {
         switch(this)
         {
            case HighProfitPerUnit:
               return("High Profit/" + CurrencyManager.getInspectedCurrency().getSingularName());
            case LowProfitPerUnit:
               return("Low Profit/" + CurrencyManager.getInspectedCurrency().getSingularName());
            default:
               return name;
         }
      }

      public int getDefaultWidth()
      {
         return defaultWidth;
      }

   }

   public ItemList()
   {
      super(new GridBagLayout());

      initContent();

      updateList();
   }

   private void initContent()
   {
      GridBagConstraints gc = new GridBagConstraints();
      gc.anchor = GridBagConstraints.WEST;
      gc.fill = GridBagConstraints.BOTH;
      gc.weightx = 1;
      gc.weighty = 1;

      gc.gridx = 1;
      gc.gridy = 1;

      createTable();

      JScrollPane scrollMe = new JScrollPane(itemTable);
      add(scrollMe,gc);

      listener = new EvaluateListener() {
         private boolean needsUpdate;

         @Override
         public void structureUpdateEvent()
         {
            needsUpdate = true;
            Runnable doRun = new Runnable()
            {
               synchronized public void run()
               {
                  if(needsUpdate)
                  {
                     needsUpdate = false;
                     updateList();
                  }
               }

            };
            Thread thread = new Thread(doRun, "List Update");
            thread.start();
         }

         @Override
         public void contentUpdateEvent()
         {
            repaint();
         }
      };
      ContextUpdateNotifier.addContentUpdateListener(listener);


   }

   private void createTable()
   {
      ColumnType[] allColumns = ColumnType.values(); 

      itemTable = new CraftingTable(new Object[0], allColumns);
      itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      itemTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
         @Override
         public void valueChanged(ListSelectionEvent event)
         {
            Recipe selection;
            try
            {	
               int index = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
               selection = (Recipe) itemTable.getModel().getValueAt(index, 0);
            }
            catch (Throwable err)
            {
               selection = null;
            }
            fireSelectionChanged(selection);
         }
      });
      itemTable.addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e)
         {
            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
            {
               if (e.getClickCount() == 2)
               {
                  try
                  {
                     System.out.println("Clicked row " + itemTable.getSelectedRow() + " in " + this); 
//                     int row = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
//                     Source source = (Source) itemTable.getModel().getValueAt(row, 0);
//                     SourceDetails.showDetailsFor(source);
                  }
                  catch(IndexOutOfBoundsException err)
                  {}
               }
            }
         }

      });

      for(ColumnType column : allColumns)
      {
         itemTable.getColumn(column.toString()).setIdentifier(column);
      }

      initColumnWidths();

      {//Table Setup
         TableColumnModel columnModel = itemTable.getColumnModel();
         Enumeration<TableColumn> columns = columnModel.getColumns();
         TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(itemTable.getModel());
         while(columns.hasMoreElements())
         {
            TableColumn column = columns.nextElement();
            ColumnType columnType = (ColumnType) column.getIdentifier();

            column.setCellRenderer(new RecipeRenderer(columnType));
            int columnIndex = columnModel.getColumnIndex(columnType);
            sorter.setComparator(columnIndex, new RowComparator(columnType));
         }
         itemTable.setRowSorter(sorter);
      }
      ColumnType[] keepColumns = getDefaultColumns();
      Vector<ColumnType> keepColumnsSet = new Vector<ColumnType>();
      for(ColumnType column : keepColumns)
      {
         keepColumnsSet.add(column);
      }

      for(ColumnType column : allColumns)
      {
         if(!keepColumnsSet.contains(column))
         {
            disableColumn(column);
         }
      }

      //Set default sort
      {
         Vector<SortKey> sortKeys = new Vector<>();
         sortKeys.add(new SortKey(ColumnType.ItemName.ordinal(), SortOrder.ASCENDING));
         itemTable.getRowSorter().setSortKeys(sortKeys);
      }
   }

   private void initColumnWidths()
   {
      Enumeration<TableColumn> columns = itemTable.getColumnModel().getColumns();
      while(columns.hasMoreElements())
      {
         TableColumn column = columns.nextElement();
         column.setPreferredWidth(((ColumnType) column.getIdentifier()).getDefaultWidth());
      }
   }

   public static ColumnType[] getDefaultColumns()
   {
      ColumnType[] columns = {
            ColumnType.ItemName, 
            ColumnType.Cost,
            ColumnType.SellHighProfitPercent,
            ColumnType.SellHighProfit,
            ColumnType.HighProfitPerUnit,
            ColumnType.BuyVolume,
            ColumnType.SellVolume};
      return columns;
   }

   public void addItemFilter(RecipeFilter filter)
   {
      // FIXME
      //	   filters.add(filter);
      //	   filter.addFilterListener(this);
      //	   updateList();
   }

   public void updateList()
   {
      //Vector<Object[]> rows = new Vector<Object[]>();

      Map<ItemKey, Item> hardCodedItems = CookingCore.getStaticItems();
      Map<Long, APIItem> apiItems = CookingCore.getAPIItems();
      Vector<Item> items = new Vector<Item>(apiItems.size()+hardCodedItems.size());
      items.addAll(hardCodedItems.values());
      items.addAll(apiItems.values());

      try
      {
         itemTable.getColumn(ColumnType.HighProfitPerUnit).setHeaderValue(ColumnType.HighProfitPerUnit);
      } catch ( IllegalArgumentException err)
      { // Ignore, happens if the column is not displayed
      }
      try
      {
         itemTable.getColumn(ColumnType.LowProfitPerUnit).setHeaderValue(ColumnType.LowProfitPerUnit);

      } catch ( IllegalArgumentException err)
      { // Ignore, happens if the column is not displayed
      }

      //FIXME
      //		for(RecipeFilter filter : filters)
      //		{
      //			recipes = filter.filterRecipes(recipes);
      //		}
      final AbstractCollection<Item> filteredItems = items;
      Runnable doRun = new Runnable()
      {

         @Override
         public void run()
         {
            ((CraftingTableModel)itemTable.getModel()).setRowData(new Vector<Object>(filteredItems));
         }

      };
      SwingUtilities.invokeLater(doRun);
   }

   @Override
   public void filterUpdated()
   {
      ContextUpdateNotifier.notifyStructureUpdates();
   }

   private Value getHighPerUnitProfit(Recipe recipe)
   {
      Value cost = recipe.getSourceUseCost();
      Value grossSaleValue = recipe.getSalePriceLessCostBest();
      if(cost.isUnobtanium() || grossSaleValue.isUnobtanium())
      {
         return null;
      } else
      {
         Value netValue = grossSaleValue.subtract(cost);
         ItemQuantitySet[] outputs = recipe.getOutputs();
         if(outputs.length == 1)
         {
            netValue = netValue.multiply(1/outputs[0].getQuantity());
         }
         Value costPerUnit = netValue.costPer(CurrencyManager.getInspectedCurrency());
         return costPerUnit;
      }
   }
   
   private Value getLowPerUnitProfit(Recipe recipe)
   {
      Value cost = recipe.getSourceUseCost();
      Value grossSaleValue = recipe.getSalePriceLessCostFast();
      if(cost.isUnobtanium() || grossSaleValue.isUnobtanium())
      {
         return null;
      } else
      {
         Value netValue = grossSaleValue.subtract(cost);
         ItemQuantitySet[] outputs = recipe.getOutputs();
         if(outputs.length == 1)
         {
            netValue = netValue.multiply(1/outputs[0].getQuantity());
         }
         Value costPerUnit = netValue.costPer(CurrencyManager.getInspectedCurrency());
         return costPerUnit;
      }
   }	

   private class RecipeRenderer extends DefaultTableCellRenderer
   {
      private static final long serialVersionUID = 1L;

      ColumnType type;

      public RecipeRenderer(ColumnType type)
      {
         this.type = type;
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
      {
         boolean cheaperToMake = false;
         boolean invalidItem = false;
         boolean usesKarma = false;
         boolean isUnobtainium = false;
         boolean profitableToMerch = false;

         Color background = Color.WHITE;
         hasFocus = false;
         if(isSelected)
         {
            background = new Color(225,255,225);
         } else
         {
            switch (row % 4)
            {
               case 0: break;
               case 1:background = new Color(235,235,255); break;
               case 2:background = new Color(225,225,255); break;
               case 3:background = new Color(235,235,255); break;
            }
         }

         if(value instanceof Item)
         {
            //FIXME
            //				if(((Item) value).isProfitableToSellDirect())
            //				{
            //					cheaperToMake = true;
            //				}
            //FIXME
            //				if(((Item) value).isProfitableToMerch())
            //				{
            //					profitableToMerch = true;
            //				}

            if(((Item) value).getItemID() == null || ((Item) value).getItemID().isEmpty())
            {
               invalidItem = true;
            }
            if(((Item) value).getBestObtainCost().uses(ValueKarma.class))
            {
               usesKarma = true;
            }
            if(((Item) value).getBestObtainCost().isUnobtanium())
            {
               isUnobtainium = true;
            }
            Item product = ((Item) value);

            switch(type)
            {
               case Cost:
                  value = product.getBestObtainCost();
                  break;
               case ItemName:
               {
                  value = product.getName();
                  if(product.getClass() == Item.class)
                  {
                     value = "\"" + value + "\"";
                  }
                  break;
               }
               case SellHighProfitPercent:
               {
                  Value cost = product.getBestObtainCost();
                  Value price = product.getSalePriceLessSaleCostBest();
                  double costCoins = cost.getCoin();
                  double margin = (price.getCoin() - costCoins) / costCoins * 100;
                  if(!Double.isFinite(margin))
                  {
                     value = "??";
                  } else
                  {
                     if(margin > 9999.99)
                     {
                        margin = 9999.99;
                     }
                     if(margin < -9999.99)
                     {
                        margin = -9999.99;
                     }
                     value = CookingCore.doubleToString(margin) + "%";
                  }
               }
               break;
               case SellHighProfit:
                  value = product.getSalePriceLessSaleCostBest();
                  break;
               case SellLowProfit:
                  value = product.getSalePriceLessSaleCostFast();
                  break;
               case HighProfitPerUnit:
               {
                  //FIXME:
                  value = "fixme";
                  //						Recipe recipe = (Recipe) value;
                  //						value = getHighPerUnitProfit(recipe);
                  //						if(value == null)
                  //						{
                  //							value = "-";
                  //						}
               }
               break;
               case LowProfitPerUnit:
               {
                  //FIXME:
                  value = "fixme";
                  //						Recipe recipe = (Recipe) value;
                  //						value = getLowPerUnitProfit(recipe);
                  //						if(value == null)
                  //						{
                  //							value = "-";
                  //						}
               }
               break;
               case BuyVolume:
               {
                  long buying = product.getQtyBuy();
                  if(buying >= 0)
                  {
                     value = buying;
                  } else
                  {
                     value = "";
                  }
               }
               break;
               case SellVolume:
               {
                  long buying = product.getQtySell();
                  if(buying >= 0)
                  {
                     value = buying;
                  } else
                  {
                     value = "";
                  }
               }
               break;
               default:
                  value = "Non-Configured Column";
            }
         }
         Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         component.setBackground(background);
         component.setForeground(Color.black);

         if(profitableToMerch)
         {
            component.setForeground(Color.green);
         }
         if(isUnobtainium)
         {
            component.setForeground(Color.gray);
         }
         //			if(invalidRecipe)
         //			{
         //				component.setBackground(Color.pink);
         //			}
         //			if(invalidItem)
         //			{
         //				component.setBackground(Color.red);
         //			}
         if(usesKarma && !isUnobtainium)
         {
            component.setForeground(Color.magenta);
         }
         if(cheaperToMake)
         {
            component.setFont(component.getFont().deriveFont(Font.BOLD));
         }
         return component;
      }
   }

   public class RowComparator implements Comparator<Item>
   {
      ColumnType type;

      public RowComparator(ColumnType type)
      {
         this.type = type;
      }

      @Override
      public int compare(Item row1, Item row2)
      {
         switch(type)
         {
            case ItemName:
               return row1.getName().compareTo(row2.getName());
            case Cost:
               return row1.getBestObtainCost().compareTo(row2.getBestObtainCost());

            case SellHighProfitPercent:
            {
               //FIXME
               return 0;
               //	      		Double margin1 = row1.getMarketProfit();
               //	      		Double margin2 = row2.getmarketProfit();
               //	      		if(margin1 == margin2)
               //	      		{
               //	      			return 0;
               //	      		}
               //	      		if(margin1 == null)
               //	      		{
               //	      			return -1;
               //	      		}
               //	      		if(margin2 == null)
               //	      		{
               //	      			return 1;
               //	      		}
               //	      		return margin1.compareTo(margin2);
            }
            case SellHighProfit:
            {
               //FIXME
               return 0;
               //	      	   
               //	      		Value net1 = Item.getProfitHigh(row1);
               //	      		Value net2 = Item.getProfitHigh(row2);
               //	      		if(net1 == null && net2 == null)
               //	      		{
               //	      			return 0;
               //	      		}
               //	      		if(net1 == null)
               //	      		{
               //	      			return -1;
               //	      		}
               //	      		if(net2 == null)
               //	      		{
               //	      			return 1;
               //	      		}
               //	      		if(net1.isUnobtanium() && !net2.isUnobtanium())
               //	      		{
               //	      			return -1;
               //	      		}
               //	      		if(!net1.isUnobtanium() && net2.isUnobtanium())
               //	      		{
               //	      			return 1;
               //	      		}
               //	      		return (net1.compareTo(net2));
            }
            case SellLowProfit:
            {
               //FIXME
               return 0;

               //	      		Value net1 = Item.getProfitLow(row1);
               //	      		Value net2 = Item.getProfitLow(row2);
               //	      		if(net1 == null && net2 == null)
               //	      		{
               //	      			return 0;
               //	      		}
               //	      		if(net1 == null)
               //	      		{
               //	      			return -1;
               //	      		}
               //	      		if(net2 == null)
               //	      		{
               //	      			return 1;
               //	      		}
               //	      		if(net1.isUnobtanium() && !net2.isUnobtanium())
               //	      		{
               //	      			return -1;
               //	      		}
               //	      		if(!net1.isUnobtanium() && net2.isUnobtanium())
               //	      		{
               //	      			return 1;
               //	      		}
               //	      		return (net1.compareTo(net2));
            }
            case HighProfitPerUnit:
            {
               //FIXME
               return 0;
               //					Value profit1 = getHighPerUnitProfit(row1);
               //					Value profit2 = getHighPerUnitProfit(row2);
               //					if(profit1 == profit2)
               //					{
               //						return 0;
               //					}
               //					if(profit1 == null)
               //					{
               //						return -1;
               //					}
               //					
               //					if(profit2 == null)
               //					{
               //						return 1;
               //					}
               //					boolean hasUnit1 = profit1.uses(CurrencyManager.getInspectedCurrency());
               //					boolean hasUnit2 = profit2.uses(CurrencyManager.getInspectedCurrency());
               //					if(hasUnit1 == false && hasUnit2)
               //					{
               //						return -1;
               //					}
               //					if(hasUnit2 == false && hasUnit1)
               //					{
               //						return 1;
               //					}
               //
               //					return profit1.compareTo(profit2);
            }
            case LowProfitPerUnit:
            {
               //FIXME:
               return 0;
               //					Value profit1 = getLowPerUnitProfit(row1);
               //					Value profit2 = getLowPerUnitProfit(row2);
               //					if(profit1 == profit2)
               //					{
               //						return 0;
               //					}
               //					if(profit1 == null)
               //					{
               //						return -1;
               //					}
               //					if(profit2 == null)
               //					{
               //						return 1;
               //					}
               //					boolean hasUnit1 = profit1.uses(CurrencyManager.getInspectedCurrency());
               //					boolean hasUnit2 = profit2.uses(CurrencyManager.getInspectedCurrency());
               //					if(hasUnit1 == false && hasUnit2)
               //					{
               //						return -1;
               //					}
               //					if(hasUnit2 == false && hasUnit1)
               //					{
               //						return 1;
               //					}
               //					return profit1.compareTo(profit2);
            }
            case BuyVolume:
            {
               long volume1 = row1.getQtyBuy();
               long volume2 = row2.getQtyBuy();
               if(volume1 > volume2)
               {
                  return 1;
               }
               if(volume1 < volume2)
               {
                  return -1;
               }
               return 0;
            }
            case SellVolume:
            {
               long volume1 = row1.getQtySell();
               long volume2 = row2.getQtySell();
               if(volume1 > volume2)
               {
                  return 1;
               }
               if(volume1 < volume2)
               {
                  return -1;
               }
               return 0;
            }

         }
         return 0;
      }
   }

   public void addSelectionListener(SelectionListener listener)
   {
      listeners.add(listener);
   }

   public void fireSelectionChanged(Recipe newSelection)
   {
      for(SelectionListener listener : listeners)
      {
         listener.selectionChanged(newSelection);
      }
   }

   /* Mind the erasure on the parameter */
   public void toggleColumn(ColumnToggleMenuItem<ColumnType> source)
   {
      if(source.isSelected())
      {
         activateColumn(source.getColumnType());
      } else
      {
         disableColumn(source.getColumnType());
      }
   }

   private void disableColumn(ColumnType columnType)
   {
      TableColumn column = itemTable.getColumn(columnType);
      if(column == null)
      {
         return;
      }
      columnReservationSet.put(columnType, column);
      //	   itemTable.getColumnModel().getColumnIndex(columnType);
      itemTable.removeColumn(column);
      //	   ((CraftingTableModel)itemTable.getModel()).removeColumn(column);
      //      @SuppressWarnings("unchecked")
      //      TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) itemTable.getRowSorter();
      //      sorter.modelStructureChanged();
   }

   private void activateColumn(ColumnType columnType)
   {
      try
      {
         if(itemTable.getColumn(columnType) != null);
         return;
      } catch (IllegalArgumentException err)
      { 
         // Exception when we need to add it
      }

      //		TableColumnModel columnModel = itemTable.getColumnModel();
      //		
      TableColumn column = columnReservationSet.get(columnType);
      //		if(column == null)
      //		{
      //			column = new TableColumn(0, columnType.getDefaultWidth(), new RecipeRenderer(columnType), null);
      //			columnReservationSet.put(columnType,column);
      //			column.setIdentifier(columnType);
      //		}
      itemTable.addColumn(column);
      //	   ((CraftingTableModel)itemTable.getModel()).addColumn(column);
      //		column.setHeaderValue(columnType);
      //		
      //	   @SuppressWarnings("unchecked")
      //      TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) itemTable.getRowSorter();
      //      sorter.modelStructureChanged();
      //		int columnIndex = columnModel.getColumnIndex(columnType);
      //		sorter.setComparator(columnIndex, new RowComparator(columnType));
      //		itemTable.setRowSorter(sorter);
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      // TODO Auto-generated method stub
   }
}
