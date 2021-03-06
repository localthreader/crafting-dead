/*
 * Crafting Dead
 * Copyright (C) 2021  NexusNode LTD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.craftingdead.core.item;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.client.renderer.item.GunRenderer;
import com.craftingdead.core.client.renderer.item.IRendererProvider;
import com.craftingdead.core.item.gun.AbstractGun;
import com.craftingdead.core.item.gun.AbstractGunType;
import com.craftingdead.core.item.gun.magazine.IMagazine;
import com.craftingdead.core.util.Text;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;

public class GunItem extends ShootableItem implements IRendererProvider {

  private final AbstractGunType<?> gunType;

  /**
   * A factory that creates a {@link GunRenderer} instance for this gun.
   */
  private final Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory;

  public GunItem(Properties properties) {
    super(properties);
    this.gunType = properties.gunType;
    this.rendererFactory = properties.rendererFactory;
  }

  public AbstractGunType<?> getGunType() {
    return this.gunType;
  }

  @Override
  public GunRenderer getRenderer() {
    return DistExecutor.safeCallWhenOn(Dist.CLIENT, this.rendererFactory);
  }

  @Override
  public Predicate<ItemStack> getAllSupportedProjectiles() {
    return itemStack -> this.gunType.getAcceptedMagazines()
        .stream()
        .anyMatch(itemStack.getItem()::equals);
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack itemStack, @Nullable CompoundNBT nbt) {
    return this.gunType.createCapabilityProvider(itemStack);
  }

  @Override
  public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
    return true;
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
      boolean slotChanged) {
    return oldStack.getItem() != newStack.getItem();
  }

  @Override
  public void appendHoverText(ItemStack itemStack, World world,
      List<ITextComponent> lines,
      ITooltipFlag tooltipFlag) {
    super.appendHoverText(itemStack, world, lines, tooltipFlag);

    itemStack.getCapability(ModCapabilities.GUN).ifPresent(gun -> {
      ITextComponent ammoCount =
          Text.of(gun.getAmmoProvider().getMagazine().map(IMagazine::getSize).orElse(0))
              .withStyle(TextFormatting.RED);
      ITextComponent damageText =
          Text.of(this.gunType.getDamage()).withStyle(TextFormatting.RED);
      ITextComponent headshotDamageText = Text
          .of((int) (this.gunType.getDamage() * AbstractGun.HEADSHOT_MULTIPLIER))
          .withStyle(TextFormatting.RED);
      ITextComponent accuracyText =
          Text.of((int) (this.gunType.getAccuracyPct() * 100D) + "%")
              .withStyle(TextFormatting.RED);
      ITextComponent rpmText =
          Text.of(this.gunType.getFireRateRPM()).withStyle(TextFormatting.RED);
      ITextComponent rangeText =
          Text.of(this.gunType.getRange() + " blocks").withStyle(TextFormatting.RED);

      lines.add(Text.translate("item_lore.gun_item.ammo_amount")
          .withStyle(TextFormatting.GRAY)
          .append(ammoCount));
      lines.add(Text.translate("item_lore.gun_item.damage")
          .withStyle(TextFormatting.GRAY)
          .append(damageText));
      lines.add(Text.translate("item_lore.gun_item.headshot_damage")
          .withStyle(TextFormatting.GRAY)
          .append(headshotDamageText));

      if (this.gunType.getBulletAmountToFire() > 1) {
        ITextComponent pelletsText =
            Text.of(this.gunType.getBulletAmountToFire()).withStyle(TextFormatting.RED);

        lines.add(Text.translate("item_lore.gun_item.pellets_shot")
            .withStyle(TextFormatting.GRAY)
            .append(pelletsText));
      }

      for (AttachmentItem attachment : gun.getAttachments()) {
        ITextComponent attachmentNameText =
            attachment.getDescription().plainCopy().withStyle(TextFormatting.RED);
        lines.add(Text.translate("item_lore.gun_item.attachment")
            .withStyle(TextFormatting.GRAY)
            .append(attachmentNameText));
      }

      lines.add(Text.translate("item_lore.gun_item.rpm")
          .withStyle(TextFormatting.GRAY)
          .append(rpmText));
      lines.add(Text.translate("item_lore.gun_item.accuracy")
          .withStyle(TextFormatting.GRAY)
          .append(accuracyText));
      lines.add(Text.translate("item_lore.gun_item.range")
          .withStyle(TextFormatting.GRAY)
          .append(rangeText));
    });
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return true;
  }

  @Override
  public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
    return enchantment == Enchantments.FLAMING_ARROWS || enchantment == Enchantments.POWER_ARROWS
        || enchantment == Enchantments.UNBREAKING
        || super.canApplyAtEnchantingTable(stack, enchantment);
  }

  @Override
  public int getEnchantmentValue() {
    return 1;
  }

  @Override
  public int getDefaultProjectileRange() {
    return 0;
  }

  public static class Properties extends Item.Properties {

    private AbstractGunType<?> gunType;

    private Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory;

    public Properties setGunType(AbstractGunType<?> gunType) {
      this.gunType = gunType;
      return this;
    }

    public Properties setRendererFactory(
        Supplier<DistExecutor.SafeCallable<GunRenderer>> rendererFactory) {
      this.rendererFactory = rendererFactory;
      return this;
    }
  }
}
