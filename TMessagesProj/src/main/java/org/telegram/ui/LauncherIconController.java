package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.ic_launcher_background_dahl_main, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconDefault),
        VINTAGE("VintageIcon", R.drawable.icon_dahl_background_vintage, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconVintage),
        BLACK("BlackIcon", R.drawable.ic_launcher_background_dahl_black, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconBlack),
        METALLIC("MetallicIcon", R.drawable.ic_launcher_background_dahl_metallic, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconMetallic),
        SILVER("SilverIcon", R.drawable.ic_launcher_background_dahl_silver, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconSilver),
        INVERTED("InvertedIcon", R.drawable.icon_dahl_background_inverted, R.mipmap.icon_foreground_dahl_inverted_sa, R.string.AppIconInverted),
        WHITE("WhiteIcon", R.drawable.icon_dahl_background_white, R.mipmap.icon_foreground_dahl_white_sa, R.string.AppIconWhite),
        UNICORN("UnicornIcon", R.drawable.ic_launcher_background_dahl_unicorn, R.mipmap.icon_foreground_dahl_unicorn_sa, R.string.AppIconUnicorn),
        NOX("NoxIcon", R.mipmap.ic_launcher_dahl_night_background, R.mipmap.icon_foreground_dahl_night_sa, R.string.AppIconNox),
        OUTLINE("OutlineIcon", R.drawable.ic_launcher_background_dahl_black, R.mipmap.icon_foreground_dahl_outline_sa, R.string.AppIconOutline),
        TITANIUM("TitaniumIcon", R.drawable.ic_launcher_background_dahl_titanium, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconTitanium),
        SPARKLING("SparklingIcon", R.mipmap.ic_launcher_background_dahl_sparkling, R.mipmap.icon_foreground_dahl_sparkling_sa, R.string.AppIconSparkling),
        RUSSIA("RussiaIcon", R.drawable.ic_launcher_background_dahl_russia, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconRussia),
        TRIANGLE("TriangleIcon", R.drawable.icon_dahl_background_triangle, R.mipmap.icon_foreground_dahl_triangle_sa, R.string.AppIconTriangle),
        TATARSTAN("TatarstanIcon", R.drawable.ic_launcher_background_dahl_tatarstan, R.mipmap.icon_foreground_dahl_sa, R.string.AppIconTatarstan),
        BIRCH("BirchIcon", R.drawable.icon_dahl_background_inverted, R.mipmap.icon_foreground_dahl_birch_sa, R.string.AppIconBirch),
        WINTER("WinterIcon", R.mipmap.ic_launcher_background_dahl_winter, R.mipmap.icon_foreground_dahl_winter_sa, R.string.AppIconWinter);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
