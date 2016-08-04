package pl.asie.computronics.integration.railcraft.item;

import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.carts.EntityLocomotiveElectric;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import pl.asie.computronics.Computronics;
import pl.asie.computronics.integration.railcraft.tile.TileLocomotiveRelay;
import pl.asie.computronics.oc.manual.IItemWithPrefix;
import pl.asie.computronics.reference.Config;
import pl.asie.computronics.util.StringUtil;

import java.util.List;

/**
 * @author Vexatos
 */
public class ItemRelaySensor extends Item implements IItemWithPrefix {

	public ItemRelaySensor() {
		super();
		this.setCreativeTab(Computronics.tab);
		this.setHasSubtypes(false);
		this.setUnlocalizedName("computronics.relaySensor");
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setFull3D();
		this.setNoRepair();
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if(player.worldObj.isRemote) {
			return EnumActionResult.FAIL;
		}
		TileEntity tile = world.getTileEntity(pos);
		if(player.isSneaking() && tile != null && tile instanceof TileLocomotiveRelay) {
			if(!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if(stack.hasTagCompound()) {
				NBTTagCompound data = stack.getTagCompound();
				data.setInteger("relayX", pos.getX());
				data.setInteger("relayY", pos.getY());
				data.setInteger("relayZ", pos.getZ());
				data.setBoolean("bound", true);
				stack.setTagCompound(data);
				player.swingArm(hand);
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.FAIL;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if(player.isSneaking() && entity != null) {
			if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("bound") && !player.worldObj.isRemote) {
				NBTTagCompound data = stack.getTagCompound();
				final BlockPos pos = new BlockPos(
					data.getInteger("relayX"),
					data.getInteger("relayY"),
					data.getInteger("relayZ")
				);
				if(entity instanceof EntityLocomotiveElectric) {
					if(!player.worldObj.isBlockLoaded(pos)) {
						player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.noRelayDetected"));
						return true;
					}
					TileEntity tile = entity.worldObj.getTileEntity(pos);
					if(tile != null && tile instanceof TileLocomotiveRelay) {
						TileLocomotiveRelay relay = (TileLocomotiveRelay) tile;
						EntityLocomotiveElectric loco = (EntityLocomotiveElectric) entity;
						if(loco.dimension == relay.getWorld().provider.getDimension()) {
							if(loco.getDistanceSq(relay.getPos()) <= Config.LOCOMOTIVE_RELAY_RANGE * Config.LOCOMOTIVE_RELAY_RANGE) {
								relay.setLocomotive(loco);
								player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.bound"));
								player.swingArm(EnumHand.MAIN_HAND);
								player.setHeldItem(EnumHand.MAIN_HAND, null);
								ForgeEventFactory.onPlayerDestroyItem(player, stack, EnumHand.MAIN_HAND);
							} else {
								player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.tooFarAway"));
							}
						} else {
							player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.wrongDim"));
						}
					} else {
						player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.noRelay"));
					}
				} else if(entity instanceof EntityLocomotive) {
					player.addChatComponentMessage(new TextComponentTranslation("chat.computronics.sensor.wrongLoco"));
					return true;
				}
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List text, boolean par4) {
		String descKey;
		if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("bound")) {
			NBTTagCompound data = stack.getTagCompound();
			int x = data.getInteger("relayX");
			int y = data.getInteger("relayY");
			int z = data.getInteger("relayZ");
			text.add(TextFormatting.AQUA + StringUtil.localizeAndFormat("tooltip.computronics.sensor.bound",
				String.valueOf(x), String.valueOf(y), String.valueOf(z)));

			descKey = "tooltip.computronics.sensor.desc2";
		} else {
			descKey = "tooltip.computronics.sensor.desc1";
		}
		String[] local = StringUtil.localize(descKey).split("\n");
		for(String s : local) {
			text.add(TextFormatting.GRAY + s);
		}
	}

	@Override
	public String getDocumentationName(ItemStack stack) {
		return "relay_sensor";
	}

	@Override
	public String getPrefix(ItemStack stack) {
		return "railcraft/";
	}
}
