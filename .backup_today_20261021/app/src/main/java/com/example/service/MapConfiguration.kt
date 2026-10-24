package com.example.service

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.File

/**
 * Centralized Map Configuration for PawRescue.
 * Implements OpenStreetMap Foundation Tile Usage Policy:
 * - Proper unique User-Agent
 * - HTTPS tile endpoints
 * - Tile caching on internal app storage
 * - Clear attribution requirement
 * - Configurable tile source provider
 */
object MapConfiguration {

    const val OSM_ATTRIBUTION = "© OpenStreetMap contributors"
    const val USER_AGENT = "PawRescue-Mobile-App/1.0 (Android; Rescue Incident Intake; https://pawrescue.org)"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return

            val appCtx = context.applicationContext
            val osmConfig = Configuration.getInstance()

            // 1. Set required User-Agent
            osmConfig.userAgentValue = USER_AGENT

            // 2. Set dedicated cache directory respecting storage policies
            val osmCacheDir = File(appCtx.cacheDir, "osmdroid_tiles").apply {
                if (!exists()) mkdirs()
            }
            val osmBaseDir = File(appCtx.filesDir, "osmdroid").apply {
                if (!exists()) mkdirs()
            }
            osmConfig.osmdroidBasePath = osmBaseDir
            osmConfig.osmdroidTileCache = osmCacheDir

            // 3. Tile cache size limits to prevent runaway storage usage (max 50 MB)
            osmConfig.tileFileSystemCacheMaxBytes = 50L * 1024L * 1024L
            osmConfig.tileFileSystemCacheTrimBytes = 40L * 1024L * 1024L

            isInitialized = true
        }
    }

    /**
     * Default free, open-source tile source (OpenStreetMap Standard MAPNIK over HTTPS).
     * Can be configured or swapped at runtime without altering UI code.
     */
    fun getDefaultTileSource(): ITileSource {
        return TileSourceFactory.MAPNIK
    }
}
