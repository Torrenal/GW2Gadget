package com.torrenal.craftingGadget.ui.primarypane;

import javax.swing.JMenu;

public interface PrimaryPaneInterface
{
   public String getToolTip();
   public String getTabName();
   public JMenu[] getPaneMenues();
   public void performOnDemandRefresh();
}
