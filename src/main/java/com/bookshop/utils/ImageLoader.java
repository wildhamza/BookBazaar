package com.bookshop.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class for loading images in JavaFX.
 */
public class ImageLoader {
    
    private static final String DEFAULT_BOOK_IMAGE = "/images/default_book.png";
    
    public static void loadImage(String url, ImageView imageView) {
        try {
            Image image;
            if (url != null && !url.isEmpty()) {
                // Try to load from URL
                image = new Image(url, true);
                
                // If URL fails, try loading as resource
                if (image.isError()) {
                    image = new Image(url.startsWith("/") ? url : "/" + url);
                }
            } else {
                // Load default image
                image = new Image(DEFAULT_BOOK_IMAGE);
            }
            
            imageView.setImage(image);
        } catch (Exception e) {
            // If any error occurs, load default image
            imageView.setImage(new Image(DEFAULT_BOOK_IMAGE));
        }
    }
} 