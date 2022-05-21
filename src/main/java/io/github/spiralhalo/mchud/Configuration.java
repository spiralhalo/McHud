package io.github.spiralhalo.mchud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.background.EmptyBackground;
import dev.lambdaurora.spruceui.option.SpruceBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceDoubleOption;
import dev.lambdaurora.spruceui.option.SpruceIntegerInputOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import java.nio.file.Files;

public class Configuration implements ModMenuApi {
	public static ConfigObject co = new ConfigObject();

	private static TranslatableComponent CONFIG_TITLE = new TranslatableComponent("sh_mchud.config.title");
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static File configJsonFile;

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::new;
	}

	private static File initOrGetFile() {
		if (configJsonFile == null) {
			configJsonFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sh_mchud.json5");
		}

		return configJsonFile;
	}

	private static void saveUserConfig(ConfigObject toSave) {
		co = toSave;

		try {
			final String result = GSON.toJson(toSave);

			if (!configJsonFile.exists()) {
				configJsonFile.createNewFile();
			}

			try (FileOutputStream out = new FileOutputStream(initOrGetFile(), false)) {
				out.write(result.getBytes());
				out.flush();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			BucketWarning.LOGGER.warn("Failed to save config file");
		}
	}

	public static void loadConfigFromDisk() {
		try (Reader reader = Files.newBufferedReader(initOrGetFile().toPath())) {
			co = GSON.fromJson(reader, ConfigObject.class);
		} catch (final Exception e) {
			e.printStackTrace();
			BucketWarning.LOGGER.error("Failed to load config file. Using default config");
		}
	}

	public static class ConfigObject implements Cloneable {
		public boolean showBucketUseWarning = true;
		public int warningOffsetLine = 0;
		public boolean cancelPlayerActionEnabled = false;
		public boolean notifyActionCancellation = true;

		@Override
		public ConfigObject clone() {
			try {
				return (ConfigObject) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}
	}

	private static class ConfigScreen extends SpruceScreen {
		private final Screen parent;
		private final ConfigObject editing;

		protected ConfigScreen(Screen parent) {
			super(CONFIG_TITLE);
			this.parent = parent;
			this.editing = co.clone();
		}

		@Override
		protected void init() {
			super.init();

			SpruceOptionListWidget optionList = new SpruceOptionListWidget(Position.of(0, 22 + 2), this.width, this.height - 35 - 22 - 2);
			optionList.setBackground(EmptyBackground.EMPTY_BACKGROUND);

			optionList.addSingleOptionEntry(new SpruceBooleanOption(
					"sh_mchud.config.label.show_bucket_use_warning",
					()->editing.showBucketUseWarning,
					b->editing.showBucketUseWarning=b,
					new TranslatableComponent("sh_mchud.config.help.show_bucket_use_warning")
			));
			optionList.addSingleOptionEntry(new SpruceDoubleOption("sh_mchud.config.label.warning_offset_line",
					0,
					10,
					1,
					()->(double)editing.warningOffsetLine,
					d->editing.warningOffsetLine = (int)Math.round(d),
					sdo->new TextComponent(I18n.get("sh_mchud.config.label.warning_offset_line") + ": " + Math.round(sdo.get())),
					new TranslatableComponent("sh_mchud.config.help.warning_offset_line")
			));

			optionList.addSingleOptionEntry(new SpruceSeparatorOption("sh_mchud.config.category.advanced", true, null));
			optionList.addSingleOptionEntry(new SpruceBooleanOption(
					"sh_mchud.config.label.cancel_bucket_use_enabled",
					()->editing.cancelPlayerActionEnabled,
					b->editing.cancelPlayerActionEnabled=b,
					new TranslatableComponent("sh_mchud.config.help.cancel_bucket_use_enabled")
			));
			optionList.addSingleOptionEntry(new SpruceBooleanOption(
					"sh_mchud.config.label.notify_action_cancellation",
					()->editing.notifyActionCancellation,
					b->editing.notifyActionCancellation=b,
					new TranslatableComponent("sh_mchud.config.help.notify_action_cancellation")
			));

			this.addWidget(optionList);
			this.addWidget(new SpruceButtonWidget(Position.of(this.width / 2 + 1, this.height - 35 + 6), 120 - 2, 20, CommonComponents.GUI_DONE, b -> save()));
			this.addWidget(new SpruceButtonWidget(Position.of(this.width / 2 - 120 - 1, this.height - 35 + 6), 120 - 2, 20, CommonComponents.GUI_CANCEL, b -> close()));
		}

		private void save() {
			saveUserConfig(editing);
			close();
		}

		private void close() {
			this.minecraft.setScreen(this.parent);
		}

		@Override
		public void renderTitle(PoseStack matrices, int mouseX, int mouseY, float delta) {
			drawCenteredString(matrices, this.font, this.title, this.width / 2, 8, 16777215);
		}
	}
}
