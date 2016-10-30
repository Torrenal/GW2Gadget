package com.torrenal.craftingGadget.ui.recipeDetails;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.components.CraftingTable;
import com.torrenal.craftingGadget.ui.components.CraftingTableModel;

public class ProductsPanel extends JPanel
{
   private static final long serialVersionUID = 1L;
   private Item product;
   private Collection<Source> sources;
   private JTable table;
   private EvaluateListener evalListener;

   private enum ColumnType
   {
      Method,
      SourceName,
      ProductName,
      Value,
      Margin,
      SellNowMargin;

   }

   public ProductsPanel()
   {
      super(new GridBagLayout());
      buildTable();

      GridBagConstraints gc = new GridBagConstraints();

      gc.gridx = 1;
      gc.gridy = 1;

      gc.gridy++;
      gc.fill = GridBagConstraints.BOTH;
      gc.gridwidth = GridBagConstraints.REMAINDER;
      gc.weightx = 1;
      gc.weighty = 1;
      add(new JScrollPane(table), gc);

      evalListener = new EvaluateListener() {
         @Override
         public void contentUpdateEvent()
         {
            if(sources == null)
            {
               return;
            }

            repaint();
         }

         @Override
         public void structureUpdateEvent()
         {
            if(sources == null)
            {
               return;
            }
            updateTable();
         }
      };
      ContextUpdateNotifier.addContentUpdateListener(evalListener);

   }

   protected void onDoubleClick(Source source)
   {
      SourceDetails.showDetailsFor(source);
   }

   private String[] getColumnNames()
   {
      String[] names =  {
            "Method",
            "Source",
            "Item",
            "Value",
            "Margin",
            "Now Margin"
      };
      return names;
   }

   public void setProductAndSources(Item product, Collection<Source> collection)
   {
      this.product = product;
      this.sources = collection;
      updateTable();
   }

