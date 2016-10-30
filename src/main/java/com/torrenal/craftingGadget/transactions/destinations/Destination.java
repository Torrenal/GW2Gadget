package com.torrenal.craftingGadget.transactions.destinations;

import com.torrenal.craftingGadget.dataModel.value.Value;

public abstract class Destination
{
	/**
	 * Per unit Value of this source
	 * @return
	 */
	abstract public Value getGrossValue();
	
    /**
     * Per unit Value of this source, less costs in selling it.
     * 
     * Excludes all costs required to obtain the item.
     * @return
     */
	abstract public Value getNetValue();

	/** 
	 * Terse Name of the Destination
	 */
	abstract public String getDestionationName();

	/** 
	 * Long of the Destination
	 */
    abstract public String getDestionationFullName();

	   /**
     * Detailed description of the method this source represents
     * Used for 'full detail' views.
     * @return
     */
    abstract public String getFullDestinationDetails();

	public String toString()
	{
	   return getFullDestinationDetails();
	}

}
