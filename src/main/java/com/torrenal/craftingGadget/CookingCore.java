package com.torrenal.craftingGadget;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.text.NumberFormatter;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueBlooms;
import com.torrenal.craftingGadget.dataModel.value.ValueCarvings;
import com.torrenal.craftingGadget.dataModel.value.ValueCoin;
import com.torrenal.craftingGadget.dataModel.value.ValueGem;
import com.torrenal.craftingGadget.dataModel.value.ValueGeodes;
import com.torrenal.craftingGadget.dataModel.value.ValueHonorBadges;
import com.torrenal.craftingGadget.dataModel.value.ValueKarma;
import com.torrenal.craftingGadget.dataModel.value.ValueKnowledgeCrystals;
import com.torrenal.craftingGadget.dataModel.value.ValueKoda;
import com.torrenal.craftingGadget.dataModel.value.ValueLaurel;
import com.torrenal.craftingGadget.dataModel.value.ValueManifestos;
import com.torrenal.craftingGadget.dataModel.value.ValuePristineFractalRelics;
import com.torrenal.craftingGadget.dataModel.value.ValueRelics;
import com.torrenal.craftingGadget.dataModel.value.ValueSeals;
import com.torrenal.craftingGadget.dataModel.value.ValueShardsOfZhaitan;
import com.torrenal.craftingGadget.dataModel.value.ValueSkillPoint;
import com.torrenal.craftingGadget.dataModel.value.ValueTears;
import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.db.bags.BagsManager;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.items.ItemRecord;
import com.torrenal.craftingGadget.db.items.PriorityLoadListGen;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;
import com.torrenal.craftingGadget.priceFetcher.PriceTool;
import com.torrenal.craftingGadget.transactions.destinations.SellOnMarket;
import com.torrenal.craftingGadget.transactions.sources.BuyFromMerchant;
import com.torrenal.craftingGadget.transactions.sources.BuyOnMarket;
import com.torrenal.craftingGadget.transactions.sources.ComplexConsume;
import com.torrenal.craftingGadget.transactions.sources.Container;
import com.torrenal.craftingGadget.transactions.sources.RawRecipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.transactions.sources.TaskSource;

public class CookingCore
{
   //static String path = "C:\\Dev\\Projects\\wikiCodeScratchpage\\Resources\\CookingData.txt";
   static String recipePath = ".\\CookingData.txt";
   static String marketPath = ".\\item.csv";
   static String loginPath = ".\\credentials.txt";

   static Hashtable<ValueType, Double> currencyValues = new Hashtable<>();


   static private float maxBuySellPriceGapRatio = .30F;

   static private PriceTool priceTool = new PriceTool();


   static private Hashtable<ItemKey,Item> items = new Hashtable<>();
   static private Hashtable<Long,APIItem> itemsByID = new Hashtable<>();
   private static NumberFormatter skillFormatter = null;
   private static NumberFormatter doubleFormatter = null;
   private static NumberFormatter doublePrecisionFormatter = null;
   private static boolean captureData = false;
   private static boolean chokeAndDie = false;

   public static void updateAllPrices()
   {
      EvaluateEngine.reevaluateAll();
   }

   public static float getMaxBuySellPriceGapRatio()
   {
      return maxBuySellPriceGapRatio;
   }

   public static void setMaxBuySellPriceGapRatio(float maxBuySellPriceGapRatio)
   {
      CookingCore.maxBuySellPriceGapRatio = maxBuySellPriceGapRatio;
   }

