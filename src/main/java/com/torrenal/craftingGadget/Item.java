package com.torrenal.craftingGadget;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueCoin;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;
import com.torrenal.craftingGadget.transactions.destinations.Destination;
import com.torrenal.craftingGadget.transactions.destinations.SellOnMarket;
import com.torrenal.craftingGadget.transactions.destinations.SellToMerchant;
import com.torrenal.craftingGadget.transactions.sources.Bag;
import com.torrenal.craftingGadget.transactions.sources.BuyFromMerchant;
import com.torrenal.craftingGadget.transactions.sources.BuyOnMarket;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.transactions.sources.TaskSource;
import com.torrenal.craftingGadget.util.Base64;


public class Item extends ItemKey
{
   private static long itemIndex = 1;
   private Source cheapSource = null;
   private Value cheapSourceValue = null;
   private Destination bestDestination = null;
   private Value destinationValueGrossFast = null;
   private Value destinationValueGrossBest = null;
   private Value destinationValueNetFast = null;
   private Value destinationValueNetBest = null;
   private double rawSellTomerchantPrice = 0;
   
   private boolean isTradeable = true;
   private long pricesUpdatedTimeStamp = 0;
   private boolean priorityUpdate = false;
   private long qtyBuy = -itemIndex;
   private long qtySell = -itemIndex;
   protected Object nameSync = new Object();
   private APIItem apiItem = null;

   private Collection<Source> sources = Collections.synchronizedCollection(new HashSet<Source>());
   private Object sourcesLock = new Object();
   Collection<Source> unmodifiableSources = Collections.unmodifiableCollection(sources);
   private Collection<Destination> destinations = Collections.synchronizedCollection(new HashSet<Destination>());
   private Object destinationsLock = new Object();
   Collection<Destination> unmodifiableDestinations = Collections.unmodifiableCollection(destinations);

   private Vector<Item> products = new Vector<Item>();
   private String itemID = null;

   public Item(String name)
   {
      super(name);
//      System.out.println("Instantiating " + getClass().getSimpleName() + "(" + name + ")");
      itemIndex++;
   }

   public String toString()
   {
      if(cheapSource == null)
      {
         return name + " as unobtainium";
      }
      return name + " " + cheapSource.getObtainOneCost(this) + "\n" + indent(cheapSource.toString());
   }

   public boolean isLinkedToAPI()
   {
      return false;
   }

   private String indent(String string)
   {
      StringBuilder ret = new StringBuilder();
      String[] bits = string.split("\n");
      for(String bit : bits)
      {
         ret.append("\n   ").append(bit);
      }
      if(ret.length() == 0)
      {
         return "";
      }
      return ret.substring(1);
   }
   
   /**
    * Return the best value of this product, less selling costs 
    * @return
    */
   public Value getSalePriceLessSaleCostBest()
   {
      Value value = destinationValueNetBest;
      if(value == null)
      {
         synchronized(destinationsLock)
         {
            for(Destination destination : destinations)
            {
               if(value == null)
               {
                  value = destination.getNetValue();
                  continue;
               }
               Value testValue = destination.getNetValue();
               if(value.compareTo(testValue) < 0)
               {
                  value = testValue;
               }
            }
            if(value == null)
            {
               value = new Value(false);
            }
            destinationValueNetBest = value;
         }
      }
      return value;
   }
   /**
    * Return the instant sale value of this product, less selling costs 
    * @return
    */

   public Value getSalePriceLessSaleCostFast()
   {
      Value value = destinationValueNetFast;
      if(value == null)
      {
         synchronized(destinationsLock)
         {
            for(Destination destination : destinations)
            {
               Value testValue;
               if(destination instanceof SellOnMarket)
               {
                 testValue = ((SellOnMarket) destination).getFastSaleNetValue();
               } else
               {
                  testValue = destination.getNetValue();
               }
               
               if(value == null)
               {
                  value = testValue;
                  continue;
               }
               if(value.compareTo(testValue) < 0)
               {
                  value = testValue;
               }
            }
            if(value == null)
            {
               value = new Value(false);
            }
            destinationValueNetFast = value;
         }
      }
      return value;
   }
   
   /**
    * Return the Best sale value of this product, without consideration to sale costs 
    * @return
    */   
   public Value getSaleValueBest()
   {
      Value value = destinationValueGrossBest;
      if(value == null)
      {
         return new Value(false);
      }
      return value;
   }
   /**
    * Return the Instant sale value of this product, without consideration to sale costs 
    * @return
    */   
   public Value getSaleValueFast()
   {
      Value value = destinationValueGrossFast;
      if(value == null)
      {
         return new Value(false);
      }
      return value;
   }

   
   public void addDestination(Destination destination)
   {
      if(!isTradeable())
      {
         if(destination instanceof SellOnMarket)
         {
            return;
         }
      }
      
      destinations.add(destination);
      destinationValueNetFast = null;
      destinationValueNetBest = null;
      EvaluateEngine.evaluate(products);

      
   }
   
