package com.bookshop.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageLoader {
    
    private static final String DEFAULT_BOOK_IMAGE = "/images/default_book.png";
    
    public static void loadImage(String url, ImageView imageView) {
        try {
            Image image;
            if (url != null && !url.isEmpty()) {
                image = new Image(url, true);
            
                if (image.isError()) {
                    image = new Image(url.startsWith("/") ? url : "/" + url);
                }
            } else {
                image = new Image(DEFAULT_BOOK_IMAGE);
            }
            
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(DEFAULT_BOOK_IMAGE));
        }
    }
} 