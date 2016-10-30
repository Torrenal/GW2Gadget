package com.torrenal.craftingGadget.ui.recipeDetails;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.transactions.sources.Container;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.transactions.sources.Unobtainium;
import com.torrenal.craftingGadget.ui.SelectionListener;
import com.torrenal.craftingGadget.ui.components.CraftingTable;
import com.torrenal.craftingGadget.ui.components.CraftingTableModel;

public class RecipePanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   private Source source;
   private JTable table;

   private enum ColumnType
   {
      Quantity,
      ItemName,
      Value;
   }

   public RecipePanel(SourcesPanel sourcesPanel)
   {
      super();
      setLayout(new GridBagLayout());
      buildTable();

      GridBagConstraints gc = new GridBagConstraints();
      gc.fill = GridBagConstraints.BOTH;
      gc.weightx = 1;
      gc.weighty = 1;
      add(new JScrollPane(table), gc);

      sourcesPanel.addSelectionListener(new SelectionListener(){
         @Override
         public void selectionChanged(Object newSelection)
         {
            onSelectionChange(newSelection);
         }
      });
      updateTable();

   }

   protected void onSelectionChange(Object newSelection)
   {
      if(newSelection instanceof Source)
      {
         setSource((Source) newSelection);
      }
      if(newSelection == null)
      {
         setSource(null);
      }

   }

   protected void onDoubleClick(Item item)
   {
      Source source = item.getSource();
      if(source == null)
      {
         source = new Unobtainium(item);
      }
      SourceDetails.showDetailsFor(source);
   }


   private String[] getColumnNames()
   {
      String[] names =  {
            "Qty",
            "Item",
            "Value"
      };
      return names;
   }

   public void setSource(Source source)
   {
      this.source = source;
      updateTable();
      if(source != null)
      {
         ItemQuantitySet[] outputs = source.getOutputs();
         Item product = null;
         if(outputs.length == 1)
         {
            product = outputs[0].getItem();
         }
         if(product != null)
         {
            CookingCore.getPriceTool().performPriorityPricingTreeUpdate(product, false);
         }
      }
   }

   private void buildTable()
   {
      table = new CraftingTable(new Object[0], getColumnNames());

      table.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               JTable target = (JTable)e.getSource();
               int row = table.convertRowIndexToModel(target.getSelectedRow());
               Item item = ((ItemWrapper) table.getModel().getValueAt(row, 0)).getItem();
               onDoubleClick(item);
            }
         }
      });


      TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());

      TableColumn recipeColumn = table.getColumnModel().getColumn(0);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Quantity));
      recipeColumn = table.getColumnModel().getColumn(1);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.ItemName));
      recipeColumn = table.getColumnModel().getColumn(2);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Value));

      sorter.setComparator(0, new RowComparator(ColumnType.Quantity));
      sorter.setComparator(1, new RowComparator(ColumnType.ItemName));
      sorter.setComparator(2, new RowComparator(ColumnType.Value));

      table.setRowSorter(sorter);
      initColumnWidths();
   }

   private void updateTable()
   {
      Vector<Object> items = new Vector<>();
      /* items to contain only ItemWrapper objects */

      if(source instanceof Container)
      {
         Container container = (Container) source;
         items.add(new ItemWrapper(container.getContainer(), 1));
      }
      if(source instanceof Recipe)
      {
         Recipe recipe = (Recipe) source;
         ItemQuantitySet[] inputs = recipe.getInputs();
         for(ItemQuantitySet input : inputs)
         {
            items.add(new ItemWrapper(input.getItem(), input.getQuantity()));
         }
      }
      ((CraftingTableModel)table.getModel()).setRowData(items);

   }
   private void initColumnWidths()
   {
      table.getColumnModel().getColumn(0).setPreferredWidth(100);
      table.getColumnModel().getColumn(1).setPreferredWidth(200);
      table.getColumnModel().getColumn(2).setPreferredWidth(600);
      table.getColumnModel().getColumn(2).setPreferredWidth(600);
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
         if(value instanceof ItemWrapper)
         {
            ItemWrapper item = (ItemWrapper) value;
            switch(type)
            {
               case Quantity:
                  value = CookingCore.doubleToString(item.getQty());
                  break;
               case Value:
                  value = item.getItem().getBestObtainCost();
                  break;
               case ItemName:
                  value = item.getItem().getName() + " of foo";
                  if(item.getItem().getClass() == Item.class)
                  {
                     value = "\"" + value + "\"";
                  }
                  break;
            }
         }
         return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
   }


   public class RowComparator implements Comparator<ItemWrapper>
   {
      ColumnType type;

      public RowComparator(ColumnType type)
      {
         this.type = type;
      }

      @Override
      public int compare(ItemWrapper row1, ItemWrapper row2)
      {
         switch(type)
         {
            case Quantity:
            {
               double diffDouble1 = row1.getQty();
               double diffDouble2 = row2.getQty();
               if(diffDouble1 > diffDouble2)
               {
                  return 1;
               }
               if(diffDouble1 < diffDouble2)
               {
                  return -1;
               }
               return 0;
            }
            case ItemName:
            {
               String diffString1 = row1.getItem().getName();
               String diffString2 = row2.getItem().getName();
               return diffString1.compareTo(diffString2);
            }
            case Value:
               return row1.getItem().getBestObtainCost().compareTo(row2.getItem().getBestObtainCost());
         }
         return 0;
      }
   }

   /** The Item wrapper.  Wraps a quantity along with the item */
   static private class ItemWrapper
   {
      private Item item;
      private double qty;

      public ItemWrapper(Item item, double qty)
      {
         this.item = item;
         this.qty = qty;
      }

      public Item getItem()
      {
         return item;
      }

      public double getQty()
      {
         return qty;
      }

   }


}