   static void loadRecipeData()
   {

      new CookingSecurityPolicy();

      File file = ResourceManager.getFixedDataFile();
      Scanner scanner = ResourceManager.getTextInputStream(file);
      String line = null;
      int lineNum = 0;

      while(scanner.hasNext())
      {
         line = scanner.nextLine();
         lineNum++;
         line = line.trim();

         if(line.isEmpty() || line.startsWith("#"))
         {
            continue;
         }
         try
         {
            if(line.toLowerCase().startsWith("karmavalue"))
            {
               String text = line.split("=")[1].trim();
               int value = Integer.valueOf(text);
               setCurrencyValue(ValueType.KARMA_TOKEN, value);
               continue;
            }
            if(line.toLowerCase().startsWith("coinvalue"))
            {
               String text = line.split("=")[1].trim();
               int value = Integer.valueOf(text);
               setCurrencyValue(ValueType.COIN_TOKEN, value);
               continue;
            }
            if(line.toLowerCase().startsWith("skillpointvalue"))
            {
               String text = line.split("=")[1].trim();
               int value = Integer.valueOf(text);
               setCurrencyValue(ValueType.SKILL_POINTS, value);
               continue;
            }
            if(line.toLowerCase().startsWith("MaxBuySellGapRatio"))
            {
               String text = line.split("=")[1].trim();
               float value = Float.valueOf(text);
               setMaxBuySellPriceGapRatio(value);
               continue;
            }

            String[] bits = line.split("\\|");
            String sourceName = bits[0].trim();

            if("Merchant".equalsIgnoreCase(sourceName))
            {
               String itemName = bits[1].trim();
               Item item = findItemByName(itemName);
               //item.setImportBuyAt(importBuyAt);
               Value value = new Value(false);
               for(int i = 3; i < bits.length; i++)
               {
                  value = value.add(parseValue(bits[i]));
               }
               Source source = new BuyFromMerchant(item, bits[2], value);
               item.addSource(source);
               continue;
            }
            if("Package".equalsIgnoreCase(sourceName))
            {
               Item result = findItemByName(bits[2]);
               //result.setImportBuyAt(importBuyAt);

               String itemName = bits[1].trim();
               Item item = findItemByName(itemName);
               result.addSource(new Container(item, result, Double.parseDouble(bits[3])));
               continue;
            }
            if("Recipe".equalsIgnoreCase(sourceName))
            {
               Item outputItem = findItemByName(bits[2]);
               //result.setImportBuyAt(importBuyAt);
               double outputQty = Double.parseDouble(bits[1]);
               ItemQuantitySet[] outputs = new ItemQuantitySet[1];
               outputs[0] = new ItemQuantitySet(outputItem, outputQty);
               String discipline = bits[3];
               Vector<ItemQuantitySet> inputs = new Vector<>();
               String difficulty = bits[4];

               for(int idx = 5;idx < bits.length; idx += 2)
               {
                  Item item = findItemByName(bits[idx+1]);
                  double qty = Double.parseDouble(bits[idx]);
                  inputs.add(new ItemQuantitySet(item, qty));
               }

               outputItem.addSource(new RawRecipe(discipline, difficulty, outputs, inputs.toArray(new ItemQuantitySet[0])));
               continue;
            }
            if("Task".equalsIgnoreCase(sourceName))
            {
               String itemName = bits[1].trim();
               Item item = findItemByName(itemName);
               Source source = new TaskSource(item, bits[2], bits[3], bits[4]);
               item.addSource(source);
               continue;
            }
            if("Consume".equalsIgnoreCase(sourceName))
            {
               double outputQty = Double.parseDouble(bits[1]);
               Item output = findItemByName(bits[2]);
               ItemQuantitySet[] outputs = new ItemQuantitySet[1];
               outputs[0] = new ItemQuantitySet(output, outputQty);

               Vector<ItemQuantitySet> inputs = new Vector<>();
               
               for(int idx = 3;idx < bits.length; idx += 2)
               {
                  inputs.add(new ItemQuantitySet(findItemByName(bits[idx+1]),Double.parseDouble(bits[idx]))); 
               }

               output.addSource(new ComplexConsume(outputs, inputs.toArray(new ItemQuantitySet[0])));
               continue;
            }
         }
         catch (Throwable err)
         {
            System.err.println("Error parsing line " + lineNum+": " + line);
            err.printStackTrace();
            System.exit(0);
         }
      }
      scanner.close();
   }

   public static void loadMarketData()
   {
      loadRecipeData();
      ItemDB.initDB(!captureData);

      // Copy Protection - prototype state
      if((System.getSecurityManager() instanceof CookingSecurityPolicy))
      {
         chokeAndDie = true;
      }

      ItemDB.blockForInit();
      if(!captureData)
      {
         RecipeDB.initDB();
         priceTool.launchWorker();
         BagsManager.loadBags();
      }
      
   }

