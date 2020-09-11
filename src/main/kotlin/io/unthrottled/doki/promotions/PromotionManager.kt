package io.unthrottled.doki.promotions

import com.google.gson.GsonBuilder
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.io.exists
import io.unthrottled.doki.assets.AssetCategory
import io.unthrottled.doki.assets.AssetManager
import io.unthrottled.doki.assets.LocalStorageService.createDirectories
import io.unthrottled.doki.config.ThemeConfig
import io.unthrottled.doki.notification.UpdateNotification
import io.unthrottled.doki.stickers.StickerLevel
import io.unthrottled.doki.themes.DokiTheme
import io.unthrottled.doki.themes.ThemeManager
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Instant

object PromotionManager {

  private val log = Logger.getInstance(PromotionManager::class.java)

  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

  private var initalized = false

  private val ledgerPath = AssetManager.constructLocalAssetPath(
    AssetCategory.PROMOTION,
    "ledger.json"
  )

  private val promotionLedger: PromotionLedger =
    if (ledgerPath.exists()) {
      readLedger()
    } else {
      PromotionLedger(mutableMapOf())
    }

  private fun readLedger(): PromotionLedger {
    return try {
      Files.newInputStream(ledgerPath)
        .use {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            PromotionLedger::class.java
          )
        }
    } catch (e: Throwable) {
      log.warn("Unable to read promotion ledger for raisins.", e)
      PromotionLedger(mutableMapOf())
    }
  }

  fun registerPromotion(newVersion: String) {
    if (initalized.not()) {
      promotionRegistry(newVersion)
    }
  }

  private fun promotionRegistry(newVersion: String) {
    val versionInstallDates = promotionLedger.versionInstallDates
    if (versionInstallDates.containsKey(newVersion).not()) {
      versionInstallDates[newVersion] = Instant.now()
      persistLedger()
    } else {
//      val latestInstallDate = versionInstallDates[newVersion]!!
//      if (Duration.between(latestInstallDate, Instant.now()).toDays() > 2) {
      setupPromotion()
//      }
    }
  }

  private fun setupPromotion() {
    if (isMotivatorInstalled().not() && shouldPromote()) {
      MotivatorPluginPromotion {
        // mark promoted
      }
    }
  }

  // todo: has been promoted as well
  private fun shouldPromote(): Boolean =
    ThemeConfig.instance.currentStickerLevel == StickerLevel.ON

  private fun isMotivatorInstalled(): Boolean =
    PluginManagerCore.getPlugin(
      PluginId.getId("zd.zero.waifu-motivator-plugin")
    ) != null

  private fun persistLedger() {
    if (ledgerPath.exists().not()) {
      createDirectories(ledgerPath)
    }

    try {
      Files.newBufferedWriter(
        ledgerPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use {
        it.write(
          gson.toJson(promotionLedger)
        )
      }
    } catch (e: Throwable) {
      log.warn("Unable to persist ledger for raisins", e)
    }
  }
}

class MotivatorPluginPromotion(
  private val onPromotion: () -> Unit
) : Runnable {

  init {
    IdeEventQueue.getInstance().addIdleListener(
      this,
      5000
//      TimeUnit.MILLISECONDS.convert(
//        5,
//        TimeUnit.MINUTES
//      ).toInt()
    )

  }

  override fun run() {
    ThemeManager.instance.currentTheme.ifPresent { dokiTheme ->
      val themeId = dokiTheme.id
      val promotionAsset = getPromotionAsset(dokiTheme)
      UpdateNotification.displayPromotionMessage(
        ProjectManager.getInstance().openProjects.first(),
        """
          <div style="margin: 5px 5px 5px 10px; width: 400px; height: 400px" >
              <h2>Do you want more ${dokiTheme.displayName}?</h2>
              <p>The <a href='https://plugins.jetbrains.com/plugin/13381-waifu-motivator'>Waifu Motivator Plugin</a>
              gives you a virtual companion.</p>
              <p>Your companion will interact with you as code is being built.</p>
              <p>These reactions are collection of various anime memes and gifs, most of which include your favorite character!</p>
              <br/>
              <img src='https://doki.assets.unthrottled.io/misc/update_celebration.gif' alt='momsspaghetti'/>
          </div>
        """.trimIndent()
      )
    }

    IdeEventQueue.getInstance().removeIdleListener(this)
  }

  private fun getPromotionAsset(dokiTheme: DokiTheme): String {
    return when (dokiTheme.id) {
      else -> "promotion.gif"
    }
  }
}

data class PromotionLedger(
  val versionInstallDates: MutableMap<String, Instant>
)