package com.torrenal.craftingGadget.ui.recipeDetails;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.transactions.destinations.Destination;
import com.torrenal.craftingGadget.transactions.sources.Bag;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.ui.SelectionListener;
import com.torrenal.craftingGadget.ui.components.CraftingTable;
import com.torrenal.craftingGadget.ui.components.CraftingTableModel;

public class SourcesPanel extends JPanel
{
   private static final long serialVersionUID = 1L;
	private Collection<Object> sources = new Vector<>();
	private Vector<SelectionListener> listeners = new Vector<SelectionListener>();
	private JTable table;
	private Item item;
	private EvaluateListener evalListener;
	
	private enum ColumnType
	{
		Method,
		SourceName,
		Value;
	}
	
	public SourcesPanel()
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
		gc.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(table), gc);
		
	   table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
         public void valueChanged(ListSelectionEvent event)
         {
				Source selection;
				try
				{	
					int index = table.convertRowIndexToModel(table.getSelectedRow());
					selection = (Source) table.getModel().getValueAt(index, 0);
				}
				catch (Throwable err)
				{
					selection = null;
				}
				fireSelectionChanged(selection);
         }
		});
	   
		table.addMouseListener(new MouseAdapter() {
			@Override
		   public void mouseClicked(MouseEvent e) {
		      if (e.getClickCount() == 2) {
		         JTable target = (JTable)e.getSource();
		         int row = table.convertRowIndexToModel(target.getSelectedRow());
		         Source source = ((Source) table.getModel().getValueAt(row, 0));
		         onDoubleClick(source);
		         }
		   }
		});
		
   	evalListener = new EvaluateListener() {
			@Override
			public void contentUpdateEvent()
			{
				if(item == null)
				{
					return;
				}

				repaint();
			}

			@Override
         public void structureUpdateEvent()
         {
				if(item == null)
				{
					return;
				}
				HashSet<Object> currentSources = new HashSet<Object>(item.getSources());
				currentSources.addAll(item.getDestinations());
				if(sources.size() != currentSources.size())
				{
					sources = currentSources;
					updateTable();
				}
         }
		};
		ContextUpdateNotifier.addContentUpdateListener(evalListener);
	}

	protected void onDoubleClick(Source source)
   {
		System.err.println("Selected: "+ source.getMethodName() + " score of " + source.getSourceUseCost().getScore());
		SourceDetails.showDetailsFor(source);
   }

	private String[] getColumnNames()
	{
		String[] names =  {
				"Method",
				"Source",
				"Value"
		};
		return names;
	}
	
	public void setSources(Item item)
	{
		HashSet<Object> newSources = new HashSet<Object>(item.getSources());
		newSources.addAll(item.getDestinations());
		if(this.item == item && newSources.equals(this.sources))
		{
			return;
		}
		this.item = item;
		this.sources = new HashSet<>(newSources);
		updateTable();
		fireSelectionChanged(getCheapestRecipe(item));
	}

	private Source getCheapestRecipe(Item item)
   {
	   HashSet<Object> newSources = new HashSet<Object>(item.getSources());
	   newSources.addAll(item.getDestinations());
	   
	   Source cheapSource = null; 
	   for(Object source : newSources)
	   {
	   	if(source instanceof Recipe)
	   	{
	   	   Recipe recipe = (Recipe) source;
	   		if(cheapSource == null || recipe.getSourceUseCost().compareTo(cheapSource.getSourceUseCost()) < 0)
	   		{
	   			cheapSource = recipe;
	   		}
	   	}
	   }
	   return cheapSource;
   }

	private void buildTable()
   {
		table = new CraftingTable(sources.toArray(), getColumnNames());

	   TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());

	   TableColumn recipeColumn = table.getColumnModel().getColumn(0);
	   recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Method));
	   recipeColumn = table.getColumnModel().getColumn(1);
	   recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.SourceName));
	   recipeColumn = table.getColumnModel().getColumn(2);
	   recipeColumn.setCellRenderer(new RecipeRenderer(ColumnType.Value));

	   sorter.setComparator(0, new RowComparator(ColumnType.Method));
	   sorter.setComparator(1, new RowComparator(ColumnType.SourceName));
	   sorter.setComparator(2, new RowComparator(ColumnType.Value));

	   table.setRowSorter(sorter);
	   initColumnWidths();
   }
	
	protected void updateTable()
   {
		Collection<Object> sources = this.sources;
		CraftingTableModel model = (CraftingTableModel) table.getModel();
		model.setRowData(new Vector<Object>(sources));
   }

	
	private void initColumnWidths()
   {
			TableColumnModel tableColumnModel = table.getColumnModel();
			tableColumnModel.getColumn(0).setPreferredWidth(100);
			tableColumnModel.getColumn(1).setPreferredWidth(200);
			tableColumnModel.getColumn(2).setPreferredWidth(600);
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
			Source source = null;
			if(value instanceof Source)
			{
				source = (Source) value;
				switch(type)
				{
					case Method:
						value = ((Source) value).getMethodName();
						break;
					case Value:
						value = ((Source) value).getSourceUseCost();
						break;
					case SourceName:
					   if(value instanceof Bag)
					   {
					      value = "eep, bags need fixes!"; //((Bag) value).getSourceNameFor(this.source.getProduct());
					   } else
					   {
					      value = ((Source) value).getSourceName();
					   }
						break;
				}
			} else if (value instanceof Destination)
			{
			   Destination destination = (Destination) value;
               switch(type)
               {
                   case Method:
                       value = destination.getDestionationName();
                       break;
                   case Value:
                       value = destination.getGrossValue();
                       break;
                   case SourceName:
                      value = destination.getDestionationFullName();
                       break;
               }
			}
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			/* HIghlight the cheapest method */
			if(source == item.getSource())
			{
				component.setFont(component.getFont().deriveFont(Font.BOLD));
			}
			return component;
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
	      		String diffString1 = row1.getSourceName();
	      		String diffString2 = row2.getSourceName();
	      		return diffString1.compareTo(diffString2);
	      	case Value:
	      		return row1.getSourceUseCost().compareTo(row2.getSourceUseCost());
	      }
			return 0;
      }
   }


	public void addSelectionListener(SelectionListener selectionListener)
   {
	   listeners.add(selectionListener);
   }

	protected void fireSelectionChanged(Source newSelection)
   {
	   for(SelectionListener listener : listeners)
	   {
	   	listener.selectionChanged(newSelection);
	   }
   }
}