   public synchronized void addSource(Source source)
   {
      if(!isTradeable())
      {
         if(source instanceof BuyOnMarket)
         {
            return;
         }
      }

      source.linkToTradingPost();
      synchronized(sourcesLock)
      {
         sources.add(source);
      }

      EvaluateEngine.evaluate(this);
   }

   public void addProduct(Item product)
   {
      if(product == null)
      {
         throw new NullPointerException();
      }
      
      if(!products.contains(product))
      {
         products.add(product);
         if(isLinkedToAPI() && isTradeable())
         {
            PriceTool.queueItemForStandardUpdates(Long.parseLong(getItemID()));
         }
         if(product.isLinkedToAPI() && product.isTradeable())
         {
            PriceTool.queueItemForStandardUpdates(Long.parseLong(product.getItemID()));
         }
      }		
      EvaluateEngine.evaluate(product);
   }

   public void removeProduct(Item product)
   {
      if(products.remove(product))
      {
         EvaluateEngine.evaluate(product);
      }

   }

   public Value getBestObtainCost()
   {
      if(cheapSource != null)
      {
         return cheapSource.getObtainOneCost(this);
      }
      return new Value(true);
   }

   public String getName()
   {
      if(itemLevel == null || name.contains("["))
      {
         return name;
      }
      return name + "[" + itemLevel+"]";
   }

   /**
    * Tricky method, needs to be concurrency friendly, 
    * as both the loader thread and the daemon thread may invoke this,
    * and worse, the item or its sources may update while this method is
    * being invoked.
    */
   public synchronized void evaluatePricing()
   {
      Source newCheapSource = null;
      Value newCheapValue = null;
      Source taskSource = null;
      //		
      //		if("Sunrise [80](Legendary)".equals(name))
      //		{
      //			System.err.println("Found apple");
      //		}
      
      boolean broadcast = false;
      // Evaluate preferred source
      synchronized(sourcesLock)
      {
         for(Source method : sources)
         {
            method.discardCalculatedValues();
            if(method instanceof Bag)
            {
               method.toString();
            }
            
            Value methodValue;
            methodValue = method.getObtainOneCost(this).convertCurrency();
            
            //TODO: Better (read: Configurable) handling of task price comparisons
            // There may be some tasks without associated cost, and some tasks with.
            if(method instanceof TaskSource)
            {
               taskSource = method;
               continue;
            }

            if(!isTradeable)
            {
               if(method instanceof BuyOnMarket)
               {
                  continue;
               }
            }
            if(qtySell == 0 && method instanceof BuyOnMarket)
            {
               continue; // Account Bound?  Nobody's selling these
            }

            if(newCheapSource == null)
            {
               newCheapSource = method;
               newCheapValue = methodValue;
            } else
            { 
               int compareResult = methodValue.compareTo(newCheapValue);
               // Favor BuyFromMerchant when values are equal.
               if(compareResult == 0 && method instanceof BuyFromMerchant)
               {
                  compareResult = -1;
               }
               if(compareResult < 0)
               {
                  newCheapSource = method;
                  newCheapValue = methodValue;
               }
            }
         }
         if(newCheapSource == null && taskSource != null)
         {
            newCheapSource = taskSource;
            newCheapValue = taskSource.getObtainOneCost(this);
         }

         broadcast |= cheapSource != newCheapSource;
         if(!broadcast)
         {
            if(cheapSourceValue == null && newCheapValue == null)
            {
               broadcast = false;
            }
            if(cheapSourceValue == null || newCheapValue == null)
            {
               broadcast = true;
            }
         }
         if(!broadcast)
         {
            broadcast = !cheapSourceValue.equals(newCheapValue);
         }
         cheapSource = newCheapSource;
         cheapSourceValue = newCheapValue;
      }
      
      // Evaluate preferred destination
      synchronized(destinationsLock)
      {
         // We want the best value based on net (sale value less any market costs)
         // So evaluate accordingly, and capture net and gross values both.
         Destination newBestNetDestination = null;
         Value newBestDestinationNetValue = null;
         Value newBestDestinationGrossValue = null;
         SellOnMarket market = null;
         SellToMerchant merchant = null;
         for(Destination destination : destinations)
         {
            if(destination instanceof SellOnMarket)
            {
               market = (SellOnMarket) destination;
            } else if (destination instanceof SellToMerchant)
            {
               merchant = (SellToMerchant) destination;
            }
            
              if(newBestNetDestination == null)
              {
                 newBestNetDestination = destination;
                 newBestDestinationNetValue = destination.getNetValue();
                 continue;
              }
              Value destinationValue = destination.getNetValue();
              if(destinationValue.compareTo(newBestDestinationNetValue) > 0)
              {
                 newBestNetDestination = destination;
                 newBestDestinationNetValue = destination.getNetValue();
              }
         }
         if(!broadcast)
         {
            if(bestDestination != newBestNetDestination)
            {
               broadcast = true;
            }
         }
         if(!broadcast && newBestDestinationNetValue != null)
         {
            if(!newBestDestinationNetValue.equals(destinationValueGrossBest))
            {
               broadcast = true;
            }
         }
         
         Value merchantValue = null;
         Value marketFastValue = null;
         if(merchant != null)
         {
            merchantValue = merchant.getNetValue();
         }
         if(market != null)
         {
            marketFastValue = market.getFastSaleNetValue();
         }
         if(merchantValue == null || marketFastValue == null)
         {
            if(merchantValue == null)
            {
               destinationValueGrossFast = marketFastValue;
            } else
            {
               destinationValueGrossFast = merchantValue;
            }
         } else
         {
            boolean marketBest = merchantValue.compareTo(marketFastValue) < 0;
            if(marketBest)
            {
               destinationValueGrossFast = marketFastValue;
            } else
            {
               destinationValueGrossFast = merchantValue;
            }
         }

         bestDestination = newBestNetDestination;
         destinationValueNetBest = newBestDestinationNetValue;
         if(newBestNetDestination == null)
         {
            destinationValueNetBest = null;
         } else
         {
            destinationValueGrossBest = newBestNetDestination.getGrossValue();
         }
         
         // Zero out the values that we calc on demand.
         destinationValueNetFast = null;



      }
      if(broadcast)
      {
         @SuppressWarnings("unchecked")
         Vector<Item> productsClone = (Vector<Item>) products.clone();
         for(Item product : productsClone)
         {
            if(product != this)
            {
               EvaluateEngine.evaluate(product);
            }
         }
      }
   }

