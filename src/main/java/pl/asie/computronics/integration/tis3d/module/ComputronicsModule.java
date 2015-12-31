package pl.asie.computronics.integration.tis3d.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Vexatos
 */
public class ComputronicsModule extends AbstractModule {

	protected ComputronicsModule(Casing casing, Face face) {
		super(casing, face);
	}

	@Override
	public void onData(NBTTagCompound nbt) {
		super.onData(nbt);
		this.readFromNBT(nbt);
	}

	protected void sendData() {
		if(!isVisible()) {
			return;
		}
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		this.getCasing().sendData(this.getFace(), nbt, (byte) 0);
	}
}