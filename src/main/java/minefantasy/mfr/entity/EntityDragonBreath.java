package minefantasy.mfr.entity;

import minefantasy.mfr.entity.mob.DragonBreath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.Random;

public class EntityDragonBreath extends EntityFireball {
	private static final float size = 0.75F;
	private static final DataParameter<Integer> TYPE_ID = EntityDataManager.<Integer>createKey(Entity.class, DataSerializers.VARINT);
	public Random rand = new Random();
	private DragonBreath breath;

	public EntityDragonBreath(World world) {
		super(world);
		this.setSize(size, size);
	}

	public EntityDragonBreath(World world, EntityLivingBase shooter, double xVelocity, double yVelocity,
			double zVelocity, float spread) {
		this(world);
		this.shootingEntity = shooter;
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		xVelocity += this.rand.nextGaussian() * spread;
		yVelocity += this.rand.nextGaussian() * spread;
		zVelocity += this.rand.nextGaussian() * spread;
		double d3 = MathHelper.sqrt(xVelocity * xVelocity + yVelocity * yVelocity + zVelocity * zVelocity);
		this.accelerationX = xVelocity / d3 * 0.1D;
		this.accelerationY = yVelocity / d3 * 0.1D;
		this.accelerationZ = zVelocity / d3 * 0.1D;
	}

	@Override
	protected void entityInit() {
		dataManager.register(TYPE_ID, 0);
	}

	@Override
	public void setFire(int time) {
	}

	public int getType() {
		return dataManager.get(TYPE_ID);
	}

	public EntityDragonBreath setType(int type) {
		dataManager.set(TYPE_ID, type);
		return this;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.ticksExisted >= getLifeSpan()) {
			setDead();
		}
		if (ticksExisted % 5 == 0 && shouldExpand()) {
			int lifeScale = (int) Math.floor(ticksExisted / 5F);
			float newSize = size + (size / 4 * lifeScale);
			this.setSize(newSize, newSize);
		}
		if (ticksExisted % 10 == 0) {
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						int i = (int) posX + x;
						int j = (int) posY + y;
						int k = (int) posZ + z;
						BlockPos breathPos = new BlockPos(i, j, k);
						getBreath().hitBlock(world, world.getBlockState(breathPos), this, breathPos, false);
					}
				}
			}
		}
	}

	private boolean shouldExpand() {
		return getBreath().shouldExpand();
	}

	private int getLifeSpan() {
		return getBreath().getLifeSpan();
	}

	/**
	 * Called when this EntityFireball hits a block or entity.
	 */
	protected void onImpact(RayTraceResult pos) {
		if (!this.world.isRemote) {
			if (pos.entityHit != null && (shootingEntity == null || shootingEntity != pos.entityHit)) {
				getBreath().onHitEntity(pos.entityHit, this);
			} else {
				int i = pos.getBlockPos().getX();
				int j = pos.getBlockPos().getY();
				int k = pos.getBlockPos().getZ();

				switch (pos.sideHit) {
					case DOWN:
						--j;
						break;
					case UP:
						++j;
						break;
					case NORTH:
						--k;
						break;
					case SOUTH:
						++k;
						break;
					case WEST:
						--i;
						break;
					case EAST:
						++i;
				}
				BlockPos breathPos = new BlockPos(i, j, k);
				getBreath().hitBlock(world, world.getBlockState(breathPos), this, breathPos, true);
			}

			this.setDead();
		}
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this
	 * Entity.
	 */
	public boolean canBeCollidedWith() {
		return false;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource src, float power) {
		return false;
	}

	public void modifySpeed(float mod) {
		this.accelerationX *= mod;
		this.accelerationY *= mod;
		this.accelerationZ *= mod;
	}

	public float getDamage() {
		if (getEntityData().hasKey("Damage")) {
			return getEntityData().getFloat("Damage");
		}
		return 2.0F;
	}

	public EntityDragonBreath setDamage(float dam) {
		getEntityData().setFloat("Damage", dam);
		return this;
	}

	public DragonBreath getBreath() {
		if (breath == null) {
			DragonBreath load = DragonBreath.PROJECTILES.get(getType());
			if (load != null) {
				breath = load;
			} else {
				breath = DragonBreath.frost;
			}
		}
		return breath;
	}

	public DamageSource causeFireblastDamage() {
		return this.getBreath().getDamageSource(this, this.shootingEntity);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("type", getType());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		setType(nbt.getInteger("type"));
	}

	public String getTextureName() {
		return getBreath().getTexture(this);
	}
}