   public long getQtyBuy()
   {
      return qtyBuy;
   }

   public void setQtyBuy(long qtyBuy)
   {
      this.qtyBuy = qtyBuy;
   }

   public long getQtySell()
   {
      return qtySell;
   }

   public void setQtySell(long qtySell)
   {
      this.qtySell = qtySell;
   }

   public Collection<Source> getSources()
   {
 
      return unmodifiableSources;
   }

   public Collection<Destination> getDestinations()
   {
      return unmodifiableDestinations;
   }

   @SuppressWarnings("unchecked")
   public Vector<Item> getProducts()
   {
      return (Vector<Item>) products.clone();
   }


   public Collection<Item> getDerivitives()
   {
      return Collections.unmodifiableCollection(products);
   }

   public void removeSource(Source source)
   {
      if(cheapSource == source)
      {
         cheapSource = null;
         cheapSourceValue = null;
      }
      sources.remove(source);
      EvaluateEngine.evaluate(this);
      EvaluateEngine.evaluate(products);
   }

   public void removeDestination(Destination source)
   {
      destinations.remove(source);
      EvaluateEngine.evaluate(products);
   }

   public String getObtainString()
   {
      Source source = cheapSource;
      if(source == null)
      {
         return "Unobtanium";
      }
      return source.getObtainString();
   }

   public String getRootDescriptionFor(double quantity)
   {
      Source source = cheapSource;
      if(source == null)
      {
         StringBuilder ret = new StringBuilder();
         String qty = CookingCore.doubleToString(quantity);
         ret.append(qty).append(" ").append(getName());
         ret.append("\n   - ").append("Unobtainable");
         return ret.toString();
      }
      return source.getIngredientDescriptionFor(quantity);
   }

   public Source getSource()
   {
      return cheapSource;
   }

   public boolean isTradeable()
   {
      return isTradeable;
   }

   public void setTradeable(boolean isTradeable)
   {
      this.isTradeable = isTradeable;
      if(isTradeable == false)
      {
         Vector<Source> sourcesCopy = new Vector<>(sources);
         for(Source method : sourcesCopy)
         {
            if(method instanceof BuyOnMarket)
            {
               removeSource(method);
            }
         }
         Vector<Destination> destinationsCopy = new Vector<>(destinations);
         for(Destination method : destinationsCopy)
         {
            if(method instanceof SellOnMarket)
            {
               removeDestination(method);
            }
         }

      }
   }

