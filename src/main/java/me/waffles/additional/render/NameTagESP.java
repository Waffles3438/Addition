package me.waffles.additional.render;

import me.waffles.additional.config.ModConfig;
import me.waffles.additional.util.BotUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NameTagESP {

    private final Minecraft mc = Minecraft.getMinecraft();

    private ICamera frustum;

    public static final Set<UUID> renderedPlayers = new HashSet<>();
    public static final Set<EntityPlayer> occludedPlayers = new HashSet<>();

    /**
     * Called by the optional RenderLib integration only after its occlusion test
     * confirms that the player was actually culled.
     */
    public static void markOccludedPlayer(EntityPlayer player) {
        occludedPlayers.add(player);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            renderedPlayers.clear();
            occludedPlayers.clear();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        boolean legitMode = ModConfig.isLegitModeActive();

        // Legit Mode only needs a late pass when the optional culling integration
        // actually reported occluded players. Otherwise vanilla already renders
        // the labels and this avoids a per-frame lobby-wide scan.
        if (legitMode && occludedPlayers.isEmpty()) return;
        if (!legitMode && (!ModConfig.masterSwitch || !ModConfig.nametagsThroughWalls)) return;

        EntityPlayer viewer = mc.thePlayer;
        if (viewer == null || mc.theWorld == null) return;

        // An out-of-date PolyNametag cannot be bridged to, and the symptom (nametags a
        // frame behind) is easy to mistake for this feature just being broken. Say so
        // once, now that we know the player has actually enabled it.
        PolyNametagCompat.warnIfIncompatible();

        List<EntityPlayer> candidates = null;
        if (legitMode) {
            // This set contains only entities that RenderLib's actual occlusion
            // decision removed. It avoids treating every unmarked player as culled.
            for (EntityPlayer player : occludedPlayers) {
                if (player == viewer) continue;
                if (player.isDead || player.deathTime > 0 || !player.addedToChunk) continue;
                // Recheck immediately before rendering so a bot can never enter the
                // label-only fallback even if its tab-list classification changed.
                if (BotUtils.isBot(player)) continue;
                if (candidates == null) candidates = new ArrayList<EntityPlayer>(occludedPlayers.size());
                candidates.add(player);
            }
        } else {
            // Gather only the players the normal pass already culled for the existing
            // master-on through-wall feature. This path intentionally remains unchanged.
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player == viewer) continue;

                // playerEntities and the chunk entity lists RenderGlobal.renderEntities walks
                // are separate lists with independent removal paths, so a player can sit in
                // this one while being unreachable by the entity pass. addedToChunk tracks
                // membership of exactly those chunk lists, so once it is false the entity pass
                // can never label this player and neither should we - there is no body under
                // the tag to label.
                //
                // deathTime is the same field RendererLivingEntity.doRender uses to decide
                // whether to apply the death rotation, so it is precisely "this model is being
                // drawn in the dead state". A death that gets stuck there leaves the sneak flag
                // and height unsettled, and because renderName picks between
                // renderOffsetLivingLabel and renderLivingLabel on isSneaking() - only one of
                // which we shift by -0.25 - the tag lands somewhere different every frame and
                // shakes. Health is deliberately not used here: it comes from the DataWatcher
                // and servers do not always sync it honestly for other players, so filtering on
                // it risks hiding every nametag.
                if (player.isDead || player.deathTime > 0 || !player.addedToChunk) continue;

                if (renderedPlayers.contains(player.getUniqueID())) continue;
                if (BotUtils.isBot(player)) continue;
                if (candidates == null) candidates = new ArrayList<EntityPlayer>(4);
                candidates.add(player);
            }
        }
        if (candidates == null) return;

        Entity camera = mc.getRenderViewEntity();
        if (camera == null) camera = viewer;

        float pt = event.partialTicks;
        double px = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * pt;
        double py = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * pt;
        double pz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * pt;

        // Vanilla only reaches renderName for entities that survived the frustum check
        // in RenderGlobal.renderEntities. Keep the same test for both the culling-backed
        // Legit Mode candidates and the existing master-on fallback before paying for a
        // label. A label costs formatted text plus a GL draw per character.
        ClippingHelperImpl.getInstance();
        if (frustum == null) frustum = new Frustum();
        frustum.setPosition(px, py, pz);

        // Test against the plain bounding box, exactly as RenderGlobal does. The label
        // sits above it, so a player just off the bottom edge whose name would still
        // poke into view gets skipped - which is what vanilla does to that same player
        // when the entity pass culls them.
        int visible = 0;
        for (int i = 0; i < candidates.size(); i++) {
            EntityPlayer player = candidates.get(i);
            if (frustum.isBoundingBoxInFrustum(player.getEntityBoundingBox())) {
                candidates.set(visible++, player);
            }
        }
        if (visible == 0) return;

        RenderManager rm = mc.getRenderManager();

        // RenderLib replaces the vanilla RenderGlobal.renderEntities call, so
        // RenderManager.cacheActiveRenderInfo is never invoked and livingPlayer/
        // textRenderers stay null - renderName() would NPE. Re-cache with the
        // current camera so the label distance check and font rendering work.
        rm.cacheActiveRenderInfo(mc.theWorld, mc.fontRendererObj, camera, mc.pointedEntity, mc.gameSettings, pt);

        // Mirror PolyNametag.onRender: by default it dims every nametag using the
        // tagged player's own brightness via the lightmap. Entities left to this
        // event (culled behind walls) never get that handling, so they would stay
        // fullbright. Enable the lightmap and apply per-player coords here so a
        // nametag looks identical whether or not it is behind a wall.
        //
        // The reflection behind usingDirectRender only toggles once per frame,
        // so set it once for the whole loop instead of once per player.
        //
        // Vanilla only ever draws labels in the middle of the entity pass, so
        // renderName/renderLivingLabel leave GL configured for that pass: lighting on,
        // the label's blend func applied, and the lightmap coords of whatever entity
        // was drawn last. None of that holds during RenderWorldLastEvent, and anything
        // drawing after us in this event (block overlays, other ESPs) would inherit it
        // and come out the wrong color, so snapshot the lightmap coords we cannot
        // re-derive and restore the rest when we are done.
        float prevBrightnessX = OpenGlHelper.lastBrightnessX;
        float prevBrightnessY = OpenGlHelper.lastBrightnessY;

        GlStateManager.pushMatrix();
        mc.entityRenderer.enableLightmap();
        PolyNametagCompat.usingDirectRender(true);
        try {
            for (int i = 0; i < visible; i++) {
                EntityPlayer player = candidates.get(i);

                int brightness = player.isBurning() ? 15728880 : player.getBrightnessForRender(pt);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                        (float) (brightness % 65536), (float) (brightness / 65536));

                double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * pt - px;
                double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * pt - py;
                double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pt - pz;

                Render render = rm.getEntityRenderObject(player);
                if (!(render instanceof RendererLivingEntity)) continue;

                ((RendererLivingEntity<EntityLivingBase>) render).renderName(player, x, y, z);
            }
        } finally {
            PolyNametagCompat.usingDirectRender(false);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBrightnessX, prevBrightnessY);
            mc.entityRenderer.disableLightmap();

            // renderLivingLabel finishes with enableLighting(): fine inside the entity
            // pass, but out here it means every later draw is lit instead of taking the
            // color from glColor, which is what recolors block overlays.
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            // GlStateManager.blendFunc only records the RGB factors, so the label's
            // blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA) left the cached alpha factors
            // describing state GL no longer has. Dirty the cache first so the following
            // tryBlendFuncSeparate is not skipped as a redundant call.
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
