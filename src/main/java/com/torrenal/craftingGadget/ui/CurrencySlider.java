package com.torrenal.craftingGadget.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.ui.components.CSlider;

public class CurrencySlider extends JPanel implements EvaluateListener
{
	private static final long serialVersionUID = 1L;
	private CSlider slider;
	private ValueType currency;
	private boolean gagEvents = false;

	final private Object syncOb = new Object();

   public CurrencySlider(ValueType currency)
   {
	   super( new GridBagLayout() );
	   this.currency= currency; 
	   
	   GridBagConstraints gc = new GridBagConstraints();
	   
	   gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.NONE;
	   gc.weightx = 0;
	   gc.weighty = 0;
	   
	   gc.gridx = 1;
	   gc.gridy = 1;

	   slider = new CSlider(1, 1000);
	   slider.setValue((int) CookingCore.getCurrencyValue(currency));

	   Dimension minimumSize = slider.getMinimumSize();
	   minimumSize.width = 150;
	   slider.setMinimumSize(minimumSize);

	   add(slider, gc);
	   setCurrency(currency);
	   slider.addChangeListener(new ChangeListener() {

			@Override
         public void stateChanged(ChangeEvent arg0)
         {
				synchronized(syncOb)
				{
					if(!gagEvents)
					{
						CookingCore.setCurrencyValue(CurrencySlider.this.currency, 1001-slider.getValue());
					}
				}
         }
			
		});
	   
	   ContextUpdateNotifier.addContentUpdateListener(this);
   }

	public void setCurrency(ValueType currency)
   {
		synchronized(syncOb)
		{
			gagEvents = true;
			slider.setValue(1000-(int)CookingCore.getCurrencyValue(currency));
			this.currency = currency;
			Runnable doRun = new Runnable(){

				@Override
            public void run()
            {
	            gagEvents = false;
            }
				
			};
			SwingUtilities.invokeLater(doRun);
		}
	   
   }

	@Override
   public void contentUpdateEvent()
   {
	   if(slider.hasFocus())
	   {
	   	return;
	   }
		slider.setValue(1001-(int)CookingCore.getCurrencyValue(currency));
   }

	@Override
   public void structureUpdateEvent()
   {
   }
}
