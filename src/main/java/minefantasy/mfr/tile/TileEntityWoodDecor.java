package minefantasy.mfr.tile;

import minefantasy.mfr.block.BlockWoodDecor;
import minefantasy.mfr.init.MineFantasyMaterials;
import minefantasy.mfr.material.CustomMaterial;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileEntityWoodDecor extends TileEntityBase {
	private String texture;
	private CustomMaterial material;

	public TileEntityWoodDecor(String texture) {
		this.texture = texture;
		this.material = CustomMaterial.NONE;
	}

	public TileEntityWoodDecor(String tex, CustomMaterial material) {
		this.texture = tex;
		this.material = material;
	}

	public static int getCapacity(int tier) {
		return (tier) * 4 + 4;
	}

	public CustomMaterial getMaterial() {
		return this.material != CustomMaterial.NONE ? this.material : trySetMaterial(MineFantasyMaterials.Names.SCRAP_WOOD);
	}

	public void setMaterial(CustomMaterial material) {
		this.material = material;
	}

	public String getMaterialName() {
		return this.material != CustomMaterial.NONE ? material.getName() : MineFantasyMaterials.Names.SCRAP_WOOD;
	}

	public CustomMaterial trySetMaterial(String materialName) {
		CustomMaterial material = CustomMaterial.getMaterial(materialName);
		if (material != CustomMaterial.NONE) {
			this.material = material;
		}
		return this.material;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("material", this.getMaterialName());
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.trySetMaterial(nbt.getString("material"));
	}

	public int getCapacity() {
		CustomMaterial material = getMaterial();
		if (material != CustomMaterial.NONE) {
			return getCapacity(material.tier);
		}
		return getCapacity(0);
	}

	public String getTexName() {
		if (world != null) {
			Block block = world.getBlockState(pos).getBlock();
			if (block instanceof BlockWoodDecor) {
				this.texture = ((BlockWoodDecor) block).getFullTexName();
			}
		}
		return texture;
	}
}
