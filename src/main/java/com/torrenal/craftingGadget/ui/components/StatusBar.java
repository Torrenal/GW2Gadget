package com.torrenal.craftingGadget.ui.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.EvaluateListener;
import com.torrenal.craftingGadget.apiInterface.guildupgrades.GuildUpgradeDetailRequest;
import com.torrenal.craftingGadget.apiInterface.http.QueueWorker;
import com.torrenal.craftingGadget.apiInterface.items.ItemDetailRequest;
import com.torrenal.craftingGadget.apiInterface.recipies.RecipeDetailRequest;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;

public class StatusBar extends JPanel implements EvaluateListener
{

	private static final long serialVersionUID = 1L;
	private StatusLabel jsonQueueLabel;
	private StatusLabel tradingPostStatusLabel;

	public StatusBar()
	{
		super(new GridBagLayout());
		jsonQueueLabel = new StatusLabel("Initializing...");
		tradingPostStatusLabel = new StatusLabel("Idle");
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1;
		gc.weighty = 0;
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;

		
		gc.gridx = 1;
		gc.gridy = 1;

		add(tradingPostStatusLabel, gc);
		jsonQueueLabel.setHorizontalAlignment(SwingConstants.LEFT);

		gc.weightx = 0;
		gc.gridx++;
		add(Box.createHorizontalStrut(5), gc);
		
		gc.weightx = 1;
		gc.gridx++;
		add(jsonQueueLabel, gc);
		ContextUpdateNotifier.addContentUpdateListener(this);
	}

	@Override
	public void contentUpdateEvent()
	{
		int queueDepth = QueueWorker.getQueueDepth();
		queueDepth += ItemDetailRequest.getQueueDepth();
		queueDepth += RecipeDetailRequest.getQueueDepth();
		queueDepth += GuildUpgradeDetailRequest.getQueueDepth();

		if(ItemDB.isInitializing() || RecipeDB.isInitializing())
		{
			jsonQueueLabel.setText("Initializing...");
		} else if(queueDepth != 0)
		{
			jsonQueueLabel.setText("Processing " + queueDepth + " JSON queries...");
		} else
		{
			jsonQueueLabel.setText(" ");
		}
		
		if(!PriceTool.isInterfaceEnabled())
		{
			tradingPostStatusLabel.setText("TradingPost Interface off.");
		} else if(PriceTool.isStuck())
		{
			tradingPostStatusLabel.setText("TradingPost Interface has stalled.");
		} else if(PriceTool.haveWorkQueued())
		{
			tradingPostStatusLabel.setText("Loading " + PriceTool.getQueueSize() + " price updates...");
		} else
		{
			tradingPostStatusLabel.setText("Idle");
		}
	}

	@Override
	public void structureUpdateEvent()
	{
	}

	public class StatusLabel extends CLabel
	{
		private static final long serialVersionUID = 1L;

		public StatusLabel()
		{
			super();
			initBorder();
		}

		public StatusLabel(Icon image, int horizontalAlignment)
		{
			super(image, horizontalAlignment);
			initBorder();
		}

		public StatusLabel(Icon image)
		{
			super(image);
			initBorder();
		}

		public StatusLabel(String text, Icon icon, int horizontalAlignment)
		{
			super(text, icon, horizontalAlignment);
			initBorder();
		}

		public StatusLabel(String text, int horizontalAlignment)
		{
			super(text, horizontalAlignment);
			initBorder();
		}

		public StatusLabel(String text)
		{
			super(text);
			initBorder();
		}

		private void initBorder()
      {
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      }

		@Override
      public Dimension getMinimumSize()
      {
	      Dimension size = super.getMinimumSize();
	      size.width = 20;
	      return size;
      }

		@Override
      public Dimension getPreferredSize()
      {
	      Dimension size = super.getPreferredSize();
	      size.width = 20;
	      return size;
      }
	}

}
