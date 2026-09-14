ces(Context context) {
        if (profile_verifiedDrawable == null) {
            profile_aboutTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

            Resources resources = context.getResources();

            profile_verifiedDrawable = resources.getDrawable(R.drawable.verified_area).mutate();
            profile_verifiedCheckDrawable = resources.getDrawable(R.drawable.verified_check).mutate();

            applyProfileTheme();
        }

        profile_aboutTextPaint.setTextSize(dp(16));
    }

    private static ColorFilter currentShareColorFilter;
    private static int currentShareColorFilterColor;
    private static ColorFilter currentShareSelectedColorFilter;
    private static  int currentShareSelectedColorFilterColor;
    public static ColorFilter getShareColorFilter(int color, boolean selected) {
        if (selected) {
            if (currentShareSelectedColorFilter == null || currentShareSelectedColorFilterColor != color) {
                currentShareSelectedColorFilterColor = color;
                currentShareSelectedColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
            return currentShareSelectedColorFilter;
        } else {
            if (currentShareColorFilter == null || currentShareColorFilterColor != color) {
                currentShareColorFilterColor = color;
                currentShareColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
            return currentShareColorFilter;
        }
    }

    public static void applyProfileTheme() {
        if (profile_verifiedDrawable == null) {
            return;
        }

        profile_aboutTextPaint.setColor(getColor(key_windowBackgroundWhiteBlackText));
        profile_aboutTextPaint.linkColor = getColor(key_windowBackgroundWhiteLinkText);

        setDrawableColorByKey(profile_verifiedDrawable, key_profile_verifiedBackground);
        setDrawableColorByKey(profile_verifiedCheckDrawable, key_profile_verifiedCheck);
    }

    public static Drawable getThemedDrawableByKey(Context context, int resId, int key, Theme.ResourcesProvider resourcesProvider) {
        return getThemedDrawable(context, resId, getColor(key, resourcesProvider));
    }

    public static Drawable getThemedDrawableByKey(Context context, int resId, int key) {
        return getThemedDrawable(context, resId, getColor(key));
    }

    public static Drawable getThemedDrawable(Context context, int resId, int color) {
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getResources().getDrawable(resId).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return drawable;
    }

    public static int getDefaultColor(int key) {
        int value = defaultColors[key];
        if (value == 0) {
            int fallbackKey = fallbackKeys.get(key, -1);
            if (fallbackKey != -1) {
                return getDefaultColor(fallbackKey);
            }
            if (isMyMessagesBubbles(key) || key == key_chats_menuTopShadow || key == key_chats_menuTopBackground || key == key_chats_menuTopShadowCats || key == key_chat_wallpaper_gradient_to2 || key == key_chat_wallpaper_gradient_to3) {
                return 0;
            }
            return 0xffff0000;
        }
        return value;
    }

    public static boolean hasThemeKey(int key) {
        return currentColors.indexOfKey(key) >= 0;
    }

    public static void setAnimatingColor(boolean animating) {
        animatingColors = animating ? new SparseIntArray() : null;
    }

    public static boolean isAnimatingColor() {
        return animatingColors != null;
    }

    public static void setAnimatedColor(int key, int value) {
        if (animatingColors == null) {
            return;
        }
        animatingColors.put(key, value);
    }

    public static int getDefaultAccentColor(int key) {
        int index = currentColorsNoAccent.indexOfKey(key);
        if (index >= 0) {
            int color = currentColorsNoAccent.valueAt(index);
            ThemeAccent accent = currentTheme.getAccent(false);
            if (accent == null) {
                return 0;
            }
            float[] hsvTemp1 = getTempHsv(1);
            float[] hsvTemp2 = getTempHsv(2);
            Color.colorToHSV(currentTheme.accentBaseColor, hsvTemp1);
            Color.colorToHSV(accent.accentColor, hsvTemp2);
            return changeColorAccent(hsvTemp1, hsvTemp2, color, currentTheme.isDark(), color);
        }
        return 0;
    }

    public static int getNonAnimatedColor(int key) {
        return getColor(key, null, true);
    }

    public static int getColor(int key, ResourcesProvider provider) {
        if (provider != null) {
            return provider.getColor(key);
        }
        return getColor(key);
    }

    public static int getCurrentColor(int key) {
        return currentColors.get(key);
    }

    public static int getColor(int key) {
        return getColor(key, null, false);
    }

    public static int getColor(int key, boolean[] isDefault) {
        return getColor(key, isDefault, false);
    }

    public static int getColor(int key, boolean[] isDefault, boolean ignoreAnimation) {
        if (!ignoreAnimation && animatingColors != null) {
            int index = animatingColors.indexOfKey(key);
            if (index >= 0) {
                return animatingColors.valueAt(index);
            }
        }
        if (serviceBitmapShader != null && (key_chat_serviceText == key || key_chat_serviceLink == key || key_chat_serviceIcon == key
                || key_chat_stickerReplyLine == key || key_chat_stickerReplyNameText == key || key_chat_stickerReplyMessageText == key)) {
            return 0xffffffff;
        }
        if (currentTheme == defaultTheme) {
            boolean useDefault;
            if (isMyMessagesBubbles(key)) {
                useDefault = currentTheme.isDefaultMyMessagesBubbles();
            } else if (isMyMessages(key)) {
                useDefault = currentTheme.isDefaultMyMessages();
            } else if (key_chat_wallpaper == key || key_chat_wallpaper_gradient_to1 == key || key_chat_wallpaper_gradient_to2 == key || key_chat_wallpaper_gradient_to3 == key) {
                useDefault = false;
            } else {
                useDefault = currentTheme.isDefaultMainAccent();
            }
            if (useDefault) {
                if (key == key_chat_serviceBackground) {
                    return serviceMessageColor;
                } else if (key == key_chat_serviceBackgroundSelected) {
                    return serviceSelectedMessageColor;
                }
                return getDefaultColor(key);
            }
        }
        int index = currentColors.indexOfKey(key);
        int color;
        if (index < 0) {
            int fallbackKey = fallbackKeys.get(key, -1);
            if (fallbackKey != -1) {
                int fallbackIndex = currentColors.indexOfKey(fallbackKey);
                if (fallbackIndex >= 0) {
                    return currentColors.valueAt(fallbackIndex);
                }
            }

            if (isDefault != null) {
                isDefault[0] = true;
            }
            if (key == key_chat_serviceBackground) {
                return serviceMessageColor;
            } else if (key == key_chat_serviceBackgroundSelected) {
                return serviceSelectedMessageColor;
            }
            return getDefaultColor(key);
        } else {
            color = currentColors.valueAt(index);
        }
        if (key_windowBackgroundWhite == key || key_windowBackgroundGray == key || key_actionBarDefault == key || key_actionBarDefaultArchived == key) {
            color |= 0xff000000;
        }
        return color;
    }

    private static boolean isMyMessagesBubbles(int key) {
        return key >= myMessagesBubblesStartIndex && key < myMessagesBubblesEndIndex;
    }


    private static boolean isMyMessages(int key) {
        return key >= myMessagesStartIndex && key < myMessagesEndIndex;
    }

    public static void setColor(int key, int color, boolean useDefault) {
        if (key == key_chat_wallpaper || key == key_chat_wallpaper_gradient_to1 || key == key_chat_wallpaper_gradient_to2 || key == key_chat_wallpaper_gradient_to3 || key == key_windowBackgroundWhite || key == key_windowBackgroundGray || key == key_actionBarDefault || key == key_actionBarDefaultArchived) {
            color = 0xff000000 | color;
        }

        if (useDefault) {
            currentColors.delete(key);
        } else {
            currentColors.put(key, color);
        }

        if (key == key_chat_selectedBackground) {
            applyChatMessageSelectedBackgroundColor();
        } else if (key == key_chat_serviceBackground || key == key_chat_serviceBackgroundSelected) {
            applyChatServiceMessageColor();
        } else if (key == key_chat_wallpaper || key == key_chat_wallpaper_gradient_to1 || key == key_chat_wallpaper_gradient_to2
                || key == key_chat_wallpaper_gradient_to3 || key == key_chat_wallpaper_gradient_rotation) {
            reloadWallpaper(true);
        } else if (key == key_actionBarDefault) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors);
            }
        } else if (key == key_windowBackgroundGray) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors);
            }
        }
    }

    public static void setDefaultColor(int key, int color) {
        defaultColors[key] = color;
    }

    public static void setThemeWallpaper(ThemeInfo themeInfo, Bitmap bitmap, File path) {
        currentColors.delete(key_chat_wallpaper);
        currentColors.delete(key_chat_wallpaper_gradient_to1);
        currentColors.delete(key_chat_wallpaper_gradient_to2);
        currentColors.delete(key_chat_wallpaper_gradient_to3);
        currentColors.delete(key_chat_wallpaper_gradient_rotation);
        themedWallpaperLink = null;
        themeInfo.setOverrideWallpaper(null);
        if (bitmap != null) {
            themedWallpaper = new BitmapDrawable(bitmap);
            saveCurrentTheme(themeInfo, false, false, false);
            calcBackgroundColor(themedWallpaper, 0);
            applyChatServiceMessageColor();
            applyChatMessageSelectedBackgroundColor();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
        } else {
            themedWallpaper = null;
            wallpaper = null;
            saveCurrentTheme(themeInfo, false, false, false);
            reloadWallpaper(true);
        }
    }

    public static void setDrawableColor(Drawable drawable, int color) {
        if (drawable == null) {
            return;
        }
        if (drawable instanceof StatusDrawable) {
            ((StatusDrawable) drawable).setColor(color);
        } else if (drawable instanceof MsgClockDrawable) {
            ((MsgClockDrawable) drawable).setColor(color);
        } else if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable) drawable).getPaint().setColor(color);
        } else if (drawable instanceof ScamDrawable) {
            ((ScamDrawable) drawable).setColor(color);
        } else {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }
    }

    public static void setDrawableColorByKey(Drawable drawable, int key) {
        setDrawableColor(drawable, getColor(key));
    }

    public static void setEmojiDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                Drawable state;
                if (selected) {
                    state = getStateDrawable(drawable, 0);
                } else {
                    state = getStateDrawable(drawable, 1);
                }
                if (state instanceof ShapeDrawable) {
                    ((ShapeDrawable) state).getPaint().setColor(color);
                } else {
                    state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            } catch (Throwable ignore) {

            }
        }
    }

    @TargetApi(21)
    @SuppressLint("DiscouragedPrivateApi")
    public static void setRippleDrawableForceSoftware(RippleDrawable drawable) {
        if (drawable == null) {
            return;
        }
        try {
            Method method = RippleDrawable.class.getDeclaredMethod("setForceSoftware", boolean.class);
            method.invoke(drawable, true);
        } catch (Throwable ignore) {

        }
    }

    public static boolean setSelectorDrawableColor(Drawable drawable, int color, boolean selected) {
        boolean changed = false;
        if (drawable instanceof StateListDrawable) {
            try {
                Drawable state;
                if (selected) {
                    state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        changed = ((ShapeDrawable) state).getPaint().getColor() != color || changed;
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                    state = getStateDrawable(drawable, 1);
                } else {
                    state = getStateDrawable(drawable, 2);
                }
                if (state instanceof ShapeDrawable) {
                    changed = ((ShapeDrawable) state).getPaint().getColor() != color || changed;
                    ((ShapeDrawable) state).getPaint().setColor(color);
                } else {
                    state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
            } catch (Throwable ignore) {

            }
        } else if (drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            if (selected) {
                rippleDrawable.setColor(new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
                ));
            } else {
                if (rippleDrawable.getNumberOfLayers() > 0) {
                    Drawable drawable1 = rippleDrawable.getDrawable(0);
                    if (drawable1 instanceof ShapeDrawable) {
                        changed = ((ShapeDrawable) drawable1).getPaint().getColor() != color || changed;
                        ((ShapeDrawable) drawable1).getPaint().setColor(color);
                    } else {
                        drawable1.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            }
        }
        return changed;
    }

    public static boolean isThemeWallpaperPublic() {
        return !TextUtils.isEmpty(themedWallpaperLink);
    }

    public static boolean hasWallpaperFromTheme() {
        if (currentTheme.firstAccentIsDefault && currentTheme.currentAccentId == DEFALT_THEME_ACCENT_ID) {
            return false;
        }
        return currentColors.indexOfKey(key_chat_wallpaper) >= 0 || themedWallpaperFileOffset > 0 || !TextUtils.isEmpty(themedWallpaperLink);
    }

    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static void reloadWallpaper(boolean async) {
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
        if (wallpaper instanceof MotionBackgroundDrawable) {
            previousPhase = ((MotionBackgroundDrawable) wallpaper).getPhase();
        } else {
            previousPhase = 0;
        }
        wallpaper = null;
        themedWallpaper = null;
        loadWallpaper(async);
    }

    private static void calcBackgroundColor(Drawable drawable, int save) {
        if (save != 2) {
            int[] result = AndroidUtilities.calcDrawableColor(drawable);
            serviceMessageColor = serviceMessageColorBackup = result[0];
            serviceSelectedMessageColor = serviceSelectedMessageColorBackup = result[1];
        }
    }

    public static int getServiceMessageColor() {
        int index = currentColors.indexOfKey(key_chat_serviceBackground);
        if (index >= 0) {
            return currentColors.valueAt(index);
        } else {
            return serviceMessageColor;
        }
    }

    public static void loadWallpaper(boolean async) {
        if (wallpaper != null) {
            return;
        }
        boolean defaultTheme = currentTheme.firstAccentIsDefault && currentTheme.currentAccentId == DEFALT_THEME_ACCENT_ID;
        File wallpaperFile;
        boolean wallpaperMotion;
        ThemeAccent accent = currentTheme.getAccent(false);
        TLRPC.Document wallpaperDocument = null;
        if (accent != null) {
            wallpaperFile = accent.getPathToWallpaper();
            wallpaperMotion = accent.patternMotion;
            TLRPC.ThemeSettings settings = null;
            if (accent.info != null && accent.info.settings.size() > 0) {
                settings = accent.info.settings.get(0);
            }
            if (accent.info != null && settings != null && settings.wallpaper != null) {
                wallpaperDocument = settings.wallpaper.document;
            }
        } else {
            wallpaperFile = null;
            wallpaperMotion = false;
        }
        int intensity;
        OverrideWallpaperInfo overrideWallpaper = currentTheme.overrideWallpaper;
        if (overrideWallpaper != null) {
            intensity = (int) (overrideWallpaper.intensity * 100);
        } else {
            intensity = (int) (accent != null ? (accent.patternIntensity * 100) : currentTheme.patternIntensity);
        }

        TLRPC.Document finalWallpaperDocument = wallpaperDocument;
        if (async) {
            Utilities.themeQueue.postRunnable(wallpaperLoadTask = () -> {
                Drawable drawable = loadWallpaperInternal(overrideWallpaper, wallpaperFile, intensity, wallpaperMotion, finalWallpaperDocument, defaultTheme);
                AndroidUtilities.runOnUIThread(() -> {
                    wallpaperLoadTask = null;
                    createCommonChatResources();
                    if (!disallowChangeServiceMessageColor) {
                        applyChatServiceMessageColor(null, null, drawable);
                        applyChatMessageSelectedBackgroundColor(null, drawable);
                    }
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                });
            });
        } else {
            Drawable drawable = loadWallpaperInternal(overrideWallpaper, wallpaperFile, intensity, wallpaperMotion, finalWallpaperDocument, defaultTheme);
            createCommonChatResources();
            if (!disallowChangeServiceMessageColor) {
                applyChatServiceMessageColor(null, null, drawable);
                applyChatMessageSelectedBackgroundColor(null, drawable);
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
        }
    }

    private static Drawable loadWallpaperInternal(OverrideWallpaperInfo overrideWallpaper, File wallpaperFile, int intensity, boolean wallpaperMotion, TLRPC.Document finalWallpaperDocument, boolean defaultTheme) {
        BackgroundDrawableSettings settings = createBackgroundDrawable(
                currentTheme,
                overrideWallpaper,
                currentColors,
                wallpaperFile,
                themedWallpaperLink,
                themedWallpaperFileOffset,
                intensity,
                previousPhase,
                defaultTheme,
                hasPreviousTheme,
                isApplyingAccent,
                wallpaperMotion,
                finalWallpaperDocument,
                false
        );
        isWallpaperMotion = settings.isWallpaperMotion != null ? settings.isWallpaperMotion : isWallpaperMotion;
        isPatternWallpaper = settings.isPatternWallpaper != null ? settings.isPatternWallpaper : isPatternWallpaper;
        isCustomTheme = settings.isCustomTheme != null ? settings.isCustomTheme : isCustomTheme;
        patternIntensity = intensity;
        wallpaper = settings.wallpaper != null ? settings.wallpaper : wallpaper;
        Drawable drawable = settings.wallpaper;
        calcBackgroundColor(drawable, 1);
        applyChatServiceMessageColor();
        return drawable;
    }


    public static BackgroundDrawableSettings createBackgroundDrawable(
            ThemeInfo currentTheme,
            SparseIntArray currentColors,
            String wallpaperLink,
            int prevoiusPhase,
            boolean local
    ) {
        boolean defaultTheme = currentTheme.firstAccentIsDefault && currentTheme.currentAccentId == DEFALT_THEME_ACCENT_ID;
        ThemeAccent accent = currentTheme.getAccent(false);
        File wallpaperFile = accent != null ? accent.getPathToWallpaper() : null;
        boolean wallpaperMotion = accent != null && accent.patternMotion;
        OverrideWallpaperInfo overrideWallpaper = currentTheme.overrideWallpaper;
        int intensity = overrideWallpaper != null
                ? (int) (overrideWallpaper.intensity * 100)
                : (int) (accent != null ? (accent.patternIntensity * 100) : currentTheme.patternIntensity);

        int wallpaperFileOffset = currentColorsNoAccent.get(key_wallpaperFileOffset, -1);
        return createBackgroundDrawable(currentTheme, overrideWallpaper, currentColors, wallpaperFile, wallpaperLink, wallpaperFileOffset, intensity, prevoiusPhase, defaultTheme, false, false, wallpaperMotion, null, local);
    }

    public static BackgroundDrawableSettings createBackgroundDrawable(
            ThemeInfo currentTheme,
            OverrideWallpaperInfo overrideWallpaper,
            SparseIntArray currentColors,
            File wallpaperFile,
            String themedWallpaperLink,
            int themedWallpaperFileOffset,
            int intensity,
            int previousPhase,
            boolean defaultTheme,
            boolean hasPreviousTheme,
            boolean isApplyingAccent,
            boolean wallpaperMotion,
            TLRPC.Document wallpaperDocument,
            boolean local
    ) {
        BackgroundDrawableSettings settings = new BackgroundDrawableSettings();
        settings.wallpaper = local ? null : wallpaper;
        boolean overrideTheme = (!hasPreviousTheme || isApplyingAccent) && overrideWallpaper != null;
        if (overrideWallpaper != null) {
            settings.isWallpaperMotion = overrideWallpaper.isMotion;
            settings.isPatternWallpaper = overrideWallpaper.color != 0 && !overrideWallpaper.isDefault() && !overrideWallpaper.isColor();
        } else {
            settings.isWallpaperMotion = currentTheme.isMotion;
            settings.isPatternWallpaper = currentTheme.patternBgColor != 0;
        }
        if (!overrideTheme) {
            int backgroundColor = defaultTheme ? 0 : currentColors.get(key_chat_wallpaper);
            int gradientToColor3 = currentColors.get(key_chat_wallpaper_gradient_to3);
            int gradientToColor2 = currentColors.get(key_chat_wallpaper_gradient_to2);
            int gradientToColor1 = currentColors.get(key_chat_wallpaper_gradient_to1);

            boolean bitmapCreated = false;
            if (wallpaperFile != null && wallpaperFile.exists()) {
                bitmapCreated = true;
                try {
                    if (backgroundColor != 0 && gradientToColor1 != 0 && gradientToColor2 != 0) {
                        MotionBackgroundDrawable motionBackgroundDrawable = new MotionBackgroundDrawable(backgroundColor, gradientToColor1, gradientToColor2, gradientToColor3, false);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                        Bitmap patternBitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath(), options);
                        if (patternBitmap != null && patternBitmap.getConfig() != Bitmap.Config.ALPHA_8) {
                            Bitmap toRecycle = patternBitmap;
                            patternBitmap = patternBitmap.copy(Bitmap.Config.ALPHA_8, false);
                            toRecycle.recycle();
                        }
                        if (patternBitmap == null) {
                            bitmapCreated = false;
                        }
                        motionBackgroundDrawable.setPatternBitmap(intensity, patternBitmap);
                        motionBackgroundDrawable.setPatternColorFilter(motionBackgroundDrawable.getPatternColor());
                        settings.wallpaper = motionBackgroundDrawable;
                    } else {
                        settings.wallpaper = Drawable.createFromPath(wallpaperFile.getAbsolutePath());
                    }
                    settings.isWallpaperMotion = wallpaperMotion;
                    settings.isPatternWallpaper = true;
                    settings.isCustomTheme = true;
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
            if (bitmapCreated) {

            } else if (backgroundColor != 0) {
                int rotation = currentColors.get(key_chat_wallpaper_gradient_rotation, -1);
                if (rotation == -1) {
                    rotation = 45;
                }
                if (gradientToColor1 != 0 && gradientToColor2 != 0) {
                    MotionBackgroundDrawable motionBackgroundDrawable = new MotionBackgroundDrawable(backgroundColor, gradientToColor1, gradientToColor2, gradientToColor3, false);
                    Bitmap patternBitmap = null;

                    if (wallpaperFile != null) {
                        final int w = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                        final int h = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);

                        if (wallpaperDocument != null) {
                            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(wallpaperDocument, true);
                            patternBitmap = SvgHelper.getBitmap(f, w, h, false, SvgHelper.ScaleMode.ByWidth);
                        } else {
                            patternBitmap = SvgHelper.getBitmap(R.raw.default_pattern, w, h, Color.WHITE, 1f, SvgHelper.ScaleMode.ByWidth);
                        }
                        if (patternBitmap != null) {
                            FileOutputStream stream = null;
                            try {
                                stream = new FileOutputStream(wallpaperFile);
                                Bitmap bitmap = patternBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                                bitmap.recycle();
                                stream.close();
                            } catch (Exception e) {
                                FileLog.e(e);
                                e.printStackTrace();
                            }
                        }
                    }
                    motionBackgroundDrawable.setPatternBitmap(intensity, patternBitmap);
                    motionBackgroundDrawable.setPhase(previousPhase);
                    settings.wallpaper = motionBackgroundDrawable;
                } else if (gradientToColor1 == 0 || gradientToColor1 == backgroundColor) {
                    settings.wallpaper = new ColorDrawable(backgroundColor);
                } else {
                    final int[] colors = {backgroundColor, gradientToColor1};
                    final BackgroundGradientDrawable.Orientation orientation = BackgroundGradientDrawable.getGradientOrientation(rotation);
                    final BackgroundGradientDrawable backgroundGradientDrawable = new BackgroundGradientDrawable(orientation, colors);
                    final BackgroundGradientDrawable.Listener listener = new BackgroundGradientDrawable.ListenerAdapter() {
                        @Override
                        public void onSizeReady(int width, int height) {
                            final boolean isOrientationPortrait = AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y;
                            final boolean isGradientPortrait = width <= height;
                            if (isOrientationPortrait == isGradientPortrait) {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                            }
                        }
                    };
                    backgroundGradientDisposable = backgroundGradientDrawable.startDithering(BackgroundGradientDrawable.Sizes.ofDeviceScreen(), listener, 100);
                    settings.wallpaper = backgroundGradientDrawable;
                }
                settings.isCustomTheme = true;
            } else if (themedWallpaperLink != null) {
                try {
                    File pathToWallpaper = new File(ApplicationLoader.getFilesDirFixed(), Utilities.MD5(themedWallpaperLink) + ".wp");
                    Bitmap bitmap = loadScreenSizedBitmap(new FileInputStream(pathToWallpaper), 0);
                    if (bitmap != null) {
                        settings.wallpaper = new BitmapDrawable(bitmap);
                        settings.themedWallpaper = settings.wallpaper;
                        settings.isCustomTheme = true;
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else if (themedWallpaperFileOffset > 0 && (currentTheme.pathToFile != null || currentTheme.assetName != null)) {
                try {
                    File file;
                    if (currentTheme.assetName != null) {
                        file = getAssetFile(currentTheme.assetName);
                    } else {
                        file = new File(currentTheme.pathToFile);
                    }
                    Bitmap bitmap = loadScreenSizedBitmap(new FileInputStream(file), themedWallpaperFileOffset);
                    if (bitmap != null) {
                        settings.wallpaper = settings.themedWallpaper = wallpaper = new BitmapDrawable(bitmap);
                        wallpaper.setFilterBitmap(true);
                        settings.isCustomTheme = true;
                    }
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
        }
        if (settings.wallpaper == null) {
            int selectedColor = overrideWallpaper != null ? overrideWallpaper.color : 0;
            try {
                if (overrideWallpaper == null || overrideWallpaper.isDefault()) {
                    settings.wallpaper = createDefaultWallpaper();
                    settings.isCustomTheme = false;
                } else if (!overrideWallpaper.isColor() || overrideWallpaper.gradientColor1 != 0) {
                    if (selectedColor != 0 && (!isPatternWallpaper || overrideWallpaper.gradientColor2 != 0)) {
                        if (overrideWallpaper.gradientColor1 != 0 && overrideWallpaper.gradientColor2 != 0) {
                            MotionBackgroundDrawable motionBackgroundDrawable = new MotionBackgroundDrawable(overrideWallpaper.color, overrideWallpaper.gradientColor1, overrideWallpaper.gradientColor2, overrideWallpaper.gradientColor3, false);
                            motionBackgroundDrawable.setPhase(previousPhase);
                            if (settings.isPatternWallpaper) {
                                File toFile = new File(ApplicationLoader.getFilesDirFixed(), overrideWallpaper.fileName);
                                if (toFile.exists()) {
                                    motionBackgroundDrawable.setPatternBitmap((int) (overrideWallpaper.intensity * 100), loadScreenSizedBitmap(new FileInputStream(toFile), 0));
                                    settings.isCustomTheme = true;
                                }
                            }
                            settings.wallpaper = motionBackgroundDrawable;
                        } else if (settings.isPatternWallpaper) {
                            File toFile = new File(ApplicationLoader.getFilesDirFixed(), overrideWallpaper.fileName);
                            if (toFile.exists()) {
                                Bitmap bitmap = loadScreenSizedBitmap(new FileInputStream(toFile), 0);
                                if (bitmap != null) {
                                    settings.wallpaper = new BitmapDrawable(bitmap);
                                    settings.wallpaper.setFilterBitmap(true);
                                    settings.isCustomTheme = true;
                                }
                            }
                        } else if (overrideWallpaper.gradientColor1 != 0) {
                            final int[] colors = {selectedColor, overrideWallpaper.gradientColor1};
                            final BackgroundGradientDrawable.Orientation orientation = BackgroundGradientDrawable.getGradientOrientation(overrideWallpaper.rotation);
                            final BackgroundGradientDrawable backgroundGradientDrawable = new BackgroundGradientDrawable(orientation, colors);
                            final BackgroundGradientDrawable.Listener listener = new BackgroundGradientDrawable.ListenerAdapter() {
                                @Override
                                public void onSizeReady(int width, int height) {
                                    final boolean isOrientationPortrait = AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y;
                                    final boolean isGradientPortrait = width <= height;
                                    if (isOrientationPortrait == isGradientPortrait) {
                                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                                    }
                                }
                            };
                            backgroundGradientDisposable = backgroundGradientDrawable.startDithering(BackgroundGradientDrawable.Sizes.ofDeviceScreen(), listener, 100);
                            settings.wallpaper = backgroundGradientDrawable;
                        } else {
                            settings.wallpaper = new ColorDrawable(selectedColor);
                        }
                    } else {
                        File toFile = new File(ApplicationLoader.getFilesDirFixed(), overrideWallpaper.fileName);
                        if (toFile.exists()) {
                            Bitmap bitmap = loadScreenSizedBitmap(new FileInputStream(toFile), 0);
                            if (bitmap != null) {
                                settings.wallpaper = new BitmapDrawable(bitmap);
                                settings.wallpaper.setFilterBitmap(true);
                                settings.isCustomTheme = true;
                            }
                        }
                        if (settings.wallpaper == null) {
                            settings.wallpaper = createDefaultWallpaper();
                            settings.isCustomTheme = false;
                        }
                    }
                }
            } catch (Throwable throwable) {
                //ignore
            }
            if (settings.wallpaper == null) {
                if (selectedColor == 0) {
                    selectedColor = -2693905;
                }
                settings.wallpaper = new ColorDrawable(selectedColor);
            }
        }

        if (!LiteMode.isEnabled(LiteMode.FLAG_CHAT_BACKGROUND) && settings.wallpaper instanceof MotionBackgroundDrawable) {
            MotionBackgroundDrawable motionBackgroundDrawable = (MotionBackgroundDrawable) settings.wallpaper;
            int w, h;
            if (motionBackgroundDrawable.getPatternBitmap() == null) {
                w = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                h = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            } else {
                w = motionBackgroundDrawable.getPatternBitmap().getWidth();
                h = motionBackgroundDrawable.getPatternBitmap().getHeight();
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            settings.wallpaper.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            settings.wallpaper.draw(canvas);
            settings.wallpaper = new BitmapDrawable(bitmap);
        }
        return settings;
    }

    public static Drawable createDefaultWallpaper() {
        return createDefaultWallpaper(0, 0);
    }

    public static Drawable createDefaultWallpaper(int w, int h) {
        MotionBackgroundDrawable motionBackgroundDrawable = new MotionBackgroundDrawable(0xffdbddbb, 0xff6ba587, 0xffd5d88d, 0xff88b884, w != 0);
        if (w <= 0 || h <= 0) {
            w = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            h = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
        }
        motionBackgroundDrawable.setPatternBitmap(34, SvgHelper.getBitmap(R.raw.default_pattern, w, h, Color.BLACK, 1f, SvgHelper.ScaleMode.ByWidth));
        motionBackgroundDrawable.setPatternColorFilter(motionBackgroundDrawable.getPatternColor());
        return motionBackgroundDrawable;
    }

    private static Bitmap loadScreenSizedBitmap(FileInputStream stream, int offset) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            opts.inJustDecodeBounds = true;
            stream.getChannel().position(offset);
            BitmapFactory.decodeStream(stream, null, opts);
            float photoW = opts.outWidth;
            float photoH = opts.outHeight;
            float scaleFactor;
            int w_filter = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            int h_filter = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
            if (w_filter >= h_filter && photoW > photoH) {
                scaleFactor = Math.max(photoW / w_filter, photoH / h_filter);
            } else {
                scaleFactor = Math.min(photoW / w_filter, photoH / h_filter);
            }
            if (scaleFactor < 1.2f) {
                scaleFactor = 1;
            }
            opts.inJustDecodeBounds = false;
            if (scaleFactor > 1.0f && (photoW > w_filter || photoH > h_filter)) {
                int sample = 1;
                do {
                    sample *= 2;
                } while (sample * 2 < scaleFactor);
                opts.inSampleSize = sample;
            } else {
                opts.inSampleSize = (int) scaleFactor;
            }
            stream.getChannel().position(offset);
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
            if (bitmap.getWidth() < w_filter || bitmap.getHeight() < h_filter) {
                float scale = Math.max((float) w_filter / bitmap.getWidth(), (float) h_filter / bitmap.getHeight());
                if (scale >= 1.02f) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), true);
                    bitmap.recycle();
                    return scaledBitmap;
                }
            }
            return bitmap;
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    public static Drawable getThemedWallpaper(boolean thumb, View ownerView) {
        int backgroundColor = currentColors.get(key_chat_wallpaper);
        File file = null;
        MotionBackgroundDrawable motionBackgroundDrawable = null;
        int offset = 0;
        if (backgroundColor != 0) {
            int gradientToColor1 = currentColors.get(key_chat_wallpaper_gradient_to1);
            int gradientToColor2 = currentColors.get(key_chat_wallpaper_gradient_to2);
            int gradientToColor3 = currentColors.get(key_chat_wallpaper_gradient_to3);
            int rotation = currentColors.get(key_chat_wallpaper_gradient_rotation, -1);
            if (rotation == -1) {
                rotation = 45;
            }
            if (gradientToColor1 == 0) {
                return new ColorDrawable(backgroundColor);
            } else {
                ThemeAccent accent = currentTheme.getAccent(false);
                if (accent != null && !TextUtils.isEmpty(accent.patternSlug) && previousTheme == null) {
                    File wallpaperFile = accent.getPathToWallpaper();
                    if (wallpaperFile != null && wallpaperFile.exists()) {
                        file = wallpaperFile;
                    }
                }
                if (gradientToColor2 != 0) {
                    motionBackgroundDrawable = new MotionBackgroundDrawable(backgroundColor, gradientToColor1, gradientToColor2, gradientToColor3, true);
                    if (file == null) {
                        return motionBackgroundDrawable;
                    }
                } else if (file == null) {
                    final int[] colors = {backgroundColor, gradientToColor1};
                    final GradientDrawable.Orientation orientation = BackgroundGradientDrawable.getGradientOrientation(rotation);
                    final BackgroundGradientDrawable backgroundGradientDrawable = new BackgroundGradientDrawable(orientation, colors);
                    final BackgroundGradientDrawable.Sizes sizes;
                    if (!thumb) {
                        sizes = BackgroundGradientDrawable.Sizes.ofDeviceScreen();
                    } else {
                        sizes = BackgroundGradientDrawable.Sizes.ofDeviceScreen(BackgroundGradientDrawable.DEFAULT_COMPRESS_RATIO / 4f, BackgroundGradientDrawable.Sizes.Orientation.PORTRAIT);
                    }
                    final BackgroundGradientDrawable.Listener listener;
                    if (ownerView != null) {
                        listener = new BackgroundGradientDrawable.ListenerAdapter() {
                            @Override
                            public void onSizeReady(int width, int height) {
                                if (!thumb) {
                                    final boolean isOrientationPortrait = AndroidUtilities.displaySize.x <= AndroidUtilities.displaySize.y;
                                    final boolean isGradientPortrait = width <= height;
                                    if (isOrientationPortrait == isGradientPortrait) {
                                        ownerView.invalidate();
                                    }
                                } else {
                                    ownerView.invalidate();
                                }
                            }
                        };
                    } else {
                        listener = null;
                    }
                    backgroundGradientDrawable.startDithering(sizes, listener);
                    return backgroundGradientDrawable;
                }
            }
        } else if (themedWallpaperFileOffset > 0 && (currentTheme.pathToFile != null || currentTheme.assetName != null)) {
            if (currentTheme.assetName != null) {
                file = getAssetFile(currentTheme.assetName);
            } else {
                file = new File(currentTheme.pathToFile);
            }
            offset = themedWallpaperFileOffset;
        }
        if (file != null) {
            FileInputStream stream = null;
            try {
                int currentPosition = 0;
                stream = new FileInputStream(file);
                stream.getChannel().position(offset);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                int scaleFactor = 1;
                if (thumb) {
                    opts.inJustDecodeBounds = true;
                    float photoW = opts.outWidth;
                    float photoH = opts.outHeight;
                    int maxWidth = dp(100);
                    while (photoW > maxWidth || photoH > maxWidth) {
                        scaleFactor *= 2;
                        photoW /= 2;
                        photoH /= 2;
                    }
                }
                opts.inPreferredConfig = Bitmap.Config.ALPHA_8;
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scaleFactor;
                Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
                if (motionBackgroundDrawable != null) {
                    int intensity;
                    ThemeAccent accent = currentTheme.getAccent(false);
                    if (accent != null) {
                        intensity = (int) (accent.patternIntensity * 100);
                    } else {
                        intensity = 100;
                    }
                    if (bitmap != null && bitmap.getConfig() != Bitmap.Config.ALPHA_8) {
                        Bitmap toRecycle = bitmap;
                        bitmap = bitmap.copy(Bitmap.Config.ALPHA_8, false);
                        toRecycle.recycle();
                    }
                    motionBackgroundDrawable.setPatternBitmap(intensity, bitmap);
                    motionBackgroundDrawable.setPatternColorFilter(motionBackgroundDrawable.getPatternColor());
                    return motionBackgroundDrawable;
                }
                if (bitmap != null) {
                    return new BitmapDrawable(bitmap);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
        return null;
    }

    public static String getSelectedBackgroundSlug() {
        if (currentTheme.overrideWallpaper != null) {
            return currentTheme.overrideWallpaper.slug;
        }
        if (hasWallpaperFromTheme()) {
            return THEME_BACKGROUND_SLUG;
        }
        return DEFAULT_BACKGROUND_SLUG;
    }

    public static Drawable getCachedWallpaper() {
        Drawable drawable = getCachedWallpaperNonBlocking();
        if (drawable == null && wallpaperLoadTask != null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Utilities.themeQueue.postRunnable(countDownLatch::countDown);
            try {
                countDownLatch.await();
            } catch (Exception e) {
                FileLog.e(e);
            }
            drawable = getCachedWallpaperNonBlocking();
        }
        return drawable;
    }

    public static Drawable getCachedWallpaperNonBlocking() {
        if (themedWallpaper != null) {
            return themedWallpaper;
        } else {
            return wallpaper;
        }
    }

    public static boolean isWallpaperMotion() {
        return isWallpaperMotion;
    }

    public static boolean isPatternWallpaper() {
        String selectedBgSlug = getSelectedBackgroundSlug();
        return isPatternWallpaper || "CJz3BZ6YGEYBAAAABboWp6SAv04".equals(selectedBgSlug) || "qeZWES8rGVIEAAAARfWlK1lnfiI".equals(selectedBgSlug);
    }

    public static BackgroundGradientDrawable getCurrentGradientWallpaper() {
        if (currentTheme.overrideWallpaper != null && currentTheme.overrideWallpaper.color != 0 && currentTheme.overrideWallpaper.gradientColor1 != 0) {
            final int[] colors = {currentTheme.overrideWallpaper.color, currentTheme.overrideWallpaper.gradientColor1};
            final GradientDrawable.Orientation orientation = BackgroundGradientDrawable.getGradientOrientation(currentTheme.overrideWallpaper.rotation);
            return new BackgroundGradientDrawable(orientation, colors);
        }
        return null;
    }

    public static AudioVisualizerDrawable getCurrentAudiVisualizerDrawable() {
        if (chat_msgAudioVisualizeDrawable == null) {
            chat_msgAudioVisualizeDrawable = new AudioVisualizerDrawable();
        }
        return chat_msgAudioVisualizeDrawable;
    }

    public static void unrefAudioVisualizeDrawable(MessageObject messageObject) {
        if (chat_msgAudioVisualizeDrawable == null) {
            return;
        }
        if (chat_msgAudioVisualizeDrawable.getParentView() == null || messageObject == null) {
            chat_msgAudioVisualizeDrawable.setParentView(null);
        } else {
            if (animatedOutVisualizerDrawables == null) {
                animatedOutVisualizerDrawables = new HashMap<>();
            }
            animatedOutVisualizerDrawables.put(messageObject, chat_msgAudioVisualizeDrawable);
            chat_msgAudioVisualizeDrawable.setWaveform(false, true, null);
            AndroidUtilities.runOnUIThread(() -> {
                AudioVisualizerDrawable drawable = animatedOutVisualizerDrawables.remove(messageObject);
                if (drawable != null) {
                    drawable.setParentView(null);
                }
            }, 200);
            chat_msgAudioVisualizeDrawable = null;
        }
    }

    public static AudioVisualizerDrawable getAnimatedOutAudioVisualizerDrawable(MessageObject messageObject) {
        if (animatedOutVisualizerDrawables == null || messageObject == null) {
            return null;
        }
        return animatedOutVisualizerDrawables.get(messageObject);
    }

    public static StatusDrawable getChatStatusDrawable(int type) {
        if (type < 0 || type > 5) {
            return null;
        }
        StatusDrawable statusDrawable = chat_status_drawables[type];
        if (statusDrawable != null) {
            return statusDrawable;
        }
        switch (type) {
            case 0:
                chat_status_drawables[0] = new TypingDotsDrawable(true);
                break;
            case 1:
                chat_status_drawables[1] = new RecordStatusDrawable(true);
                break;
            case 2:
                chat_status_drawables[2] = new SendingFileDrawable(true);
                break;
            case 3:
                chat_status_drawables[3] = new PlayingGameDrawable(true, null);
                break;
            case 4:
                chat_status_drawables[4] = new RoundStatusDrawable(true);
                break;
            case 5:
                chat_status_drawables[5] = new ChoosingStickerStatusDrawable(true);
                break;
        }
        statusDrawable = chat_status_drawables[type];
        statusDrawable.start();
        statusDrawable.setColor(getColor(key_chats_actionMessage));
        return statusDrawable;
    }

    public static FragmentContextViewWavesDrawable getFragmentContextViewWavesDrawable() {
        if (fragmentContextViewWavesDrawable == null) {
            fragmentContextViewWavesDrawable = new FragmentContextViewWavesDrawable();
        }
        return fragmentContextViewWavesDrawable;
    }

    public static RoundVideoProgressShadow getRadialSeekbarShadowDrawable() {
        if (roundPlayDrawable == null) {
            roundPlayDrawable = new RoundVideoProgressShadow();
        }
        return roundPlayDrawable;
    }

    public static SparseIntArray getFallbackKeys() {
        return fallbackKeys;
    }

    public static int getFallbackKey(int key) {
        return fallbackKeys.get(key);
    }

    public static Map<String, Drawable> getThemeDrawablesMap() {
        return defaultChatDrawables;
    }

    public static Drawable getThemeDrawable(String drawableKey) {
        return defaultChatDrawables.get(drawableKey);
    }

    public static Drawable getThemeDrawable(String key, Theme.ResourcesProvider resourcesProvider) {
        Drawable drawable = resourcesProvider != null ? resourcesProvider.getDrawable(key) : null;
        return drawable != null ? drawable : defaultChatDrawables.get(key);
    }

    public static int getThemeDrawableColorKey(String drawableKey) {
        return defaultChatDrawableColorKeys.get(drawableKey);
    }

    public static Map<String, Paint> getThemePaintsMap() {
        return defaultChatPaints;
    }

    public static Paint getThemePaint(String paintKey) {
        if (Objects.equals(paintKey, Theme.key_paint_divider)) {
            return dividerPaint;
        }
        return defaultChatPaints.get(paintKey);
    }

    public static int getThemePaintColorKey(String paintKey) {
        return defaultChatPaintColors.get(paintKey);
    }

    private static void addChatDrawable(String key, Drawable drawable, int colorKey) {
        defaultChatDrawables.put(key, drawable);
        defaultChatDrawableColorKeys.put(key, colorKey);
    }

    private static void addChatPaint(String key, Paint paint, int colorKey) {
        defaultChatPaints.put(key, paint);
        defaultChatPaintColors.put(key, colorKey);
    }

    public static boolean isCurrentThemeDay() {
        return !getActiveTheme().isDark();
    }

    public static boolean isHome(ThemeAccent accent) {
        if (accent.parentTheme != null) {
            if (accent.parentTheme.getKey().equals("Blue") && accent.id == 99) {
                return true;
            }
            if (accent.parentTheme.getKey().equals("Day") && accent.id == 9) {
                return true;
            }
            if ((accent.parentTheme.getKey().equals("Night") || accent.parentTheme.getKey().equals("Dark Blue")) && accent.id == 0) {
                return true;
            }
        }
        return false;
    }

    public static void turnOffAutoNight(@NonNull BaseFragment fragment) {
        if (selectedAutoNightType != AUTO_NIGHT_TYPE_NONE) {
            if (fragment != null) {
                try {
                    BulletinFactory.of(fragment).createSimpleBulletin(
                        R.raw.auto_night_off,
                        selectedAutoNightType == AUTO_NIGHT_TYPE_SYSTEM ?
                                getString("AutoNightSystemModeOff", R.string.AutoNightSystemModeOff) :
                                getString("AutoNightModeOff", R.string.AutoNightModeOff),
                        getString("Settings", R.string.Settings),
                        Bulletin.DURATION_PROLONG,
                        () -> fragment.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_NIGHT))
                    ).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            selectedAutoNightType = AUTO_NIGHT_TYPE_NONE;
            saveAutoNightThemeConfig();
            cancelAutoNightThemeCallbacks();
        }
    }

    public static void turnOffAutoNight(BulletinFactory bulletinFactory, Runnable openSettings) {
        if (selectedAutoNightType != AUTO_NIGHT_TYPE_NONE) {
            if (bulletinFactory != null && openSettings != null) {
                try {
                    bulletinFactory.createSimpleBulletin(
                        R.raw.auto_night_off,
                        selectedAutoNightType == AUTO_NIGHT_TYPE_SYSTEM ?
                            getString("AutoNightSystemModeOff", R.string.AutoNightSystemModeOff) :
                            getString("AutoNightModeOff", R.string.AutoNightModeOff),
                        getString("Settings", R.string.Settings),
                        Bulletin.DURATION_PROLONG,
                        openSettings
                    ).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            selectedAutoNightType = AUTO_NIGHT_TYPE_NONE;
            saveAutoNightThemeConfig();
            cancelAutoNightThemeCallbacks();
        }
    }

    public enum IvButtonColors {
        DEFAULT(
                Theme.key_chat_msgIvButtonDefaultIn, Theme.key_chat_msgIvButtonDefaultInPressed, Theme.key_chat_msgIvButtonDefaultInText,
                Theme.key_chat_msgIvButtonDefaultOut, Theme.key_chat_msgIvButtonDefaultOutPressed, Theme.key_chat_msgIvButtonDefaultOutText
        ),
        PRIMARY(
                Theme.key_chat_msgIvButtonPrimaryIn, Theme.key_chat_msgIvButtonPrimaryInPressed, Theme.key_chat_msgIvButtonPrimaryInText,
                Theme.key_chat_msgIvButtonPrimaryOut, Theme.key_chat_msgIvButtonPrimaryOutPressed, Theme.key_chat_msgIvButtonPrimaryOutText
        ),
        DANGER(
                Theme.key_chat_msgIvButtonDangerIn, Theme.key_chat_msgIvButtonDangerInPressed, Theme.key_chat_msgIvButtonDangerInText,
                Theme.key_chat_msgIvButtonDangerOut, Theme.key_chat_msgIvButtonDangerOutPressed, Theme.key_chat_msgIvButtonDangerOutText
        ),
        SUCCESS(
                Theme.key_chat_msgIvButtonSuccessIn, Theme.key_chat_msgIvButtonSuccessInPressed, Theme.key_chat_msgIvButtonSuccessInText,
                Theme.key_chat_msgIvButtonSuccessOut, Theme.key_chat_msgIvButtonSuccessOutPressed, Theme.key_chat_msgIvButtonSuccessOutText
        ),
        DEFAULT_IN_TEXT(
                Theme.key_chat_msgIvButtonDefaultInlineIn, Theme.key_chat_msgIvButtonDefaultInlineInPressed, Theme.key_chat_msgIvButtonDefaultInlineInText,
                Theme.key_chat_msgIvButtonDefaultInlineOut, Theme.key_chat_msgIvButtonDefaultInlineOutPressed, Theme.key_chat_msgIvButtonDefaultInlineOutText
        );

        private final int backgroundIn, backgroundInPressed, textIn;
        private final int backgroundOut, backgroundOutPressed, textOut;

        IvButtonColors(int backgroundIn, int backgroundInPressed, int textIn, int backgroundOut, int backgroundOutPressed, int textOut) {
            this.backgroundIn = backgroundIn;
            this.backgroundInPressed = backgroundInPressed;
            this.textIn = textIn;
            this.backgroundOut = backgroundOut;
            this.backgroundOutPressed = backgroundOutPressed;
            this.textOut = textOut;
        }

        public int getBackgroundKey(boolean out) {
            return out ? backgroundOut : backgroundIn;
        }

        public int getBackgroundPressedKey(boolean out) {
            return out ? backgroundOutPressed : backgroundInPressed;
        }

        public int getTextKey(boolean out) {
            return out ? textOut : textIn;
        }

        public static IvButtonColors of(TL_keyboard.RichButtonStyle style) {
            if (style != null) {
                if (style.bg_primary) {
                    return PRIMARY;
                } else if (style.bg_danger) {
                    return DANGER;
                } else if (style.bg_success) {
                    return SUCCESS;
                }
            }
            return DEFAULT;
        }
    }

    public interface Colorable {
        public void updateColors();
        public default int[] getColorKeys() { return null; }
    }

    private static final Paint PAINT_FILLING = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final Paint PAINT_CLEAR = new Paint(Paint.ANTI_ALIAS_FLAG); static {
        PAINT_CLEAR.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public static Paint fillingPaint(int color) {
        if (PAINT_FILLING.getColor() != color) {
            PAINT_FILLING.setColor(color);
        }
        return PAINT_FILLING;
    }


    /* DEBUG */

    public static Paint DEBUG_RED = new Paint(); static { DEBUG_RED.setColor(0xffff0000); }
    public static Paint DEBUG_BLUE = new Paint(); static { DEBUG_BLUE.setColor(0xff0000ff); }
    public static Paint DEBUG_GREEN_40 = new Paint(); static { DEBUG_GREEN_40.setColor(0x4000FF00); }
    public static Paint DEBUG_GREEN_B0 = new Paint(); static { DEBUG_GREEN_B0.setColor(0xB000FF00); }
    public static Paint DEBUG_RED_STROKE = new Paint(); static {
        DEBUG_RED_STROKE.setColor(0xffff0000);
        DEBUG_RED_STROKE.setStrokeWidth(2);
        DEBUG_RED_STROKE.setStyle(Paint.Style.STROKE);
    }
    public static Paint DEBUG_GREEN_STROKE = new Paint(); static {
        DEBUG_GREEN_STROKE.setColor(0xff00ff00);
        DEBUG_GREEN_STROKE.setStrokeWidth(2);
        DEBUG_GREEN_STROKE.setStyle(Paint.Style.STROKE);
    }
}
