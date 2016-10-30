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
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
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
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.transactions.sources.Bag;
import com.torrenal.craftingGadget.transactions.sources.DropsSource;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.components.CraftingTable;
import com.torrenal.craftingGadget.ui.components.CraftingTableModel;
import com.torrenal.craftingGadget.ui.recipeDetails.SourceDetails;

public class BagsList extends JPanel implements FilterListener, ActionListener
{
   private static final long serialVersionUID = 1L;

   //   private Vector<RecipeFilter> filters = new Vector<RecipeFilter>();
   private EvaluateListener listener = null;

   private JTable itemTable;

   private Vector<SelectionListener> listeners = new Vector<SelectionListener>();

   private Hashtable<ColumnType, TableColumn> columnReservationSet = new Hashtable<>();


   public enum ColumnType
   {
      MethodName("Method Name", 600),
      Cost("Cost to Obtain", 600),
      SellAverageProfitPercent("Expected Profit %", 120),
      SellAverageProfit("Expected Profit", 200),
      SellLowProfitPercent("Sell Low Profit %", 200),
      SellLowProfit("Sell Low Profit", 200),
//      BuyVolume("Buy Volume", 80),
//      SellVolume("Sell Volume", 80),
      ;

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
            default:
               return name;
         }
      }

      public int getDefaultWidth()
      {
         return defaultWidth;
      }

   }

   public BagsList()
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
            DropsSource selection;
            try
            {	
               int index = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
               selection = (DropsSource) itemTable.getModel().getValueAt(index, 0);
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
                     int row = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
                     Source source = (Source) itemTable.getModel().getValueAt(row, 0);
                     SourceDetails.showDetailsFor(source);
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
         sortKeys.add(new SortKey(ColumnType.MethodName.ordinal(), SortOrder.ASCENDING));
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

   static ColumnType[] getDefaultColumns()
   {
      ColumnType[] columns = {
            ColumnType.MethodName, 
            ColumnType.Cost,
            ColumnType.SellAverageProfitPercent,
            ColumnType.SellAverageProfit,
            ColumnType.SellLowProfit,
//            ColumnType.BuyVolume
            };
      return columns;
   }

//   public void addRecipeFilter(RecipeFilter filter)
//   {
//      filters.add(filter);
//      filter.addFilterListener(this);
//      updateList();
//   }

   public void updateList()
   {
      //Vector<Object[]> rows = new Vector<Object[]>();

      Map<ItemKey, Item> hardCodedItems = CookingCore.getStaticItems();
      Map<Long, APIItem> apiItems = CookingCore.getAPIItems();
      Vector<Item> items = new Vector<Item>(apiItems.size()+hardCodedItems.size());
      items.addAll(hardCodedItems.values());
      items.addAll(apiItems.values());

      AbstractCollection<DropsSource> bags = new HashSet<DropsSource>();

      for(Item item : items)
      {
         Collection<Source> sources = item.getSources();
         for(Source source : sources)
         {
            if(source instanceof Bag)
            {
               bags.add((DropsSource) source);
            }
         }
      }

      //      for(RecipeFilter filter : filters)
      //      {
      //         bags = filter.filterRecipes(bags);
      //      }
      final AbstractCollection<DropsSource> filteredRecipes = bags;
      Runnable doRun = new Runnable()
      {
         @Override
         public void run()
         {
            ((CraftingTableModel)itemTable.getModel()).setRowData(new Vector<Object>(filteredRecipes));
         }

      };
      SwingUtilities.invokeLater(doRun);
   }

   @Override
   public void filterUpdated()
   {
      ContextUpdateNotifier.notifyStructureUpdates();
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

         if(value instanceof DropsSource)
         {
            Value sourceCost = ((DropsSource) value).getSourceUseCost(); 
            if(sourceCost.isUnobtanium())
            {
               isUnobtainium = true;
            }
            switch(type)
            {
               case Cost:
                  value = sourceCost;
                  break;
               case MethodName:
               {
                  value = ((DropsSource) value).getSourceVerb() + " " + ((DropsSource) value).getSourceName();
                  break;
               }
               case SellAverageProfitPercent:
               {
                  DropsSource source = (DropsSource) value;
                  double rowCost = sourceCost.getCoin();
                  double rowMargin = (source.getSalePriceLessCostBest().getCoin() - rowCost) / rowCost * 100;

                  if(!Double.isFinite(rowMargin))
                  {
                     value = "??";
                  } else
                  {
                     if(rowMargin > 9999.99)
                     {
                        rowMargin = 9999.99;
                     }
                     if(rowMargin < -9999.99)
                     {
                        rowMargin = -9999.99;
                     }
                     value = CookingCore.doubleToString(rowMargin) + "%";
                  }
               }
               break;
               case SellAverageProfit:
               {
                  DropsSource recipe = (DropsSource) value;
                  Value profit = recipe.getSalePriceLessCostBest().subtract(sourceCost);
                  value = profit.toString();
               }
               break;
               case SellLowProfit:
               {
                  DropsSource recipe = (DropsSource) value;
                  Value profit = recipe.getSalePriceLessCostFast().subtract(sourceCost);
                  value = profit.toString();
               }
               break;
//               case BuyVolume:
//               {
//                  DropsSource recipe = (DropsSource) value;
//                  long buying = recipe.getInputsLowBuyVolume();
//                  if(buying >= 0)
//                  {
//                     value = buying;
//                  } else
//                  {
//                     value = "";
//                  }
//               }
//               break;
//               case SellVolume:
//               {
//                  DropsSource recipe = (DropsSource) value;
//                  long selling = recipe.getInputsLowSellVolume();
//                  if(selling >= 0)
//                  {
//                     value = selling;
//                  } else
//                  {
//                     value = "";
//                  }
//               }
//               break;
               default:
                  value = "Non-Configured Column: " + type;
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

   public class RowComparator implements Comparator<DropsSource>
   {
      ColumnType type;

      public RowComparator(ColumnType type)
      {
         this.type = type;
      }

      @Override
      public int compare(DropsSource row1, DropsSource row2)
      {
         switch(type)
         {
            case MethodName:
               return row1.getFullMethodName().compareTo(row2.getFullMethodName());
            case Cost:
               return row1.getSourceUseCost().compareTo(row2.getSourceUseCost());

//            case SellAverageProfitPercent:
//            {
//               Double margin1 = row1.getMargin();
//               Double margin2 = row2.getMargin();
//               if(margin1 == margin2)
//               {
//                  return 0;
//               }
//               if(margin1 == null)
//               {
//                  return -1;
//               }
//               if(margin2 == null)
//               {
//                  return 1;
//               }
//               return margin1.compareTo(margin2);
//            }
            case SellAverageProfit:
            {
               Value net1 = row1.getSalePriceLessCostBest().subtract(row1.getSourceUseCost());
               Value net2 = row2.getSalePriceLessCostBest().subtract(row2.getSourceUseCost());
               if(net1.isUnobtanium() && !net2.isUnobtanium())
               {
                  return -1;
               }
               if(!net1.isUnobtanium() && net2.isUnobtanium())
               {
                  return 1;
               }
               return (net1.compareTo(net2));
            }
            case SellLowProfit:
            {
               Value net1 = row1.getSalePriceLessCostFast().subtract(row1.getSourceUseCost());
               Value net2 = row2.getSalePriceLessCostFast().subtract(row2.getSourceUseCost());
               return net1.compareTo(net2);
            }
            case SellAverageProfitPercent:
            {
               Double row1Margin = null;
               Double row2Margin = null; 
               
               {
                  double row1Cost = row1.getSourceUseCost().getCoin();
                  row1Margin = (row1.getSalePriceLessCostBest().getCoin() - row1Cost) / row1Cost;
                  if(!Double.isFinite(row1Margin))
                  {
                     row1Margin = null;
                  }
               }
               {
                  double row2Cost = row2.getSourceUseCost().getCoin();
                  row2Margin = (row2.getSalePriceLessCostBest().getCoin() - row2Cost) / row2Cost;
                  if(!Double.isFinite(row2Margin))
                  {
                     row2Margin = null;
                  }
                  
               }

               if(row1Margin == null || row2Margin == null)
               {
                  if(row1Margin == null)
                  {
                     return -1;
                  }
                  if(row2Margin == null)
                  {
                     return 0;
                  }
                  return 1;
               }
               if(row1Margin - row2Margin < 0)
               {
                  return -1;
               }
               if(row1Margin - row2Margin > 0)
               {
                  return 1;
               }
               return 0;
            }
            default:
               new Error("Unsupported sort: " + type).printStackTrace();
         }
         return 0;
      }
   }

   public void addSelectionListener(SelectionListener listener)
   {
      listeners.add(listener);
   }

   public void fireSelectionChanged(DropsSource newSelection)
   {
      for(SelectionListener listener : listeners)
      {
         listener.selectionChanged(newSelection);
      }
   }

   //   public void toggleColumn(ColumnToggleMenuItem source)
   //   {
   //      if(source.isSelected())
   //      {
   //         activateColumn(source.getColumnType());
   //      } else
   //      {
   //         disableColumn(source.getColumnType());
   //      }
   //   }

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


   @Override
   public void actionPerformed(ActionEvent e)
   {
      System.out.println("Bags List Pane action listener unimplemented");
      // FIXME
   }
   
   //   private void activateColumn(ColumnType columnType)
   //   {
   //      try
   //      {
   //         if(itemTable.getColumn(columnType) != null);
   //         return;
   //      } catch (IllegalArgumentException err)
   //      { 
   //         // Exception when we need to add it
   //      }
   //
   //      //		TableColumnModel columnModel = itemTable.getColumnModel();
   //      //		
   //      TableColumn column = columnReservationSet.get(columnType);
   //      //		if(column == null)
   //      //		{
   //      //			column = new TableColumn(0, columnType.getDefaultWidth(), new RecipeRenderer(columnType), null);
   //      //			columnReservationSet.put(columnType,column);
   //      //			column.setIdentifier(columnType);
   //      //		}
   //      itemTable.addColumn(column);
   //      //	   ((CraftingTableModel)itemTable.getModel()).addColumn(column);
   //      //		column.setHeaderValue(columnType);
   //      //		
   //      //	   @SuppressWarnings("unchecked")
   //      //      TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) itemTable.getRowSorter();
   //      //      sorter.modelStructureChanged();
   //      //		int columnIndex = columnModel.getColumnIndex(columnType);
   //      //		sorter.setComparator(columnIndex, new RowComparator(columnType));
   //      //		itemTable.setRowSorter(sorter);
   //   }
}
