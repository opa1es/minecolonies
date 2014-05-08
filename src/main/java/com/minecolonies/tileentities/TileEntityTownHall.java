package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.UUID;

public class TileEntityTownHall extends TileEntityHut
{
    private String          cityName; //TODO Send by packet
    private ArrayList<UUID> owners; //TODO Send by packet
    private BiomeGenBase    biome;

    private ArrayList<UUID> citizens;
    private int             maxCitizens;

    public TileEntityTownHall()
    {
        setHutName("Townhall");
        owners = new ArrayList<UUID>();
        citizens = new ArrayList<UUID>();
        maxCitizens = com.minecolonies.lib.Constants.DEFAULTMAXCITIZENS;
    }

    public void setCityName(String cityName)
    {
        this.cityName = cityName;
    }

    public void onBlockAdded()
    {
        for(Object o : worldObj.loadedTileEntityList)
            if(o instanceof TileEntityHut && Utils.getDistanceToClosestTownHall(worldObj, xCoord, yCoord, zCoord) < com.minecolonies.lib.Constants.MAXDISTANCETOTOWNHALL)
            {
                TileEntityHut tileEntityHut = (TileEntityHut) o;
                if(tileEntityHut.getTownHall() == null) tileEntityHut.setTownHall(this);
            }
    }

    public void setInfo(World w, UUID ownerName, int x, int z)
    {
        owners.add(ownerName);
        biome = w.getBiomeGenForCoords(x, z);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("maxCitizens", maxCitizens);
        nbtTagCompound.setString("cityName", cityName);
        NBTTagList nbtTagOwnersList = new NBTTagList();
        for(UUID owner : owners)
        {
            NBTTagCompound nbtTagOwnersCompound = new NBTTagCompound();
            nbtTagOwnersCompound.setString("owner", owner.toString());
            nbtTagOwnersList.appendTag(nbtTagOwnersCompound);
        }
        NBTTagList nbtTagCitizenList = new NBTTagList();
        for(UUID citizens : getCitizens())
        {
            NBTTagCompound nbtTagCitizenCompound = new NBTTagCompound();
            nbtTagCitizenCompound.setString("citizen", citizens.toString());
            nbtTagCitizenList.appendTag(nbtTagCitizenCompound);
        }
        nbtTagCompound.setTag("citizens", nbtTagCitizenList);
        nbtTagCompound.setTag("owners", nbtTagOwnersList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        this.maxCitizens = nbtTagCompound.getInteger("maxCitizens");
        this.cityName = nbtTagCompound.getString("cityName");
        NBTTagList nbtTagOwnersList = nbtTagCompound.getTagList("owners", Constants.NBT.TAG_COMPOUND);
        NBTTagList nbtTagCitizenList = nbtTagCompound.getTagList("citizens", Constants.NBT.TAG_COMPOUND);
        this.owners = new ArrayList<UUID>();
        this.citizens = new ArrayList<UUID>();
        for(int i = 0; i < nbtTagOwnersList.tagCount(); i++)
        {
            NBTTagCompound nbtTagOwnersCompound = nbtTagOwnersList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagOwnersCompound.getString("owner"));
            owners.add(i, uuid);
        }
        for(int i = 0; i < nbtTagCitizenList.tagCount(); i++)
        {
            NBTTagCompound nbtTagCitizenCompound = nbtTagCitizenList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagCitizenCompound.getString("citizen"));
            citizens.add(i, uuid);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();

        this.maxCitizens = nbt.getInteger("maxCitizens");
        this.cityName = nbt.getString("cityName");

        NBTTagList citizenList = nbt.getTagList("citizens", Constants.NBT.TAG_COMPOUND);
        this.citizens = new ArrayList<UUID>();
        for(int i = 0; i < citizenList.tagCount(); i++)
        {
            NBTTagCompound citizenTag = citizenList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(citizenTag.getString("citizen"));
            citizens.add(i, uuid);
        }
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setInteger("maxCitizens", maxCitizens);
        nbt.setString("cityName", cityName);

        NBTTagList citizenList = new NBTTagList();
        for(UUID citizen : getCitizens())
        {
            NBTTagCompound citizenTag = new NBTTagCompound();
            citizenTag.setString("citizen", citizen.toString());
            citizenList.appendTag(citizenTag);
        }
        nbt.setTag("citizens", citizenList);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
    }

    public ArrayList<UUID> getOwners()
    {
        return owners;
    }

    public String getCityName()
    {
        return cityName;
    }

    public void addCitizen(EntityCitizen citizen)
    {
        citizens.add(citizen.getUniqueID());
    }

    public ArrayList<UUID> getCitizens()
    {
        return citizens;
    }

    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    public void setMaxCitizens(int maxCitizens)
    {
        this.maxCitizens = maxCitizens;
    }

    public void addCitizenToTownhall(EntityCitizen entityCitizen)
    {
        if(getCitizens() != null)
            this.addCitizen(entityCitizen);
    }
}