   public static void updatePricingFor(Item item, int buyPrice, int sellPrice,
         int buyOffers, int sellOffers)
   {
      if(item == null)
      {
         return;
      }

      item.setPricesUpdatedTimeStamp(System.currentTimeMillis());
      item.setPriorityUpdate(false);
      item.clearMarketPrices();

      /* Cannot sell for less than merchant price */
      //FIXME: Validate against merchant pricing
      //		if(buyPrice <= merchantPrice)
      //		{
      //			buyPrice = merchantPrice + 1;
      //		}
      item.setQtySell(sellOffers);
      item.setQtyBuy(buyOffers);

      if(sellPrice != 0)
      {
         SellOnMarket saleWidget = new SellOnMarket(new Value(new ValueCoin(sellPrice)), new Value(new ValueCoin(buyPrice)));
         item.addDestination(saleWidget);
      }
      if(buyPrice != 0)
      {
         double merchCoins = item.getMerchCoins();
         if(buyPrice < merchCoins)
         {
            buyPrice = (int) (merchCoins + 1);
         }
         BuyOnMarket saleWidget = new BuyOnMarket(item, buyPrice, sellPrice);
         item.addSource(saleWidget);
      }
      //FIXME: Allow sale to merchant where possible 
      //		if(merchantPrice != 0)
      //		{
      //			SellToMerchant saleWidget = new SellToMerchant(item, new Value(new ValueCoin(merchantPrice)));
      //			item.addSource(saleWidget);
      //		}

   }

   private static String readLineFrom(File file, BufferedReader in)
   {
      try
      {
         return in.readLine();
      } catch (IOException e)
      {
         System.err.println("Error reading from " + file);
         e.printStackTrace();
         System.exit(1);
      }
      return null; /* NOOP */
   }

   private static double[] unwrap(Vector<Double> qtys)
   {
      double [] result = new double[qtys.size()];

      for(int idx = 0; idx < qtys.size();idx++)
      {
         result[idx] = qtys.get(idx);
      }
      return result;
   }

   public static synchronized Item findItemByName(String itemName)
   {
      ItemKey key = new ItemKey(itemName.trim());
      Item item = items.get(key);
      if(item == null)
      {
         item = new Item(itemName);
         items.put(item, item);
      }
      
      return item;
   }

   public static synchronized APIItem findItemByID(long itemID)
   {
      APIItem item = itemsByID.get(itemID);
      if(item == null)
      {
         ItemRecord itemRecord = ItemDB.getItem(itemID);
         /* If the itemRecord is freshly instantiated, 
          * it will instantiate an item itself, return that if it happens */
         item = itemsByID.get(itemID);
         if(item != null)
         {
            return item;
         }
         item = new APIItem(itemRecord);
         itemsByID.put(itemID, item);
         return item;
      }
      return item;
   }

   public synchronized static void removeItemByID(long itemID)
   {
      APIItem item = itemsByID.remove(itemID);
      if(item != null)
      {
         ContextUpdateNotifier.notifyLazyStructureUpdates();
      }
   }

