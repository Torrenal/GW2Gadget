package com.torrenal.craftingGadget.ui.components;

import java.util.Vector;

import javax.swing.JTable;

public class CraftingTable extends JTable
{
	public CraftingTable(Object[] rows, Object[] columns)
   {
	   super();
	   setModel(new CraftingTableModel(rows, columns));
   }

	public CraftingTable(Vector<?> arg0, Vector<?> arg1)
   {
	   super(arg0, arg1);
   }

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isCellEditable(int row, int col)
	{
		return false;
	}
	
	public void updateRows(Object[] rows)
	{
		
	}
}
