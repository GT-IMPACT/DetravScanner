package com.detrav.items.behaviours;

import com.detrav.cfg.Config;
import com.detrav.items.DetravMetaGeneratedTool01;
import com.detrav.net.DetravNetwork;
import com.detrav.net.ProspectingPacket;
import com.google.common.base.Objects;
import com.impact.common.oregeneration.OreGenerator;
import com.impact.common.oregeneration.OreVein;
import com.impact.common.oregeneration.generator.OreVeinGenerator;
import com.impact.common.oregeneration.generator.OresRegionGenerator;
import gregtech.api.items.GT_MetaBase_Item;
import gregtech.api.util.GT_Utility;
import gregtech.common.GT_UndergroundOil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.impact.core.Impact_API.regionsOres;

/**
 * Created by wital_000 on 19.03.2016.
 */
public class BehaviourDetravToolElectricProspector extends BehaviourDetravToolProPick {
	
	public BehaviourDetravToolElectricProspector(int aCosts) {
		super(aCosts);
	}
	
	public ItemStack onItemRightClick(GT_MetaBase_Item aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
		if (!aWorld.isRemote) {
			int data = DetravMetaGeneratedTool01.INSTANCE.getToolGTDetravData(aStack).intValue();
			if (data == 0 || data == 1) data = 2;
			if (aPlayer.isSneaking()) {
				data++;
				if (data == 0 || data == 1) data = 2;
				if (data > Config.modesProspect) data = 2;
				
				if (data > 3) {
					aPlayer.addChatMessage(new ChatComponentText("Set Mode: Impact Ores (Layer " + (data - 4) + ")"));
				} else if (data == 2) {
					aPlayer.addChatMessage(new ChatComponentText("Set Mode: Oil, Any Block"));
				} else if (data == 3) {
					aPlayer.addChatMessage(new ChatComponentText("Set Mode: Pollution, Any Block"));
				}
				DetravMetaGeneratedTool01.INSTANCE.setToolGTDetravData(aStack, data);
				return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);
			}
			
			final DetravMetaGeneratedTool01 tool = (DetravMetaGeneratedTool01) aItem;
			final int cX = ((int) aPlayer.posX) >> 4;
			final int cZ = ((int) aPlayer.posZ) >> 4;
			int size = aItem.getHarvestLevel(aStack, "") + 1;
			final List<Chunk> chunks = new ArrayList<>();
			aPlayer.addChatMessage(new ChatComponentText("Scanning..."));
			for (int i = -size; i <= size; i++) {
				for (int j = -size; j <= size; j++) {
					if (i != -size && i != size && j != -size && j != size) {
						chunks.add(aWorld.getChunkFromChunkCoords(cX + i, cZ + j));
					}
				}
			}
			size = size - 1;
			final ProspectingPacket packet = new ProspectingPacket(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size, data);
			for (Chunk c : chunks) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						switch (data) {
							case 2:
								if ((x == 0) || (z == 0)) { //Skip doing the locations with the grid on them.
									break;
								}
								FluidStack fStack = GT_UndergroundOil.undergroundOil(aWorld.getChunkFromBlockCoords(c.xPosition * 16 + x, c.zPosition * 16 + z), -1);
								if (fStack.amount > 0) {
									packet.addBlock(c.xPosition * 16 + x, 1, c.zPosition * 16 + z, (short) fStack.getFluidID());
									packet.addBlock(c.xPosition * 16 + x, 2, c.zPosition * 16 + z, (short) fStack.amount);
								}
								break;
							case 3:
								float polution = (float) getPolution(aWorld, c.xPosition * 16 + x, c.zPosition * 16 + z);
								polution /= 2000000;
								polution *= -0xFF;
								if (polution > 0xFF)
									polution = 0xFF;
								polution = 0xFF - polution;
								packet.addBlock(c.xPosition * 16 + x, 1, c.zPosition * 16 + z, (short) polution);
								break;
							case 4:
							case 5:
							case 6:
							case 7:
							case 8:
								Chunk chunkCurr = aWorld.getChunkFromBlockCoords(c.xPosition * 16 + x, c.zPosition * 16 + z);
								ChunkCoordIntPair chunkPosition = chunkCurr.getChunkCoordIntPair();
								int xRegCurrent = (chunkPosition.chunkXPos >> 5) % 512;
								int zRegCurrent = (chunkPosition.chunkZPos >> 5) % 512;
								int dimID = aWorld.provider.dimensionId;
								OresRegionGenerator currentRegion = new OresRegionGenerator(xRegCurrent, zRegCurrent, dimID);
								int idHash = Objects.hashCode(currentRegion.xRegion, currentRegion.zRegion, dimID);
								if (!regionsOres.containsKey(idHash)) {
									currentRegion.createVeins();
									regionsOres.put(idHash, currentRegion);
								}
								int layer = data - 4;
								OreVeinGenerator oreVein = OreGenerator.getVein(chunkCurr, layer);
								if (oreVein != null) {
									int sizeVein = OreGenerator.sizeChunk(chunkCurr, layer) / 1000;
									packet.addBlock(c.xPosition * 16 + x, 1, c.zPosition * 16 + z, (short) oreVein.oreVeinID);
									packet.addBlock(c.xPosition * 16 + x, 2, c.zPosition * 16 + z, (short) sizeVein);
								}
								break;
						}
					}
				}
			}
			packet.level = aItem.getHarvestLevel(aStack, "");
			DetravNetwork.INSTANCE.sendToPlayer(packet, (EntityPlayerMP) aPlayer);
			if (!aPlayer.capabilities.isCreativeMode)
				tool.doDamage(aStack, this.mCosts * chunks.size());
		}
		return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);
	}
	
	void addChatMassageByValue(EntityPlayer aPlayer, int value, String name) {
		if (value < 0) {
			aPlayer.addChatMessage(new ChatComponentText(foundTexts[6] + name));
		} else if (value < 1) {
			aPlayer.addChatMessage(new ChatComponentText(foundTexts[0]));
		} else
			aPlayer.addChatMessage(new ChatComponentText(foundTexts[6] + name + " " + value));
	}
	
	public boolean onItemUse(GT_MetaBase_Item aItem, ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, int aSide, float hitX, float hitY, float hitZ) {
		long dataSrc = DetravMetaGeneratedTool01.INSTANCE.getToolGTDetravData(aStack);
		int data = (int) dataSrc;
		switch (data) {
			case 2:
				if (aWorld.getBlock(aX, aY, aZ) == Blocks.bedrock) {
					if (!aWorld.isRemote) {
						FluidStack fStack = GT_UndergroundOil.undergroundOil(aWorld.getChunkFromBlockCoords(aX, aZ), -1);
						addChatMassageByValue(aPlayer, fStack.amount, fStack.getLocalizedName());
						if (!aPlayer.capabilities.isCreativeMode) {
							((DetravMetaGeneratedTool01) aItem).doDamage(aStack, this.mCosts);
						}
					}
					return true;
				} else {
					if (!aWorld.isRemote) {
						prospectSingleChunk(aItem, aStack, aPlayer, aWorld, aX, aY, aZ);
					}
					return true;
				}
			case 3:
				if (!aWorld.isRemote) {
					FluidStack fStack = GT_UndergroundOil.undergroundOil(aWorld.getChunkFromBlockCoords(aX, aZ), -1);
					addChatMassageByValue(aPlayer, fStack.amount, fStack.getLocalizedName());
					if (!aPlayer.capabilities.isCreativeMode) {
						((DetravMetaGeneratedTool01) aItem).doDamage(aStack, this.mCosts);
					}
					return true;
				}
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				if (!aWorld.isRemote) {
					Chunk ch = aWorld.getChunkFromBlockCoords(aX, aZ);
					int tier = data - 4;
					OreVeinGenerator oreVein = OreGenerator.getVein(ch, tier);
					OreVein ore = OreGenerator.getOreVein(ch, tier);
					if (oreVein != null) {
						int sizeVein = OreGenerator.sizeChunk(ch, tier);
						aPlayer.addChatMessage(new ChatComponentText(foundTexts[6] + ": " + ore.nameVein + ", size: " + GT_Utility.formatNumbers(sizeVein)));
					}
					return true;
				}
		}
		return true;
	}
	
}
