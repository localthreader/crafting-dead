package com.craftingdead.core.entity.ai;

import java.util.EnumSet;
import java.util.List;
import com.craftingdead.core.entity.grenade.GrenadeEntity;
import com.craftingdead.core.potion.ModEffects;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class FollowAttractiveGrenadeGoal extends Goal {

  private final MobEntity goalOwner;
  private final double moveSpeedMultiplier;
  private GrenadeEntity grenade;
  private int delayCounter;

  public FollowAttractiveGrenadeGoal(MobEntity goalOwner, double moveSpeedMultiplier) {
    this.goalOwner = goalOwner;
    this.moveSpeedMultiplier = moveSpeedMultiplier;
    this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
  }

  @Override
  public boolean shouldExecute() {
    if (this.goalOwner.isPotionActive(ModEffects.FLASH_BLINDNESS.get())) {
      return false;
    }
    List<GrenadeEntity> list = this.goalOwner.world.getEntitiesWithinAABB(GrenadeEntity.class,
        this.goalOwner.getBoundingBox().grow(20.0D, 5.0D, 20.0D));

    GrenadeEntity nearestGrenade = null;
    double lastSqDistance = Double.MAX_VALUE;

    for (GrenadeEntity grenade : list) {
      if (grenade.isAttracting()) {
        double sqDistance = this.goalOwner.getDistanceSq(grenade);
        if (sqDistance <= lastSqDistance) {
          lastSqDistance = sqDistance;
          nearestGrenade = grenade;
        }
      }
    }

    if (nearestGrenade == null) {
      return false;
    }

    this.grenade = nearestGrenade;
    return true;
  }

  @Override
  public boolean shouldContinueExecuting() {
    if (!this.grenade.isAlive()) {
      return false;
    }

    if (this.goalOwner.isPotionActive(ModEffects.FLASH_BLINDNESS.get())) {
      return false;
    }

    if (this.grenade.world != this.goalOwner.world) {
      return false;
    }

    return this.goalOwner.getDistanceSq(this.grenade) <= 256.0D;
  }

  @Override
  public void startExecuting() {
    this.delayCounter = 0;
  }

  @Override
  public void resetTask() {
    this.grenade = null;
  }

  @Override
  public void tick() {
    this.goalOwner.getLookController().setLookPosition(this.grenade.getPosX(),
        this.grenade.getPosYEye(),
        this.grenade.getPosZ());
    if (--this.delayCounter <= 0) {
      this.delayCounter = 5 + this.goalOwner.getRNG().nextInt(10);
      this.goalOwner.getNavigator().tryMoveToEntityLiving(this.grenade, this.moveSpeedMultiplier);
    }
  }
}
