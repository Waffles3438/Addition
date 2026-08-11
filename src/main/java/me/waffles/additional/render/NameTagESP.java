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

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            renderedPlayers.clear();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!ModConfig.masterSwitch || !ModConfig.nametagsThroughWalls) return;

        EntityPlayer viewer = mc.thePlayer;
        if (viewer == null || mc.theWorld == null) return;

        // An out-of-date PolyNametag cannot be bridged to, and the symptom (nametags a
        // frame behind) is easy to mistake for this feature just being broken. Say so
        // once, now that we know the player has actually enabled it.
        PolyNametagCompat.warnIfIncompatible();

        // Gather only the players the vanilla pass already culled (those marked in
        // renderedPlayers are skipped). If nothing is left we can bail before
        // touching the lightmap or re-caching the render pipeline at all.
        List<EntityPlayer> candidates = null;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == viewer) continue;
            if (BotUtils.isBot(player)) continue;
            if (renderedPlayers.contains(player.getUniqueID())) continue;
            if (candidates == null) candidates = new ArrayList<EntityPlayer>(4);
            candidates.add(player);
        }
        if (candidates == null) return;

        Entity camera = mc.getRenderViewEntity();
        if (camera == null) camera = viewer;

        float pt = event.partialTicks;
        double px = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * pt;
        double py = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * pt;
        double pz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * pt;

        // Vanilla only reaches renderName for entities that survived the frustum check
        // in RenderGlobal.renderEntities, so it never draws a label for someone
        // off-screen or behind the camera. Our candidate set has no such filter - it is
        // every player the entity pass skipped, whether that was the culler or the
        // frustum - so apply vanilla's own test before paying for a label. In a busy
        // lobby this is most of them, and a label costs a getFormattedText() plus a
        // glBegin/glEnd pair per character, twice.
        //
        // ClippingHelperImpl reads the current projection/modelview, which this late in
        // the frame is still the world camera: everything drawn since setupCameraTransform
        // pushes and pops. Frustum wraps the ClippingHelperImpl singleton, so the
        // instance can be reused across frames as long as getInstance() refreshes it.
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