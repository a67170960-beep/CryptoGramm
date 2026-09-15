   }
                        availableTimeWidth = firstLineWidth - dp(35);
                    } else {
                        availableTimeWidth = photoWidth - dp(14);
                    }

                    if (messageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                        availableTimeWidth = (int) (AndroidUtilities.roundMessageSize - Math.ceil(Theme.chat_audioTimePaint.measureText("00:00")) - dp(46));
                    }
                    measureTime(messageObject);
                    int timeWidthTotal = timeWidth + dp((SharedConfig.bubbleRadius >= 10 ? 22 : 18) + (messageObject.isOutOwner() ? 20 : 0));
                    if (w < timeWidthTotal) {
                        w = timeWidthTotal;
                    }

                    if (messageObject.isRoundVideo()) {
                        w = h = Math.min(w, h);
                        drawBackground = messageObject.isVoiceTranscriptionOpen();
                        mediaBackground = !drawBackground;
                        if (drawBackground) {
                            h = 0;
                        }
                        photoImage.setRoundRadius(w / 2);
                        canChangeRadius = false;
                    } else if (messageObject.needDrawBluredPreview() && !messageObject.hasExtendedMediaPreview()) {
                        if (AndroidUtilities.isTablet()) {
                            w = (int) (AndroidUtilities.getMinTabletSide() * 0.6f);
                        } else {
                            w = (int) (Math.min(getParentWidth(), AndroidUtilities.displaySize.y) * 0.6f);
                        }
                        h = (int) (0.61f * w);
                    }

                    int widthForCaption = 0;
                    int widthCaptionMin = -1;
                    boolean fixPhotoWidth = false;
                    if (currentMessagesGroup != null) {
                        float maxHeight = Math.max(getParentWidth(), AndroidUtilities.displaySize.y) * 0.5f;
                        int dWidth = getGroupPhotosWidth();
                        w = (int) Math.ceil(currentPosition.pw / 1000.0f * dWidth);
                        if (currentPosition.minY != 0 && (messageObject.isOutOwner() && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0 || !messageObject.isOutOwner() && (currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0)) {
                            int firstLineWidth = 0;
                            int currentLineWidth = 0;
                            for (int a = 0; a < currentMessagesGroup.posArray.size(); a++) {
                                MessageObject.GroupedMessagePosition position = currentMessagesGroup.posArray.get(a);
                                if (position.minY == 0) {
                                    firstLineWidth += Math.ceil(position.pw / 1000.0f * dWidth) + (position.leftSpanOffset != 0 ? Math.ceil(position.leftSpanOffset / 1000.0f * dWidth) : 0);
                                } else if (position.minY == currentPosition.minY) {
                                    currentLineWidth += Math.ceil((position.pw) / 1000.0f * dWidth) + (position.leftSpanOffset != 0 ? Math.ceil(position.leftSpanOffset / 1000.0f * dWidth) : 0);
                                } else if (position.minY > currentPosition.minY) {
                                    break;
                                }
                            }
                            w += firstLineWidth - currentLineWidth;
                        }
                        w -= dp(9);
                        if (currentMessageObject != null && !currentMessageObject.isOutOwner() && isSideMenuPossibleLeftMargin()) {
                            w -= dp(ChatActivity.SIDE_MENU_WIDTH);
                        } else if (isAvatarVisible) {
                            w -= dp(48);
                        }
                        if (currentPosition.siblingHeights != null) {
                            h = 0;
                            for (int a = 0; a < currentPosition.siblingHeights.length; a++) {
                                h += (int) Math.ceil(maxHeight * currentPosition.siblingHeights[a]);
                            }
                            h += (currentPosition.maxY - currentPosition.minY) * Math.round(7 * AndroidUtilities.density); //TODO fix
                        } else {
                            h = (int) Math.ceil(maxHeight * currentPosition.ph);
                        }
                        backgroundWidth = w;
                        if ((currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0) {
                            w -= dp(8);
                        } else if ((currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) == 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) == 0) {
                            w -= dp(11);
                        } else if ((currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0) {
                            w -= dp(10);
                        } else {
                            w -= dp(9);
                        }
                        photoWidth = w;
                        if (!currentPosition.edge) {
                            photoWidth += dp(10);
                        }
                        photoHeight = h;
                        if (currentPosition == null || (currentPosition.flags & captionFlag()) != 0) {
                            widthForCaption += photoWidth - dp(15);
                        }
                        boolean checkCaption = true;
                        if ((currentPosition.flags & captionFlag()) != 0 || captionAbove || currentMessagesGroup.hasSibling && (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0) {

                            if (currentPosition == null || (currentPosition.flags & captionFlag()) != 0) {
                                widthForCaption += getAdditionalWidthForPosition(currentPosition);
                            }
                            int count = Math.min(currentMessagesGroup.posArray.size(), currentMessagesGroup.messages.size());
                            for (int i = 0; i < count; i++) {
                                MessageObject m = currentMessagesGroup.messages.get(i);
                                MessageObject.GroupedMessagePosition rowPosition = currentMessagesGroup.posArray.get(i);
                                if (rowPosition != currentPosition && (rowPosition.flags & captionFlag()) != 0) {
                                    w = (int) Math.ceil(rowPosition.pw / 1000.0f * dWidth);
                                    if (rowPosition.minY != 0 && (messageObject.isOutOwner() && (rowPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0 || !messageObject.isOutOwner() && (rowPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0)) {
                                        int firstLineWidth = 0;
                                        int currentLineWidth = 0;
                                        for (int a = 0; a < currentMessagesGroup.posArray.size(); a++) {
                                            MessageObject.GroupedMessagePosition position = currentMessagesGroup.posArray.get(a);
                                            if (position.minY == 0) {
                                                firstLineWidth += Math.ceil(position.pw / 1000.0f * dWidth) + (position.leftSpanOffset != 0 ? Math.ceil(position.leftSpanOffset / 1000.0f * dWidth) : 0);
                                            } else if (position.minY == rowPosition.minY) {
                                                currentLineWidth += Math.ceil((position.pw) / 1000.0f * dWidth) + (position.leftSpanOffset != 0 ? Math.ceil(position.leftSpanOffset / 1000.0f * dWidth) : 0);
                                            } else if (position.minY > rowPosition.minY) {
                                                break;
                                            }
                                        }
                                        w += firstLineWidth - currentLineWidth;
                                    }
                                    w -= dp(9);
                                    if ((rowPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0 && (rowPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0) {
                                        w -= dp(8);
                                    } else if ((rowPosition.flags & MessageObject.POSITION_FLAG_RIGHT) == 0 && (rowPosition.flags & MessageObject.POSITION_FLAG_LEFT) == 0) {
                                        w -= dp(11);
                                    } else if ((rowPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0) {
                                        w -= dp(10);
                                    } else {
                                        w -= dp(9);
                                    }
                                    if (((isChat || m.isRepostPreview) && !isThreadPost && !m.isOutOwner() || m.forceAvatar || m.messageOwner.guestchat_via_from != null || m.getDialogId() == UserObject.VERIFY) && m.needDrawAvatar() && (rowPosition == null || rowPosition.edge)) {
                                        w -= dp(48);
                                    }
                                    w += getAdditionalWidthForPosition(rowPosition);
                                    if (!rowPosition.edge) {
                                        w += dp(10);
                                    }
                                    widthForCaption += w;
                                    if (rowPosition.minX < currentPosition.minX || currentMessagesGroup.hasSibling && rowPosition.minY != rowPosition.maxY) {
                                        captionOffsetX -= w;
                                    }
                                }
                                if (checkCaption) {
                                    if (m.caption != null) {
                                        if (currentCaption != null) {
                                            currentCaption = null;
                                            checkCaption = false;
                                        } else {
                                            currentCaption = m.caption;
                                        }
                                    }
                                }
                            }
                            if ((currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0 && currentCaption != null) {
                                currentMessagesGroup.cachedWidthForCaption = widthForCaption;
                            } else if (currentMessagesGroup.cachedWidthForCaption > 0) {
                                widthForCaption = currentMessagesGroup.cachedWidthForCaption;
                            }
                        }
                    } else {
                        photoHeight = h;
                        photoWidth = w;
                        currentCaption = messageObject.caption;

                        int minCaptionWidth = currentMessageObject.getMaxMessageTextWidth();
                        if (!messageObject.needDrawBluredPreview() && (currentCaption != null || (!reactionsLayoutInBubble.isEmpty && !reactionsLayoutInBubble.isSmall)) && photoWidth < minCaptionWidth) {
                            widthForCaption = minCaptionWidth;
                            fixPhotoWidth = true;
                        } else {
                            widthForCaption = photoWidth - dp(10);
                        }

                        backgroundWidth = photoWidth + dp(8);
                        if (!mediaBackground) {
                            backgroundWidth += dp(9);
                        }
                    }

                    if (currentCaption != null) {
                        try {
                            captionFullWidth = widthForCaption;
                            widthForCaption -= getExtraTextX() * 2;
                            captionLayout = new MessageObject.TextLayoutBlocks(getPrimaryMessageObject(), currentCaption, Theme.chat_msgTextPaint, widthForCaption);
                            captionLayout.bounceFrom(prevCaptionLayout);
                            if (fixPhotoWidth) {
                                captionWidth = captionLayout.textWidth;
                                // feature: blur on sides, instead of cropping photo
//                                if (!currentMessageObject.isVideo() && !currentMessageObject.isGif() && captionWidth > photoWidth - dp(10)) {
//                                    fitPhotoImage = true;
//                                    photoImage.setAspectFit(true);
//                                    photoImage.setRoundRadiusEnabled(false);
//                                }
                                if (captionWidth > widthForCaption) {
                                    captionWidth = widthForCaption;
                                }
                            } else {
                                captionWidth = widthForCaption;
                            }
                            if (widthCaptionMin > 0 && captionWidth > widthCaptionMin) {
                                photoWidth += captionWidth - widthCaptionMin;
                                backgroundWidth += captionWidth - widthCaptionMin;
                            }
                            captionHeight = captionLayout.textHeight();
                            addedCaptionHeight = captionHeight + dp(9);
                            if (!captionAbove && (captionLayout.hasCodeAtBottom || captionLayout.hasQuoteAtBottom)) {
                                captionHeight += dp(14);
                                addedCaptionHeight += dp(14);
                            }
                            if (currentPosition == null || (currentPosition.flags & captionFlag()) != 0) {
                                additionHeight += addedCaptionHeight;
                                int widthToCheck = Math.max(captionWidth, photoWidth - dp(10));
                                if ((reactionsLayoutInBubble.isEmpty || reactionsLayoutInBubble.isSmall) && !shouldDrawTimeOnMedia() && widthToCheck + dp(2) - captionLayout.lastLineWidth < timeWidthTotal + getExtraTimeX() && !hasFactCheck) {
                                    additionHeight += dp(14);
                                    addedCaptionHeight += dp(14);
                                    captionNewLine = 1;
                                }
                            } else {
                                captionLayout = null;
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    int addedFactCheckHeight = layoutFactCheck(widthForCaption - dp(30));
                    if (currentPosition == null && hasFactCheck) {
                        additionHeight += dp(12) + addedFactCheckHeight;
                    }
                    if (highlightCaptionToSetStart >= 0 && captionLayout != null) {
                        highlight(highlightCaptionToSetStart, highlightCaptionToSetEnd, captionLayout.textLayoutBlocks);
                    }
                    highlightCaptionToSetStart = highlightCaptionToSetEnd = -1;
                    if (!reactionsLayoutInBubble.isSmall) {
                        boolean useBackgroundWidth = backgroundWidth - dp(24) > widthForCaption;
                        int maxWidth = Math.max(backgroundWidth - dp(36), widthForCaption);
                        reactionsLayoutInBubble.measure(maxWidth, Gravity.LEFT);
                        if (!reactionsLayoutInBubble.isEmpty) {
                            if (isRoundVideo) {
                                reactionsLayoutInBubble.drawServiceShaderBackground = 1f - getVideoTranscriptionProgress();
                            } else if (shouldDrawTimeOnMedia()) {
                                reactionsLayoutInBubble.drawServiceShaderBackground = 1f;
                            }
                            int heightLocal = reactionsLayoutInBubble.height;
                            if (captionLayout == null) {
                                heightLocal += dp(12);
                                heightLocal += dp(4);
                            } else {
                                heightLocal += dp(12);
                                reactionsLayoutInBubble.positionOffsetY += dp(12);
                            }
                            reactionsLayoutInBubble.totalHeight = heightLocal;
                            additionHeight += reactionsLayoutInBubble.totalHeight;

                            if (isRoundVideo && currentMessageObject != null && currentMessageObject.isVoiceTranscriptionOpen()) {
                                reactionsLayoutInBubble.positionOffsetY += dp(8);
                            }

                            if (!shouldDrawTimeOnMedia()) {
                                int widthToCheck = Math.min(maxWidth, reactionsLayoutInBubble.width + timeWidthTotal + getExtraTimeX() + dp(2));
                                float lastLineWidth = reactionsLayoutInBubble.lastLineX;
                                if (!shouldDrawTimeOnMedia() && widthToCheck - lastLineWidth < timeWidthTotal + getExtraTimeX()) {
                                    additionHeight += dp(14);
                                    reactionsLayoutInBubble.totalHeight += dp(14);
                                    reactionsLayoutInBubble.positionOffsetY -= dp(14);
                                    captionNewLine = 1;
                                    if (!useBackgroundWidth && captionWidth < reactionsLayoutInBubble.width) {
                                        captionWidth = reactionsLayoutInBubble.width;
                                    }
                                } else if (!useBackgroundWidth) {
                                    if (reactionsLayoutInBubble.lastLineX + timeWidthTotal > captionWidth) {
                                        captionWidth = reactionsLayoutInBubble.lastLineX + timeWidthTotal;
                                    }
                                    if (reactionsLayoutInBubble.width > captionWidth) {
                                        captionWidth = reactionsLayoutInBubble.width;
                                    }
                                }
                            }
                        }
                    }

                    int minWidth = (int) (Theme.chat_infoPaint.measureText("100%") + dp(100/*48*/)/* + timeWidth*/);
                    if (currentMessagesGroup == null && (documentAttachType == DOCUMENT_ATTACH_TYPE_VIDEO || documentAttachType == DOCUMENT_ATTACH_TYPE_GIF) && photoWidth < minWidth) {
                        photoWidth = minWidth;
                        backgroundWidth = photoWidth + dp(8);
                        if (!mediaBackground) {
                            backgroundWidth += dp(9);
                        }
                    }

                    if (fixPhotoWidth && photoWidth < captionWidth + dp(10)) {
                        photoWidth = captionWidth + dp(10);
                        backgroundWidth = photoWidth + dp(8);
                        if (!mediaBackground) {
                            backgroundWidth += dp(9);
                        }
                    }
                    if (messageChanged || messageIdChanged || dataChanged) {
                        currentPhotoFilter = currentPhotoFilterThumb = String.format(Locale.US, "%d_%d", (int) (w / AndroidUtilities.density), (int) (h / AndroidUtilities.density));
                        if (
                            messageObject.photoThumbs != null && messageObject.photoThumbs.size() > 1 ||
                            messageObject.type == MessageObject.TYPE_VIDEO ||
                            messageObject.type == MessageObject.TYPE_GIF ||
                            messageObject.type == MessageObject.TYPE_ROUND_VIDEO
                        ) {
                            if (messageObject.needDrawBluredPreview()) {
                                photoImage.setColorFilter(getFancyBlurFilter());
                                if (!messageObject.isRoundOnce()) {
                                    currentPhotoFilter += "_b2";
                                }
                                if (messageObject.isRoundOnce()) {
                                    currentPhotoFilterThumb += "_b2r";
                                } else {
                                    currentPhotoFilterThumb += "_b2";
                                }
                            } else {
                                currentPhotoFilterThumb += "_b";
                            }
                        }
                    } else {
                        String filterNew = String.format(Locale.US, "%d_%d", (int) (w / AndroidUtilities.density), (int) (h / AndroidUtilities.density));
                        if (!messageObject.needDrawBluredPreview() && !filterNew.equals(currentPhotoFilter)) {
                            ImageLocation location = ImageLocation.getForObject(currentPhotoObject, photoParentObject);
                            if (location != null) {
                                String key = location.getKey(photoParentObject, null, false) + "@" + currentPhotoFilter;
                                if (ImageLoader.getInstance().isInMemCache(key, false)) {
                                    currentPhotoObjectThumb = currentPhotoObject;
                                    currentPhotoFilterThumb = currentPhotoFilter;
                                    currentPhotoFilter = filterNew;
                                }
                            }
                        } else if (messageObject.needDrawBluredPreview()) {
                            photoImage.setColorFilter(getFancyBlurFilter());
                        }
                    }

                    boolean noSize = false;
                    if (messageObject.type == MessageObject.TYPE_VIDEO || messageObject.type == MessageObject.TYPE_GIF || messageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                        noSize = true;
                    }
                    if (currentPhotoObject != null && !noSize && currentPhotoObject.size == 0) {
                        currentPhotoObject.size = -1;
                    }
                    if (currentPhotoObjectThumb != null && !noSize && currentPhotoObjectThumb.size == 0) {
                        currentPhotoObjectThumb.size = -1;
                    }

                    if (!currentMessageObject.isHiddenSensitive() && SharedConfig.isAutoplayVideo() && (!currentMessageObject.hasVideoCover() || currentMessageObject.isLivePhoto()) && !currentMessageObject.isRepostPreview && (!currentMessageObject.hasMediaSpoilers() || currentMessageObject.isMediaSpoilersRevealed || currentMessageObject.revealingMediaSpoilers) && (messageObject.type == MessageObject.TYPE_VIDEO /*|| messageObject.type == MessageObject.TYPE_STORY && messageObject.getDocument() != null*/) && !messageObject.needDrawBluredPreview() &&
                            ((currentMessageObject.mediaExists || currentMessageObject.attachPathExists) || messageObject.canStreamVideo() && DownloadController.getInstance(currentAccount).canDownloadMedia(currentMessageObject))
                    ) {
                        if (currentPosition != null) {
                            autoPlayingMedia = (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0;
                        } else {
                            autoPlayingMedia = true;
                        }
                    }

                    final int cacheType = currentMessageObject.shouldEncryptPhotoOrVideo() ? ImageLoader.CACHE_TYPE_ENCRYPTED : ImageLoader.CACHE_TYPE_NONE;
                    if (currentMessageObject.sendPreviewEntry != null) {
                        photoImage.setCrossfadeWithOldImage(false);
                        if (currentMessageObject.sendPreviewEntry.thumbPath != null) {
                            photoImage.setImage(ImageLocation.getForPath(currentMessageObject.sendPreviewEntry.thumbPath), null, null, null, null, null, currentMessageObject.sendPreviewEntry.thumb, 0, null, messageObject, 0);
                        } else if (currentMessageObject.sendPreviewEntry.path != null) {
                            if (currentMessageObject.sendPreviewEntry.isVideo && !currentMessageObject.sendPreviewEntry.isLivePhoto()) {
                                photoImage.setImage(ImageLocation.getForPath("vthumb://" + currentMessageObject.sendPreviewEntry.imageId + ":" + currentMessageObject.sendPreviewEntry.path), null, null, null, null, null, currentMessageObject.sendPreviewEntry.thumb, 0, null, messageObject, 0);
                            } else {
                                photoImage.setOrientation(currentMessageObject.sendPreviewEntry.orientation, currentMessageObject.sendPreviewEntry.invert, true);
                                photoImage.setImage(ImageLocation.getForPath("thumb://" + currentMessageObject.sendPreviewEntry.imageId + ":" + currentMessageObject.sendPreviewEntry.path), null, null, null, null, null, currentMessageObject.sendPreviewEntry.thumb, 0, null, messageObject, 0);
                            }
                        }
                    } else if (autoPlayingMedia) {
                        photoImage.setAllowStartAnimation(true);
                        photoImage.startAnimation();
                        TLRPC.Document document = messageObject.getDocument();
                        if (messageObject.hasVideoQualities() && messageObject.thumbQuality != null) {
                            document = messageObject.thumbQuality.document;
                        }

                        final String filter = currentMessageObject.isLivePhoto() ? ImageLoader.AUTOPLAY_FILTER_NONLOOP : ImageLoader.AUTOPLAY_FILTER;
                        if (currentMessageObject.videoEditedInfo != null && currentMessageObject.videoEditedInfo.canAutoPlaySourceVideo() && document != null) {
                            photoImage.setImage(ImageLocation.getForPath(currentMessageObject.videoEditedInfo.originalPath), filter, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForDocument(currentPhotoObjectThumb, document), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, document.size, null, messageObject, 0);
                            photoImage.setMediaStartEndTime(currentMessageObject.videoEditedInfo.startTime / 1000, currentMessageObject.videoEditedInfo.endTime / 1000);
                        } else if (messageObject.cachedQuality != null) {
                            if (!messageIdChanged && !dataChanged) {
                                photoImage.setCrossfadeWithOldImage(true);
                            }
                            photoImage.setImage(ImageLocation.getForVideoPath(messageObject.cachedQuality.uri.getPath()), filter, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForDocument(currentPhotoObjectThumb, document), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, messageObject.getDocument().size, null, messageObject, 0);
                        } else if (document != null) {
                            if (!messageIdChanged && !dataChanged) {
                                photoImage.setCrossfadeWithOldImage(true);
                            }
                            photoImage.setImage(ImageLocation.getForDocument(document), filter, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForDocument(currentPhotoObjectThumb, document), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, messageObject.getDocument().size, null, messageObject, 0);
                        }
                    } else if (messageObject.type == MessageObject.TYPE_STORY || messageObject.type == MessageObject.TYPE_STORY_MENTION) {
                        TL_stories.StoryItem storyItem = messageObject.messageOwner.media.storyItem;
                        if (storyItem != null) {
                            if (storyItem instanceof TL_stories.TL_storyItemDeleted) {
                                photoImage.setImageBitmap(StoriesUtilities.getExpiredStoryDrawable());
                            } else {
                                StoriesUtilities.setImage(photoImage, storyItem);
                            }
                        } else {
                            photoImage.clearImage();
                        }
                    } else if (messageObject.type == MessageObject.TYPE_EXTENDED_MEDIA_PREVIEW) {
                        photoImage.setImage(null, null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, currentMessageObject, cacheType);
                    } else if (messageObject.type == MessageObject.TYPE_PHOTO) {
                        if (messageObject.useCustomPhoto) {
                            photoImage.setImageBitmap(getResources().getDrawable(R.drawable.theme_preview_image));
                        } else {
                            if (currentPhotoObject != null) {
                                boolean photoExist = true;
                                String fileName = FileLoader.getAttachFileName(currentPhotoObject);
                                if (messageObject.mediaExists) {
                                    DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                                } else {
                                    photoExist = false;
                                }
                                if (photoExist || !currentMessageObject.loadingCancelled && DownloadController.getInstance(currentAccount).canDownloadMedia(currentMessageObject) || FileLoader.getInstance(currentAccount).isLoadingFile(fileName)) {
                                    photoImage.setImage(ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, currentPhotoObject.size, null, currentMessageObject, cacheType);
                                } else {
                                    photoNotSet = true;
                                    if (currentPhotoObjectThumb != null || currentPhotoObjectThumbStripped != null) {
                                        photoImage.setImage(null, null, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, currentMessageObject, cacheType);
                                    } else {
                                        photoImage.setImageBitmap((Drawable) null);
                                    }
                                }
                            } else {
                                photoImage.setImageBitmap((Drawable) null);
                            }
                        }
                    } else if (messageObject.type == MessageObject.TYPE_GIF || messageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                        String fileName = FileLoader.getAttachFileName(messageObject.getDocument());
                        int localFile = 0;
                        if (messageObject.attachPathExists) {
                            DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                            localFile = 1;
                        } else if (messageObject.mediaExists) {
                            localFile = 2;
                        }
                        boolean autoDownload = false;
                        TLRPC.Document document = messageObject.getDocument();
                        if (MessageObject.isGifDocument(document, messageObject.hasValidGroupId()) || messageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                            autoDownload = DownloadController.getInstance(currentAccount).canDownloadMedia(currentMessageObject);
                        }
                        if (messageObject.isHiddenSensitive()) {
                            autoDownload = false;
                        }
                        TLRPC.VideoSize videoSize = MessageObject.getDocumentVideoThumb(document);
                        if (((MessageObject.isGifDocument(document, messageObject.hasValidGroupId()) && messageObject.videoEditedInfo == null) || (!messageObject.isSending() && !messageObject.isEditing())) && (localFile != 0 || FileLoader.getInstance(currentAccount).isLoadingFile(fileName) || autoDownload)) {
                            if (localFile != 1 && !messageObject.needDrawBluredPreview() && (localFile != 0 || messageObject.canStreamVideo() && autoDownload)) {
                                autoPlayingMedia = true;
                                if (!messageIdChanged) {
                                    photoImage.setCrossfadeWithOldImage(true);
                                    photoImage.setCrossfadeDuration(250);
                                }
                                if (localFile == 0 && videoSize != null && (currentPhotoObject == null || currentPhotoObjectThumb == null)) {
                                    photoImage.setImage(ImageLocation.getForDocument(document), ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForDocument(videoSize, documentAttach), null, ImageLocation.getForDocument(currentPhotoObject != null ? currentPhotoObject : currentPhotoObjectThumb, documentAttach), currentPhotoObject != null ? currentPhotoFilter : currentPhotoFilterThumb, currentPhotoObjectThumbStripped, document.size, null, messageObject, cacheType);
                                } else {
                                    if (isRoundVideo && !messageIdChanged && photoImage.hasStaticThumb()) {
                                        photoImage.setImage(ImageLocation.getForDocument(document), ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, null, null, photoImage.getStaticThumb(), document.size, null, messageObject, messageObject.isRoundOnce() ? cacheType : 0);
                                    } else {
                                        photoImage.setImage(ImageLocation.getForDocument(document), ImageLoader.AUTOPLAY_FILTER, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, document.size, null, messageObject, cacheType);
                                    }
                                }
                            } else if (localFile == 1) {
                                photoImage.setImage(ImageLocation.getForPath(messageObject.isSendError() ? null : messageObject.messageOwner.attachPath), null, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, messageObject, cacheType);
                            } else {
                                if (videoSize != null && (currentPhotoObject == null || currentPhotoObjectThumb == null)) {
                                    photoImage.setImage(ImageLocation.getForDocument(document), null, ImageLocation.getForDocument(videoSize, documentAttach), null, ImageLocation.getForDocument(currentPhotoObject != null ? currentPhotoObject : currentPhotoObjectThumb, documentAttach), currentPhotoObject != null ? currentPhotoFilter : currentPhotoFilterThumb, currentPhotoObjectThumbStripped, document.size, null, messageObject, cacheType);
                                } else {
                                    photoImage.setImage(ImageLocation.getForDocument(document), null, ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, document.size, null, messageObject, messageObject.isRoundOnce() ? cacheType : 0);
                                }
                            }
                        } else {
                            if (messageObject.videoEditedInfo != null && messageObject.type == MessageObject.TYPE_ROUND_VIDEO && !currentMessageObject.needDrawBluredPreview()) {
                                photoImage.setImage(ImageLocation.getForPath(messageObject.videoEditedInfo.originalPath), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, messageObject, 0);
                                photoImage.setMediaStartEndTime(currentMessageObject.videoEditedInfo.startTime / 1000, currentMessageObject.videoEditedInfo.endTime / 1000);
                            } else {
                                if (!messageIdChanged && !currentMessageObject.needDrawBluredPreview()) {
                                    photoImage.setCrossfadeWithOldImage(true);
                                    photoImage.setCrossfadeDuration(250);
                                }
                                photoImage.setImage(ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, messageObject, cacheType);
                            }
                        }
                    } else {
                        if (messageObject.videoEditedInfo != null && messageObject.type == MessageObject.TYPE_ROUND_VIDEO && !currentMessageObject.needDrawBluredPreview()) {
                            photoImage.setImage(ImageLocation.getForPath(messageObject.videoEditedInfo.originalPath), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, messageObject, cacheType);
                            photoImage.setMediaStartEndTime(currentMessageObject.videoEditedInfo.startTime / 1000, currentMessageObject.videoEditedInfo.endTime / 1000);
                        } else {
                            if (!messageIdChanged && !currentMessageObject.needDrawBluredPreview()) {
                                photoImage.setCrossfadeWithOldImage(true);
                                photoImage.setCrossfadeDuration(250);
                            }
                            photoImage.setImage(ImageLocation.getForObject(currentPhotoObject, photoParentObject), currentPhotoFilter, ImageLocation.getForObject(currentPhotoObjectThumb, photoParentObject), currentPhotoFilterThumb, currentPhotoObjectThumbStripped, 0, null, messageObject, cacheType);
                        }
                    }
                }
                clearBlurredImage(blurredPhotoImage);
                if (photoImage.getBitmap() != null && !photoImage.getBitmap().isRecycled() && (currentMessageObject.hasMediaSpoilers() && !currentMessageObject.isMediaSpoilersRevealed || fitPhotoImage)) {
                    blurredPhotoImage.setImageBitmap(Utilities.stackBlurBitmapMax(photoImage.getBitmap(), currentMessageObject.isRoundVideo()));
                    blurredPhotoImage.setColorFilter(getFancyBlurFilter());
                }
                setMessageObjectInternal(messageObject);

                if (drawForwardedName && messageObject.needDrawForwarded() && (currentPosition == null || currentPosition.minY == 0)) {
                    if (messageObject.type != MessageObject.TYPE_ROUND_VIDEO) {
                        namesOffset += dp(5);
                    }
                } else if (drawNameLayout && (messageObject.getReplyMsgId() == 0 || isThreadChat && messageObject.getReplyTopMsgId() == 0)) {
                    namesOffset += dp(7);
                }
                totalHeight = photoHeight + dp(14) + namesOffset + additionHeight;
                if (messageObject.isVoiceTranscriptionOpen()) {
                    totalHeight += dp(70 - 14);
                }
                if (currentPosition != null && (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0 && !currentMessageObject.isDocument() && currentMessageObject.type != MessageObject.TYPE_EMOJIS) {
                    totalHeight -= dp(3);
                }
                if (currentMessageObject.isDice()) {
                    totalHeight += dp(21);
                    additionalTimeOffsetY = dp(21);
                }

                int additionalTop = 0;
                if (currentPosition != null && !currentMessageObject.isDocument()) {
                    photoWidth += getAdditionalWidthForPosition(currentPosition);
                    if ((currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0) {
                        photoHeight += dp(4);
                        additionalTop -= dp(4);
                    }
                    if ((currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0) {
                        photoHeight += dp(1);
                    }
                } else if (currentPosition != null && currentMessageObject.isDocument()) {
                    if ((currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) != 0 && !messageObject.isOutOwner() && !drawPhotoImage) {
                        totalHeight -= dp(2);
                    }
                }
                if (captionLayout != null && captionAbove && (currentPosition == null || (currentPosition.flags & captionFlag()) != 0)) {
                    additionalTop = captionLayout.textHeight() + dp(8);
                }

                int y = 0;
                if (currentMessageObject.type != MessageObject.TYPE_EMOJIS) {
                    if (drawPinnedTop) {
                        namesOffset -= dp(documentAttachType == DOCUMENT_ATTACH_TYPE_DOCUMENT ? 2 : 1);
                    }
                    if (drawPinnedTop && !messageObject.isOutOwner()) {
                        totalHeight += dp(documentAttachType == DOCUMENT_ATTACH_TYPE_DOCUMENT ? 2 : 0);
                    }
//                    if (drawPinnedBottom && !messageObject.isOutOwner()) {
//                        totalHeight += dp(documentAttachType == DOCUMENT_ATTACH_TYPE_DOCUMENT ? 1 : 0);
//                    }
                    if (namesOffset > 0) {
                        y = dp(7);
                        totalHeight -= dp(2);
                    } else {
                        y = dp(5);
                        totalHeight -= dp(4);
                    }
                }
                if (currentPosition != null && currentMessagesGroup.isDocuments && currentMessagesGroup.messages.size() > 1) {
                    if ((currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0) {
                        totalHeight -= dp(drawPhotoImage ? 3 : 6);
                        mediaOffsetY -= dp(drawPhotoImage ? 3 : 6);
                        y -= dp(drawPhotoImage ? 3 : 6);
                    }
                    if ((currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0) {
                        totalHeight -= dp(drawPhotoImage ? 3 : 6);
                    }
                }
                if (messageObject.isRoundVideo() && messageObject.isVoiceTranscriptionOpen()) {
                    photoImage.setImageCoords(0, dp(13), dp(44), dp(44));
                } else {
                    photoImage.setImageCoords(0, y + namesOffset + additionalTop, photoWidth, photoHeight);
                }
                if (messageObject.hasMediaSpoilers() && SpoilerEffect2.supports()) {
                    if (mediaSpoilerEffect2 == null && attachedToWindow) {
                        mediaSpoilerEffect2 = makeSpoilerEffect();
                        if (mediaSpoilerEffect2Index != null) {
                            mediaSpoilerEffect2.reassignAttach(this, mediaSpoilerEffect2Index);
                        }
                    }
                } else {
                    if (mediaSpoilerEffect2 != null) {
                        mediaSpoilerEffect2.detach(this);
                        mediaSpoilerEffect2 = null;
                    }
                }
                invalidate();
            }

            if ((currentPosition == null || currentMessageObject.isMusic() || currentMessageObject.type == MessageObject.TYPE_PAID_MEDIA || currentMessageObject.isDocument()) && !messageObject.isSponsored() && !messageObject.isAnyKindOfSticker() && addedCaptionHeight == 0 && !messageObject.isExpiredStory() && !messageObject.isUnsupported()) {
                int addCaptionLayoutWidth = 0;
                int width = backgroundWidth;
                if ((currentMessageObject.type == MessageObject.TYPE_VOICE || isRoundVideo) && messageObject.isVoiceTranscriptionOpen() && messageObject.getFactCheck() == null) {
                    if (AndroidUtilities.isTablet()) {
                        width = AndroidUtilities.getMinTabletSide() - dp(50 + (isSideMenued ? ChatActivity.SIDE_MENU_WIDTH : drawAvatar ? 52 : 0));
                    } else {
                        width = getParentWidth() - dp(50 + (isSideMenued ? ChatActivity.SIDE_MENU_WIDTH : drawAvatar ? 52 : 0));
                    }
                }
                if (drawSideButton != 0 && isRoundVideo) {
                    width -= dp(24);
                }
                int widthForCaption = width - dp(31 + (currentMessageObject.type != MessageObject.TYPE_ROUND_VIDEO ? 14 : 0)) - getExtraTextX() * 2;
                if (!messageObject.isRestrictedMessage && captionLayout == null && (messageObject.caption != null || messageObject.isVoiceTranscriptionOpen())) {
                    currentCaption = messageObject.isVoiceTranscriptionOpen() ? messageObject.getVoiceTranscription() : messageObject.caption;
                    if (currentCaption != null && !TextUtils.isEmpty(messageObject.messageOwner.voiceTranscription) && currentMessageObject.isVoiceTranscriptionOpen() && !currentMessageObject.messageOwner.voiceTranscriptionFinal) {
                        currentCaption += " ";
                        if (!(currentCaption instanceof Spannable)) {
                            currentCaption = new SpannableString(currentCaption);
                        }
                        ((SpannableString) currentCaption).setSpan(new TranscribeButton.LoadingPointsSpan(), currentCaption.length() - 1, currentCaption.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    try {
                        captionFullWidth = widthForCaption;
                        captionLayout = new MessageObject.TextLayoutBlocks(getPrimaryMessageObject(), currentCaption, Theme.chat_msgTextPaint, widthForCaption);
                        captionLayout.bounceFrom(prevCaptionLayout);
                        updateSeekBarWaveformWidth(null);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                if (highlightCaptionToSetStart >= 0 && captionLayout != null) {
                    highlight(highlightCaptionToSetStart, highlightCaptionToSetEnd, captionLayout.textLayoutBlocks);
                }
                highlightCaptionToSetStart = highlightCaptionToSetEnd = -1;
                if ((currentMessageObject.type == MessageObject.TYPE_FILE || currentMessageObject.type == MessageObject.TYPE_VOICE) && currentPosition == null) {
                    totalHeight += layoutFactCheck(widthForCaption);
                    if (hasFactCheck) {
                        totalHeight += dp(6 + (captionLayout == null ? 18 : 8));
                    }
                }
                if (captionLayout != null || currentMessageObject.type == MessageObject.TYPE_VOICE || currentMessageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                    try {
                        if (messageObject.isVoiceTranscriptionOpen() && captionLayout != null) {
                            float startMaxWidth = backgroundWidth - dp(31) - dp(10) - getExtraTextX() * 2, maxWidth = startMaxWidth;
                            maxWidth = Math.max(maxWidth, captionLayout.textWidth);
                            backgroundWidth += maxWidth - startMaxWidth;
                        }
                        int width2 = backgroundWidth - dp(31);
                        Float lastCaptionLineWidth = null;
                        if (captionLayout != null) {
                            lastCaptionLineWidth = (float) captionLayout.lastLineWidth;
                        } else if (currentMessageObject.type == MessageObject.TYPE_VOICE) {
                            lastCaptionLineWidth = (float) dp(64);
                        }
                        if (lastCaptionLineWidth != null) {
                            if (captionLayout != null) {
                                captionWidth = width2;
                                captionHeight = captionLayout.textHeight();
                            }
                            totalHeight += captionHeight + (currentMessageObject.type != MessageObject.TYPE_POLL ? dp(9) : 0);
                            if (captionLayout != null && (captionLayout.hasCodeAtBottom || captionLayout.hasQuoteAtBottom)) {
                                captionHeight += dp(10);
                                totalHeight += dp(10);
                            }
                            if (currentMessageObject.type != MessageObject.TYPE_POLL && (reactionsLayoutInBubble.isEmpty || reactionsLayoutInBubble.isSmall) && (currentPosition == null || (currentPosition.flags & captionFlag()) != 0)) {
                                int timeWidthTotal = timeWidth + (messageObject.isOutOwner() ? dp(20) : 0) + getExtraTimeX();
                                if (width2 - dp(8) - lastCaptionLineWidth < timeWidthTotal || hasFactCheck) {
                                    totalHeight += dp(14);
                                    if (captionLayout != null) {
                                        captionHeight += dp(14);
                                        captionNewLine = 2;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
                if (captionLayout != null && currentMessageObject.type == MessageObject.TYPE_POLL) {
                    TLRPC.MessageMedia m = MessageObject.getMedia(messageObject.messageOwner);
                    if (pollButtons != null && m instanceof TLRPC.TL_messageMediaPoll) {
                        for (PollButton button : pollButtons) {
                            button.y += captionHeight;
                        }
                    }
                }
            }

            if (currentMessageObject.eventId != 0 && !currentMessageObject.isMediaEmpty() && MessageObject.getMedia(currentMessageObject.messageOwner).webpage != null) {
                int linkPreviewMaxWidth = backgroundWidth - dp(41);
                hasOldCaptionPreview = true;
                linkPreviewHeight = 0;
                TLRPC.WebPage webPage = MessageObject.getMedia(currentMessageObject.messageOwner).webpage;
                try {
                    int width = siteNameWidth = (int) Math.ceil(Theme.chat_replyNamePaint.measureText(webPage.site_name) + 1);
                    siteNameLayout = new StaticLayout(webPage.site_name, Theme.chat_replyNamePaint, Math.min(width, linkPreviewMaxWidth), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    siteNameLeft = siteNameLayoutWidth = 0;
                    for (int i = 0; i < siteNameLayout.getLineCount(); ++i) {
                        siteNameLeft = siteNameLayout.getLineLeft(i);
                        siteNameLayoutWidth = siteNameLayout.getLineWidth(i);
                    }
                    int height = siteNameLayout.getLineBottom(siteNameLayout.getLineCount() - 1);
                    linkPreviewHeight += height;
                    totalHeight += height;
                } catch (Exception e) {
                    FileLog.e(e);
                }

                try {
                    descriptionX = 0;
                    if (linkPreviewHeight != 0) {
                        totalHeight += dp(2);
                    }

                    descriptionLayout = StaticLayoutEx.createStaticLayout(webPage.description, Theme.chat_replyTextPaint, linkPreviewMaxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, dp(1), false, TextUtils.TruncateAt.END, linkPreviewMaxWidth, 6);

                    int height = descriptionLayout.getLineBottom(descriptionLayout.getLineCount() - 1);
                    linkPreviewHeight += height;
                    totalHeight += height;

                    boolean hasNonRtl = false;

                    for (int a = 0; a < descriptionLayout.getLineCount(); a++) {
                        int lineLeft = (int) Math.ceil(descriptionLayout.getLineLeft(a));
                        if (lineLeft != 0) {
                            if (descriptionX == 0) {
                                descriptionX = -lineLeft;
                            } else {
                                descriptionX = Math.max(descriptionX, -lineLeft);
                            }
                        } else {
                            hasNonRtl = true;
                        }
                    }
                    if (hasNonRtl) {
                        descriptionX = 0;
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }

                if (
                    messageObject.type == MessageObject.TYPE_PHOTO ||
                    messageObject.type == MessageObject.TYPE_VIDEO ||
                    messageObject.type == MessageObject.TYPE_EXTENDED_MEDIA_PREVIEW
                ) {
                    totalHeight += dp(6);
                }

                totalHeight += dp(17);
                if (captionNewLine != 0) {
                    totalHeight -= dp(14);
                    if (captionNewLine == 2) {
                        captionHeight -= dp(14);
                    }
                }
            }

            for (BotButton botButton : botButtons) {
                if (botButton.animatedEmojiDrawable != null) {
                    botButton.animatedEmojiDrawable.clear();
                }
            }
            botButtons.clear();
            if (messageIdChanged) {
                botButtonsByData.clear();
                botButtonsByPosition.clear();
                botButtonsLayout = null;
            }

            drawStartBotTopic = false;
            drawContinueBotTopic = false;

            final BotInlineKeyboard.Source inlineButtons;
            if (lastInChatList && isAllChats && isBotForum && !isPinnedChat && lastSendState == 0) {
                if (messageObject.getTopicId() > 0) {
                    BotInlineKeyboard.Builder b = new BotInlineKeyboard.Builder();
                    b.addKeyboardSource(messageObject.getInlineBotButtons());
                    b.addSeparator();
                    b.addContinueThreadKeyboard();
                    inlineButtons = b.build();
                    drawContinueBotTopic = true;
                } else {
                    inlineButtons = messageObject.getInlineBotButtons();
                }
                drawStartBotTopic = true;
            } else {
                inlineButtons = messageObject.getInlineBotButtons();
            }

            final int separatorHeight = dp(4 + 4);
            if (!messageObject.isRestrictedMessage && !messageObject.isRepostPreview && (currentPosition == null || currentMessagesGroup != null && currentMessagesGroup.isDocuments && currentPosition.last) && (inlineButtons != null) && !messageObject.hasExtendedMedia()) {
                int rows, separators;

                if (inlineButtons != null) {
                    rows = inlineButtons.getRowsCount();
                    separators = 0;
                    for (int a = 0; a < rows; a++) {
                        if (inlineButtons.hasSeparator(a)) {
                            separators++;
                        }
                    }
                } else {
                    rows = 1;
                    separators = 0;
                }
                substractBackgroundHeight = keyboardHeight = dp(44 + 4) * rows + dp(1) + separatorHeight * separators;
                widthForButtons = backgroundWidth - dp(mediaBackground ? 0 : 9);
                boolean fullWidth = false;
                if (messageObject.wantedBotKeyboardWidth > widthForButtons) {
                    int maxButtonWidth = -dp(10 + (isSideMenued ? ChatActivity.SIDE_MENU_WIDTH : drawAvatar ? 52 : 0));
                    if (AndroidUtilities.isTablet()) {
                        maxButtonWidth += AndroidUtilities.getMinTabletSide();
                    } else {
                        maxButtonWidth += Math.min(getParentWidth(), AndroidUtilities.displaySize.y) - dp(5);
                    }
                    widthForButtons = Math.max(backgroundWidth, Math.min(messageObject.wantedBotKeyboardWidth, maxButtonWidth));
                }

                int maxButtonsWidth = 0;
                HashMap<String, BotButton> oldByData = new HashMap<>(botButtonsByData);
                HashMap<String, BotButton> oldByPosition;
                if (messageObject.botButtonsLayout != null && botButtonsLayout != null && botButtonsLayout.equals(messageObject.botButtonsLayout.toString())) {
                    oldByPosition = new HashMap<>(botButtonsByPosition);
                } else {
                    if (messageObject.botButtonsLayout != null) {
                        botButtonsLayout = messageObject.botButtonsLayout.toString();
                    }
                    oldByPosition = null;
                }
                botButtonsByData.clear();
                if (inlineButtons != null) {
                    int separatorsHeight = 0;
                    for (int row = 0; row < rows; row++) {
                        int buttonsCount = inlineButtons.getColumnsCount(row);
                        if (buttonsCount == 0) {
                            continue;
                        }
                        int buttonWidth = (widthForButtons - dp(5) * (buttonsCount - 1) - dp(2)) / buttonsCount;
                        for (int column = 0; column < buttonsCount; column++) {
                            BotInlineKeyboard.Button inlineButton = inlineButtons.getButton(row, column);
                            BotButton botButton = new BotButton(this::invalidateOutbounds);
                            botButton.buttonImpl = inlineButton;
                            if (inlineButton instanceof BotInlineKeyboard.ButtonBot) {
                                botButton.button = ((BotInlineKeyboard.ButtonBot) inlineButton).button;
                            } else if (inlineButton instanceof BotInlineKeyboard.ButtonCustom) {
                                botButton.buttonCustom = (BotInlineKeyboard.ButtonCustom) inlineButton;
                                if (currentMessageObject != null && ChatObject.canManageMonoForum(currentAccount, currentMessageObject.getDialogId()) && !ChatObject.canUserDoChannelDirectAdminAction(currentAccount, currentMessageObject.getDialogId(), ChatObject.ACTION_POST) && (botButton.buttonCustom.id == BotInlineKeyboard.ButtonCustom.SUGGESTION_ACCEPT || botButton.buttonCustom.id == BotInlineKeyboard.ButtonCustom.SUGGESTION_EDIT)) {
                                    botButton.isLocked = true;
                                }
                            }

                            final long emojiId = inlineButton.getIconEmoji();
                            final int iconRes = inlineButton.getIconRes();
                            if (emojiId != 0) {
                                botButton.animatedEmojiDrawable = new AnimatedEmojiDrawable(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, currentAccount, emojiId);
                                botButton.animatedEmojiDrawable.addView(this::invalidateOutbounds);
                                botButton.animatedEmojiDrawable.setColorFilter(new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_IN));
                            } else if (iconRes != 0) {
                                botButton.iconDrawable = getResources().getDrawable(iconRes);
    ) || useTranscribeButton) ? dp(48) : 0);
        int left = getBackgroundDrawableLeft() - avatarWidth - shareButtonWidth;
        if (botButtons != null) {
            int buttonMostLeft = Integer.MAX_VALUE;
            int addX;
            final int widthForButtons = getWidthForButtons();
            if (currentMessageObject != null && currentMessageObject.isOutOwner()) {
                addX = getMeasuredWidth() - widthForButtons - dp(10);
            } else {
                addX = backgroundDrawableLeft + dp(mediaBackground || drawPinnedBottom ? 1 : 7);
            }
            for (int i = 0; i < botButtons.size(); ++i) {
                BotButton btn = botButtons.get(i);
                buttonMostLeft = Math.max(buttonMostLeft, addX + (int) (btn.x * widthForButtons));
            }
            left = Math.min(left, buttonMostLeft);
        }
        if (starsPriceText != null) {
            left = Math.min(left, (int) (getParentWidth() - starsPriceText.getWidth() - dp(18)) / 2);
        }
        if (topicSeparator != null) {
            left = Math.min(sideMenuWidth, left);
        }
        return Math.max(0, left);
    }

    @Override
    public int getBoundsRight() {
        boolean isIn = currentMessageObject != null && !currentMessageObject.isOutOwner();
        int shareButtonWidth = (isIn && (checkNeedDrawShareButton(currentMessageObject) || useTranscribeButton) ? dp(48) : 0);
        int right = getBackgroundDrawableRight() + shareButtonWidth;
        if (botButtons != null) {
            int buttonMostRight = 0;
            int addX;
            final int widthForButtons = getWidthForButtons();
            if (currentMessageObject != null && currentMessageObject.isOutOwner()) {
                addX = getMeasuredWidth() - getWidthForButtons() - dp(10);
            } else {
                addX = backgroundDrawableLeft + dp(mediaBackground || drawPinnedBottom ? 1 : 7);
            }
            for (int i = 0; i < botButtons.size(); ++i) {
                BotButton btn = botButtons.get(i);
                buttonMostRight = Math.max(buttonMostRight, addX + (int) (btn.x * widthForButtons) + (int) (btn.width * widthForButtons));
            }
            right = Math.max(right, buttonMostRight);
        }
        if (starsPriceText != null) {
            right = Math.max(right, (int) (getParentWidth() + starsPriceText.getWidth() + dp(18)) / 2);
        }
        if (topicSeparator != null) {
            right = Math.max(right, getWidth());
        }
        return right;
    }

    @SuppressLint("WrongCall")
    @Override
    protected void onDraw(Canvas canvas) {
        drawInternal(canvas);
    }
    public void drawInternal(Canvas canvas) {
        if (currentMessageObject == null) {
            return;
        }
        if (!wasLayout) {
            onLayout(false, getLeft(), getTop(), getRight(), getBottom());
        }
        if (enterTransitionInProgress && currentMessageObject.isAnimatedEmojiStickers()) {
            return;
        }

        if (channelRecommendationsCell != null && currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL) {
            if (delegate == null || delegate.canDrawOutboundsContent()) {
                channelRecommendationsCell.draw(canvas);
            }
            transitionParams.recordDrawingState();
            return;
        }

        final int sponosoredAlpha = (int) (255 * (1f - isSponsoredMessageHidden.getFloatValue()));
        if (sponosoredAlpha == 0) {
            return;
        }

        final int restore = canvas.getSaveCount();
        int restoreToSponosoredAlpha = -1;
        if (sponosoredAlpha != 255) {
            restoreToSponosoredAlpha = canvas.saveLayerAlpha(0, 0, getMeasuredWidth(), getMeasuredHeight(), sponosoredAlpha);
        }

        setupTextColors();

        if (getStarsPriceTopPadding() + suggestionOfferTopPadding + getTopicSeparatorTopPadding() > 0) {
            canvas.save();
            canvas.translate(0, getStarsPriceTopPadding() + suggestionOfferTopPadding + getTopicSeparatorTopPadding());
        }

        if (isWidthAdaptive()) {
            canvas.save();
            canvas.translate(-getBoundsLeft(), 0);
        }

        drawBackgroundInternal(canvas, false);
        if (isHighlightedAnimated) {
            long newTime = System.currentTimeMillis();
            long dt = Math.abs(newTime - lastHighlightProgressTime);
            if (dt > 17) {
                dt = 17;
            }
            highlightProgress -= dt;
            lastHighlightProgressTime = newTime;
            if (highlightProgress <= 0) {
                highlightProgress = 0;
                isHighlightedAnimated = false;
                if (highlightedQuote) {
                    resetUrlPaths();
                }
                highlightedQuote = false;
            }
            invalidate();
            if (getParent() != null) {
                ((View) getParent()).invalidate();
            }
        }

        if (alphaInternal != 1.0f) {
            int top = 0;
            int left = 0;
            int bottom = getMeasuredHeight();
            int right = getMeasuredWidth();

            if (currentBackgroundDrawable != null) {
                top = currentBackgroundDrawable.getBounds().top;
                bottom = currentBackgroundDrawable.getBounds().bottom;
                left = currentBackgroundDrawable.getBounds().left;
                right = currentBackgroundDrawable.getBounds().right;
            }

            if (drawSideButton != 0) {
                if (currentMessageObject.isOutOwner()) {
                    left -= dp(8 + 32);
                } else {
                    right += dp(8 + 32);
                }
            }
            if (getY() < 0) {
                top = (int) -getY();
            }
            if (getY() + getMeasuredHeight() > parentHeight) {
                bottom = (int) (parentHeight - getY());
            }
            rect.set(left, top, right, bottom);
            canvas.saveLayerAlpha(rect, (int) (255 * alphaInternal), Canvas.ALL_SAVE_FLAG);
        }
        boolean clipContent = false;
        if (transitionParams.animateBackgroundBoundsInner && currentBackgroundDrawable != null && !isRoundVideo && (currentMessageObject == null || !currentMessageObject.sendPreview)) {
            Rect r = currentBackgroundDrawable.getBounds();
            canvas.save();
            canvas.clipRect(
                    r.left + dp(4), r.top + dp(4),
                    r.right - dp(4), r.bottom - dp(4)
            );
            clipContent = true;
        }
        drawContent(canvas, false);

        if (expiredStoryView != null && expiredStoryView.visible) {
            expiredStoryView.draw(canvas, this);
        }

        if (clipContent) {
            canvas.restore();
        }

        if (replyNameLayout != null) {
            float replyTextHeight = this.replyTextHeight;
            if (transitionParams != null && transitionParams.animateReplyTextLayout != null) {
                replyTextHeight = AndroidUtilities.lerp(transitionParams.animateFromReplyTextHeight, replyTextHeight, transitionParams.animateChangeProgress);
            }
            replyHeight = dp(9) + Theme.chat_replyNamePaint.getTextSize() + Math.max(replyTextHeight - dp(3.66f), Theme.chat_replyTextPaint.getTextSize());
            if (currentMessageObject.shouldDrawWithoutBackground() && currentMessageObject.type != MessageObject.TYPE_EMOJIS && !isSideMenued) {
                if (currentMessageObject.isOutOwner()) {
                    replyStartX = dp(23);
                    if (isPlayingRound) {
                        replyStartX -= AndroidUtilities.roundPlayingMessageSize(isSideMenued) - AndroidUtilities.roundMessageSize;
                    }
                } else if (currentMessageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                    replyStartX = backgroundDrawableLeft + backgroundDrawableRight + dp(4);
                } else {
                    replyStartX = backgroundDrawableLeft + backgroundDrawableRight + dp(17);
                }
                if (drawForwardedName) {
                    forwardHeight = dp(4) + (int) Theme.chat_forwardNamePaint.getTextSize() * 2;
                    replyStartY = forwardNameY + forwardHeight + dp(6);
                } else {
                    replyStartY = dp(12);
                }
            } else {
                if (currentMessageObject.isOutOwner()) {
                    replyStartX = backgroundDrawableLeft + dp(12) + getExtraTextX();
                    if (currentMessageObject.type == MessageObject.TYPE_EMOJIS) {
                        replyStartX -= Math.max(0, replyStartX + Math.max(replyNameWidth, replyTextWidth) + dp(14) - AndroidUtilities.displaySize.x);
                    }
                } else {
                    if (mediaBackground) {
                        replyStartX = backgroundDrawableLeft + dp(12) + getExtraTextX();
                    } else {
                        replyStartX = backgroundDrawableLeft + dp(drawPinnedBottom ? 12 : 18) + getExtraTextX();
                    }
                }
                if (currentMessageObject.type == MessageObject.TYPE_EMOJIS) {
                    replyStartX -= dp(7);
                }
                forwardHeight = dp(4) + (int) Theme.chat_forwardNamePaint.getTextSize() * 2;
                replyStartY = dp(12) + (int) (drawNameLayout && nameLayout != null ? getNameHeightAnimated() + dp(1) : 0) + (drawForwardedName && forwardedNameLayout[0] != null ? dp(4) + forwardHeight : 0);
            }
            replyStartY += dp(.66f);
            if (ephemeralLayout != null) {
                replyStartY += dp(EPHEMERAL_HINT_HEIGHT - 4);
            }
        }
        if (drawSummaryReply) {
            if (currentMessageObject.isOutOwner()) {
                summaryStartX = backgroundDrawableLeft + dp(12) + getExtraTextX();
            } else {
                if (mediaBackground) {
                    summaryStartX = backgroundDrawableLeft + dp(12) + getExtraTextX();
                } else {
                    summaryStartX = backgroundDrawableLeft + dp(drawPinnedBottom ? 12 : 18) + getExtraTextX();
                }
            }
            summaryStartY = dp(12) + (int) (drawNameLayout && nameLayout != null ? getNameHeightAnimated() + dp(1) : 0) + (drawForwardedName && forwardedNameLayout[0] != null ? dp(4) + forwardHeight : 0) + (int) (replyNameLayout != null ? replyHeight + dp(12) : 0);
        }
        if (currentPosition == null && !transitionParams.animateBackgroundBoundsInner && !(enterTransitionInProgress && !currentMessageObject.isVoice())) {
            drawNamesLayout(canvas, 1f);
        }

        if ((!autoPlayingMedia || !MediaController.getInstance().isPlayingMessageAndReadyToDraw(currentMessageObject) || isRoundVideo) && !transitionParams.animateBackgroundBoundsInner && !(currentMessageObject != null && currentMessageObject.preview)) {
            drawOverlays(canvas);
        }
        if ((drawTime || !mediaBackground) && !forceNotDrawTime && !transitionParams.animateBackgroundBoundsInner && !(enterTransitionInProgress && !currentMessageObject.isVoice()) && (!currentMessageObject.isQuickReply() || currentMessageObject.isSendError())) {
            drawTime(canvas, 1f, false);
        }

        if ((controlsAlpha != 1.0f || timeAlpha != 1.0f) && currentMessageObject.type != MessageObject.TYPE_ROUND_VIDEO) {
            long newTime = System.currentTimeMillis();
            long dt = Math.abs(lastControlsAlphaChangeTime - newTime);
            if (dt > 17) {
                dt = 17;
            }
            totalChangeTime += dt;
            if (totalChangeTime > TIME_APPEAR_MS) {
                totalChangeTime = TIME_APPEAR_MS;
            }
            lastControlsAlphaChangeTime = newTime;
            if (controlsAlpha != 1.0f) {
                controlsAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(totalChangeTime / (float) TIME_APPEAR_MS);
            }
            if (timeAlpha != 1.0f) {
                timeAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation(totalChangeTime / (float) TIME_APPEAR_MS);
            }
            invalidate();
            if (forceNotDrawTime && currentPosition != null && currentPosition.last && getParent() != null) {
                View parent = (View) getParent();
                parent.invalidate();
            }
        }

        if ((drawBackground || transitionParams.animateDrawBackground) && shouldDrawSelectionOverlay() && currentMessagesGroup == null && hasSelectionOverlay()) {
            if (selectionOverlayPaint == null) {
                selectionOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }
            selectionOverlayPaint.setColor(getSelectionOverlayColor());
            int wasAlpha = selectionOverlayPaint.getAlpha();
            selectionOverlayPaint.setAlpha((int) (wasAlpha * getHighlightAlpha() * getAlpha()));
            if (selectionOverlayPaint.getAlpha() > 0) {
                canvas.save();
                canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight());
                currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams, selectionOverlayPaint);
                canvas.restore();
            }
            selectionOverlayPaint.setAlpha(wasAlpha);
        }

        if (delegate == null || delegate.canDrawOutboundsContent() || transitionParams.messageEntering || getAlpha() != 1f) {
            drawOutboundsContent(canvas);
        }
        updateSelectionTextPosition();

        if (restoreToSponosoredAlpha != -1) {
            canvas.restoreToCount(restoreToSponosoredAlpha);
        }

        canvas.restoreToCount(restore);
    }

    public void drawBackgroundInternal(Canvas canvas, boolean fromParent) {
        drawBackgroundInternal(canvas, fromParent, false);
    }

    @SuppressLint("WrongCall")
    private boolean drawBackgroundInternal(Canvas canvas, boolean fromParent, boolean internal) {
        if (currentMessageObject == null) {
            return false;
        }
        if (!wasLayout && !animationRunning) {
            forceLayout();
            return false;
        }
        if (!wasLayout || forcedLayout) {
            onLayout(false, getLeft(), getTop(), getRight(), getBottom());
        }



        final boolean hasUnsupportedBlocks = currentMessageObject.richLayout != null && currentMessageObject.richLayout.hasUnsupportedBlocks();
        final boolean hasRootUnsupportedBlocks = currentMessageObject.richLayout != null && currentMessageObject.richLayout.hasRootUnsupportedBlocks();
        ArrayList<RichMessageLayout.RichUnsupportedBlock> holes = null;
        ArrayList<RichMessageLayout.RichUnsupportedBlock> holesRoot = null;
        if (!internal && hasUnsupportedBlocks) {
            holes = currentMessageObject.richLayout.getUnsupportedHoles();
            holesRoot = currentMessageObject.richLayout.getUnsupportedHolesRoot();
        }

        if (!internal && hasRootUnsupportedBlocks && holes != null && !holes.isEmpty()) {
            //if (transitionParams != null && transitionParams.animateChange || currentMessageObject.richLayout.blockquoteAnimating || currentMessageObject.richLayout.detailsAnimating) {
                layoutTextXY(false);
            //}

            final int textY = this.textY;

            boolean result = false;
            int bY = 0;
            for (int a = 0, N = holesRoot.size(); a <= N; a++) {
                RichMessageLayout.RichUnsupportedBlock hole = a < N ? holesRoot.get(a) : null;
                canvas.save();
                if (hole != null) {
                    canvas.clipRect(0, a == 0 ? Integer.MIN_VALUE : textY + bY, getWidth(), textY + (int) hole.getY(transitionParams));
                    result |= drawBackgroundInternal(canvas, fromParent, true);
                    bY = (int) (hole.getY(transitionParams) + hole.getHeight(transitionParams));
                } else {
                    canvas.clipRect(0, textY + bY, getWidth(), Integer.MAX_VALUE);
                    result |= drawBackgroundInternal(canvas, fromParent, true);
                }
                canvas.restore();
            }

            if (!result) {
                return false;
            }

            float backgroundX;
            if (currentMessageObject.isOutOwner()) {
                backgroundX = layoutWidth - backgroundWidth + dp(11);
            } else {
                if (isSideMenuEnabled) {
                    backgroundX = dp(20 + ChatActivity.SIDE_MENU_WIDTH);
                } else if (needDrawAvatar()) {
                    backgroundX = dp(20 + 48);
                } else {
                    backgroundX = dp(20);
                }
            }
            if (transitionParams.animateBackgroundBoundsInner) {
                backgroundX += transitionParams.deltaLeft;
            }
            float bwidth = backgroundWidth;
            if (transitionParams.animateBackgroundBoundsInner) {
                bwidth += transitionParams.deltaRight - transitionParams.deltaLeft;
            }

            final int backgroundLeft = (int) (backgroundX - dp(9));
            final int backgroundRight = (int) (backgroundX + bwidth - dp(22));

            final Paint paint;
            if (isDrawSelectionBackground()) {
                if (currentBackgroundSelectedDrawable != null) {
                    paint = currentBackgroundSelectedDrawable.getPaint();
                } else {
                    return result;
                }
            } else {
                if (currentBackgroundDrawable != null) {
                    paint = currentBackgroundDrawable.getPaint();
                } else {
                    return result;
                }
            }

            if (drawBackground) {
                for (int a = 0, N = holes.size(); a < N; a++) {
                    final RichMessageLayout.RichUnsupportedBlock hole = holes.get(a);
                    final float t = textY + hole.getY(transitionParams);
                    final float b = t + hole.getHeight(transitionParams);
                    if (hole.level == 0) {
                        final int tornWidth = backgroundRight - backgroundLeft;
                        if (hole.tornBitmap == null || hole.tornBitmap.getWidth() < tornWidth) {
                            hole.tornParams = new TornEdge.Params();
                            hole.tornBitmap = TornEdge.createTearBitmap(hole.tornParams, tornWidth,
                                    currentMessageObject.getId() * 100 + hole.index);
                        }
                        canvas.save();
                        canvas.clipRect(backgroundLeft, 0, backgroundRight, getHeight());
                        TornEdge.drawTopEdge(canvas, hole.tornBitmap, hole.tornParams, tornWidth, backgroundLeft, t, paint);
                        TornEdge.drawBottomEdge(canvas, hole.tornBitmap, hole.tornParams, tornWidth, backgroundLeft, b, paint);
                        canvas.restore();
                    }
                    AndroidUtilities.rectTmp.set(
                        backgroundLeft + (hole.padding.left + dp(7)) + dp(3.33f), t + hole.padding.top,
                        backgroundRight - (hole.padding.right + dp(7)) - dp(3.33f), b - hole.padding.bottom);
                    drawServiceBackground(canvas, AndroidUtilities.rectTmp, dp(18), 1f);
                }
            }

            return result;
        }



        Drawable currentBackgroundShadowDrawable;
        int additionalTop = 0;
        int additionalBottom = 0;
        boolean forceMediaByGroup = currentPosition != null && (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0 && currentMessagesGroup.isDocuments && !drawPinnedBottom;
        if (currentMessageObject.isOutOwner()) {
            if (transitionParams.changePinnedBottomProgress >= 1 && !mediaBackground && !drawPinnedBottom && !forceMediaByGroup) {
                currentBackgroundDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgOut);
                currentBackgroundSelectedDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgOutSelected);
                transitionParams.drawPinnedBottomBackground = false;
            } else {
                currentBackgroundDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgOutMedia);
                currentBackgroundSelectedDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgOutMediaSelected);
                transitionParams.drawPinnedBottomBackground = true;
            }
            setBackgroundTopY(true);
            if (isDrawSelectionBackground() && (currentPosition == null || getBackground() != null)) {
                currentBackgroundShadowDrawable = currentBackgroundSelectedDrawable.getShadowDrawable();
            } else {
                currentBackgroundShadowDrawable = currentBackgroundDrawable.getShadowDrawable();
            }
            backgroundDrawableLeft = layoutWidth - backgroundWidth - (!mediaBackground ? 0 : dp(9));
            backgroundDrawableRight = backgroundWidth - (mediaBackground ? 0 : dp(3));
            if (currentMessagesGroup != null && !currentMessagesGroup.isDocuments) {
                if (!currentPosition.edge) {
                    backgroundDrawableRight += dp(10);
                }
            }
            int backgroundLeft = backgroundDrawableLeft;
            if (!forceMediaByGroup && transitionParams.changePinnedBottomProgress != 1) {
                if (!mediaBackground) {
                    backgroundDrawableRight -= dp(6);
                }
            } else if (!mediaBackground && drawPinnedBottom) {
                backgroundDrawableRight -= dp(6);
            }

            if (currentPosition != null) {
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) == 0) {
                    backgroundDrawableRight += dp(SharedConfig.bubbleRadius + 2);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) == 0) {
                    backgroundLeft -= dp(SharedConfig.bubbleRadius + 2);
                    backgroundDrawableRight += dp(SharedConfig.bubbleRadius + 2);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0) {
                    additionalTop -= dp(SharedConfig.bubbleRadius + 3);
                    additionalBottom += dp(SharedConfig.bubbleRadius + 3);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0) {
                    additionalBottom += dp(SharedConfig.bubbleRadius + 3);
                }
            }
            int offsetBottom;
            if (drawPinnedBottom && drawPinnedTop) {
                offsetBottom = 0;
            } else if (drawPinnedBottom) {
                offsetBottom = dp(1);
            } else {
                offsetBottom = dp(2);
            }
            backgroundDrawableTop = additionalTop + (drawPinnedTop ? 0 : dp(1));
            int backgroundHeight = layoutHeight - offsetBottom + additionalBottom;
            backgroundDrawableBottom = backgroundDrawableTop + backgroundHeight;

            if (!mediaBackground) {
                if (drawPinnedTop) {
                    backgroundDrawableTop -= dp(1);
                    backgroundHeight += dp(1);
                }
                if (drawPinnedBottom) {
                    backgroundDrawableBottom += dp(1);
                    backgroundHeight += dp(1);
                }
            }

            if (forceMediaByGroup) {
                setDrawableBoundsInner(currentBackgroundDrawable, backgroundLeft, backgroundDrawableTop - additionalTop, backgroundDrawableRight, backgroundHeight - additionalBottom + 10);
                setDrawableBoundsInner(currentBackgroundSelectedDrawable, backgroundDrawableLeft, backgroundDrawableTop, backgroundDrawableRight - dp(6), backgroundHeight);
            } else {
                setDrawableBoundsInner(currentBackgroundDrawable, backgroundLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
                setDrawableBoundsInner(currentBackgroundSelectedDrawable, backgroundLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
            }
            setDrawableBoundsInner(currentBackgroundShadowDrawable, backgroundLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
        } else {
            if (transitionParams.changePinnedBottomProgress >= 1 && !mediaBackground && !drawPinnedBottom && !forceMediaByGroup) {
                currentBackgroundDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgIn);
                currentBackgroundSelectedDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgInSelected);
                transitionParams.drawPinnedBottomBackground = false;
            } else {
                currentBackgroundDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgInMedia);
                currentBackgroundSelectedDrawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgInMediaSelected);
                transitionParams.drawPinnedBottomBackground = true;
            }
            setBackgroundTopY(true);
            if (isDrawSelectionBackground() && (currentPosition == null || getBackground() != null)) {
                currentBackgroundShadowDrawable = currentBackgroundSelectedDrawable.getShadowDrawable();
            } else {
                currentBackgroundShadowDrawable = currentBackgroundDrawable.getShadowDrawable();
            }

            backgroundDrawableLeft = dp(isSideMenuEnabled ? ChatActivity.SIDE_MENU_WIDTH : (isChat || currentMessageObject != null && (currentMessageObject.isRepostPreview || currentMessageObject.forceAvatar || currentMessageObject.messageOwner.guestchat_via_from != null) || currentMessageObject.getDialogId() == UserObject.VERIFY) && isAvatarVisible ? 48 : 0) + dp(!mediaBackground ? 3 : 9);
            backgroundDrawableRight = backgroundWidth - (mediaBackground ? 0 : dp(3));
            if (currentMessagesGroup != null && !currentMessagesGroup.isDocuments) {
                if (!currentPosition.edge) {
                    backgroundDrawableLeft -= dp(10);
                    backgroundDrawableRight += dp(10);
                }
                if (currentPosition.leftSpanOffset != 0) {
                    backgroundDrawableLeft += (int) Math.ceil(currentPosition.leftSpanOffset / 1000.0f * getGroupPhotosWidth());
                }
            }
            if ((!mediaBackground && drawPinnedBottom) || !forceMediaByGroup && transitionParams.changePinnedBottomProgress != 1) {
                if (!(!drawPinnedBottom && mediaBackground)) {
                    backgroundDrawableRight -= dp(6);
                }
                if (!mediaBackground) {
                    backgroundDrawableLeft += dp(6);
                }
            }
            if (currentPosition != null) {
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) == 0) {
                    backgroundDrawableRight += dp(SharedConfig.bubbleRadius + 2);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) == 0) {
                    backgroundDrawableLeft -= dp(SharedConfig.bubbleRadius + 2);
                    backgroundDrawableRight += dp(SharedConfig.bubbleRadius + 2);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_TOP) == 0) {
                    additionalTop -= dp(SharedConfig.bubbleRadius + 3);
                    additionalBottom += dp(SharedConfig.bubbleRadius + 3);
                }
                if ((currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) == 0) {
                    additionalBottom += dp(SharedConfig.bubbleRadius + 4);
                }
            }
            int offsetBottom;
            if (drawPinnedBottom && drawPinnedTop) {
                offsetBottom = 0;
            } else if (drawPinnedBottom) {
                offsetBottom = dp(1);
            } else {
                offsetBottom = dp(2);
            }
            backgroundDrawableTop = additionalTop + (drawPinnedTop ? 0 : dp(1));
            int backgroundHeight = layoutHeight - offsetBottom + additionalBottom;
            backgroundDrawableBottom = backgroundDrawableTop + backgroundHeight;

            if (!mediaBackground) {
                if (drawPinnedTop) {
                    backgroundDrawableTop -= dp(1);
                    backgroundHeight += dp(1);
                }
                if (drawPinnedBottom) {
                    backgroundDrawableBottom += dp(1);
                    backgroundHeight += dp(1);
                }
            }

            setDrawableBoundsInner(currentBackgroundDrawable, backgroundDrawableLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
            if (forceMediaByGroup) {
                setDrawableBoundsInner(currentBackgroundSelectedDrawable, backgroundDrawableLeft + dp(6), backgroundDrawableTop, backgroundDrawableRight - dp(6), backgroundHeight);
            } else {
                setDrawableBoundsInner(currentBackgroundSelectedDrawable, backgroundDrawableLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
            }
            setDrawableBoundsInner(currentBackgroundShadowDrawable, backgroundDrawableLeft, backgroundDrawableTop, backgroundDrawableRight, backgroundHeight);
        }

        if (!currentMessageObject.isOutOwner() && transitionParams.changePinnedBottomProgress != 1 && !mediaBackground && !drawPinnedBottom) {
            backgroundDrawableLeft -= dp(6);
            backgroundDrawableRight += dp(6);
        }

        if (hasPsaHint) {
            int x;
            if (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_RIGHT) != 0) {
                x = currentBackgroundDrawable.getBounds().right;
            } else {
                x = 0;
                int dWidth = getGroupPhotosWidth();
                for (int a = 0; a < currentMessagesGroup.posArray.size(); a++) {
                    MessageObject.GroupedMessagePosition position = currentMessagesGroup.posArray.get(a);
                    if (position.minY == 0) {
                        x += Math.ceil((position.pw + position.leftSpanOffset) / 1000.0f * dWidth);
                    } else {
                        break;
                    }
                }
            }
            Drawable drawable = Theme.chat_psaHelpDrawable[currentMessageObject.isOutOwner() ? 1 : 0];

            int y;
            if (currentMessageObject.type == MessageObject.TYPE_ROUND_VIDEO) {
                y = dp(12);
            } else {
                y = dp(10 + (drawNameLayout ? 19 : 0));
            }

            psaHelpX = x - drawable.getIntrinsicWidth() - dp(currentMessageObject.isOutOwner() ? 20 : 14);
            psaHelpY = y + dp(4);
        }

        if (checkBoxVisible || checkBoxAnimationInProgress) {
            animateCheckboxTranslation();
            int size = dp(21);
            checkBox.setBounds(dp(8 - 35) + checkBoxTranslation, currentBackgroundDrawable.getBounds().bottom - dp(8) - size, size, size);
        }

        if (!fromParent && drawBackgroundInParent()) {
            return false;
        }

        int restoreCount = canvas.getSaveCount();
        if (transitionYOffsetForDrawables != 0) {
            canvas.save();
            canvas.translate(0, transitionYOffsetForDrawables);
        }

        float pinnedBottomOffset = 0;
        if (currentMessageObject != null && currentMessageObject.isRoundVideo()) {
            float progress = getVideoTranscriptionProgress();
//            if (transitionParams.animateDrawBackground) {
//                Rect bounds = currentBackgroundDrawable.getBounds();
//                bounds.bottom = AndroidUtilities.lerp(bounds.top + bounds.width(), bounds.bottom, progress);
//            }
            currentBackgroundDrawable.setRoundingRadius(1f - progress);
            pinnedBottomOffset = AndroidUtilities.lerp(backgroundWidth / 2, 0, progress);
            canvas.saveLayerAlpha(0, 0, getWidth(), Math.max(currentBackgroundDrawable.getBounds().bottom, getHeight()), (int) (255 * progress), Canvas.ALL_SAVE_FLAG);

            roundVideoPlayPipFloat.set((MediaController.getInstance().isPiPShown() && MediaController.getInstance().isPlayingMessageAndReadyToDraw(currentMessageObject) || wouldBeInPip) && canvas.isHardwareAccelerated() ? 1f : 0f);
            if (MediaController.getInstance().isPiPShown()) {
                wouldBeInPip = false;
            }
        } else {
            roundVideoPlayPipFloat.set(0, true);
        }

        if ((drawBackground || transitionParams.animateDrawBackground) && currentBackgroundDrawable != null && (currentPosition == null || isDrawSelectionBackground() && (currentMessageObject.isMusic() || currentMessageObject.isDocument())) && !(enterTransitionInProgress && !currentMessageObject.isVoice())) {
            float alphaInternal = this.alphaInternal;
            if (fromParent) {
                alphaInternal *= getAlpha();
            }
            if (hasSelectionOverlay()) {
//                if ((isPressed() && isCheckPressed || !isCheckPressed && isPressed) && !textIsSelectionMode()) {
//                    currentSelectedBackgroundAlpha = 1f;
//                    currentBackgroundSelectedDrawable.setAlpha((int) (255 * alphaInternal));
//                    currentBackgroundSelectedDrawable.drawCached(canvas, backgroundCacheParams);
//                } else {
                currentSelectedBackgroundAlpha = 0;
                currentBackgroundDrawable.setAlpha((int) (255 * alphaInternal));
                currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams);
//                }
                if (currentBackgroundShadowDrawable != null && currentPosition == null) {
                    currentBackgroundShadowDrawable.setAlpha((int) (255 * alphaInternal));
                    currentBackgroundShadowDrawable.draw(canvas);
                }
            } else {
                if (isHighlightedAnimated) {
                    currentBackgroundDrawable.setAlpha((int) (255 * alphaInternal));
                    currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams);
                    currentSelectedBackgroundAlpha = getHighlightAlpha();
                    if (currentPosition == null) {
                        currentBackgroundSelectedDrawable.setAlpha((int) (alphaInternal * currentSelectedBackgroundAlpha * 255));
                        currentBackgroundSelectedDrawable.drawCached(canvas, backgroundCacheParams);
                    }
                } else if (selectedBackgroundProgress != 0 && (currentMessageObject == null || !currentMessageObject.preview) && !(currentMessagesGroup != null && currentMessagesGroup.isDocuments)) {
                    currentBackgroundDrawable.setAlpha((int) (255 * alphaInternal));
                    currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams);
                    currentSelectedBackgroundAlpha = selectedBackgroundProgress;
                    currentBackgroundSelectedDrawable.setAlpha((int) (currentSelectedBackgroundAlpha * alphaInternal * 255));
                    currentBackgroundSelectedDrawable.drawCached(canvas, backgroundCacheParams);
                    if (currentBackgroundDrawable.getGradientShader() == null) {
                        currentBackgroundShadowDrawable = null;
                    }
                } else {
                    if (isDrawSelectionBackground() && quoteHighlight == null && (currentPosition == null || currentMessageObject.isMusic() || currentMessageObject.isDocument() || getBackground() != null)) {
                        if (currentPosition != null) {
                            canvas.save();
//                            canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight());
                        }
                        currentSelectedBackgroundAlpha = 1f;
                        currentBackgroundSelectedDrawable.setAlpha((int) (255 * alphaInternal));
                        currentBackgroundSelectedDrawable.drawCached(canvas, backgroundCacheParams);
                        if (currentPosition != null) {
                            canvas.restore();
                        }
                    } else {
                        currentSelectedBackgroundAlpha = 0;
                        currentBackgroundDrawable.setAlpha((int) (255 * alphaInternal));
                        currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams);
                    }
                }
                if (currentBackgroundShadowDrawable != null && currentPosition == null) {
                    currentBackgroundShadowDrawable.setAlpha((int) (255 * alphaInternal));
                    currentBackgroundShadowDrawable.draw(canvas);
                }

                if (transitionParams.changePinnedBottomProgress != 1f && currentPosition == null) {
                    if (currentMessageObject.isOutOwner()) {
                        MessageDrawable drawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgOut);

                        Rect rect = currentBackgroundDrawable.getBounds();
                        drawable.setBounds(rect.left, rect.top, rect.right + dp(6), rect.bottom);
                        canvas.save();
                        canvas.translate(-pinnedBottomOffset, 0);
                        canvas.clipRect(rect.right - dp(16), rect.bottom - dp(16), rect.right + dp(16), rect.bottom);
                        int w = parentWidth;
                        int h = parentHeight;
                        if (h == 0) {
                            w = getParentWidth();
                            h = AndroidUtilities.displaySize.y;
                            if (getParent() instanceof View) {
                                View view = (View) getParent();
                                w = view.getMeasuredWidth();
                                h = view.getMeasuredHeight();
                            }
                        }
                        drawable.setTop((int) (getY() + parentViewTopOffset), w, h, (int) parentViewTopOffset, blurredViewTopOffset, blurredViewBottomOffset, pinnedTop, pinnedBottom);
                        drawable.setBotButtonsBottom(hasInlineBotButtons());
                        float alpha = !mediaBackground && !pinnedBottom ? transitionParams.changePinnedBottomProgress : (1f - transitionParams.changePinnedBottomProgress);
                        drawable.setAlpha((int) (255 * alpha));
                        drawable.draw(canvas);
                        drawable.setAlpha(255);
                        canvas.restore();
                    } else {
                        MessageDrawable drawable;
                        if (transitionParams.drawPinnedBottomBackground) {
                            drawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgIn);
                        } else {
                            drawable = (MessageDrawable) getThemedDrawable(Theme.key_drawable_msgInMedia);
                        }
                        float alpha = !mediaBackground && !pinnedBottom ? transitionParams.changePinnedBottomProgress : (1f - transitionParams.changePinnedBottomProgress);
                        drawable.setAlpha((int) (255 * alpha));
                        Rect rect = currentBackgroundDrawable.getBounds();
                        drawable.setBounds(rect.left - dp(6), rect.top, rect.right, rect.bottom);
                        canvas.save();
                        canvas.translate(pinnedBottomOffset, 0);
                        canvas.clipRect(rect.left - dp(6), rect.bottom - dp(16), rect.left + dp(6 + 12), rect.bottom);
                        drawable.draw(canvas);
                        drawable.setAlpha(255);
                        canvas.restore();
                    }
                }
            }
        }
        if (currentMessageObject != null && currentMessageObject.isRoundVideo()) {
            currentBackgroundDrawable.setRoundingRadius(0);
        }
        canvas.restoreToCount(restoreCount);

        return true;
    }

    private void animateCheckboxTranslation() {
        if (checkBoxVisible || checkBoxAnimationInProgress) {
            if (checkBoxVisible && checkBoxAnimationProgress == 1.0f || !checkBoxVisible && checkBoxAnimationProgress == 0.0f) {
                checkBoxAnimationInProgress = false;
            }
            Interpolator interpolator = checkBoxVisible ? CubicBezierInterpolator.EASE_OUT : CubicBezierInterpolator.EASE_IN;
            checkBoxTranslation = (int) Math.ceil(interpolator.getInterpolation(checkBoxAnimationProgress) * dp(35));
            if (currentMessageObject.type == MessageObject.TYPE_ARTICLE && getCurrentBackgroundRight() + dp(35) > getWidth())
                checkBoxTranslation = 0;
            if (!currentMessageObject.isOutOwner() || currentMessageObject.hasWideCode) {
                updateTranslation();
            }

            if (checkBoxAnimationInProgress) {
                long newTime = SystemClock.elapsedRealtime();
                long dt = newTime - lastCheckBoxAnimationTime;
                lastCheckBoxAnimationTime = newTime;

                if (checkBoxVisible) {
                    checkBoxAnimationProgress += dt / 200.0f;
                    if (checkBoxAnimationProgress > 1.0f) {
                        checkBoxAnimationProgress = 1.0f;
                    }
                } else {
                    checkBoxAnimationProgress -= dt / 200.0f;
                    if (checkBoxAnimationProgress <= 0.0f) {
                        checkBoxAnimationProgress = 0.0f;
                    }
                }
                invalidate();
                ((View) getParent()).invalidate();
            }
        }
    }

    public boolean drawBackgroundInParent() {
        if (canDrawBackgroundInParent && currentMessageObject != null && currentMessageObject.isOutOwner()) {
            return getThemedColor(Theme.key_chat_outBubbleGradient1) != 0;
        }
        return false;
    }

    public void drawServiceBackground(Canvas canvas, RectF rect, float radius, float alpha) {
        applyServiceShaderMatrix();
        if (alpha != 1f) {
            int oldAlpha = getThemedPaint(Theme.key_paint_chatActionBackground).getAlpha();
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha((int) (alpha * oldAlpha));
            canvas.drawRoundRect(rect, radius, radius, getThemedPaint(Theme.key_paint_chatActionBackground));
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha(oldAlpha);
        } else {
            canvas.drawRoundRect(rect, radius, radius, getThemedPaint(sideButtonPressed ? Theme.key_paint_chatActionBackgroundSelected : Theme.key_paint_chatActionBackground));
        }
        if (hasGradientService()) {
            if (alpha != 1f) {
                int oldAlpha = Theme.chat_actionBackgroundGradientDarkenPaint.getAlpha();
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha((int) (alpha * oldAlpha));
                canvas.drawRoundRect(rect, radius, radius, Theme.chat_actionBackgroundGradientDarkenPaint);
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha(oldAlpha);
            } else {
                canvas.drawRoundRect(rect, radius, radius, Theme.chat_actionBackgroundGradientDarkenPaint);
            }
        }
    }

    public void drawServiceBackground(Canvas canvas, Path path, float alpha) {
        applyServiceShaderMatrix();
        if (alpha != 1f) {
            int oldAlpha = getThemedPaint(Theme.key_paint_chatActionBackground).getAlpha();
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha((int) (alpha * oldAlpha));
            canvas.drawPath(path, getThemedPaint(Theme.key_paint_chatActionBackground));
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha(oldAlpha);
        } else {
            canvas.drawPath(path, getThemedPaint(sideButtonPressed ? Theme.key_paint_chatActionBackgroundSelected : Theme.key_paint_chatActionBackground));
        }
        if (hasGradientService()) {
            if (alpha != 1f) {
                int oldAlpha = Theme.chat_actionBackgroundGradientDarkenPaint.getAlpha();
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha((int) (alpha * oldAlpha));
                canvas.drawPath(path, Theme.chat_actionBackgroundGradientDarkenPaint);
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha(oldAlpha);
            } else {
                canvas.drawPath(path, Theme.chat_actionBackgroundGradientDarkenPaint);
            }
        }
    }

    public void drawCommentButton(Canvas canvas, float alpha) {
        if (drawSideButton != 3) {
            return;
        }
        int height = dp(32);
        if (commentLayout != null) {
            sideStartY -= dp(18);
            height += dp(18);
        }

        rect.set(sideStartX, sideStartY, sideStartX + dp(32), sideStartY + height);
        applyServiceShaderMatrix();
        if (alpha != 1f) {
            int oldAlpha = getThemedPaint(Theme.key_paint_chatActionBackground).getAlpha();
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha((int) (alpha * oldAlpha));
            canvas.drawRoundRect(rect, dp(16), dp(16), getThemedPaint(Theme.key_paint_chatActionBackground));
            getThemedPaint(Theme.key_paint_chatActionBackground).setAlpha(oldAlpha);
        } else {
            canvas.drawRoundRect(rect, dp(16), dp(16), getThemedPaint(sideButtonPressed ? Theme.key_paint_chatActionBackgroundSelected : Theme.key_paint_chatActionBackground));
        }
        if (hasGradientService()) {
            if (alpha != 1f) {
                int oldAlpha = Theme.chat_actionBackgroundGradientDarkenPaint.getAlpha();
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha((int) (alpha * oldAlpha));
                canvas.drawRoundRect(rect, dp(16), dp(16), Theme.chat_actionBackgroundGradientDarkenPaint);
                Theme.chat_actionBackgroundGradientDarkenPaint.setAlpha(oldAlpha);
            } else {
                canvas.drawRoundRect(rect, dp(16), dp(16), Theme.chat_actionBackgroundGradientDarkenPaint);
            }
        }

        Drawable commentStickerDrawable = Theme.getThemeDrawable(Theme.key_drawable_commentSticker);
        setDrawableBounds(commentStickerDrawable, sideStartX + dp(4), sideStartY + dp(4));
        if (alpha != 1f) {
            commentStickerDrawable.setAlpha((int) (255 * alpha));
            commentStickerDrawable.draw(canvas);
            commentStickerDrawable.setAlpha(255);
        } else {
            commentStickerDrawable.draw(canvas);
        }

        if (commentLayout != null) {
            Theme.chat_stickerCommentCountPaint.setColor(getThemedColor(Theme.key_chat_stickerReplyNameText));
            Theme.chat_stickerCommentCountPaint.setAlpha((int) (255 * alpha));
            if (transitionParams.animateComments) {
                if (transitionParams.animateCommentsLayout != null) {
                    canvas.save();
                    Theme.chat_stickerCommentCountPaint.setAlpha((int) (255 * (1.0 - transitionParams.animateChangeProgress) * alpha));
                    canvas.translate(sideStartX + (dp(32) - transitionParams.animateTotalCommentWidth) / 2, sideStartY + dp(30));
                    transitionParams.animateCommentsLayout.draw(canvas);
                    canvas.restore();
                }
                Theme.chat_stickerCommentCountPaint.setAlpha((int) (255 * transitionParams.animateChangeProgress));
            }
            canvas.save();
            canvas.translate(sideStartX + (dp(32) - totalCommentWidth) / 2, sideStartY + dp(30));
            commentLayout.draw(canvas);
            canvas.restore();
        }
    }

    public void applyServiceShaderMatrix() {
        applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, getX(), viewTop);
    }

    public void applyServiceShaderMatrix(int measuredWidth, int backgroundHeight, float x, float viewTop) {
        if (resourcesProvider != null) {
            resourcesProvider.applyServiceShaderMatrix(measuredWidth, backgroundHeight, x, viewTop + starsPriceTopPadding + topicSeparatorTopPadding + suggestionOfferTopPadding);
        } else {
            Theme.applyServiceShaderMatrix(measuredWidth, backgroundHeight, x, viewTop + starsPriceTopPadding + topicSeparatorTopPadding + suggestionOfferTopPadding);
        }
    }

    public boolean hasOutboundsContent() {
        if (effectDrawable != null && effectDrawable.isNotEmpty() > 0) {
            return true;
        }
        if (hasFactCheck) {
            return true;
        }
        if (transitionParams.animateExpandedQuotes && (currentMessageObject.type == MessageObject.TYPE_TEXT || currentMessageObject.type == MessageObject.TYPE_STORY_MENTION || currentMessageObject.type == MessageObject.TYPE_EMOJIS || currentMessageObject.isGiveawayOrGiveawayResults())) {
            return true;
        }
        if (channelRecommendationsCell != null && currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL) {
            return true;
        }
        if ((drawTopic || transitionParams.animateDrawTopic) && topicButton != null) {
            return true;
        }
        if (starsPriceText != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            return true;
        }
        if (bottomActionText != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            return true;
        }
        if (topicSeparator != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            return true;
        }
        if (suggestionOffer != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            return true;
        }
        if (drawStartBotTopic && (currentPosition == null || BitwiseUtils.hasFlag(currentPosition.flags, MessageObject.POSITION_FLAG_BOTTOM) && BitwiseUtils.hasFlag(currentPosition.flags, MessageObject.POSITION_FLAG_LEFT))) {
            return true;
        }
        if (getAlpha() != 1f) {
            return false;
        }
        return (
            reactionsLayoutInBubble.hasOverlay() ||
            (!transitionParams.transitionBotButtons.isEmpty() && transitionParams.animateBotButtonsChanged) ||
            !botButtons.isEmpty() ||
            drawSideButton != 0 ||
            drawNameLayout && nameLayout != null && currentNameEmojiStatusDrawable != null && !currentNameEmojiStatusDrawable.isEmpty() ||
            animatedEmojiStack != null && !animatedEmojiStack.holders.isEmpty() ||
            currentNameStatusDrawable != null && !currentNameStatusDrawable.isEmpty() ||
            currentMessagesGroup == null &&
                (transitionParams.animateReplaceCaptionLayout && transitionParams.animateChangeProgress != 1f || transitionParams.animateChangeProgress != 1.0f && transitionParams.animateMessageText) &&
                transitionParams.animateOutAnimateEmoji != null && !transitionParams.animateOutAnimateEmoji.holders.isEmpty() ||
            drawSummaryReply || transitionParams.animateSummaryReply ||
            currentMessageObject != null && currentMessageObject.richLayout != null && currentMessageObject.richLayout.hasOverlay() ||
            transitionParams.animateRichLayout && transitionParams.animateOutRichLayout != null && transitionParams.animateOutRichLayout.hasOverlay()
        );
    }

    public boolean showTopicSeparator = true;
    public void setShowTopic(boolean show) {
        if (showTopicSeparator != show) {
            showTopicSeparator = show;
            invalidateOutbounds();
            invalidate();
        }
    }

    public int getTopicSeparatorTopPadding() {
        if (transitionParams.animateMonoforumPadding) {
            return lerp(transitionParams.animateMonoforumPaddingFrom, topicSeparatorTopPadding, transitionParams.animateChangeProgress);
        }
        return topicSeparatorTopPadding;
    }

    public int getStarsPriceTopPadding() {
        if (transitionParams.animateStarsPriceTopPadding) {
            return lerp(transitionParams.animateStarsPriceTopPaddingFrom, starsPriceTopPadding, transitionParams.animateChangeProgress);
        }
        return starsPriceTopPadding;
    }

    public int getBottomActionPadding() {
        if (transitionParams.animateBottomActionPadding) {
            return lerp(transitionParams.animateBottomActionPaddingFrom, bottomActionPadding, transitionParams.animateChangeProgress);
        }
        return bottomActionPadding;
    }

    public void drawOutboundsContent(Canvas canvas) {
        if (channelRecommendationsCell != null && currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL) {
            channelRecommendationsCell.draw(canvas);
            return;
        }
        if (currentMessageObject == null) {
            return;
        }

        if (currentMessageObject.type == MessageObject.TYPE_TEXT || currentMessageObject.type == MessageObject.TYPE_STORY_MENTION || currentMessageObject.type == MessageObject.TYPE_EMOJIS || currentMessageObject.isGiveawayOrGiveawayResults()) {
            if (transitionParams.animateExpandedQuotes) {
                layoutTextXY(false);
                drawMessageText(canvas);
            }
        }

        if (starsPriceText != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            final float appear = transitionParams.animateStarsPriceText ? transitionParams.animateChangeProgress : 1.0f;
            final float alpha = transitionParams.ignoreAlpha ? timeAlpha : getAlpha() * appear;
            final float scale = lerp(0.6f, 1.0f, appear);
            canvas.save();
            canvas.translate((getParentWidth() - starsPriceText.getWidth()) / 2.0f, -getStarsPriceTopPadding() + dp(4.5f) + dp(3.33f));
            canvas.scale(scale, scale, getParentWidth() / 2.0f, dp(6.83f) - dp(4.5f) - dp(3.33f));
            applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, getX(), viewTop - getStarsPriceTopPadding() + dp(4.5f) + dp(3.33f));
            final Paint backgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackground);
            int oldAlpha = backgroundPaint.getAlpha();
            backgroundPaint.setPathEffect(starsPriceTextPathEffect);
            backgroundPaint.setAlpha((int) (oldAlpha * alpha));
            canvas.drawPath(starsPriceTextPath, backgroundPaint);
            backgroundPaint.setPathEffect(null);
            backgroundPaint.setAlpha(oldAlpha);
            if (hasGradientService()) {
                final Paint darkenPaint = getThemedPaint(Theme.key_paint_chatActionBackgroundDarken);
                oldAlpha = darkenPaint.getAlpha();
                darkenPaint.setPathEffect(starsPriceTextPathEffect);
                darkenPaint.setAlpha((int) (oldAlpha * alpha));
                canvas.drawPath(starsPriceTextPath, darkenPaint);
                darkenPaint.setPathEffect(null);
                darkenPaint.setAlpha(oldAlpha);
            }
            canvas.restore();
            canvas.save();
            canvas.scale(scale, scale, getParentWidth() / 2.0f, -getStarsPriceTopPadding() + dp(6.83f));
            starsPriceText.draw(canvas, (getParentWidth() - starsPriceText.getWidth()) / 2.0f, -getStarsPriceTopPadding() + dp(6.83f), getThemedColor(Theme.key_chat_serviceText), alpha);
            canvas.restore();
        }
        if (bottomActionText != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            final float y = getHeight() - getPaddingTop() - transitionParams.deltaTop + transitionParams.deltaBottom - getBottomActionPadding() - dp(9);
            final float appear = transitionParams.animateBottomActionText ? transitionParams.animateChangeProgress : 1.0f;
            final float alpha = transitionParams.ignoreAlpha ? timeAlpha : getAlpha() * appear;
            final float scale = lerp(0.6f, 1.0f, appear);
            canvas.save();
            canvas.translate((getParentWidth() - bottomActionText.getWidth()) / 2.0f, y + dp(4.5f) + dp(3.33f));
            canvas.scale(scale, scale, getParentWidth() / 2.0f, dp(6.83f) - dp(4.5f) - dp(3.33f));
            applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, getX(), viewTop + y + dp(4.5f) + dp(3.33f));
            final Paint backgroundPaint = getThemedPaint(Theme.key_paint_chatActionBackground);
            int oldAlpha = backgroundPaint.getAlpha();
            backgroundPaint.setPathEffect(bottomActionTextPathEffect);
            backgroundPaint.setAlpha((int) (oldAlpha * alpha));
            canvas.drawPath(bottomActionTextPath, backgroundPaint);
            backgroundPaint.setPathEffect(null);
            backgroundPaint.setAlpha(oldAlpha);
            if (hasGradientService()) {
                final Paint darkenPaint = getThemedPaint(Theme.key_paint_chatActionBackgroundDarken);
                oldAlpha = darkenPaint.getAlpha();
                darkenPaint.setPathEffect(bottomActionTextPathEffect);
                darkenPaint.setAlpha((int) (oldAlpha * alpha));
                canvas.drawPath(bottomActionTextPath, darkenPaint);
                darkenPaint.setPathEffect(null);
                darkenPaint.setAlpha(oldAlpha);
            }
            canvas.restore();
            canvas.save();
            canvas.scale(scale, scale, getParentWidth() / 2.0f, y + dp(6.83f));
            bottomActionText.draw(canvas, (getParentWidth() - bottomActionText.getWidth()) / 2.0f, y + dp(6.83f), getThemedColor(Theme.key_chat_serviceText), alpha);
            canvas.restore();
        }

        if (topicSeparator != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            float alpha = transitionParams.ignoreAlpha ? timeAlpha : getAlpha();
            if (transitionParams.animateMonoforumPadding) {
                alpha *= showTopicSeparator ? transitionParams.animateChangeProgress : (1.0f - transitionParams.animateChangeProgress);
            }
            final float top = -starsPriceTopPadding - topicSeparatorTopPadding - suggestionOfferTopPadding;
            applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, sideMenuWidth / 2f + getX(), viewTop + top);
            topicSeparator.draw(canvas, getParentWidth(), sideMenuWidth, top, 1.0f, alpha, showTopicSeparator);
        }

        if (suggestionOffer != null && (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_TOP) != 0 && (currentPosition.flags & MessageObject.POSITION_FLAG_LEFT) != 0)) {
            float alpha = transitionParams.ignoreAlpha ? timeAlpha : getAlpha();
            final float top = -starsPriceTopPadding - suggestionOfferTopPadding + dp(4);
            applyServiceShaderMatrix(getMeasuredWidth(), backgroundHeight, sideMenuWidth / 2f + getX(),
                    viewTop + top);

            final int x = (getParentWidth() - suggestionOffer.getWidth()) / 2;
            suggestionOffer.draw(canvas, getParentWidth(), sideMenuWidth, top , 1.0f, alpha, showTopicSeparator);

        }

        if (summaryTitle != null && summaryLine != null && summaryBounce != null) {
            final float summaryAlpha = transitionParams.animateSummaryReply ? (drawSummaryReply ? transitionParams.animateChangeProgress : 1.0f - transitionParams.animateChangeProgress) : (drawSummaryReply ? 1.0f : 0.0f);
            if (summaryAlpha > 0) {
                final float s = summaryBounce.getScale(0.025f) * lerp(0.8f, 1.0f, summaryAlpha);
                canvas.save();
                canvas.scale(s, s, summarySelectorRect.centerX(), summarySelectorRect.centerY());

                final int textColor = summaryLine.getColor();
                if (summaryParticles == null) {
                    summaryParticles = new StarsReactionsSheet.Particles(StarsReactionsSheet.Particles.TYPE_RADIAL_INSIDE, 30);
                }
                summaryParticles.setBounds(summarySelectorRect);
                summaryParticles.process();
                summaryParticles.draw(canvas, Theme.multAlpha(textColor, 0.66f), summaryAlpha);
                invalidateOutbounds();

              ngFrom;
        public boolean animateMonoforumPadding;
        public boolean needsStopClipping;

        public boolean animateStarsPriceText;
        public int animateStarsPriceTopPaddingFrom;
        public boolean animateStarsPriceTopPadding;

        public boolean animateBottomActionText;
        public int animateBottomActionPaddingFrom;
        public boolean animateBottomActionPadding;

        public boolean lastDrawingLinkAbove;
        public boolean animateLinkAbove;
        public boolean lastDrawingMediaAbove;
        public boolean animateMediaAbove;

        public int lastDrawingLinkPreviewHeight;
        public int animateFromLinkPreviewHeight;
        public boolean animateLinkPreviewHeight;

        public boolean animateDrawingTimeAlpha;

        public float animateChangeProgress = 1f;
        private ArrayList<BotButton> lastDrawBotButtons = new ArrayList<>();
        private ArrayList<BotButton> transitionBotButtons = new ArrayList<>();
        public boolean animateWidthForButton;
        public int animateFromWidthForButton;
        public int lastDrawnWidthForButtons;

        public final float crossfadeProgressK = 0.5f;
        public float oldProgress = 0.0f, newProgress = 1.0f;

        private void updateCrossfadeProgress() {
            final float progress = Math.max(0.0f, Math.min(1.0f, animateChangeProgress));
            oldProgress = (float) Math.pow(1.0f - progress, crossfadeProgressK);
            newProgress = (float) Math.pow(progress, crossfadeProgressK);
        }

        public float lastButtonX;
        public float lastButtonY;
        private float animateFromButtonX;
        private float animateFromButtonY;
        private boolean animateButton;

        public int lastMediaOffsetY;
        private int animateFromMediaOffsetY;
        private boolean animateMediaOffsetY;

        public int lastStatusDrawableParams = -1;

        public int lastViewsCount;
        public StaticLayout lastViewsLayout;
        private StaticLayout animateViewsLayout;

        public boolean lastShouldDrawTimeOnMedia;
        private boolean animateShouldDrawTimeOnMedia;
        public boolean lastShouldDrawMenuDrawable;
        private boolean animateShouldDrawMenuDrawable;
        public StaticLayout lastTimeLayout;
        public boolean lastIsPlayingRound;
        public boolean animatePlayingRound;
        public boolean animateTextY;

        public float lastDrawingTextY;
        public float lastDrawingTextX;

        public float animateFromTextY;
        public float lastTextXOffset;

        public int lastDrawingLinkPreviewY;
        public int animateFromLinkPreviewY;
        public boolean animateLinkPreviewY;

        public int lastTopOffset;
        public boolean animateForwardedLayout;
        public int animateForwardedNamesOffset;
        public int lastForwardedNamesOffset;
        public boolean lastDrawnForwardedName;
        public StaticLayout[] lastDrawnForwardedNameLayout = new StaticLayout[2];
        public StaticLayout[] animatingForwardedNameLayout = new StaticLayout[2];
        float animateForwardNameX;
        public float lastForwardNameX;
        int animateForwardNameWidth;
        public int lastForwardNameWidth;
        boolean animateBotButtonsChanged;
        public StaticLayout lastDrawnReplyTextLayout;

        public boolean animateNamesOffset;
        public int animateNamesOffsetFrom;
        public int lastNamesOffset;

        public int lastReplyTextXOffset;
        public float animateReplyTextOffset;

        public boolean lastDrawingRecommendationsExpanded;
        public boolean animateRecommendationsExpanded;
        public boolean animateFromRecommendationsExpanded;

        public boolean lastDrawNameLayout;
        public boolean lastDrawAvatar;
        public boolean lastDrawTopic;
        public boolean animateDrawNameLayout;
        public boolean animateDrawAvatar;
        public boolean animateDrawTopic;

        public boolean lastDrawingFactCheck;
        public boolean animateFactCheck;
        public int lastDrawingFactCheckHeight;
        public int animateFactCheckHeightFrom;

        public boolean animatePollAddOptionHeight;
        public int lastDrawingPollAddOptionHeight;
        public int animatePollAddOptionHeightFrom;

        public boolean animateFactCheckHeight;
        public boolean lastDrawingFactCheckExpanded;
        public boolean animateFactCheckExpanded;

        public boolean lastDrawingSummaryReply;
        public boolean animateSummaryReply;

        public HashSet<Integer> lastDrawingExpandedQuotes;
        public HashSet<Integer> animateExpandedQuotesFrom;
        public boolean animateExpandedQuotes;

        public boolean lastDrawingExpandedExplanation;
        public boolean expandedExplanationFrom;
        public boolean animateExpandedExplanation;

        public boolean lastDrawnTranslated;
        public StaticLayout lastDrawnTitleLayout;
        public StaticLayout animateTitleLayout;
        private AnimatedEmojiSpan.EmojiGroupedSpans animateTitleLayoutEmoji;

        public void recordDrawingState() {
            wasDraw = true;
            lastDrawingImageX = photoImage.getImageX();
            lastDrawingImageY = photoImage.getImageY();
            lastDrawingImageW = photoImage.getImageWidth();
            lastDrawingImageH = photoImage.getImageHeight();
            int[] r = photoImage.getRoundRadius();
            System.arraycopy(r, 0, imageRoundRadius, 0, 4);
            if (currentBackgroundDrawable != null) {
                lastDrawingBackgroundRect.set(currentBackgroundDrawable.getBounds());
            }
            lastDrawingSideMenuEnabled = isSideMenuEnabled;
            lastDrawingTextBlocks = currentMessageObject != null ? currentMessageObject.textLayoutBlocks : null;
            lastDrawingTextWidth = currentMessageObject != null ? currentMessageObject.textWidth : 0;
            lastDrawingEdited = edited;
            lastDrawingRichLayout = currentMessageObject != null ? currentMessageObject.richLayout : null;

            lastDrawingCaptionX = captionX;
            lastDrawingCaptionY = captionY;

            lastDrawingCaptionLayout = captionLayout;
            lastDrawingSummarized = currentMessageObject != null ? currentMessageObject.summarized : false;
            lastDrawBotButtons.clear();
            if (!botButtons.isEmpty()) {
                lastDrawBotButtons.addAll(botButtons);
            }
            lastDrawingSmallImage = isSmallImage;
            lastDrawnMonoforumPadding = topicSeparatorTopPadding;
            lastDrawnStarsPriceTopPadding = starsPriceTopPadding;
            lastDrawnBottomActionPadding = bottomActionPadding;
            lastDrawnStarsPriceText = starsPriceText != null;
            lastDrawnBottomActionText = bottomActionText != null;
            lastDrawingLinkPreviewHeight = linkPreviewHeight;
            lastDrawingLinkAbove = linkPreviewAbove;
            lastDrawingMediaAbove = captionAbove;

            lastDrawingRecommendationsExpanded = currentMessageObject != null && currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL && channelRecommendationsCell != null && channelRecommendationsCell.isExpanded();

            if (commentLayout != null) {
                lastCommentsCount = getRepliesCount();
                lastTotalCommentWidth = totalCommentWidth;
                lastCommentLayout = commentLayout;
                lastCommentArrowX = commentArrowX;
                lastCommentUnreadX = commentUnreadX;
                lastCommentDrawUnread = commentDrawUnread;
                lastCommentX = commentX;
                lastDrawCommentNumber = drawCommentNumber;
            }

            lastRepliesCount = getRepliesCount();
            this.lastViewsCount = getMessageObject().messageOwner.views;
            lastRepliesLayout = repliesLayout;
            lastViewsLayout = viewsLayout;

            lastIsPinned = isPinned;

            lastSignMessage = lastPostAuthor;

            lastDrawBackground = drawBackground;
            lastUseTranscribeButton = useTranscribeButton;

            lastButtonX = buttonX;
            lastButtonY = buttonY;
            lastMediaOffsetY = mediaOffsetY;

            lastDrawTime = !forceNotDrawTime;
            lastTimeX = timeX;
            lastTimeLayout = timeLayout;
            lastTimeWidth = timeWidth;

            lastShouldDrawTimeOnMedia = shouldDrawTimeOnMedia();
            lastTopOffset = getTopMediaOffset();
            lastShouldDrawMenuDrawable = shouldDrawMenuDrawable();

            lastLocatinIsExpired = locationExpired;
            lastIsPlayingRound = isPlayingRound;

            lastDrawingTextY = textY;
            lastDrawingTextX = textX;
            lastDrawingLinkPreviewY = linkPreviewY;

            lastDrawnWidthForButtons = widthForButtons;
            lastDrawnForwardedNameLayout[0] = forwardedNameLayout[0];
            lastDrawnForwardedNameLayout[1] = forwardedNameLayout[1];
            lastDrawnForwardedName = currentMessageObject != null && currentMessageObject.needDrawForwarded();
            lastForwardNameX = forwardNameX;
            lastNamesOffset = lastForwardedNamesOffset = namesOffset;
            lastForwardNameWidth = forwardedNameWidth;
            lastBackgroundLeft = getCurrentBackgroundLeft();
            if (currentBackgroundDrawable != null) {
                lastBackgroundRight = currentBackgroundDrawable.getBounds().right;
            }
            lastTextXOffset = currentMessageObject != null ? currentMessageObject.textXOffset : 0;

            lastDrawingReplyTextHeight = replyTextHeight;
            lastDrawnReplyTextLayout = replyTextLayout;
            lastReplyTextXOffset = replyTextOffset;

            reactionsLayoutInBubble.recordDrawingState();
            if (replyNameLayout != null) {
                lastDrawReplyY = replyStartY;
            } else {
                lastDrawReplyY = 0;
            }

            lastDrawNameLayout = drawNameLayout;
            lastDrawAvatar = drawNameAvatar;
            lastDrawTopic = drawTopic;

            lastDrawingFactCheckHeight = factCheckHeight;
            lastDrawingFactCheckExpanded = getPrimaryMessageObject() != null && getPrimaryMessageObject().factCheckExpanded;
            lastDrawingFactCheck = hasFactCheck;
            lastDrawingPollAddOptionHeight = pollAddButtonHeight;

            lastDrawingSummaryReply = drawSummaryReply;

            lastDrawingExpandedQuotes = getPrimaryMessageObject() != null ? getPrimaryMessageObject().expandedQuotes : null;
            lastDrawingExpandedExplanation = currentMessageObject != null && currentMessageObject.expandedExplanation;

            lastDrawnTranslated = currentMessageObject != null && currentMessageObject.translated;
            lastDrawnTitleLayout = titleLayout;
        }

        public void recordDrawingStatePreview() {
            lastDrawnForwardedNameLayout[0] = forwardedNameLayout[0];
            lastDrawnForwardedNameLayout[1] = forwardedNameLayout[1];
            lastDrawnForwardedName = currentMessageObject.needDrawForwarded();
            lastForwardNameX = forwardNameX;
            lastNamesOffset = lastForwardedNamesOffset = namesOffset;
            lastForwardNameWidth = forwardedNameWidth;
        }

        public boolean animateChange() {
            if (!wasDraw) {
                return false;
            }
            boolean changed = false;

            needsStopClipping = false;
            animateDrawingSideMenuEnabled = false;
            if (lastDrawingSideMenuEnabled != isSideMenuEnabled) {
                animateDrawingSideMenuEnabled = true;
                changed = true;
            }

            animateMessageText = false;
            if (currentMessageObject.textLayoutBlocks != lastDrawingTextBlocks) {
                boolean sameText = true;
                if (currentMessageObject.textWidth != lastDrawingTextWidth && lastDrawingSideMenuEnabled != isSideMenuEnabled) {
                    sameText = false;
                }
                if (currentMessageObject.textLayoutBlocks != null && lastDrawingTextBlocks != null && currentMessageObject.textLayoutBlocks.size() == lastDrawingTextBlocks.size()) {
                    for (int i = 0; i < lastDrawingTextBlocks.size(); i++) {
                        String newText = currentMessageObject.textLayoutBlocks.get(i).textLayout == null ? null : currentMessageObject.textLayoutBlocks.get(i).textLayout.getText().toString();
                        String oldText = lastDrawingTextBlocks.get(i).textLayout == null ? null : lastDrawingTextBlocks.get(i).textLayout.getText().toString();
                        if ((newText == null && oldText != null) || (newText != null && oldText == null) || !newText.equals(oldText)) {
                            sameText = false;
                            break;
                        } else {
                            if (animatedEmojiStack != null) {
                                animatedEmojiStack.replaceLayout(currentMessageObject.textLayoutBlocks.get(i).textLayout, lastDrawingTextBlocks.get(i).textLayout);
                            }
                        }
                    }
                } else {
                    sameText = false;
                }
                if (!sameText) {
                    animateMessageText = true;
                    animateOutTextBlocks = lastDrawingTextBlocks;
                    animateOutTextXOffset = lastTextXOffset;
                    animateOutAnimateEmoji = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, animateOutAnimateEmoji, lastDrawingTextBlocks, true);
                    animatedEmojiStack = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, animatedEmojiStack, currentMessageObject.textLayoutBlocks);
                    changed = true;
                } else {
                    animatedEmojiStack = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, animatedEmojiStack, currentMessageObject.textLayoutBlocks);
                }
            }

            if (currentMessageObject.richLayout != null && (currentMessageObject.richLayout.detailsAnimating || currentMessageObject.richLayout.blockquoteAnimating)) {
                needsStopClipping = true;
                changed = true;
            }

            animateRichLayout = false;
            if (currentMessageObject.richLayout != lastDrawingRichLayout) {
                final boolean streaming = botDraftTypingAnimator != null && botDraftTypingAnimator.isRunning();
                if (animateOutRichLayout != null) {
                    animateOutRichLayout.detach(ChatMessageCell.this);
                    animateOutRichLayout = null;
                }
                if (streaming) {
                    if (currentMessageObject.richLayout != null && lastDrawingRichLayout != null) {
                        currentMessageObject.richLayout.layout(lastDrawingRichLayout);
                    }
                } else {
                    animateRichLayout = true;
                    animateOutRichLayout = lastDrawingRichLayout;
                    if (animateOutRichLayout != null) {
                        animateOutRichLayout.attach(ChatMessageCell.this);
                    }
                }
            }

            animateDrawNameLayout = false;
            if (drawNameLayout != lastDrawNameLayout) {
                animateDrawNameLayout = true;
                changed = true;
            }
            animateDrawAvatar = false;
            if (drawNameAvatar != lastDrawAvatar) {
                animateDrawAvatar = true;
                changed = true;
            }
            animateDrawTopic = false;
            if (drawTopic != lastDrawTopic) {
                animateDrawTopic = true;
                changed = true;
            }
            if (replyTextLayout != lastDrawnReplyTextLayout) {
                CharSequence newText = replyTextLayout != null ? replyTextLayout.getText() : null;
                CharSequence oldText = lastDrawnReplyTextLayout != null ? lastDrawnReplyTextLayout.getText() : null;
                if (!TextUtils.equals(newText, oldText)) {
                    animateFromReplyTextHeight = lastDrawingReplyTextHeight;
                    animateReplyTextLayout = lastDrawnReplyTextLayout;
                    animateReplyTextOffset = lastReplyTextXOffset;
                    animateOutAnimateEmojiReply = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, false, animateOutAnimateEmojiReply, true, lastDrawnReplyTextLayout);
                    changed = true;
                }
            }
            if (edited && !lastDrawingEdited && timeLayout != null) {
                String editedStr = getString("EditedMessage", R.string.EditedMessage);
                CharSequence text = timeLayout.getText();
                int i = text.toString().indexOf(editedStr);
                if (i >= 0) {
                    if (i == 0) {
                        animateEditedLayout = new StaticLayout(editedStr, Theme.chat_timePaint, timeTextWidth + dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        spannableStringBuilder.append(editedStr);
                        spannableStringBuilder.append(text.subSequence(editedStr.length(), text.length()));
                        spannableStringBuilder.setSpan(new EmptyStubSpan(), 0, editedStr.length(), 0);
                        animateTimeLayout = new StaticLayout(spannableStringBuilder, Theme.chat_timePaint, timeTextWidth + dp(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        animateEditedWidthDiff = timeWidth - lastTimeWidth;
                    } else {
                        animateEditedWidthDiff = 0;
                        animateEditedLayout = null;
                        animateTimeLayout = lastTimeLayout;
                    }
                    animateEditedEnter = true;
                    animateTimeWidth = lastTimeWidth;
                    animateFromTimeX = lastTimeX;
                    shouldAnimateTimeX = true;
                    changed = true;
                }
                accessibilityText = null;
            } else if (!edited && lastDrawingEdited && timeLayout != null) {
                animateTimeLayout = lastTimeLayout;
                animateEditedWidthDiff = timeWidth - lastTimeWidth;
                animateEditedEnter = true;
                animateTimeWidth = lastTimeWidth;
                animateFromTimeX = lastTimeX;
                shouldAnimateTimeX = true;
                changed = true;
            }

            animateDrawBackground = false;
            if (drawBackground != lastDrawBackground) {
                animateDrawBackground = true;
                changed = true;
            }

            animateSmallImage = false;
            if (isSmallImage != lastDrawingSmallImage) {
                animateSmallImage = true;
                photoImageFromWidth = lastDrawingImageW;
                photoImageFromHeight = lastDrawingImageH;
                photoImageFromCenterX = lastDrawingImageX + lastDrawingImageW / 2f;
                photoImageFromCenterY = lastDrawingImageY + lastDrawingImageH / 2f;
                changed = true;
            }
            animateMonoforumPadding = false;
            if (topicSeparatorTopPadding != lastDrawnMonoforumPadding) {
                needsStopClipping = true;
                animateMonoforumPaddingFrom = lastDrawnMonoforumPadding;
                animateMonoforumPadding = true;
                changed = true;
            }

            animateStarsPriceTopPadding = false;
            if (starsPriceTopPadding != lastDrawnStarsPriceTopPadding) {
                animateStarsPriceTopPaddingFrom = lastDrawnStarsPriceTopPadding;
                animateStarsPriceTopPadding = true;
                changed = true;
            }

            animateStarsPriceText = false;
            if ((starsPriceText != null) != lastDrawnStarsPriceText) {
                animateStarsPriceText = true;
                changed = true;
            }

            animateBottomActionPadding = false;
            if (bottomActionPadding != lastDrawnBottomActionPadding) {
                animateBottomActionPaddingFrom = lastDrawnBottomActionPadding;
                animateBottomActionPadding = true;
                changed = true;
            }

            animateBottomActionText = false;
            if ((bottomActionText != null) != lastDrawnBottomActionText) {
                animateBottomActionText = true;
                changed = true;
            }

            animateRecommendationsExpanded = false;
            final boolean channelsExpanded = currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL && channelRecommendationsCell != null && channelRecommendationsCell.isExpanded();
            if (channelsExpanded != lastDrawingRecommendationsExpanded) {
                animateRecommendationsExpanded = true;
                animateFromRecommendationsExpanded = lastDrawingRecommendationsExpanded;
                changed = true;
            }

            animateLinkAbove = false;
            if (linkPreviewAbove != lastDrawingLinkAbove) {
                animateLinkAbove = true;
                changed = true;
            }

            animateMediaAbove = false;
            if (captionAbove != lastDrawingMediaAbove) {
                animateMediaAbove = true;
                changed = true;
            }

            animateLinkPreviewHeight = false;
            if (hasLinkPreview && linkPreviewHeight != lastDrawingLinkPreviewHeight) {
                animateLinkPreviewHeight = true;
                animateFromLinkPreviewHeight = lastDrawingLinkPreviewHeight;
                changed = true;
            }

            animateUseTranscribeButton = false;
            if (useTranscribeButton != lastUseTranscribeButton) {
                animateUseTranscribeButton = true;
                changed = true;
            }

            boolean summarized = currentMessageObject != null ? currentMessageObject.summarized : false;
            if (captionLayout != lastDrawingCaptionLayout) {
                String oldCaption = lastDrawingCaptionLayout == null ? null : lastDrawingCaptionLayout.text.toString();
                String currentCaption = captionLayout == null ? null : captionLayout.text.toString();
                if (
                    (lastDrawingSideMenuEnabled != isSideMenuEnabled || lastDrawingSummarized != summarized) && (captionLayout == null ? 0 : captionLayout.textWidth) != (lastDrawingCaptionLayout == null ? 0 : lastDrawingCaptionLayout.textWidth) ||
                    (currentCaption == null) != (oldCaption == null) ||
                    (oldCaption != null && !oldCaption.equals(currentCaption))
                ) {
                    animateReplaceCaptionLayout = true;
                    animateOutCaptionLayout = lastDrawingCaptionLayout;
                    animateOutAnimateEmoji = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, null, animateOutCaptionLayout == null ? null : animateOutCaptionLayout.textLayoutBlocks);
                    animatedEmojiStack = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, animatedEmojiStack, captionLayout == null ? null : captionLayout.textLayoutBlocks);
                    if (lastDrawingSideMenuEnabled != isSideMenuEnabled || lastDrawingSummarized != summarized) {
                        moveCaption = true;
                        captionFromX = lastDrawingCaptionX;
                        captionFromY = lastDrawingCaptionY;
                    }
                    changed = true;
                } else {
                    updateCaptionLayout();
                    if (lastDrawingCaptionX != captionX || lastDrawingCaptionY != captionY) {
                        moveCaption = true;
                        captionFromX = lastDrawingCaptionX;
                        captionFromY = lastDrawingCaptionY;
                        changed = true;
                    }
                }
            } else if (captionLayout != null && lastDrawingCaptionLayout != null) {
                updateCaptionLayout();
                if (lastDrawingCaptionX != captionX || lastDrawingCaptionY != captionY) {
                    moveCaption = true;
                    captionFromX = lastDrawingCaptionX;
                    captionFromY = lastDrawingCaptionY;
                    changed = true;
                }
            }
            if (!lastDrawBotButtons.isEmpty() || !botButtons.isEmpty()) {
                if (lastDrawBotButtons.size() != botButtons.size()) {
                    animateBotButtonsChanged = true;
                }
                if (!animateBotButtonsChanged) {
                    for (int i = 0; i < botButtons.size(); i++) {
                        BotButton button1 = botButtons.get(i);
                        BotButton button2 = lastDrawBotButtons.get(i);
                        if (button1.isSeparator || button2.isSeparator) {
                            continue;
                        }

                        if (Math.abs(button1.x - button2.x) > 0.01f || Math.abs(button1.width - button2.width) > 0.01f || !TextUtils.equals(button1.title.getText(), button2.title.getText())) {
                            animateBotButtonsChanged = true;
                            break;
                        }
                    }
                }
                if (animateBotButtonsChanged) {
                    transitionBotButtons.addAll(lastDrawBotButtons);
                }
            }
            if (lastDrawnWidthForButtons != widthForButtons) {
                animateFromWidthForButton = lastDrawnWidthForButtons;
                animateWidthForButton = true;
            }

            if (documentAttachType == DOCUMENT_ATTACH_TYPE_MUSIC || documentAttachType == DOCUMENT_ATTACH_TYPE_AUDIO || documentAttachType == DOCUMENT_ATTACH_TYPE_DOCUMENT) {
                if (buttonX != lastButtonX || buttonY != lastButtonY) {
                    animateFromButtonX = lastButtonX;
                    animateFromButtonY = lastButtonY;
                    animateButton = true;
                    changed = true;
                }
            }
            
            if (mediaOffsetY != lastMediaOffsetY) {
                animateFromMediaOffsetY = lastMediaOffsetY;
                animateMediaOffsetY = true;
                changed = true;
            }

            boolean timeDrawablesIsChanged = false;

            if (lastIsPinned != isPinned) {
                animatePinned = true;
                changed = true;
                timeDrawablesIsChanged = true;
                accessibilityText = null;
            }

            if ((lastRepliesLayout != null || repliesLayout != null) && lastRepliesCount != getRepliesCount()) {
                animateRepliesLayout = lastRepliesLayout;
                animateReplies = true;
                changed = true;
                timeDrawablesIsChanged = true;
                accessibilityText = null;
            }

            if (lastViewsLayout != null && this.lastViewsCount != getMessageObject().messageOwner.views) {
                animateViewsLayout = lastViewsLayout;
                changed = true;
                timeDrawablesIsChanged = true;
                accessibilityText = null;
            }

            if (commentLayout != null && lastCommentsCount != getRepliesCount()) {
                if (lastCommentLayout != null && !TextUtils.equals(lastCommentLayout.getText(), commentLayout.getText())) {
                    animateCommentsLayout = lastCommentLayout;
                } else {
                    animateCommentsLayout = null;
                }
                animateTotalCommentWidth = lastTotalCommentWidth;
                animateCommentX = lastCommentX;
                animateCommentArrowX = lastCommentArrowX;
                animateCommentUnreadX = lastCommentUnreadX;
                animateCommentDrawUnread = lastCommentDrawUnread;
                animateDrawCommentNumber = lastDrawCommentNumber;
                animateComments = true;
                changed = true;
            }

            if (!TextUtils.equals(lastSignMessage, lastPostAuthor)) {
                animateSign = true;
                animateNameX = nameX;
                changed = true;
            }

            if (lastDrawTime == forceNotDrawTime) {
                animateDrawingTimeAlpha = true;
                animateViewsLayout = null;
                changed = true;
            } else if (lastShouldDrawTimeOnMedia != shouldDrawTimeOnMedia()) {
                animateEditedEnter = false;
                animateShouldDrawTimeOnMedia = true;
                animateFromTimeX = lastTimeX;
                animateTimeLayout = lastTimeLayout;
                animateTimeWidth = lastTimeWidth;
                changed = true;
            } else if (timeDrawablesIsChanged || Math.abs(timeX - lastTimeX) > 1 && lastDrawingSideMenuEnabled == isSideMenuEnabled) {
                shouldAnimateTimeX = true;
                animateTimeWidth = lastTimeWidth;
                animateFromTimeX = lastTimeX;
                animateFromTimeXViews = lastTimeXViews;
                animateFromTimeXReplies = lastTimeXReplies;
                animateFromTimeXPinned = lastTimeXPinned;
            }

            if (lastShouldDrawMenuDrawable != shouldDrawMenuDrawable()) {
                animateShouldDrawMenuDrawable = true;
            }

            if (lastLocatinIsExpired != locationExpired) {
                animateLocationIsExpired = true;
            }

            if (lastIsPlayingRound != isPlayingRound) {
                animatePlayingRound = true;
                changed = true;
            }

            if (lastDrawingTextY != textY) {
                animateTextY = true;
                animateFromTextY = lastDrawingTextY;
                changed = true;
            }

            if (lastDrawingLinkPreviewY != linkPreviewY) {
                animateLinkPreviewY = true;
                animateFromLinkPreviewY = lastDrawingLinkPreviewY;
                changed = true;
            }

            if (lastDrawingPollAddOptionHeight != pollAddButtonHeight) {
                animatePollAddOptionHeight = true;
                animatePollAddOptionHeightFrom = lastDrawingPollAddOptionHeight;
                changed = true;
            }

            if (lastDrawingFactCheckHeight != factCheckHeight) {
                animateFactCheckHeight = true;
                animateFactCheckHeightFrom = lastDrawingFactCheckHeight;
                changed = true;
            }
            if (lastDrawingFactCheckExpanded != (getPrimaryMessageObject() != null && getPrimaryMessageObject().factCheckExpanded)) {
                animateFactCheckExpanded = true;
                changed = true;
            }
            if (lastDrawingFactCheck != hasFactCheck) {
                animateFactCheck = true;
                changed = true;
            }
            if (lastDrawingSummaryReply != drawSummaryReply) {
                animateSummaryReply = true;
                changed = true;
            }

            if (!MessageObject.expandedQuotesEquals(lastDrawingExpandedQuotes, currentMessageObject != null ? currentMessageObject.expandedQuotes : null)) {
                animateExpandedQuotes = true;
                animateExpandedQuotesFrom = lastDrawingExpandedQuotes;
                changed = true;
            }

            if (lastDrawingExpandedExplanation != (currentMessageObject != null && currentMessageObject.expandedExplanation)) {
                animateExpandedExplanation = true;
                expandedExplanationFrom = lastDrawingExpandedExplanation;
                changed = true;
            }

            if (currentMessageObject != null && lastDrawnForwardedName != currentMessageObject.needDrawForwarded()) {
                animateForwardedLayout = true;
                animatingForwardedNameLayout[0] = lastDrawnForwardedNameLayout[0];
                animatingForwardedNameLayout[1] = lastDrawnForwardedNameLayout[1];
                animateForwardNameX = lastForwardNameX;
                animateForwardedNamesOffset = lastForwardedNamesOffset;
                animateForwardNameWidth = lastForwardNameWidth;
                changed = true;
            } else if (lastNamesOffset != namesOffset) {
                animateNamesOffset = true;
                animateNamesOffsetFrom = lastNamesOffset;
                changed = true;
            }
            updateReactionLayoutPosition();
            if (reactionsLayoutInBubble.animateChange()) {
                changed = true;
            }
            if (currentMessageObject.isRoundVideo()) {
                float y1 = layoutHeight - dp(28 - (drawPinnedBottom ? 2 : 0));
                if (!reactionsLayoutInBubble.isEmpty) {
                    y1 -= reactionsLayoutInBubble.totalHeight;
                }
                if (y1 != lastDrawRoundVideoDotY) {
                    animateRoundVideoDotY = true;
                    animateFromRoundVideoDotY = lastDrawRoundVideoDotY;
                    changed = true;
                }
            }

            if (replyNameLayout != null && replyStartX != lastDrawReplyY && lastDrawReplyY != 0) {
                animateReplyY = true;
                animateFromReplyY = lastDrawReplyY;
                changed = true;
            }

            final boolean translated = currentMessageObject != null && currentMessageObject.translated;
            if (translated != lastDrawnTranslated) {
                if (titleLayout != null && lastDrawnTitleLayout != null) {
                    animateTitleLayout = lastDrawnTitleLayout;
                    animateTitleLayoutEmoji = AnimatedEmojiSpan.update(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, ChatMessageCell.this, false, animateTitleLayoutEmoji, animateTitleLayout);
                    changed = true;
                }
            }

            return changed;
        }

        public void onDetach() {
            wasDraw = false;
        }

        public void resetAnimation() {
            animateChange = false;
            animatePinned = false;
            animateBackgroundBoundsInner = false;
            animateBackgroundWidth = false;
            deltaLeft = 0;
            deltaRight = 0;
            deltaBottom = 0;
            deltaTop = 0;
            toDeltaLeft = 0;
            toDeltaRight = 0;
            if (imageChangeBoundsTransition && animateToImageW != 0 && animateToImageH != 0) {
                photoImage.setImageCoords(animateToImageX, animateToImageY, animateToImageW, animateToImageH);
            }
            if (animateRadius) {
                photoImage.setRoundRadius(animateToRadius);
            }
            animateToImageX = 0;
            animateToImageY = 0;
            animateToImageW = 0;
            animateToImageH = 0;
            imageChangeBoundsTransition = false;
            changePinnedBottomProgress = 1f;
            captionEnterProgress = 1f;
            animateRadius = false;
            animateChangeProgress = 1f;
            oldProgress = 0f;
            newProgress = 1f;
            animateMessageText = false;
            animateRichLayout = false;
            animateDrawingSideMenuEnabled = false;
            animateDrawNameLayout = false;
            animateDrawAvatar = false;
            animateDrawTopic = false;
            animateOutTextBlocks = null;
            if (animateOutRichLayout != null) {
                animateOutRichLayout.detach(ChatMessageCell.this);
            }
            animateOutRichLayout = null;
            animateEditedLayout = null;
            animateTimeLayout = null;
            animateEditedEnter = false;
            animateReplaceCaptionLayout = false;
            transformGroupToSingleMessage = false;
            animateOutCaptionLayout = null;
            AnimatedEmojiSpan.release(ChatMessageCell.this, animateOutAnimateEmoji);
            animateOutAnimateEmoji = null;
            moveCaption = false;
            animateDrawingTimeAlpha = false;
            transitionBotButtons.clear();
            animateButton = false;
            animateBotButtonsChanged = false;
            animateWidthForButton = false;
            animateMediaOffsetY = false;
            animateReplyTextLayout = null;

            animateReplies = false;
            animateRepliesLayout = null;

            animateComments = false;
            animateCommentsLayout = null;
            animateViewsLayout = null;
            animateShouldDrawTimeOnMedia = false;
            animateShouldDrawMenuDrawable = false;
            shouldAnimateTimeX = false;
            animateDrawBackground = false;
            animateSign = false;
            animateSmallImage = false;
            animateMonoforumPadding = false;
            animateStarsPriceTopPadding = false;
            animateStarsPriceText = false;
            animateBottomActionText = false;
            animateBottomActionPadding = false;
            needsStopClipping = false;
            animateLinkAbove = false;
            animateMediaAbove = false;
            animateRecommendationsExpanded = false;
            animateDrawingTimeAlpha = false;
            animateLocationIsExpired = false;
            animatePlayingRound = false;
            animateTextY = false;
            animateLinkPreviewY = false;
            animateFactCheckHeight = false;
            animateFactCheckExpanded = false;
            animateExpandedQuotes = false;
            animateExpandedExplanation = false;
            animateFactCheck = false;
            animateSummaryReply = false;
            animateForwardedLayout = false;
            animateNamesOffset = false;
            animatingForwardedNameLayout[0] = null;
            animatingForwardedNameLayout[1] = null;
            animateRoundVideoDotY = false;
            animateReplyY = false;
            reactionsLayoutInBubble.resetAnimation();
            animateTitleLayout = null;
            AnimatedEmojiSpan.release(ChatMessageCell.this, animateTitleLayoutEmoji);
        }

        public boolean supportChangeAnimation() {
            return true;
        }

        public int createStatusDrawableParams() {
            if (currentMessageObject.isOutOwner()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;

                if (currentMessageObject.isSending() || currentMessageObject.isEditing()) {
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (currentMessageObject.isSendError()) {
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                } else if (currentMessageObject.isSent()) {
                    if (!currentMessageObject.scheduled && !currentMessageObject.isUnread()) {
                        drawCheck1 = true;
                    } else {
                        drawCheck1 = false;
                    }
                    drawCheck2 = true;
                    drawClock = false;
                    drawError = false;
                }
                if (currentMessageObject.notime || currentMessageObject.isQuickReply()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                }
                return (drawCheck1 ? 1 : 0) | (drawCheck2 ? 2 : 0) | (drawClock ? 4 : 0) | (drawError ? 8 : 0);
            } else {
                boolean drawClock = currentMessageObject.isSending() || currentMessageObject.isEditing();
                boolean drawError = currentMessageObject.isSendError();

                return (drawClock ? 4 : 0) | (drawError ? 8 : 0);
            }
        }
    }

    public int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    private Drawable getThemedDrawable(String key) {
        Drawable drawable = resourcesProvider != null ? resourcesProvider.getDrawable(key) : null;
        return drawable != null ? drawable : Theme.getThemeDrawable(key);
    }

    public Paint getThemedPaint(String paintKey) {
        Paint paint = resourcesProvider != null ? resourcesProvider.getPaint(paintKey) : null;
        return paint != null ? paint : Theme.getThemePaint(paintKey);
    }

    public boolean hasGradientService() {
        return resourcesProvider != null ? resourcesProvider.hasGradientService() : Theme.hasGradientService();
    }

    private ColorMatrixColorFilter getFancyBlurFilter() {
        if (fancyBlurFilter == null) {
            ColorMatrix colorMatrix = new ColorMatrix();
            AndroidUtilities.multiplyBrightnessColorMatrix(colorMatrix, .9f);
            AndroidUtilities.adjustSaturationColorMatrix(colorMatrix, +.6f);
            fancyBlurFilter = new ColorMatrixColorFilter(colorMatrix);
        }
        return fancyBlurFilter;
    }

    public int getNameStatusX() {
        return (int) (nameX + nameOffsetX + (viaNameWidth > 0 ? viaNameWidth - dp(4 + 28) : nameLayoutWidth) + dp(2) + dp(4 + 12 + 4) / 2);
    }

    public int getNameStatusY() {
        return (int) (nameY + (nameLayout == null ? 0 : nameLayout.getHeight()) / 2);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (channelRecommendationsCell != null) {
            channelRecommendationsCell.computeScroll();
        }
    }

    private ColorFilter[] adaptiveEmojiColorFilter;
    private int[] adaptiveEmojiColor;
    private ColorFilter getAdaptiveEmojiColorFilter(int n, int color) {
        if (adaptiveEmojiColorFilter == null) {
            adaptiveEmojiColor = new int[3];
            adaptiveEmojiColorFilter = new ColorFilter[3];
        }
        if (color != adaptiveEmojiColor[n] || adaptiveEmojiColorFilter[n] == null) {
            adaptiveEmojiColorFilter[n] = new PorterDuffColorFilter(adaptiveEmojiColor[n] = color, PorterDuff.Mode.SRC_IN);
        }
        return adaptiveEmojiColorFilter[n];
    }

    private boolean isDark() {
        if (resourcesProvider != null) {
            return resourcesProvider.isDark();
        }
        return Theme.isCurrentThemeDark();
    }

    public boolean needDrawAvatar() {
        return (
            isChat && !isSavedPreviewChat && (!isThreadPost || isForum) && (
                currentMessageObject != null && !currentMessageObject.isOutOwner() && currentMessageObject.needDrawAvatar()
            ) ||
            currentMessageObject != null && currentMessageObject.getDialogId() == UserObject.VERIFY ||
            currentMessageObject != null && currentMessageObject.forceAvatar ||
            currentMessageObject != null && currentMessageObject.messageOwner.guestchat_via_from != null
        );
    }

    protected boolean drawPhotoImage(Canvas canvas) {
        if (currentMessageObject != null && currentMessageObject.isLivePhoto()) {
            final AnimatedFileDrawable animation = photoImage.getAnimation();
            if (animation != null && animation.getDurationMs() > 0) {
                final float imageAlpha = 1.0f - Utilities.clamp01((animation.getDurationMs() - 90 - animation.getCurrentProgressMs()) / 500.0f);
                if (imageAlpha > 0) {
                    boolean r = true;
                    if (imageAlpha < 1) {
                        r = photoImage.draw(canvas);
                    }
                    photoImage.setForceNotMedia(true);
                    final float wasAlpha = photoImage.getAlpha();
                    photoImage.setAlpha(wasAlpha * imageAlpha);
                    photoImage.draw(canvas);
                    photoImage.setAlpha(wasAlpha);
                    photoImage.setForceNotMedia(false);
                    return r;
                }
            }
        }
        return photoImage.draw(canvas);
    }

    public boolean areTags() {
        MessageObject msg = getPrimaryMessageObject();
        if (msg == null) return false;
        if (msg.messageOwner == null) return false;
        if (msg.messageOwner.reactions == null) return false;
        return msg.messageOwner.reactions.reactions_as_tags;
    }

    public String getFilename() {
        if (currentMessageObject == null)
            return null;
        if (currentMessageObject.type == MessageObject.TYPE_PHOTO) {
            if (currentPhotoObject == null) {
                return null;
            }
            return FileLoader.getAttachFileName(currentPhotoObject);
        } else if (
                currentMessageObject.type == MessageObject.TYPE_GIF ||
                        documentAttachType == DOCUMENT_ATTACH_TYPE_ROUND ||
                        documentAttachType == DOCUMENT_ATTACH_TYPE_VIDEO ||
                        documentAttachType == DOCUMENT_ATTACH_TYPE_WALLPAPER ||
                        currentMessageObject.type == MessageObject.TYPE_FILE ||
                        documentAttachType == DOCUMENT_ATTACH_TYPE_AUDIO ||
                        documentAttachType == DOCUMENT_ATTACH_TYPE_MUSIC
        ) {
            if (currentMessageObject.useCustomPhoto) {
                return null;
            }
            if (currentMessageObject.attachPathExists && !TextUtils.isEmpty(currentMessageObject.messageOwner.attachPath)) {
                return currentMessageObject.messageOwner.attachPath;
            } else if (!currentMessageObject.isSendError() || documentAttachType == DOCUMENT_ATTACH_TYPE_AUDIO || documentAttachType == DOCUMENT_ATTACH_TYPE_MUSIC) {
                return currentMessageObject.getFileName();
            }
        } else if (documentAttachType != DOCUMENT_ATTACH_TYPE_NONE) {
            return FileLoader.getAttachFileName(documentAttach);
        } else if (currentPhotoObject != null) {
            return FileLoader.getAttachFileName(currentPhotoObject);
        }
        return null;
    }

    public boolean checkLoadCaughtPremiumFloodWait() {
        return FileLoader.getInstance(currentAccount).checkLoadCaughtPremiumFloodWait(getFilename());
    }

    public boolean checkUploadCaughtPremiumFloodWait() {
        return FileLoader.getInstance(currentAccount).checkUploadCaughtPremiumFloodWait(getFilename());
    }

    public TLRPC.TL_availableEffect getEffect() {
        if (currentPosition != null && !currentPosition.last) {
            return null;
        }
        if (currentMessageObject != null) {
            return currentMessageObject.getEffect();
        }
        return null;
    }

    private int layoutFactCheck(final int maxTextWidth) {
        MessageObject msg = getPrimaryMessageObject();
        int addheight = 0;
        factCheckHeight = 0;
        factCheckWidth = 0;
        hasFactCheck = (currentPosition == null || (currentPosition.flags & MessageObject.POSITION_FLAG_BOTTOM) != 0) && msg != null && msg.getFactCheck() != null && !msg.isRepostPreview;
        if (hasFactCheck) {
            TLRPC.TL_factCheck factCheck = msg.getFactCheck();
            CharSequence text = msg.getFactCheckText();
            if (factCheck.need_check || text == null) {
                hasFactCheck = false;
            } else {
                factCheckHeight += dp(4.66f);

                factCheckTitle = new Text(getString(R.string.FactCheck), 14, AndroidUtilities.bold());
                factCheckWhat = new Text(getString(R.string.FactCheckWhat), 11);
                factCheckHeight += dp(17.33f);
                factCheckWidth = (int) (dp(20) + factCheckTitle.getCurrentWidth() + factCheckWhat.getCurrentWidth() + dp(18));

                String country;
                try {
                    country = new Locale("", factCheck.country).getDisplayCountry(LocaleController.getInstance().getCurrentLocale());
                } catch (Exception e) {
                    FileLog.e(e);
                    country = factCheck.country;
                }

                factCheckTextLayout = StaticLayoutEx.createStaticLayout(text, Theme.chat_replyTextPaint, maxTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, dp(1), false, TextUtils.TruncateAt.END, maxTextWidth, 99999);
                factCheckText2Layout = StaticLayoutEx.createStaticLayout(formatString(R.string.FactCheckFooter, country), Theme.chat_titleLabelTextPaint, maxTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, dp(1), false, TextUtils.TruncateAt.END, maxTextWidth, 99999);
                factCheckTextLayoutLeft = factCheckTextLayout.getWidth();
                int factCheckTextLayoutRight = 0;
                for (int a = 0; a < factCheckTextLayout.getLineCount(); a++) {
                    factCheckTextLayoutLeft = (int) Math.min(factCheckTextLayoutLeft, factCheckTextLayout.getLineLeft(a));
                    factCheckTextLayoutRight = (int) Math.max(factCheckTextLayoutRight, factCheckTextLayout.getLineRight(a));
                }
                int factCheckTextLayoutWidth = Math.abs(factCheckTextLayoutRight - factCheckTextLayoutLeft);
//                factCheckTextLayoutLastLineEnd = factCheckLarge && factCheckTextLayout.getLineRight(factCheckTextLayout.getLineCount() - 1) - factCheckTextLayoutLeft > factCheckTextLayoutWidth - dp(30);
                factCheckWidth = Math.max(factCheckWidth, factCheckTextLayoutWidth + dp(20));

                factCheckText2LayoutLeft = factCheckText2Layout.getWidth();
                int factCheckText2LayoutRight = 0;
                for (int a = 0; a < factCheckText2Layout.getLineCount(); a++) {
                    factCheckText2LayoutLeft = (int) Math.min(factCheckText2LayoutLeft, factCheckText2Layout.getLineLeft(a));
                    factCheckText2LayoutRight = (int) Math.max(factCheckText2LayoutRight, factCheckText2Layout.getLineRight(a));
                }
                int factCheckText2LayoutWidth = Math.abs(factCheckText2LayoutRight - factCheckText2LayoutLeft);
                factCheckWidth = Math.max(factCheckWidth, factCheckText2LayoutWidth + dp(20));

                int height = (factCheckTextLayoutHeight = factCheckTextLayout.getLineBottom(factCheckTextLayout.getLineCount() - 1)) + dp(12.66f) + factCheckText2Layout.getLineBottom(factCheckText2Layout.getLineCount() - 1);
                int limit = (int) (3.5f * Theme.chat_replyTextPaint.getTextSize() * 1.4f);
                factCheckLarge = factCheckTextLayout.getLineCount() > 3 && height + dp(10) > limit;
                factCheckTextLayoutLastLineEnd = factCheckLarge && factCheckText2Layout.getLineRight(factCheckText2Layout.getLineCount() - 1) - factCheckText2LayoutLeft > factCheckWidth - dp(50);
                if (factCheckLarge && !msg.factCheckExpanded) {
                    if (height < limit) {
                        factCheckLarge = false;
                    }
                    height = Math.min(limit, height);
                }
                if (factCheckTextLayoutLastLineEnd) {
                    height += Theme.chat_replyTextPaint.getTextSize() * 1.3f;
                }
                factCheckHeight += height;

                factCheckHeight += dp(6.66f);

                addheight += dp(2);
            }
        }
        return hasFactCheck ? factCheckHeight + addheight : 0;
    }

    public int getWidthForButtons() {
        if (transitionParams.animateWidthForButton) {
            return lerp(transitionParams.animateFromWidthForButton, widthForButtons, transitionParams.animateChangeProgress);
        }
        return widthForButtons;
    }

    public void drawVideoTimestamps(Canvas canvas, int color) {
        if (currentMessageObject == null || currentMessageObject.isLivePhoto() || controlsAlpha <= 0 || !photoImage.getVisible()) return;
        float progress;
        if (!currentMessageObject.openedInViewer && currentMessageObject.getVideoStartsTimestamp() != -1) {
            progress = currentMessageObject.getVideoStartsTimestamp() / (float) currentMessageObject.getDuration();
        } else {
            progress = currentMessageObject.getVideoSavedProgress();
        }
        progress = Utilities.clamp01(progress);
//        if (startsAtText != null && controlsAlpha > 0 && photoImage.getVisible()) {
//            final int w = dp(28) + (int) startsAtText.getCurrentWidth(), h = dp(17);
//            final float left = photoImage.getImageX() + dp(5), bottom = photoImage.getImageY2() - dp(5);
//            rect.set(left, bottom - h, left + w, bottom);
//            int oldAlpha = timeBackgroundPaint.getAlpha();
//            timeBackgroundPaint.setAlpha((int) (oldAlpha * controlsAlpha));
//            canvas.drawRoundRect(rect, dp(9), dp(9), timeBackgroundPaint);
//            timeBackgroundPaint.setAlpha(oldAlpha);
//            startsAtDrawable.setBounds((int) left + dp(6.33f), (int) bottom - h / 2 - startsAtDrawable.getIntrinsicHeight() / 2, (int) left + dp(6.33f) + startsAtDrawable.getIntrinsicWidth(), (int) bottom - h / 2 + startsAtDrawable.getIntrinsicHeight() / 2);
//            startsAtDrawable.setAlpha((int) (0xFF * controlsAlpha));
//            startsAtDrawable.draw(canvas);
//            startsAtText.draw(canvas, left + dp(22.66f), bottom - h / 2, 0xFFFFFFFF, controlsAlpha);
//        } else
        if (progress > 0) {
            final int[] r = photoImage.getRoundRadius();
            canvas.save();
            if (r[0] <= 0 && r[1] <= 0 && r[2] <= 0 && r[3] <= 0) {
                canvas.clipRect(photoImage.getImageX(), photoImage.getImageY(), photoImage.getImageX2(), photoImage.getImageY2());
            } else {
                if (photoImageClipPath == null) {
                    photoImageClipPath = new Path();
                    photoImageClipPathRadii = new float[8];
                }
                photoImageClipPathRadii[0] = photoImageClipPathRadii[1] = Math.max(0, r[0]);
                photoImageClipPathRadii[2] = photoImageClipPathRadii[3] = Math.max(0, r[1]);
                photoImageClipPathRadii[4] = photoImageClipPathRadii[5] = Math.max(0, r[2]);
                photoImageClipPathRadii[6] = photoImageClipPathRadii[7] = Math.max(0, r[3]);
                photoImageClipPath.rewind();
                AndroidUtilities.rectTmp.set(photoImage.getImageX(), photoImage.getImageY(), photoImage.getImageX2(), photoImage.getImageY2());
                photoImageClipPath.addRoundRect(AndroidUtilities.rectTmp, photoImageClipPathRadii, Path.Direction.CW);
                canvas.clipPath(photoImageClipPath);
            }

            Theme.chat_videoProgressPaint.setColor(Theme.multAlpha(Color.WHITE, .35f * controlsAlpha));
            canvas.drawRect(photoImage.getImageX(), photoImage.getImageY2() - dp(3), photoImage.getImageX2(), photoImage.getImageY2(), Theme.chat_videoProgressPaint);
            Theme.chat_videoProgressPaint.setColor(Theme.multAlpha(color, controlsAlpha));
            AndroidUtilities.rectTmp.set(photoImage.getImageX() - dp(2), photoImage.getImageY2() - dp(3), photoImage.getImageX() + photoImage.getImageWidth() * progress, photoImage.getImageY2());
            canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(2), dp(2), Theme.chat_videoProgressPaint);

            canvas.restore();
        }
    }

    public long getStarsPrice() {
        if (currentMessagesGroup != null) {
            long totalPrice = 0;
            for (MessageObject msg : currentMessagesGroup.messages) {
                final long messagePrice = msg == null || msg.messageOwner == null ? 0 : msg.messageOwner.paid_message_stars;
                totalPrice += messagePrice;
            }
            return totalPrice;
        } else {
            return currentMessageObject == null || currentMessageObject.messageOwner == null ? 0 : currentMessageObject.messageOwner.paid_message_stars;
        }
    }

    private int getNameHeight() {
        if (drawNameAvatar) {
            if (adminLayout == null) {
                return dp(5 + 26);
            } else {
                return dp(5 + 26 + 6.66f);
            }
        } else {
            return (int) (dp(5) + Theme.chat_namePaint.getTextSize());
        }
    }

    private float getNameHeightAnimated() {
        final float avatarAlpha;
        if (transitionParams.animateDrawAvatar) {
            avatarAlpha = lerp(!drawNameAvatar, drawNameAvatar, transitionParams.animateChangeProgress);
        } else {
            avatarAlpha = drawNameAvatar ? 1.0f : 0.0f;
        }
        return lerp(
            (dp(5) + Theme.chat_namePaint.getTextSize()),
            dp(adminLayout == null ? 5 + 26 : 5 + 26 + 4),
            avatarAlpha
        );
    }

    private TL_stars.StarGift instantViewTypeIsGiftAuction;

    
    
    private static boolean isSmallImageLinkPreviewType(String type) {
        return "app".equals(type) || "profile".equals(type) ||
                "article".equals(type) || "telegram_bot".equals(type) ||
                "telegram_user".equals(type) || "telegram_channel".equals(type) || "telegram_channel_direct".equals(type) ||
                "telegram_megagroup".equals(type) || "telegram_voicechat".equals(type) || "telegram_videochat".equals(type) ||
                "telegram_livestream".equals(type) || "telegram_channel_boost".equals(type) || "telegram_group_boost".equals(type) ||
                "telegram_aicomposetone".equals(type);
    }
    
    private static void normalizePollPercents(boolean hasDifferent, int restPercent, ArrayList<PollButton> sortedPollButtons) {
        if (!hasDifferent || restPercent == 0 || sortedPollButtons == null || sortedPollButtons.isEmpty()) {
            return;
        }

        Collections.sort(sortedPollButtons, (o1, o2) -> {
            if (o1.decimal > o2.decimal) {
                return -1;
            } else if (o1.decimal < o2.decimal) {
                return 1;
            }
            if (o1.percent > o2.percent) {
                return 1;
            } else if (o1.percent < o2.percent) {
                return -1;
            }
            return 0;
        });
        for (int i = 0, n = sortedPollButtons.size(); i < n; i++) {
            if (restPercent <= 0) {
                break;
            }

            PollButton b = sortedPollButtons.get(i);
            if (b.percent > 0) {
                b.percent += 1;
                restPercent--;
            }
        }
    }

    public boolean getPollAddButtonBounds(Rect bounds) {
        if (pollAddButtonDrawable != null && pollAllowAdding) {
            bounds.set(pollAddButtonDrawable.getBounds());
            return true;
        }
        return false;
    }

    private static void clearBlurredImage(ImageReceiver blurredPhotoImage) {
        if (blurredPhotoImage == null) {
            return;
        }
        Bitmap bitmap = blurredPhotoImage.getBitmap();
        if (bitmap != null) {
            bitmap.recycle();
            blurredPhotoImage.setImageBitmap((Bitmap) null);
        }
    }

    private static String getCallMessageText(MessageObject messageObject, boolean isMissed, boolean isBusy, boolean video) {
        String text;
        if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionConferenceCall) {
            if (messageObject.isOutOwner()) {
                text = getString(R.string.ConferenceCallOutgoing);
            } else if (isMissed) {
                text = getString(R.string.ConferenceCallMissed);
            } else {
                text = getString(R.string.ConferenceCallIncoming);
            }
        } else if (messageObject.isOutOwner()) {
            if (isMissed) {
                if (video) {
                    text = getString(R.string.CallMessageVideoOutgoingMissed);
                } else {
                    text = getString(R.string.CallMessageOutgoingMissed);
                }
            } else {
                if (video) {
                    text = getString(R.string.CallMessageVideoOutgoing);
                } else {
                    text = getString(R.string.CallMessageOutgoing);
                }
            }
        } else {
            if (isMissed) {
                if (video) {
                    text = getString(R.string.CallMessageVideoIncomingMissed);
                } else {
                    text = getString(R.string.CallMessageIncomingMissed);
                }
            } else if (isBusy) {
                if (video) {
                    text = getString(R.string.CallMessageVideoIncomingDeclined);
                } else {
                    text = getString(R.string.CallMessageIncomingDeclined);
                }
            } else {
                if (video) {
                    text = getString(R.string.CallMessageVideoIncoming);
                } else {
                    text = getString(R.string.CallMessageIncoming);
                }
            }
        }
        return text;
    }
}