   private void buildTable()
   {
      table = new CraftingTable(new Object[0], getColumnNames());
      table.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               int row = table.convertRowIndexToModel(table.getSelectedRow());
               Source source = (Source) table.getModel().getValueAt(row, 0);
               onDoubleClick(source);
            }
         }
      });

      TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());

      TableColumn recipeColumn = table.getColumnModel().getColumn(0);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Method));
      recipeColumn = table.getColumnModel().getColumn(1);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.SourceName));
      recipeColumn = table.getColumnModel().getColumn(2);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.ProductName));
      recipeColumn = table.getColumnModel().getColumn(3);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Value));
      recipeColumn = table.getColumnModel().getColumn(4);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Margin));
      recipeColumn = table.getColumnModel().getColumn(5);
      recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.SellNowMargin));

      sorter.setComparator(0, new RowComparator(ColumnType.Method));
      sorter.setComparator(1, new RowComparator(ColumnType.SourceName));
      sorter.setComparator(2, new RowComparator(ColumnType.ProductName));
      sorter.setComparator(3, new RowComparator(ColumnType.Value));
      sorter.setComparator(4, new RowComparator(ColumnType.Margin));
      sorter.setComparator(5, new RowComparator(ColumnType.SellNowMargin));

      table.setRowSorter(sorter);
      initColumnWidths();
   }

   private void updateTable()
   {
      Collection<Source> sources = this.sources;

      ((CraftingTableModel)table.getModel()).setRowData(new Vector<Object>(sources));
   }


   private void initColumnWidths()
   {
      table.getColumnModel().getColumn(0).setPreferredWidth(100);
      table.getColumnModel().getColumn(1).setPreferredWidth(200);
      table.getColumnModel().getColumn(2).setPreferredWidth(600);
      table.getColumnModel().getColumn(3).setPreferredWidth(600);
      table.getColumnModel().getColumn(4).setPreferredWidth(90);
      table.getColumnModel().getColumn(5).setPreferredWidth(90);
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
         if(value instanceof Source)
         {
            Source source = (Source) value;
            switch(type)
            {
               case Method:
                  value = source.getMethodName();
                  break;
               case Value:
                  value = source.getSourceUseCost();
                  break;
               case ProductName:
               {
                  ItemQuantitySet[] outputs = source.getOutputs();
                  if(outputs.length != 1)
                  {
                     value = "multi-output item";
                  } else
                  {
                     if(outputs[0].getQuantity() == 1)
                     {
                        String name = outputs[0].getItem().getName();
                        if(outputs[0].getItem().getClass() == Item.class)
                        {
                           name = "\"" + name + "\"";
                        }
                        value = name;
                     } else
                     {
                        value = outputs[0].toString();
                     }
                  }
                  break;
               }
               case SourceName:
                  value = source.getSourceName();
                  break;
               case Margin:
               {
                  if(value instanceof Recipe)
                  {
                     Recipe recipe = (Recipe) value;
                     Value cost = recipe.getSourceUseCost();
                     Value price = recipe.getSalePriceLessCostBest();
                     double costCoins = cost.getCoin();
                     Double margin = (price.getCoin() - costCoins) / costCoins * 100;
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
                  } else
                  {
                     value = "--";
                  }
               }
               break;
               case SellNowMargin:
               {
                  if(value instanceof Recipe)
                  {
                     Recipe recipe = (Recipe) value;
                     Value cost = recipe.getSourceUseCost();
                     Value price = recipe.getSalePriceLessCostFast();
                     double costCoins = cost.getCoin();
                     Double margin = (price.getCoin() - costCoins) / costCoins * 100;
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
                  } else
                  {
                     value = "--";
                  }
               }
               break;
            }
         }
         return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
   }


   public class RowComparator implements Comparator<Source>
   {
      ColumnType type;

      public RowComparator(ColumnType type)
      {
         this.type = type;
      }

      @Override
      public int compare(Source row1, Source row2)
      {
         switch(type)
         {
            case Method:
            {
               String diffString1 = row1.getMethodName();
               String diffString2 = row2.getMethodName();
               return diffString1.compareTo(diffString2);
            }
            case SourceName:
            {
               String diffString1 = row1.getSourceName();
               String diffString2 = row2.getSourceName();
               return diffString1.compareTo(diffString2);
            }
            case ProductName:
            {

               String diffString1 = row1.getOutputQty(product) + " " + row1.getOutputs()[0].getItem().getName();
               String diffString2 = row2.getOutputQty(product) + " " + row2.getOutputs()[0].getItem().getName();
               return diffString1.compareTo(diffString2);
            }
            case Value:
               return row1.getSourceUseCost().compareTo(row2.getSourceUseCost());
            case Margin:
            {
               Double margin1 = null;
               {
                  Value cost = row1.getSourceUseCost();
                  Value price = row1.getSalePriceLessCostBest();
                  double costCoins = cost.getCoin();
                  margin1 = (price.getCoin() - costCoins) / costCoins * 100;
                  if(!Double.isFinite(margin1))
                  {
                     margin1 = null;
                  }
               }
               Double margin2 = null;
               {
                  Value cost = row2.getSourceUseCost();
                  Value price = row2.getSalePriceLessCostBest();
                  double costCoins = cost.getCoin();
                  margin2 = (price.getCoin() - costCoins) / costCoins * 100;
                  if(!Double.isFinite(margin2))
                  {
                     margin2 = null;
                  }
               }
               if(margin1 == margin2)
               {
                  return 0;
               }
               if(margin1 == null)
               {
                  return -1;
               }
               if(margin2 == null)
               {
                  return 1;
               }
               return margin1.compareTo(margin2);
            }
            case SellNowMargin:
            {
               Double margin1 = null;
               {
                  Value cost = row1.getSourceUseCost();
                  Value price = row1.getSalePriceLessCostFast();
                  double costCoins = cost.getCoin();
                  margin1 = (price.getCoin() - costCoins) / costCoins * 100;
                  if(!Double.isFinite(margin1))
                  {
                     margin1 = null;
                  }
               }
               Double margin2 = null;
               {
                  Value cost = row2.getSourceUseCost();
                  Value price = row2.getSalePriceLessCostFast();
                  double costCoins = cost.getCoin();
                  margin2 = (price.getCoin() - costCoins) / costCoins * 100;
                  if(!Double.isFinite(margin2))
                  {
                     margin2 = null;
                  }
               }

               if(margin1 == margin2)
               {
                  return 0;
               }
               if(margin1 == null)
               {
                  return -1;
               }
               if(margin2 == null)
               {
                  return 1;
               }
               return margin1.compareTo(margin2);
            }

         }
         return 0;
      }
   }
}
