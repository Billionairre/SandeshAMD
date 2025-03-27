package edu.sandesh.mealmate.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Random;

public class CaptchaGenerator {
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    
    public static class CaptchaResult {
        public final Bitmap image;
        public final String text;
        
        public CaptchaResult(Bitmap image, String text) {
            this.image = image;
            this.text = text;
        }
    }
    
    public static CaptchaResult generateCaptcha() {
        int width = 300;
        int height = 100;
        
        // Generate random text
        Random random = new Random();
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            captchaText.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        
        // Create bitmap and canvas
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Set background
        canvas.drawColor(Color.WHITE);
        
        // Initialize paint for text
        Paint textPaint = new Paint();
        textPaint.setTextSize(50);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        
        // Draw noise (random lines)
        Paint noisePaint = new Paint();
        noisePaint.setStrokeWidth(2);
        noisePaint.setAntiAlias(true);
        
        for (int i = 0; i < 8; i++) {
            noisePaint.setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            canvas.drawLine(
                random.nextInt(width), random.nextInt(height),
                random.nextInt(width), random.nextInt(height),
                noisePaint
            );
        }
        
        // Draw wavy text
        float x = 30;
        for (int i = 0; i < captchaText.length(); i++) {
            textPaint.setColor(Color.rgb(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            canvas.save();
            float angle = (random.nextFloat() - 0.5f) * 30; // Random rotation between -15 and 15 degrees
            canvas.rotate(angle, x, height/2f);
            canvas.drawText(String.valueOf(captchaText.charAt(i)), x, height/2f + 15, textPaint);
            canvas.restore();
            x += textPaint.measureText(String.valueOf(captchaText.charAt(i))) + 10;
        }
        
        return new CaptchaResult(bitmap, captchaText.toString());
    }
} 