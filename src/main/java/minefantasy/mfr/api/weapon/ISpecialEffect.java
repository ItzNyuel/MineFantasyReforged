package minefantasy.mfr.api.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface ISpecialEffect {
    void onProperHit(EntityLivingBase user, ItemStack weapon, Entity hit, float dam);
}
