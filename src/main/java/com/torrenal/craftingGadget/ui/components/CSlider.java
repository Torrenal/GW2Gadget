package com.torrenal.craftingGadget.ui.components;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

public class CSlider extends JSlider
{
   private static final long serialVersionUID = 1L;

	public CSlider()
	{
	}

	public CSlider(int orientation)
	{
		super(orientation);
	}

	public CSlider(BoundedRangeModel brm)
	{
		super(brm);
	}

	public CSlider(int min, int max)
	{
		super(min, max);
	}

	public CSlider(int min, int max, int value)
	{
		super(min, max, value);
	}

	public CSlider(int orientation, int min, int max, int value)
	{
		super(orientation, min, max, value);
	}

	
	/** Overridden to force invocation off the event thread. */
	@Override
	public void setValue(final int value)
	{
		if(SwingUtilities.isEventDispatchThread())
		{
			setValueImpl(value);
		} else
		{
			Runnable doRun = new Runnable(){

				@Override
            public void run()
            {
					setValueImpl(value);
            }
			};
			SwingUtilities.invokeLater(doRun);
			
		}
	}

	private void setValueImpl(int value)
   {
      super.setValue(value);
   }
	

}
