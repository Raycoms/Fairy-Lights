package me.paulf.fairylights.client;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.renderer.block.entity.BlockEntityFastenerRenderer;
import me.paulf.fairylights.client.renderer.entity.RenderFenceFastener;
import me.paulf.fairylights.client.renderer.entity.RenderLadder;
import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.entity.BlockEntityFastener;
import me.paulf.fairylights.server.entity.EntityFenceFastener;
import me.paulf.fairylights.server.entity.EntityLadder;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.ItemLight;
import me.paulf.fairylights.server.jingle.JingleLibrary;
import me.paulf.fairylights.server.net.clientbound.MessageJingle;
import me.paulf.fairylights.server.net.clientbound.MessageOpenEditLetteredConnectionGUI;
import me.paulf.fairylights.server.net.clientbound.MessageUpdateFastenerEntity;
import me.paulf.fairylights.util.styledstring.StyledString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.ListNBT;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ClientProxy extends ServerProxy {
	@Override
	protected BiConsumer<MessageJingle, Supplier<NetworkEvent.Context>> createJingleHandler() {
		return new MessageJingle.Handler();
	}

	@Override
	protected BiConsumer<MessageUpdateFastenerEntity, Supplier<NetworkEvent.Context>> createUpdateFastenerEntityHandler() {
		return new MessageUpdateFastenerEntity.Handler();
	}

	@Override
	protected BiConsumer<MessageOpenEditLetteredConnectionGUI, Supplier<NetworkEvent.Context>> createOpenEditLetteredConnectionGUIHandler() {
		return new MessageOpenEditLetteredConnectionGUI.Handler();
	}

	@Override
	public void initHandlers() {
		super.initHandlers();
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}

	@Override
	public void initRenders() {
		ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityFastener.class, new BlockEntityFastenerRenderer(ServerProxy.buildBlockView()));
		RenderingRegistry.registerEntityRenderingHandler(EntityFenceFastener.class, RenderFenceFastener::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLadder.class, RenderLadder::new);
	}

	@Override
	public void initRendersLate() {
		ItemColors colors = Minecraft.getInstance().getItemColors();
		colors.register((stack, index) -> {
			if (index == 0) {
				return 0xFFFFFFFF;
			}
			return ItemLight.getColorValue(ItemLight.getLightColor(stack));
		},
			FLItems.FAIRY_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.PAPER_LANTERN.orElseThrow(IllegalStateException::new),
			FLItems.ORB_LANTERN.orElseThrow(IllegalStateException::new),
			FLItems.FLOWER_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.ORNATE_LANTERN.orElseThrow(IllegalStateException::new),
			FLItems.OIL_LANTERN.orElseThrow(IllegalStateException::new),
			FLItems.JACK_O_LANTERN.orElseThrow(IllegalStateException::new),
			FLItems.SKULL_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.GHOST_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.SPIDER_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.WITCH_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.SNOWFLAKE_LIGHT.orElseThrow(IllegalStateException::new),
			FLItems.ICICLE_LIGHTS.orElseThrow(IllegalStateException::new),
			FLItems.METEOR_LIGHT.orElseThrow(IllegalStateException::new)
		);
		colors.register((stack, index) -> {
			if (index == 0) {
				return 0xFFFFFFFF;
			}
			if (stack.hasTag()) {
				ListNBT tagList = stack.getTag().getList("pattern", NBT.TAG_COMPOUND);
				if (tagList.size() > 0) {
					return ItemLight.getColorValue(DyeColor.byId(tagList.getCompound((index - 1) % tagList.size()).getByte("color")));
				}
			}
			if (FairyLights.christmas.isOcurringNow()) {
				return (index + System.currentTimeMillis() / 2000) % 2 == 0 ? 0x993333 : 0x7FCC19;
			}
			return 0xFFD584;
		}, FLItems.HANGING_LIGHTS.orElseThrow(IllegalStateException::new));
		colors.register((stack, index) -> {
			DyeColor color;
			if (stack.hasTag()) {
				color = DyeColor.byId(stack.getTag().getByte("color"));
			} else {
				color = DyeColor.BLACK;
			}
			return ItemLight.getColorValue(color);
		}, FLItems.TINSEL.orElseThrow(IllegalStateException::new));
		colors.register((stack, index) -> {
			if (index == 0) {
				return 0xFFFFFFFF;
			}
			if (stack.hasTag()) {
				ListNBT tagList = stack.getTag().getList("pattern", NBT.TAG_COMPOUND);
				if (tagList.size() > 0) {
					return ItemLight.getColorValue(DyeColor.byId(tagList.getCompound((index - 1) % tagList.size()).getByte("color")));
				}
			}
			return 0xFFFFFFFF;
		}, FLItems.PENNANT_BUNTING.orElseThrow(IllegalStateException::new));
		colors.register((stack, index) -> {
			if (index == 0) {
				return 0xFFFFFF;
			}
			return ItemLight.getColorValue(ItemLight.getLightColor(stack));
		}, FLItems.PENNANT.orElseThrow(IllegalStateException::new));
		colors.register((stack, index) -> {
			if (index > 0 && stack.hasTag()) {
				StyledString str = StyledString.deserialize(stack.getTag().getCompound("text"));
				if (str.length() > 0) {
					TextFormatting lastColor = null, color = null;
					int n = (index - 1) % str.length();
					for (int i = 0; i < str.length(); lastColor = color, i++) {
						color = str.colorAt(i);
						if (lastColor != color && (n-- == 0)) {
							break;
						}
					}
					return StyledString.getColor(color) | 0xFF000000;
				}
			}
			return 0xFFFFFFFF;
		}, FLItems.LETTER_BUNTING.orElseThrow(IllegalStateException::new));
		// Early runTick hook after getMouseOver
		Minecraft.getInstance().getTextureManager().loadTickableTexture(new ResourceLocation(FairyLights.ID, "hacky_hook"), new ITickableTextureObject() {
			@Override
			public void tick() {
				ClientEventHandler.updateHitConnection();
			}

			@Override
			public void setBlurMipmap(boolean blur, boolean mipmap) {}

			@Override
			public void restoreLastBlurMipmap() {}

			@Override
			public void loadTexture(IResourceManager manager) {}

			@Override
			public int getGlTextureId() {
				return 0;
			}
		});
	}

	@Override
	protected void loadJingleLibraries() {
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener((ISelectiveResourceReloadListener) (resourceManager, resourcePredicate) -> {
			if (resourcePredicate.test(VanillaResourceType.SOUNDS)) {
				JingleLibrary.loadAll();
			}
		});
	}
}