   private static Value parseValue(String valueText)
   {
      String[] bits = valueText.split(" ");
      Value value = null;
      if("Karma".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueKarma(Float.parseFloat(bits[1])));
      }
      if("Coin".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueCoin(Float.parseFloat(bits[1])));
      }
      if("SkillPoint".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueSkillPoint(Float.parseFloat(bits[1])));
      }
      if("Gems".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueGem(Float.parseFloat(bits[1])));
      }
      if("AC_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueTears(Float.parseFloat(bits[1])));
      }
      if("CM_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueSeals(Float.parseFloat(bits[1])));
      }
      if("TA_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueBlooms(Float.parseFloat(bits[1])));
      }
      if("SE_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueManifestos(Float.parseFloat(bits[1])));
      }
      if("CF_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueCarvings(Float.parseFloat(bits[1])));
      }
      if("HotW_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueKoda(Float.parseFloat(bits[1])));
      }
      if("CoE_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueKnowledgeCrystals(Float.parseFloat(bits[1])));
      }
      if("Arah_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueShardsOfZhaitan(Float.parseFloat(bits[1])));
      }
      if("FRACTAL_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueRelics(Float.parseFloat(bits[1])));
      }
      if("GEODE_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueGeodes(Float.parseFloat(bits[1])));
      }
      if("PRISTINE_FRACTAL_RELICS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValuePristineFractalRelics(Float.parseFloat(bits[1])));
      }
      if("WvW_TOKENS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueHonorBadges(Float.parseFloat(bits[1])));
      }
      if("LAURELS".equalsIgnoreCase(bits[0]))
      {
         value = new Value(new ValueLaurel(Float.parseFloat(bits[1])));
      }

      if(value == null)
      {
         throw new InvalidParameterException("invalid valueText.  Must be either \"karma <value>\", \"coin <value>\", \"Gems\", \"skillPoint <value>\", or \"Token(type)\"");
      }
      return value;
   }

   public static void printData()
   {
      Item[] itemsArray = items.values().toArray(new Item[0]);

      Arrays.sort(itemsArray, new Comparator<Item>() {

         @Override
         public int compare(Item o1, Item o2)
         {
            return o1.getName().compareTo(o2.getName());
         }
      });
      for(Item item : itemsArray)
      {
         //System.out.println(item);
         System.out.println(item.getName());
         System.out.println();
      }

   }
   @SuppressWarnings("unchecked")
   public static Map<ItemKey, Item> getStaticItems()
   {
      return Collections.unmodifiableMap((Map<ItemKey,Item>)items.clone());
   }

   public static Item getStaticItem(ItemKey key)
   {
      return items.get(key);
   }

   @SuppressWarnings("unchecked")
   public static Map<Long, APIItem> getAPIItems()
   {
      return Collections.unmodifiableMap((Map<Long, APIItem>)itemsByID.clone());
   }

   public static String doubleToString(double doubleValue)
   {

      if(doubleFormatter == null)
      {
         doubleFormatter  = new NumberFormatter(new DecimalFormat("0.##"));
      }

      String result;
      try
      {
         result = doubleFormatter.valueToString(Math.round(doubleValue * 100 ) / 100.0);
      } catch (ParseException e)
      {
         result = "<ERR>";
      }
      return result;
   }
   public static String doubleToPrecisionString(double doubleValue)
   {

      if(doublePrecisionFormatter == null)
      {
         doublePrecisionFormatter  = new NumberFormatter(new DecimalFormat("0.####"));
      }

      String result;
      try
      {
         result = doublePrecisionFormatter.valueToString(Math.round(doubleValue * 10000 ) / 10000.0);
      } catch (ParseException e)
      {
         result = "<ERR>";
      }
      return result;
   }

   public static String dollarsToString(double doubleValue)
   {

      if(doubleFormatter == null)
      {
         doubleFormatter  = new NumberFormatter(new DecimalFormat("0.##"));
      }
      if(skillFormatter == null)
      {
         skillFormatter  = new NumberFormatter(new DecimalFormat("0.####"));
      }

      NumberFormatter formatter;
      if(Math.abs(doubleValue) < 0.05)
      {
         formatter = skillFormatter;
      } else
      {
         formatter = doubleFormatter;
      }

      String result;
      try
      {
         result = formatter.valueToString(Math.round(doubleValue * 10000 ) / 10000.0);
      } catch (ParseException e)
      {
         // Copy-protection, fail if chokeAndDie is set.
         if(chokeAndDie && doubleValue < 1)
         {
            throw new Error("Invalid Argument, value is fractional, got: " + doubleValue);
         }
         result = "<ERR>";
      }
      return result;
   }

   public static String skillPointsToString(double doubleValue)
   {

      if(skillFormatter == null)
      {
         skillFormatter  = new NumberFormatter(new DecimalFormat("0.####"));
      }

      String result;
      try
      {
         result = skillFormatter.valueToString(Math.round(doubleValue * 10000 ) / 10000.0);
      } catch (ParseException e)
      {
         // Copy-protection, fail if chokeAndDie is set.
         if(chokeAndDie && doubleValue < 1)
         {
            throw new Error("Invalid Argument, value is fractional, got: " + doubleValue);
         }
         result = "<ERR>";
      }
      return result;
   }


   public static PriceTool getPriceTool()
   {
      return priceTool;
   }

   static public synchronized void setCurrencyValue(ValueType currency, double currencyValue)
   {
      Double oldValue = currencyValues.get(currency);
      currencyValues.put(currency, currencyValue);

      ContextUpdateNotifier.notifyStructureUpdates();
      if(oldValue == null || currencyValue != oldValue)
      {
         EvaluateEngine.reevaluateAll();
      }
   }

   public static double getCurrencyValue(ValueType type)
   {
      Double currencyValue = currencyValues.get(type);
      if(currencyValue == null)
      {
         return 500;
      }
      return currencyValue;
   }

   public static float getCurrencyValueMult(ValueType type)
   {
      double mult = getCurrencyValue(type);
      /* Below 50, give it a steeper curve to end near 0.0008 at 1 */
      if(mult < 50)
      {
         //=0.000843*POWER(A52,2.8092)
         mult = 0.000843 * Math.pow(mult, 2.8092);
      }
      return (float) mult;
   }


   public static void setCaptureData()
   {
      captureData  = true;
   }

   public static void printPriorityLoadList()
   {
      PriorityLoadListGen.printPriorityLoadList();
   }
}
