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

import com.craftingdead.core.action.ActionTypes;
import com.craftingdead.core.item.gun.ammoprovider.IAmmoProvider;
import com.craftingdead.core.item.gun.ammoprovider.RefillableAmmoProvider;
import com.craftingdead.core.living.ILiving;

public class RefillableReloadAction extends AbstractReloadAction {

  private final RefillableAmmoProvider ammoProvider;

  private int oldAmmoCount;

  public RefillableReloadAction(ILiving<?, ?> performer) {
    super(ActionTypes.REFILLABLE_RELOAD.get(), performer);
    IAmmoProvider ammoProvider = this.gun.getAmmoProvider();
    if (!(ammoProvider instanceof RefillableAmmoProvider)) {
      throw new IllegalStateException("No RefillableAmmoProvider present");
    }
    this.ammoProvider = (RefillableAmmoProvider) ammoProvider;
  }

  @Override
  public boolean start() {
    return (this.ammoProvider.hasInfiniteAmmo() || this.ammoProvider.getReserveSize() > 0)
        && super.start();
  }

  @Override
  protected void loadNewMagazineStack(boolean displayOnly) {
    if (!displayOnly) {
      this.oldAmmoCount = this.ammoProvider.getExpectedMagazine().getSize();
      this.ammoProvider.refillMagazine();
    }
  }

  @Override
  protected void revert() {
    this.ammoProvider.moveAmmoToReserve(this.ammoProvider.getExpectedMagazine().getSize());
    this.ammoProvider.moveAmmoToMagazine(this.oldAmmoCount);
  }
}
