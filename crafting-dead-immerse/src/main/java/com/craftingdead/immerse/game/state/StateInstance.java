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

package com.craftingdead.immerse.game.state;

public class StateInstance<CTX> {

  private final IState<?> state;
  private final CTX context;

  public StateInstance(IState<?> state, CTX context) {
    this.state = state;
    this.context = context;
  }

  protected boolean tick() {
    return false;
  }

  public IState<?> getState() {
    return this.state;
  }

  public CTX getContext() {
    return this.context;
  }
}
