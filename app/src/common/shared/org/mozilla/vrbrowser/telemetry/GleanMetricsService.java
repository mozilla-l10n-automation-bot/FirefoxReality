package org.mozilla.vrbrowser.telemetry;

import android.content.Context;
import android.util.Log;

import androidx.annotation.UiThread;

import org.mozilla.vrbrowser.browser.SettingsStore;
import org.mozilla.vrbrowser.search.SearchEngineWrapper;
import org.mozilla.vrbrowser.utils.SystemUtils;
import mozilla.components.service.glean.Glean;


public class GleanMetricsService {

    private static boolean initialized = false;
    private final static String LOGTAG = SystemUtils.createLogtag(GleanMetricsService.class);
    //private static GleanInternalAPI glean = null;
    private static Context context = null;

    // We should call this at the application initial stage. Instead,
    // it would be called when users turn on/off the setting of telemetry.
    // e.g., SettingsStore.getInstance(context).setTelemetryEnabled();
    public static void init(Context aContext) {
        if (initialized)
            return;

        context = aContext;
        initialized = true;

        final boolean telemetryEnabled = SettingsStore.getInstance(aContext).isTelemetryEnabled();

        // TODO: Can't find symbol variable pings. Confirm if we need a ping.yaml?
        //Glean.INSTANCE.registerPings(Pings);
        Glean.INSTANCE.setUploadEnabled(telemetryEnabled);
        Glean.INSTANCE.initialize(aContext);

        if (!Glean.INSTANCE.isInitialized()) {
            Log.d(LOGTAG, "Glean doesn't be initialized yet.");
        }
    }

    private static String getDefaultSearchEngineIdentifierForTelemetry(Context aContext) {
        return SearchEngineWrapper.get(aContext).getResourceURL();
    }

    @UiThread
    public static void start() {
        Glean.INSTANCE.setUploadEnabled(true);
    }

    @UiThread
    public static void stop() {
        Glean.INSTANCE.setUploadEnabled(false);
    }
}
