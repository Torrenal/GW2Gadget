package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueSkillPoint extends ValueElement<ValueSkillPoint>
{
	double skillPoints;
	
	static
	{
		valueModifier.put(ValueType.SKILL_POINTS,  4900F);
	}

	public ValueSkillPoint(double skillPoints)
	{
		this.skillPoints = skillPoints;
	}

	@Override
	public ValueType getType()
	{
		return ValueType.SKILL_POINTS;
	}

	@Override
   void setQuantity(double skillPoints)
   {
	   this.skillPoints = skillPoints;
   }
	
	@Override
	public double getQuantity()
	{
		return skillPoints;
	}

	public String toString()
	{
		return CookingCore.skillPointsToString(skillPoints) + (skillPoints == 1 ? " skill point" : " skill points");
	}

	@Override

	public ValueElement<ValueSkillPoint> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueSkillPoint))
		{
			throw new Error("Invalid Argument, expected ValueSkillPoint");
		}
		
		return new ValueSkillPoint(skillPoints + that.getQuantity());
	}

	@Override
   public ValueElement<ValueSkillPoint> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueSkillPoint))
		{
			throw new Error("Invalid Argument, expected ValueSkillPoint");
		}

		return new ValueSkillPoint(skillPoints - that.getQuantity());
   }

}
