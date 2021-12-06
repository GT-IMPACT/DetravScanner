package com.detrav.items.behaviours;

import com.detrav.items.DetravMetaGeneratedTool01;
import com.detrav.net.DetravNetwork;
import com.detrav.net.ProspectingPacket;
import com.impact.common.oregeneration.OreVein;
import com.impact.common.oregeneration.OresRegion;
import com.impact.mods.gregtech.enums.OreGenerator;
import gregtech.api.items.GT_MetaBase_Item;
import gregtech.common.GT_UndergroundOil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
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
				if (data > 5) data = 2;
				switch (data) {
					case 2:
						aPlayer.addChatMessage(new ChatComponentText("Set Mode: Oil, Any Block"));
						break;
					case 3:
						aPlayer.addChatMessage(new ChatComponentText("Set Mode: Pollution, Any Block"));
						break;
					case 4:
						aPlayer.addChatMessage(new ChatComponentText("Set Mode: Impact Ores (Layer 1)"));
						break;
					case 5:
						aPlayer.addChatMessage(new ChatComponentText("Set Mode: Impact Ores (Layer 2)"));
						break;
					default:
						aPlayer.addChatMessage(new ChatComponentText("Set Mode: ERROR"));
						break;
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
			for (int i = -size; i <= size; i++)
				for (int j = -size; j <= size; j++)
					if (i != -size && i != size && j != -size && j != size)
						chunks.add(aWorld.getChunkFromChunkCoords(cX + i, cZ + j));
			size = size - 1;
			final ProspectingPacket packet = new ProspectingPacket(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size, data);
			final String small_ore_keyword = StatCollector.translateToLocal("detrav.scanner.small_ore.keyword");
			for (Chunk c : chunks) {
				for (int x = 0; x < 16; x++)
					for (int z = 0; z < 16; z++) {
						final int ySize = c.getHeightValue(x, z);
						for (int y = 1; y < ySize; y++) {
							switch (data) {
//                                case 0:
//                                case 1:
//                                    final Block tBlock = c.getBlock(x, y, z);
//                                    short tMetaID = (short) c.getBlockMetadata(x, y, z);
//                                    if (tBlock instanceof GT_Block_Ores_Abstract) {
//                                        TileEntity tTileEntity = c.getTileEntityUnsafe(x, y, z);
//                                        if ((tTileEntity instanceof GT_TileEntity_Ores) && ((GT_TileEntity_Ores) tTileEntity).mNatural) {
//                                            tMetaID = (short) ((GT_TileEntity_Ores) tTileEntity).getMetaData();
//                                            try {
//                                                String name = GT_LanguageManager.getTranslation(tBlock.getUnlocalizedName() + "." + tMetaID + ".name");
//                                                if (data != 1 && name.startsWith(small_ore_keyword)) continue;
//                                                packet.addBlock(c.xPosition * 16 + x, y, c.zPosition * 16 + z, tMetaID);
//                                            } catch (Exception e) {
//                                                String name = tBlock.getUnlocalizedName() + ".";
//                                                if (data != 1 && name.contains(".small.")) continue;
//                                                packet.addBlock(c.xPosition * 16 + x, y, c.zPosition * 16 + z, tMetaID);
//                                            }
//                                        }
//                                    } else if (data == 1) {
//                                        ItemData tAssotiation = GT_OreDictUnificator.getAssociation(new ItemStack(tBlock, 1, tMetaID));
//                                        if ((tAssotiation != null) && (tAssotiation.mPrefix.toString().startsWith("ore"))) {
//                                            packet.addBlock(c.xPosition * 16 + x, y, c.zPosition * 16 + z, (short) tAssotiation.mMaterial.mMaterial.mMetaItemSubID);
//                                        }
//                                    }
//                                    break;
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
									Chunk chunkCurr = aWorld.getChunkFromBlockCoords(c.xPosition * 16 + x, c.zPosition * 16 + z);
									ChunkCoordIntPair chunkPosition = chunkCurr.getChunkCoordIntPair();
									int xRegCurrent = (chunkPosition.chunkXPos >> 5) % 512;
									int zRegCurrent = (chunkPosition.chunkZPos >> 5) % 512;
									OresRegion currentRegion = new OresRegion(xRegCurrent, zRegCurrent);
									if (!regionsOres.contains(currentRegion)) {
										currentRegion.createVeins();
										regionsOres.add(currentRegion);
									}
									int layer = data - 4;
									int sizeVein = OreGenerator.sizeChunk(chunkCurr, layer) / 1000;
									OreVein oreVein = OreGenerator.getVein(chunkCurr, layer);
									if (oreVein != null) {
										int idVein = OreGenerator.values().length - 1;
										for (int i = 0; i < OreGenerator.values().length; i++) {
											if (OreGenerator.values()[i].name().equals(oreVein.oreGenerator)) {
												idVein = i;
												break;
											}
										}
										packet.addBlock(c.xPosition * 16 + x, 1, c.zPosition * 16 + z, (short) idVein);
										packet.addBlock(c.xPosition * 16 + x, 2, c.zPosition * 16 + z, (short) sizeVein);
									}
									break;
							}
							if (data > 1)
								break;
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
		long data = DetravMetaGeneratedTool01.INSTANCE.getToolGTDetravData(aStack);
		if (data < 2) {
			if (aWorld.getBlock(aX, aY, aZ) == Blocks.bedrock) {
				if (!aWorld.isRemote) {
					FluidStack fStack = GT_UndergroundOil.undergroundOil(aWorld.getChunkFromBlockCoords(aX, aZ), -1);
					addChatMassageByValue(aPlayer, fStack.amount, fStack.getLocalizedName());
					if (!aPlayer.capabilities.isCreativeMode)
						((DetravMetaGeneratedTool01) aItem).doDamage(aStack, this.mCosts);
				}
				return true;
			} else {
				if (!aWorld.isRemote) {
					prospectSingleChunk(aItem, aStack, aPlayer, aWorld, aX, aY, aZ);
				}
				return true;
			}
		}
		if (data < 3)
			if (!aWorld.isRemote) {
				FluidStack fStack = GT_UndergroundOil.undergroundOil(aWorld.getChunkFromBlockCoords(aX, aZ), -1);
				addChatMassageByValue(aPlayer, fStack.amount, fStack.getLocalizedName());
				if (!aPlayer.capabilities.isCreativeMode)
					((DetravMetaGeneratedTool01) aItem).doDamage(aStack, this.mCosts);
				return true;
			}
		if (!aWorld.isRemote) {
			int polution = getPolution(aWorld, aX, aZ);
			addChatMassageByValue(aPlayer, polution, "Pollution");
		}
		return true;
	}
	
}
