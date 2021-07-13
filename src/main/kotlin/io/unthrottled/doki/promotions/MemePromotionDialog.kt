package io.unthrottled.doki.promotions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser.installAndEnable
import com.intellij.ui.JBColor
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import io.unthrottled.doki.assets.AssetCategory
import io.unthrottled.doki.assets.AssetManager
import io.unthrottled.doki.assets.AssetManager.ASSET_SOURCE
import io.unthrottled.doki.assets.AssetManager.FALLBACK_ASSET_SOURCE
import io.unthrottled.doki.icon.DokiIcons
import io.unthrottled.doki.service.AMII_PLUGIN_ID
import io.unthrottled.doki.themes.DokiTheme
import io.unthrottled.doki.util.toHexString
import java.awt.Dimension
import java.awt.Window
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JTextPane
import javax.swing.event.HyperlinkEvent

class PromotionAssets(
  private val dokiTheme: DokiTheme
) {

  val pluginLogoURL: String
  val promotionAssetURL: String

  init {
    pluginLogoURL = getPluginLogo()
    promotionAssetURL = getPromotionAsset()
  }

  private fun getPluginLogo(): String = AssetManager.resolveAssetUrl(
    AssetCategory.PROMOTION,
    "amii/logo.png"
  ).orElse("$ASSET_SOURCE/promotion/amii/logo.png")

  private fun getPromotionAsset(): String =
    AssetManager.resolveAssetUrl(AssetCategory.PROMOTION, "motivator/${dokiTheme.displayName.toLowerCase()}.gif")
      .orElseGet {
        AssetManager.resolveAssetUrl(AssetCategory.PROMOTION, "motivator/promotion.gif")
          .orElse("$FALLBACK_ASSET_SOURCE/promotion/motivator/promotion.gif")
      }
}

class AniMemePromotionDialog(
  private val dokiTheme: DokiTheme,
  private val promotionAssets: PromotionAssets,
  parent: Window,
  private val onPromotion: (PromotionResults) -> Unit
) : DialogWrapper(parent, true) {

  companion object {
    private const val INSTALLED_EXIT_CODE = 69
  }

  init {
    title = MessageBundle.message("amii.name")
    setCancelButtonText(MessageBundle.message("promotion.action.cancel"))
    setDoNotAskOption(
      DoNotPromote { shouldContinuePromotion, exitCode ->
        onPromotion(
          PromotionResults(
            when {
              !shouldContinuePromotion -> PromotionStatus.BLOCKED
              exitCode == INSTALLED_EXIT_CODE -> PromotionStatus.ACCEPTED
              else -> PromotionStatus.REJECTED
            }
          )
        )
      }
    )
    init()
  }

  override fun createActions(): Array<Action> {
    return arrayOf(
      buildInstallAction(),
      cancelAction
    )
  }

  private fun buildInstallAction(): AbstractAction {
    return object : AbstractAction() {
      init {
        val message = MessageBundle.message("promotion.action.ok")
        putValue(NAME, message)
        putValue(SMALL_ICON, DokiIcons.Plugins.AMII.TOOL_WINDOW)
      }

      override fun actionPerformed(e: ActionEvent) {
        installAndEnable(
          setOf(
            PluginId.getId(AMII_PLUGIN_ID)
          )
        ) {
          close(INSTALLED_EXIT_CODE, true)
        }
      }
    }
  }

  override fun createCenterPanel(): JComponent? {
    val promotionPane = buildPromotionPane()
    return panel {
      row {
        promotionPane()
      }
    }
  }

  @Suppress("LongMethod")
  private fun buildPromotionPane(): JEditorPane {
    val pane = JTextPane()
    pane.isEditable = false
    pane.contentType = "text/html"
    val accentHex = JBColor.namedColor(
      DokiTheme.ACCENT_COLOR,
      UIUtil.getTextAreaForeground()
    ).toHexString()
    val infoForegroundHex = UIUtil.getContextHelpForeground().toHexString()
    val motivatorLogoURL = promotionAssets.pluginLogoURL
    val promotionAssetURL = promotionAssets.promotionAssetURL
    pane.background = JBColor.namedColor(
      "Menu.background",
      UIUtil.getEditorPaneBackground()
    )
    pane.text =
      """
      <html lang="en">
      <head>
          <style type='text/css'>
              body {
                font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
              }
              .center {
                text-align: center;
              }
              a {
                  color: $accentHex;
                  font-weight: bold;
              }
              p {
                color: ${UIUtil.getLabelForeground().toHexString()};
              }
              h2 {
                margin: 16px 0;
                font-weight: bold;
                font-size: 22px;
              }
              h3 {
                margin: 4px 0;
                font-weight: bold;
                font-size: 14px;
              }
              .accented {
                color: $accentHex;
              }
              .info-foreground {
                color: $infoForegroundHex;
                text-align: center;
              }
              .header {
                color: $accentHex;
                text-align: center;
              }
              .logo-container {
                margin-top: 8px;
                text-align: center;
              }
              .display-image {
                max-height: 256px;
                text-align: center;
              }
          </style>
          <title>Motivator</title>
      </head>
      <body>
      <div class='logo-container'><img src="$motivatorLogoURL" class='display-image' alt='Motivator Plugin Logo'/> 
      </div>
      <h2 class='header'>Your new virtual companion!</h2>
      <div style='margin: 8px 0 0 100px'>
        <p>
          <a href='https://plugins.jetbrains.com/plugin/15865-amii'>AMII</a>
          gives your IDE more personality by using anime memes. <br/> You will get an assistant that will interact 
          with you as you build code.
          <br/>Such as when your programs fail to run or tests pass/fail. Your companion<br/> 
          has the ability to react to these events. Which will most likely take the form <br/> of a reaction gif of 
          your favorite character(s)!
        </p>
      </div>
      <br/>
      <h3 class='info-foreground'>Bring Anime Memes to your IDE today!</h3>
      <br/>
      <div class='display-image'><img src='$promotionAssetURL' height="150" 
      alt='${dokiTheme.displayName}&#39;s Promotion Asset'/></div>
      <br/>
      </body>
      </html>
      """.trimIndent()
    pane.preferredSize = Dimension(pane.preferredSize.width + 120, pane.preferredSize.height)
    pane.addHyperlinkListener {
      if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        BrowserUtil.browse(it.url)
      }
    }
    return pane
  }
}

class DoNotPromote(
  private val onToBeShown: (Boolean, Int) -> Unit
) : DialogWrapper.DoNotAskOption {
  override fun isToBeShown(): Boolean = true

  override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
    onToBeShown(toBeShown, exitCode)
  }

  override fun canBeHidden(): Boolean = true

  override fun shouldSaveOptionsOnCancel(): Boolean = true

  override fun getDoNotShowMessage(): String =
    MessageBundle.message("promotions.dont.ask")
}
