package net.minecraft.src.universalelectricity;

import java.util.ArrayList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/**
 * REQUIRED
 * Extend from this class if your item requires electricity or to be charged.
 * @author Henry
 *
 */
public abstract class UEElectricItem extends Item
{
    public UEElectricItem(int par1)
    {
        super(par1);
        this.setMaxStackSize(1);
        this.setMaxDamage(getElectricityCapacity());
        this.setNoRepair();
    }
    
    @Override
    /**
     * Called when item is crafted/smelted. Used only by maps so far.
     */
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	par1ItemStack.setItemDamage(this.getElectricityCapacity());
    }

    
    /**
     * Called when this item is being "recharged" or receiving electricity.
     * @param watts - The amount of watts this item is receiving.
     * @param itemStack - The ItemStack of this item
     * @return Return the rejected electricity from this item
     */
    public int onReceiveElectricity(int watts, ItemStack itemStack)
    {
    	int rejectedElectricity = Math.max((this.getElectricityStored(itemStack) + watts) - this.getElectricityCapacity(), 0);
		this.setElectricityStored(itemStack, this.getElectricityStored(itemStack) + watts - rejectedElectricity);
		return rejectedElectricity;
    }
    
    /**
     * Called when this item's electricity is being used.
     * @param watts - The amount of electricity requested from this item
     * @param itemStack - The ItemStack of this item
     * @return The electricity that is given to the requester
     */
    public int onUseElectricity(int watts, ItemStack itemStack)
    {
    	int electricityToUse = Math.min(this.getElectricityStored(itemStack), watts);
		this.setElectricityStored(itemStack, this.getElectricityStored(itemStack) - electricityToUse);
		return electricityToUse;
    }
    
    /**
	 * @return Returns true or false if this consumer can receive electricity at this given tick or moment.
	 */
	public boolean canReceiveElectricity()
    {
    	return true;
    }
	
	/**
	 * Can this item give out electricity when placed in an tile entity? Electric items like batteries
	 * should be able to produce electricity (if they are rechargable).
	 * @return - True or False.
	 */
	public boolean canProduceElectricity()
	{
		return false;
	}
    
    /**
     * This function sets the electriicty. Do not directly call this function.
     * Try to use onReceiveElectricity or onUseElectricity instead.
     * @param amount - The amount of electricity in watts
     */
    protected void setElectricityStored(ItemStack itemStack, int amount)
    {
    	//Saves the frequency in the itemstack
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		int electricityStored = Math.max(Math.min(amount, this.getElectricityCapacity()), 0);
		
		itemStack.stackTagCompound.setInteger("electricity", electricityStored);
		
		itemStack.setItemDamage(getElectricityCapacity() - electricityStored);
    }
    
    /**
     * This function is called to get the electricity stored in this item
     * @return - The amount of electricity stored
     */
    protected int getElectricityStored(ItemStack itemStack)
    {
    	if (itemStack.stackTagCompound == null)
		{
    		return 0;
		}
    	
    	int electricityStored = itemStack.stackTagCompound.getInteger("electricity");
    	
    	itemStack.setItemDamage(getElectricityCapacity() - electricityStored);
    	
    	return electricityStored;	
    }
    
    /**
     * This function is called to get the electricity maximum capacity in this item
     * @return - The amount of electricity maximum capacity
     */
    public abstract int getElectricityCapacity();
    
    /**
     * This function is called to get the maximum transfer rate this electric item can receive per tick
     * @return - The amount of electricity maximum capacity
     */
    public abstract int getTransferRate();
    
    /**
     * Gets the voltage of this item
     * @return The Voltage of this item
     */
    public abstract int getVolts();
    
    /**
     * Adds a charged version and a non-charged version of this item. You may replace
     * this function if you do not wish to have a charged version of your electric item.
     * 
     * @param itemList The list of items currently in the creative inventory
     */
    @Override
	public void addCreativeItems(ArrayList itemList)
    {       
    	//Add an uncharged version of the electric item
    	ItemStack unchargedItem = new ItemStack(this, 1);
    	unchargedItem.setItemDamage(this.getElectricityCapacity());
    	itemList.add(unchargedItem);
    	//Add an electric item to the creative list that is fully charged
    	ItemStack chargedItem = new ItemStack(this, 1);
    	this.setElectricityStored(chargedItem, ((UEElectricItem)chargedItem.getItem()).getElectricityCapacity());
    	itemList.add(chargedItem);
    }
}