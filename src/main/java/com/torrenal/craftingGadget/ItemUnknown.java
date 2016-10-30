package com.torrenal.craftingGadget;

import java.util.Collection;
import java.util.Vector;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.transactions.sources.Unobtainium;

/**
 * This item exists as a stand-in for Guild Upgrades in recipes until the
 * upgrade is identified.
 * 
 * Technically it has a value of unobtainable, and to prevent any wierd build trees,
 * has no sources nor products.
 * @author Eric
 *
 */
public class ItemUnknown extends Item
{

   private static final ItemUnknown instance = new ItemUnknown();
   private Unobtainium source;

   private ItemUnknown()
   {
      super("??");
      source = new Unobtainium(this);
   }

   @Override
   public boolean isLinkedToAPI()
   {
      return false;
   }

   @Override
   public synchronized void addSource(Source source)
   {
   }

   @Override
   public void addProduct(Item product)
   {
   }

   @Override
   public void removeProduct(Item product)
   {
   }

    @Override
   public String getName()
   {
      return "<Unknown Item>";
   }

   @Override
   public long getQtyBuy()
   {
      return 1;
   }

   @Override
   public void setQtyBuy(long qtyBuy)
   {
      throw new UnsupportedOperationException("This item has no quantity relations");
   }

   @Override
   public long getQtySell()
   {
      throw new UnsupportedOperationException("This item has no quantity relations");   
   }

   @Override
   public void setQtySell(long qtySell)
   {
      throw new UnsupportedOperationException("This item has no quantity relations");
   }

   @Override
   public Collection<Source> getSources()
   {
      Vector<Source> list = new Vector<>(1);
      list.add(source);
      return list;
   }

   @Override
   public Vector<Item> getProducts()
   {
      return new Vector<>(0);
   }

   @Override
   public Collection<Item> getDerivitives()
   {
      return new Vector<>(0);   }

   @Override
   public void removeSource(Source source)
   {
   }

   @Override
   public String getObtainString()
   {
      return source.getObtainString();
   }

   @Override
   public String getRootDescriptionFor(double quantity)
   {
      return "?? - Unobtainable";
   }

   @Override
   public Source getSource()
   {
      return source;
   }

   @Override
   public boolean isTradeable()
   {
      return false;
   }

   @Override
   public void setTradeable(boolean isTradeable)
   {
      throw new UnsupportedOperationException("This item has no trade relations");
   }

   @Override
   public String getItemID()
   {
      return "";
   }

   @Override
   public void setItemID(String itemID)
   {
      throw new UnsupportedOperationException("This item has no proper identity");
   }

   @Override
   public long getPricesUpdatedTimeStamp()
   {
      throw new UnsupportedOperationException("This item has no updateable values");
   }

   @Override
   public void setPricesUpdatedTimeStamp(long pricesUpdatedTimeStamp)
   {
      throw new UnsupportedOperationException("This item has no updateable values");
   }

   @Override
   public boolean isPriorityUpdate()
   {
      return false;
   }

   @Override
   public void setPriorityUpdate(boolean priorityUpdate)
   {
      throw new UnsupportedOperationException("This item has no updateable values");
   }

   @Override
   public synchronized void clearMarketPrices()
   {
   }

   @Override
   public Value getMerchValue()
   {
      return new Value(false);
   }

   @Override
   public synchronized void updateMerchPrice(double merchPrice)
   {
      throw new UnsupportedOperationException("This item has no updateable values");
   }

   @Override
   public Value getMarketPurchaseValue()
   {
      return new Value(false);   }

   @Override
   public String getPingCode()
   {
      return "";
   }

   @Override
   public boolean isConsumable()
   {
      return false;
   }

   @Override
   public boolean hasRecipeTies()
   {
      return false;
   }

   @Override
   public void updateNameFromTP(String nameOnTP)
   {
   }

   static public ItemUnknown getInstance()
   {
      return instance;
   }

}