   public String getItemID()
   {
      return itemID ;
   }

   /*
    * Sets the item ID for this item.
    * 
    * If invoked more than once for a single item, it overrides the ID to "", effectively disabling pricing updates for the item.
    */
   public void setItemID(String itemID)
   {
      if(this.itemID == null)
      {
         this.itemID = itemID;
      } else
      {
         this.itemID = "";
      }
   }

   public long getPricesUpdatedTimeStamp()
   {
      return pricesUpdatedTimeStamp;
   }

   public void setPricesUpdatedTimeStamp(long pricesUpdatedTimeStamp)
   {
      this.pricesUpdatedTimeStamp = pricesUpdatedTimeStamp;
   }

   public boolean isPriorityUpdate()
   {
      return priorityUpdate;
   }

   public void setPriorityUpdate(boolean priorityUpdate)
   {
      this.priorityUpdate = priorityUpdate;
   }

   public synchronized void clearMarketPrices()
   {

      synchronized(sourcesLock)
      {
         for(Source source : sources.toArray(new Source[0]) )
         {
            if(source instanceof BuyOnMarket)
            {
               sources.remove(source);
            }
         }
         for(Destination destination : destinations.toArray(new Destination[0]) )
         {
            if(destination instanceof SellToMerchant)
            {
               destinations.remove(destination);
            }
            if(destination instanceof SellOnMarket)
            {
               destinations.remove(destination);
            }
         }
      }
   }

   /** Returns the merchant price supplied by the API - that is,
    * what joe-merchant will pay to take the item off your hands   
    * @return
    */
   public double getMerchCoins()
   {
      return rawSellTomerchantPrice;
   }
   public Value getMerchValue()
   {
      //TODO Possibly allow for multiple different merchant values (sell for Dungeon Tokens or Sell for Karma, or etc...)
      for(Destination destination : getDestinations())
      {
         if(destination instanceof SellToMerchant)
         {
            return destination.getGrossValue();
         }
      }
      return new Value(false);
   }

   public synchronized void updateMerchPrice(double merchPrice)
   {
      rawSellTomerchantPrice = merchPrice;
      for(Destination destination : getDestinations())
      {
         if(destination instanceof SellToMerchant)
         {
            if(merchPrice == 0)
            {
               removeDestination(destination);
               return;
            }
            Value merchValue = new Value(new ValueCoin(merchPrice));
            Value currentValue = destination.getGrossValue();
            if(currentValue.equals(merchValue))
            {
               return;
            }
            ((SellToMerchant)destination).setValue(merchValue);
            EvaluateEngine.evaluate(this);
            return;
         }
      }
      if(merchPrice != 0)
      {
         addDestination(new SellToMerchant(new Value(new ValueCoin(merchPrice))));
      }
   }

   public Value getMarketSaleValueBest()
   {
      for(Destination destination : getDestinations())
      {
         if(destination instanceof SellOnMarket)
         {
            return destination.getGrossValue();
         }
      }
      return new Value(false);
   }

   public Value getMarketSaleValueFast()
   {
      for(Destination destination : getDestinations())
      {
         if(destination instanceof SellOnMarket)
         {
            return ((SellOnMarket) destination).getFastSaleGrossValue();
         }
      }
      return new Value(false);
   }

   public Value getMarketPurchaseValue()
   {
      for(Source source : getSources())
      {
         if(source instanceof BuyOnMarket)
         {
            return source.getObtainOneCost(this);
         }
      }
      return new Value(false);
   }

   public Value getProfitBest()
   {
      Value obtainOne = cheapSourceValue;
      Value saleValue = destinationValueGrossBest;
      if(obtainOne == null)
      {
         obtainOne = new Value(true);
      }
      if(saleValue == null)
      {
         saleValue = new Value(false);
      }
      
      Value profitValue = saleValue.subtract(obtainOne);
      return profitValue;
   }
   
   public Value getProfitFast()
   {
      Value obtainOne = cheapSourceValue;
      Value saleValue = destinationValueGrossFast;
      if(obtainOne == null)
      {
         obtainOne = new Value(true);
      }
      if(saleValue == null)
      {
         saleValue = new Value(false);
      }
      
      Value profitValue = saleValue.subtract(obtainOne);
      return profitValue;
   }

