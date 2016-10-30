package com.torrenal.craftingGadget.ui.components;

import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class CraftingTableModel extends AbstractTableModel implements
TableModel
{
   private static final long serialVersionUID = 1L;
   
	Vector<Object> columns;
	Vector<Object> rows;
	
	public CraftingTableModel(Object[] rows, Object[] columns)
   {
	   this.columns = new Vector<>();
	   for(Object column : columns)
	   {
	   	this.columns.add(column);
	   }
	   this.rows = new Vector<>();
	   for(Object row : rows)
	   {
	   	this.rows.add(row);
	   }
   }

	@Override
	public int getColumnCount()
	{
		return columns.size();
	}

	@Override
	public int getRowCount()
	{
		return rows.size();
	}

	public Object getValueAt(int rowIndex)
	{
		return rows.get(rowIndex);
	}

	@Override
   public Object getValueAt(int rowIndex, int columnIndex)
   {
      return getValueAt(rowIndex);
   }
	
	public void setRowData(final Vector<Object> rows)
	{
		if(SwingUtilities.isEventDispatchThread())
		{
			this.rows = rows;
			fireTableDataChanged();
		} else
		{
			Runnable doRun = new Runnable()
			{
				public void run()
				{
					setRowData(rows);
				}
			};
			SwingUtilities.invokeLater(doRun);
		}
	}
	
	@Override
   public String getColumnName(int column)
   {
	   return columns.get(column).toString();
   }
	
	public void addColumn(Object column)
	{
		if(!columns.contains(column))
		{
			columns.add(column);
		}
	}

	public void removeColumn(Object column)
	{
		columns.remove(column);
	}
}
