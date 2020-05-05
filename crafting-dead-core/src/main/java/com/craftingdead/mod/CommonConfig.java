package com.craftingdead.mod;

import org.apache.commons.lang3.tuple.Pair;
import com.craftingdead.mod.client.tutorial.ModTutorialSteps;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {

  public static final CommonConfig commonConfig;
  public static final ForgeConfigSpec commonConfigSpec;

  public static final ClientConfig clientConfig;
  public static final ForgeConfigSpec clientConfigSpec;

  static {
    final Pair<CommonConfig, ForgeConfigSpec> commonConfigPair =
        new ForgeConfigSpec.Builder().configure(CommonConfig::new);
    commonConfigSpec = commonConfigPair.getRight();
    commonConfig = commonConfigPair.getLeft();

    final Pair<ClientConfig, ForgeConfigSpec> clientConfigPair =
        new ForgeConfigSpec.Builder().configure(ClientConfig::new);
    clientConfigSpec = clientConfigPair.getRight();
    clientConfig = clientConfigPair.getLeft();
  }

  public final ForgeConfigSpec.ConfigValue<String> masterServerHost;

  public final ForgeConfigSpec.IntValue masterServerPort;

  private CommonConfig(ForgeConfigSpec.Builder builder) {
    builder.push("common");
    {
      this.masterServerHost = builder //
          .translation("options.craftingdead.common.master_server_host") //
          .define("masterServerHost", "localhost");
      this.masterServerPort = builder //
          .translation("options.craftingdead.common.master_server_port") //
          .defineInRange("masterServerPort", 25578, 0, 65535);
    }
    builder.pop();
  }

  public static class ClientConfig {

    public final ForgeConfigSpec.BooleanValue applyBranding;

    public final ForgeConfigSpec.BooleanValue enableDiscordRpc;

    public final ForgeConfigSpec.BooleanValue displayBlood;

    public final ForgeConfigSpec.EnumValue<ModTutorialSteps> tutorialStep;

    private ClientConfig(ForgeConfigSpec.Builder builder) {
      builder.push("client");
      {
        this.applyBranding = builder //
            .translation("options.craftingdead.client.apply_branding") //
            .define("applyBranding", false);
        this.enableDiscordRpc = builder //
            .translation("options.craftingdead.client.enable_discord_rpc") //
            .define("enableDiscordRpc", true);
        this.displayBlood = builder //
            .translation("options.craftingdead.client.display_blood") //
            .define("displayBlood", true);
        this.tutorialStep = builder.defineEnum("tutorialStep", ModTutorialSteps.OPEN_INVENTORY);
      }
      builder.pop();
    }
  }
}