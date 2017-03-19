package com.torrenal.craftingGadget;

public class ObjectQuantitySet<T extends Object> implements Cloneable
{

	protected T item;
	protected double quantity;

	public ObjectQuantitySet(T item, double quantity)
	{
		this.item = item;
		this.quantity = quantity;
	}

	public T getItem()
	{
		return item;
	}

	public double getQuantity()
	{
		return quantity;
	}


	protected void updateItem(T newItem)
	{
		item = newItem;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ObjectQuantitySet<T> clone()
	{
		try {
			return (ObjectQuantitySet) super.clone();
		} catch (CloneNotSupportedException impossible) {
			throw new Error("Unexpected error!", impossible);
		}
	}

	/** Our drop rates logic needs to combine rows, and
	 * uses this to adjust quantities while doing so.  It really should have
	 * no other use...
	 * @param addMe
	 */
	public void addToQuantity(double addMe)
	{
		quantity += addMe;
	}

}