   public void convertItem(Item staticItem, APIItem dynamicItem)
   {
      Source[] sourcesSet = sources.toArray(new Source[0]);

      for(Source source : sourcesSet)
      {
         if(source instanceof Recipe)
         {
            ((Recipe) source).convertItem(staticItem, dynamicItem);
         }
      }

      Item[] productsCopy = products.toArray(new Item[0]);
      for(Item product : productsCopy)
      {
         if(product == staticItem)
         {
            products.remove(product);
            products.add(staticItem);
         }
      }

   }

   public String getPingCode()
   {
      if(getItemID() == null || getItemID() == "")
      {
         return "??";
      }

      StringBuilder ret = new StringBuilder();

      ret.append("[&");
      byte[] bytes = getEncodeBytes(getItemID());
      String encoded = Base64.encode(bytes);
      while(encoded.length() > 0 && Character.isWhitespace(encoded.charAt(encoded.length()-1)))
      {
         encoded = encoded.substring(0, encoded.length() - 1);
      }
      ret.append(encoded);
      ret.append("]");
      return ret.toString();
   }

   static public String getPingCode(String itemID)
   {
      if(itemID == null || itemID == "")
      {
         return "??";
      }

      StringBuilder ret = new StringBuilder();

      ret.append("[&");
      byte[] bytes = getEncodeBytes(itemID);
      String encoded = Base64.encode(bytes);
      while(encoded.length() > 0 && Character.isWhitespace(encoded.charAt(encoded.length()-1)))
      {
         encoded = encoded.substring(0, encoded.length() - 1);
      }
      ret.append(encoded);
      ret.append("]");
      return ret.toString();
   }
   static public byte[] getEncodeBytes(String itemID)
   {
      byte bytes[] = new byte[12];
      bytes[0] = 2;
      bytes[1] = 1;
      long value = Long.parseLong(itemID);
      bytes[2] = (byte) (value % 256);
      value = value / 256;
      bytes[3] = (byte) (value % 256);
      value = value / 256;
      bytes[4] = (byte) (value % 256);
      value = value / 256;
      bytes[5] = (byte) (value % 256);
      value = value / 256;
      if(value == 0)
      {
         bytes = Arrays.copyOf(bytes, 6);
         return bytes;
      }

      bytes[6] = (byte) (value % 256);
      value = value / 256;
      bytes[7] = (byte) (value % 256);
      value = value / 256;
      bytes[8] = (byte) (value % 256);
      value = value / 256;
      if(value == 0)
      {
         bytes = Arrays.copyOf(bytes, 9);
         return bytes;
      }
      bytes[9] = (byte) (value % 256);
      bytes[10] = 0;
      bytes[11] = 0;
      return bytes;
   }

   public boolean isConsumable()
   {
      return false;
   }

   public boolean hasRecipeTies()
   {
      if(products.size() > 0)
      {
         return true;
      }
      for(Source method : sources.toArray(new Source[0]))
      {
         if(method instanceof BuyFromMerchant)
            continue;
         return true;
      }
      return false;
   }

   public void updateNameFromTP(String nameOnTP)
   {
      if(nameOnTP == null || nameOnTP.isEmpty())
      {
         return;
      }

      synchronized(nameSync)
      {
         if("??".equals(this.name))
         {
            this.name = "\""+nameOnTP+"\"";
            ContextUpdateNotifier.notifyContentUpdates();
         }
      }

   }

   static public Vector<Item> walkPricingTree(Item item)
   {
      Vector<Item> components = new Vector<Item>();

      walkPricingTree(item, components);
      return components;
   }

   static private void walkPricingTree(Item item, Vector<Item> components)
   {
      Collection<Source> sources = item.getSources();
      if(components.contains(item))
      {
         return;
      }
      components.add(item);
      for(Source source : sources)
      {
         if(source instanceof Recipe)
         {
            ItemQuantitySet[] ingredients = ((Recipe) source).getInputs();
            for(ItemQuantitySet ingredient : ingredients)
            {
               walkPricingTree(ingredient.getItem(), components);
            }
         }
      }
   }

   public void linkTo(APIItem dynamicItem)
   {
      if(this.getClass() != Item.class)
      {
         throw new IllegalStateException("Cannot Link Non-Static items to API items");
      }

      if(apiItem == dynamicItem)
      {
         return;
      }
      if(apiItem != null)
      {
         throw new IllegalStateException("Static item " + this.getName() + " already linked to " + apiItem.getName() + "["+apiItem.getItemID()+"], cannot link " + dynamicItem.getName()+ "["+dynamicItem.getItemID()+"]");
      }
      apiItem = dynamicItem;
   }
   public APIItem getAPIItem()
   {
      if(isLinkedToAPI())
      {
         return null;
      }
      return apiItem;
   }

}
