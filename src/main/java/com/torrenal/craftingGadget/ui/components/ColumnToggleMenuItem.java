package com.torrenal.craftingGadget.ui.components;

import javax.swing.JCheckBoxMenuItem;

public class ColumnToggleMenuItem<T extends Enum<?>> extends JCheckBoxMenuItem
{

   private static final long serialVersionUID = 1L;
   
     private T columnType;

     public ColumnToggleMenuItem(T type, boolean b)
    {
        super(type.name(), b);
        this.columnType = type;
    }
     
     public T getColumnType()
     {
         return columnType;
     }
}
