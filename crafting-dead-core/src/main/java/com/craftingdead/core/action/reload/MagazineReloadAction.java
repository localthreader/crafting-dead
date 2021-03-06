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

package com.craftingdead.core.action.reload;

import java.util.List;
import java.util.Optional;
import com.craftingdead.core.CraftingDead;
import com.craftingdead.core.action.ActionTypes;
import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.event.CollectMagazineItemHandlers;
import com.craftingdead.core.inventory.InventorySlotType;
import com.craftingdead.core.item.gun.ammoprovider.IAmmoProvider;
import com.craftingdead.core.item.gun.ammoprovider.MagazineAmmoProvider;
import com.craftingdead.core.item.gun.magazine.IMagazine;
import com.craftingdead.core.living.ILiving;
import com.google.common.collect.ImmutableList;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MagazineReloadAction extends AbstractReloadAction {

  private final MagazineAmmoProvider ammoProvider;

  private ItemStack newMagazineStack;

  private MagazineLocation magazineLocation;

  public MagazineReloadAction(ILiving<?, ?> performer) {
    super(ActionTypes.MAGAZINE_RELOAD.get(), performer);
    IAmmoProvider ammoProvider = this.gun.getAmmoProvider();
    if (!(ammoProvider instanceof MagazineAmmoProvider)) {
      throw new IllegalStateException("No MagazineAmmoProvider present");
    }
    this.ammoProvider = (MagazineAmmoProvider) ammoProvider;
  }

  @Override
  public boolean start() {
    Optional<MagazineLocation> result = this.findMagazine(this.performer);
    if (!result.isPresent()) {
      return false;
    }

    this.magazineLocation = result.get();
    this.newMagazineStack =
        this.magazineLocation.itemHandler.extractItem(this.magazineLocation.slot, 1, false);

    return super.start();
  }

  @Override
  protected void loadNewMagazineStack(boolean displayOnly) {
    this.ammoProvider.setMagazineStack(this.newMagazineStack);
    if (!displayOnly) {
      if (!this.oldMagazineStack.isEmpty() && this.performer.getEntity() instanceof PlayerEntity) {
        ((PlayerEntity) this.performer.getEntity()).addItem(this.oldMagazineStack);
      }
    }
  }

  @Override
  protected void revert() {
    this.ammoProvider.setMagazineStack(this.oldMagazineStack);
    ItemStack remainingStack =
        this.magazineLocation.itemHandler.insertItem(this.magazineLocation.slot, newMagazineStack,
            false);
    this.performer.getEntity().spawnAtLocation(remainingStack);
  }

  private List<IItemHandler> collectItemHandlers(ILiving<?, ?> living) {
    ImmutableList.Builder<IItemHandler> builder = ImmutableList.builder();

    CollectMagazineItemHandlers event = new CollectMagazineItemHandlers(living);
    MinecraftForge.EVENT_BUS.post(event);
    builder.addAll(event.getItemHandlers());

    // Vest - first
    living.getItemHandler().getStackInSlot(InventorySlotType.VEST.getIndex())
        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(builder::add);
    // Backpack - second
    if (CraftingDead.getInstance().isTravelersBackpacksLoaded()
        && living.getEntity() instanceof PlayerEntity) {
      PlayerEntity playerEntity = (PlayerEntity) living.getEntity();
      TravelersBackpackInventory backpackInventory = CapabilityUtils.getBackpackInv(playerEntity);
      if (backpackInventory != null) {
        builder.add(backpackInventory.getInventory());
      }
    }
    // Inventory - third
    living.getEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        .ifPresent(builder::add);
    return builder.build();
  }

  private Optional<MagazineLocation> findMagazine(ILiving<?, ?> living) {
    for (IItemHandler itemHandler : this.collectItemHandlers(living)) {
      for (int i = 0; i < itemHandler.getSlots(); ++i) {
        ItemStack itemStack = itemHandler.getStackInSlot(i);
        if (this.gun.getAcceptedMagazines().contains(itemStack.getItem())
            && !itemStack.getCapability(ModCapabilities.MAGAZINE)
                .map(IMagazine::isEmpty)
                .orElse(true)) {
          return Optional.of(new MagazineLocation(itemHandler, i));
        }
      }
    }
    return Optional.empty();
  }

  private static class MagazineLocation {

    private final IItemHandler itemHandler;
    private final int slot;

    public MagazineLocation(IItemHandler itemHandler, int slot) {
      this.itemHandler = itemHandler;
      this.slot = slot;
    }
  }
}
