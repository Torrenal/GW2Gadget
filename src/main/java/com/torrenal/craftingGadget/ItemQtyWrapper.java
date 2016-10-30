package com.torrenal.craftingGadget;

import java.util.Comparator;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.transactions.sources.CurrencyConversion;
import com.torrenal.craftingGadget.transactions.sources.Source;

/* Wrapper item used for compiling a list of the base componet items based on quantity<br> 
 * Designed for use in a Hashtable<RootItem,RootItem> a-la Russell,
 * but it may be used in other ways.
 */
public class ItemQtyWrapper
{
	private Source source;
	private Item product;
	private double qty;
	private boolean isRootStep;
	private boolean isPhantomRootStep;

	public ItemQtyWrapper(Source source, double qty)
	{
	   this(source.getOutputs()[0].getItem(), source, qty);
	   if(source.getOutputs().length != 1)
	   {
	      throw new IllegalStateException("Cannot wrap multiple outputs in one wrapper");
	   }
	}
	public ItemQtyWrapper(Item product, Source source, double qty)
   {
      super();
      this.source = source;
      this.product = product;
      if(product == null)
      {
         throw new NullPointerException();
      }
      this.qty = qty;
      if(Double.isInfinite(qty))
      {
         throw new IllegalArgumentException("Infinity is not a valid quantity!"); 
      }
      this.isRootStep = source.isRootStep();
   }

	public Source getSource()
   {
      return source;
   }

	public Item getProduct()
	{
	   return product;
	}


	public double getQty()
   {
      return qty;
   }

	public void addQty(double qty)
   {
	   if(Double.isInfinite(qty))
	   {
	      throw new IllegalArgumentException("Infinity is not a valid quantity!"); 
	   }
      this.qty += qty;
   }

	public void multQty(double multipiler)
   {
	   if(Double.isInfinite(multipiler))
	   {
	      throw new IllegalArgumentException("Infinity is not a valid multiplier!"); 
	   }
		this.qty *= multipiler;
   }

	@Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      
      result = prime * result + ((product == null) ? 0 : product.hashCode());
      return result;
   }

	@Override
   public boolean equals(Object obj)
   {
      if (this == obj)
	      return true;
      if (obj == null)
	      return false;
      if (getClass() != obj.getClass())
	      return false;
      ItemQtyWrapper other = (ItemQtyWrapper) obj;
      Source mySource = getSource();
      Source yourSource = other.getSource();
      if(mySource == null && yourSource != null)
      {
      	return false;
      }
      if(mySource != null && yourSource == null)
      {
      	return false;
      }
      if(mySource != null)
      {
      	if(mySource instanceof CurrencyConversion && yourSource instanceof CurrencyConversion)
      	{
      		if(((CurrencyConversion) mySource).getFromCurrency() != ((CurrencyConversion) yourSource).getFromCurrency())
      		{
      			return false;
      		}
      		if(((CurrencyConversion) mySource).getToCurrency() != ((CurrencyConversion) yourSource).getToCurrency())
      		{
      			return false;
      		}
      		return true;
      	}
      	if(mySource instanceof CurrencyConversion || yourSource instanceof CurrencyConversion)
      	{
      		return false;
      	}
      }
      
      Item myProduct = product;
      Item yourProduct = other.product;
      if(myProduct == null || yourProduct == null)
      {
         if(myProduct != yourProduct)
         {
            return false;
         }
         
         return true;
      }
      if (mySource != null && !product.equals(yourProduct))
	      return false;
      if(this.isRootStep != other.isRootStep)
      {
      	return false;
      }
      if(this.isPhantomRootStep != other.isPhantomRootStep)
      {
      	return false;
      }

      return true;
   }

	public void setRootStep(boolean isRootStep)
   {
	   this.isRootStep = isRootStep;
   }

	public boolean isRootStep()
   {
   	return isRootStep;
   }

	/* Phantom root steps do not count as root steps or as build steps. */
	public void setPhantomRootStep(boolean isPhantomRootStep)
   {
	   this.isRootStep = isPhantomRootStep;
   }

	public boolean isPhantomRootStep()
   {
   	return isPhantomRootStep;
   }

	@Override
   public String toString()
   {
	   return "ItemQtyWrapper [item=" + product.getName() + ", source=" + source.getMethodName() + ", qty=" + qty
	         + ", isRootStep=" + isRootStep + ", isPhantomRootStep="
	         + isPhantomRootStep + "]";
   }
   public static Comparator<? super ItemQtyWrapper> getReverseObtainValueSort()
   {
      return new Comparator<ItemQtyWrapper>() {

         @Override
         public int compare(ItemQtyWrapper o1, ItemQtyWrapper o2)
         {
            Value o1Cost = o1.getProduct().getBestObtainCost().multiply(o1.getQty());
            Value o2Cost = o2.getProduct().getBestObtainCost().multiply(o2.getQty());
            return o2Cost.compareTo(o1Cost);
         }
      };
   }
}