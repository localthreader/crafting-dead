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

package com.craftingdead.core;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

  public final ForgeConfigSpec.BooleanValue hydrationEnabled;
  public final ForgeConfigSpec.BooleanValue brokenLegsEnabled;
  public final ForgeConfigSpec.BooleanValue bleedingEnabled;

  public ServerConfig(ForgeConfigSpec.Builder builder) {
    builder.push("server");
    {
      this.hydrationEnabled = builder
          .translation("options.craftingdead.server.hydration_enabled")
          .define("hydrationEnabled", true);
      this.brokenLegsEnabled = builder
          .translation("options.craftingdead.server.broken_legs_enabled")
          .define("brokenLegsEnabled", true);
      this.bleedingEnabled = builder
          .translation("options.craftingdead.server.bleeding_enabled")
          .define("bleedingEnabled", true);
    }
  }
}
