package me.waffles.additional.render;

import cc.polyfrost.oneconfig.utils.Notifications;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Bridge to PolyNametag's deferred nametag pipeline.
 *
 * PolyNametag does not draw nametags during the entity pass. Its renderName hook
 * queues the label and cancels, then flushes the queue immediately after
 * RenderGlobal.renderEntities. The guard it uses is {@code !drawingTags && drawingWorld},
 * and drawingWorld stays true until renderWorld returns - which is after
 * RenderWorldLastEvent. So a nametag we draw from that event is queued and cancelled
 * rather than drawn, and only reaches the screen on the next frame, positioned with the
 * previous frame's camera-relative coordinates. Holding drawingTags true around our own
 * rendering is what makes PolyNametag draw it immediately instead.
 *
 * drawingTags was added in PolyNametag 1.0.7. The entrypoint class itself exists in every
 * 1.8.9 build, so "the class resolves but the setter does not" is a precise signal that
 * PolyNametag is installed but predates that flag. That case is reported loudly, because
 * otherwise it presents as nametags trailing a frame behind with nothing in the log.
 */
public final class PolyNametagCompat {

    private static final Logger LOGGER = LogManager.getLogger("Additional");

    /** PolyNametag's 1.8.9 Forge entrypoint. The 1.1.0 rewrite moved and renamed this. */
    private static final String POLYNAMETAG_CLASS = "org.polyfrost.polynametag.PolyNametag";

    /** First PolyNametag release exposing drawingTags. */
    private static final String MINIMUM_VERSION = "1.0.7";

    private static final Object INSTANCE;
    private static final Method SET_DRAWING_TAGS;

    /** PolyNametag is present and we can drive its drawingTags flag. */
    private static final boolean AVAILABLE;

    /** PolyNametag is present, but too old to bridge to. */
    private static final boolean INCOMPATIBLE;

    private static boolean notified;
    private static boolean invokeFailureLogged;

    static {
        Object instance = null;
        Method setter = null;
        boolean present = false;
        Throwable failure = null;

        try {
            Class<?> clazz = Class.forName(POLYNAMETAG_CLASS);
            // Getting this far means some PolyNametag build is installed.
            present = true;

            Field inst = clazz.getField("INSTANCE");
            instance = inst.get(null);
            setter = clazz.getMethod("setDrawingTags", boolean.class);
        } catch (ClassNotFoundException notInstalled) {
            // PolyNametag is not installed. Nothing to bridge to, and nothing to report:
            // vanilla draws labels inline, so our rendering needs no special handling.
        } catch (Throwable t) {
            failure = t;
        }

        AVAILABLE = instance != null && setter != null;
        INSTANCE = instance;
        SET_DRAWING_TAGS = setter;
        INCOMPATIBLE = present && !AVAILABLE;

        if (INCOMPATIBLE) {
            String message = "PolyNametag is installed but Additional could not bind to its 'drawingTags' flag, "
                    + "which requires PolyNametag " + MINIMUM_VERSION + " or newer. While it is out of date, "
                    + "'Show nametags behind walls' will draw nametags one frame late at stale positions. "
                    + "Update PolyNametag to fix this.";
            if (failure != null) {
                LOGGER.error(message, failure);
            } else {
                LOGGER.error(message);
            }
        } else if (AVAILABLE) {
            LOGGER.info("PolyNametag detected; nametag ESP will render through its pipeline directly.");
        }
    }

    private PolyNametagCompat() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    public static boolean isIncompatible() {
        return INCOMPATIBLE;
    }

    public static void usingDirectRender(boolean direct) {
        if (!AVAILABLE) return;
        try {
            SET_DRAWING_TAGS.invoke(INSTANCE, direct);
        } catch (Throwable t) {
            // Bound successfully at load but failing at call time means PolyNametag changed
            // under us. Report it once rather than silently degrading every frame.
            if (!invokeFailureLogged) {
                invokeFailureLogged = true;
                LOGGER.error("Failed to set PolyNametag's 'drawingTags' flag; nametags behind walls may "
                        + "render one frame late at stale positions.", t);
            }
        }
    }

    /**
     * Surfaces an out-of-date PolyNametag in game, once per launch. Called from the render
     * path so it only fires for players who actually turn the feature on, rather than
     * nagging everyone at startup.
     */
    public static void warnIfIncompatible() {
        if (!INCOMPATIBLE || notified) return;
        notified = true;
        Notifications.INSTANCE.send("Additional",
                "PolyNametag is out of date (needs " + MINIMUM_VERSION + " or newer). Nametags behind walls "
                        + "will render a frame late until you update it.",
                8000);
    }
}
