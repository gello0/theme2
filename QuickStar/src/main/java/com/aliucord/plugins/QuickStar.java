package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.domain.emoji.Emoji;
import com.discord.utilities.color.ColorCompat;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

public class QuickStar extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("zt", 289556910426816513L) };
        manifest.description = "Adds a star button to the message context menu that reacts to the message with the star emoji.";
        manifest.version = "1.1.3";
        manifest.updateUrl = "https://raw.githubusercontent.com/zt64/aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void start(Context context) throws NoSuchMethodException {
        final Drawable icon = ContextCompat.getDrawable(context, R.d.ic_star_24dp);
        final int id = View.generateViewId();

        final Class<WidgetChatListActions> c = WidgetChatListActions.class;
        final Method getBinding = c.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);

        final Method addReaction = WidgetChatListActions.class.getDeclaredMethod("addReaction", Emoji.class);
        addReaction.setAccessible(true);

        final Emoji starEmoji = new Emoji() {
            @Override
            public String getChatInputText() { return null; }

            @Override
            public String getCommand(@Nullable String str) { return null; }

            @Override
            public String getFirstName() { return null; }

            @Override
            public String getImageUri(boolean z2, int i, Context context) { return null; }

            @Override
            public String getMessageContentReplacement() { return null; }

            @Override
            public List<String> getNames() { return null; }

            @Override
            public String getReactionKey() {
                try {
                    return URLEncoder.encode(":xirold:", "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return "";
                }
            }

            @Override
            public Pattern getRegex(@Nullable String str) { return null; }

            @Override
            public String getUniqueId() {
                return null;
            }

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public boolean isUsable() {
                return false;
            }

            @Override
            public boolean requiresColons() {
                return false;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
            }
        };

        patcher.patch(c.getDeclaredMethod("configureUI", WidgetChatListActions.Model.class), new PinePatchFn(callFrame -> {
            try {
                WidgetChatListActionsBinding binding = (WidgetChatListActionsBinding) getBinding.invoke(callFrame.thisObject);

                boolean canReact = ((WidgetChatListActions.Model) callFrame.args[0]).getManageMessageContext().getCanAddReactions();

                assert binding != null;
                TextView quickStar = binding.a.findViewById(id);
                quickStar.setVisibility(canReact ? View.VISIBLE : View.GONE);
                if (!quickStar.hasOnClickListeners()) quickStar.setOnClickListener(l -> {
                    try {
                        addReaction.invoke(callFrame.thisObject, starEmoji);
                        ((WidgetChatListActions) callFrame.thisObject).dismiss();
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Throwable ignored) { }
        }));

        patcher.patch(c.getDeclaredMethod("onViewCreated", View.class, Bundle.class), new PinePatchFn(callFrame -> {
            LinearLayout linearLayout = (LinearLayout) ((NestedScrollView) callFrame.args[0]).getChildAt(0);
            Context ctx = linearLayout.getContext();
            TextView quickStar = new TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Icon);
            quickStar.setText("Quick Star");
            quickStar.setId(id);

            if (icon != null)
                icon.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal));

            quickStar.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            linearLayout.addView(quickStar, 1);
        }));
    }

    @Override
    public void stop(Context context) { patcher.unpatchAll(); }
}