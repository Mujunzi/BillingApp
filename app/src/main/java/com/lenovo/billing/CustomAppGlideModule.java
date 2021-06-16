package com.lenovo.billing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class CustomAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {

        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setBitmapPoolScreens(3)
                .build();

        int memorySize = calculator.getMemoryCacheSize();
//        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20mb
        int memoryCacheSizeBytes = 10; // 20mb
        memoryCacheSizeBytes = memorySize > memoryCacheSizeBytes ? memorySize : memoryCacheSizeBytes;

        int diskCacheSizeBytes = 1024 * 1024 * 100;  //100 MB

        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes))
                .setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return true;
    }

}
