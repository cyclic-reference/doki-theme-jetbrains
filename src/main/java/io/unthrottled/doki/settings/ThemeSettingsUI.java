package io.unthrottled.doki.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import io.unthrottled.doki.config.ThemeConfig;
import io.unthrottled.doki.stickers.CurrentSticker;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.util.Arrays;

public class ThemeSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {
  private final ThemeSettingsModel themeSettingsModel = ThemeSettings.createThemeSettingsModel();
  private ThemeSettingsModel initialThemeSettingsModel = ThemeSettings.createThemeSettingsModel();

  private JTabbedPane fontTabPane;
  private JPanel rootPane;
  private JComboBox currentThemeWomboComboBox;
  private JCheckBox showStickerCheckBox;
  private JCheckBox emptyEditorBackgroundCheckBox;
  private JRadioButton primaryRadioButton;
  private JRadioButton secondaryRadioButton;
  private JCheckBox allowPositioningCheckBox;
  private JCheckBox backgroundWallpaperCheckBox;
  private DialogPanel materialIconPane;
  private JLabel warningLabel;
  private JCheckBox nameInStatusBarCheckBox;
  private JCheckBox framelessModeMacOSOnlyCheckBox;
  private JButton chooseImageButton;
  private JCheckBox useCustomStickerCheckBox;
  private JTextPane generalLinks;
  private JSlider notificationOpacitySlider;
  private JCheckBox makeNotificationsTransparentCheckBox;
  private JCheckBox themeChangeAnimationCheckBox;
  private JSpinner customFontSize;
  private JCheckBox overrideEditorFontSizeCheckBox;


  @Override
  public @NotNull
  @NonNls
  String getId() {
    return ThemeSettings.SETTINGS_ID;
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return ThemeSettings.THEME_SETTINGS_DISPLAY_NAME;
  }

  @Override
  public @Nullable JComponent createComponent() {
    initializeAutoCreatedComponents();
    return rootPane;
  }

  private void initializeAutoCreatedComponents() {
    chooseImageButton.addActionListener(e -> {
      CustomStickerChooser dialog = new CustomStickerChooser(
        Arrays.stream(ProjectManager.getInstance().getOpenProjects()).findFirst().orElse(
          ProjectManager.getInstance().getDefaultProject()
        ),
        themeSettingsModel.getCustomStickerPath()
      );

      dialog.showAndGet();

      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        themeSettingsModel.setCustomStickerPath(dialog.getPath());
      }
    });

    useCustomStickerCheckBox.setSelected(initialThemeSettingsModel.isCustomSticker());
    useCustomStickerCheckBox.addChangeListener(e ->
      themeSettingsModel.setCustomSticker(useCustomStickerCheckBox.isSelected())
    );

    warningLabel.setForeground(UIUtil.getContextHelpForeground());

    showStickerCheckBox.setSelected(initialThemeSettingsModel.getAreStickersEnabled());
    showStickerCheckBox.addActionListener(e ->
      themeSettingsModel.setAreStickersEnabled(showStickerCheckBox.isSelected())
    );

    allowPositioningCheckBox.setSelected(initialThemeSettingsModel.isMoveableStickers());
    allowPositioningCheckBox.addActionListener(e ->
      themeSettingsModel.setMoveableStickers(allowPositioningCheckBox.isSelected())
    );

    primaryRadioButton.setSelected(ThemeConfig.Companion.getInstance().getCurrentSticker() == CurrentSticker.DEFAULT);
    primaryRadioButton.addActionListener(e ->
      themeSettingsModel.setCurrentSticker(
        primaryRadioButton.isSelected() ? CurrentSticker.DEFAULT : CurrentSticker.SECONDARY
      )
    );
    secondaryRadioButton.setSelected(ThemeConfig.Companion.getInstance().getCurrentSticker() == CurrentSticker.SECONDARY);
    secondaryRadioButton.addActionListener(e ->
      themeSettingsModel.setCurrentSticker(
        secondaryRadioButton.isSelected() ? CurrentSticker.SECONDARY : CurrentSticker.DEFAULT
      )
    );

    backgroundWallpaperCheckBox.setSelected(initialThemeSettingsModel.isDokiBackground());
    backgroundWallpaperCheckBox.addActionListener(e ->
      themeSettingsModel.setDokiBackground(backgroundWallpaperCheckBox.isSelected())
    );

    emptyEditorBackgroundCheckBox.setSelected(initialThemeSettingsModel.isEmptyFrameBackground());
    emptyEditorBackgroundCheckBox.addActionListener(e ->
      themeSettingsModel.setEmptyFrameBackground(emptyEditorBackgroundCheckBox.isSelected())
    );

    nameInStatusBarCheckBox.setSelected(initialThemeSettingsModel.getShowThemeStatusBar());
    nameInStatusBarCheckBox.addActionListener(e ->
      themeSettingsModel.setShowThemeStatusBar(nameInStatusBarCheckBox.isSelected())
    );

    framelessModeMacOSOnlyCheckBox.setSelected(initialThemeSettingsModel.isThemedTitleBar());
    framelessModeMacOSOnlyCheckBox.addActionListener(e ->
      themeSettingsModel.setThemedTitleBar(framelessModeMacOSOnlyCheckBox.isSelected())
    );

    overrideEditorFontSizeCheckBox.setSelected(initialThemeSettingsModel.isCustomFontSize());
    overrideEditorFontSizeCheckBox.addActionListener(e ->
      themeSettingsModel.setCustomFontSize(overrideEditorFontSizeCheckBox.isSelected()));

    SpinnerNumberModel customFontSizeModel = new SpinnerNumberModel(
      initialThemeSettingsModel.getCustomFontSizeValue(),
      1,
      Integer.MAX_VALUE,
      1
    );
    customFontSize.setModel(customFontSizeModel);
    customFontSize.addChangeListener(change ->
      themeSettingsModel.setCustomFontSizeValue(
        customFontSizeModel.getNumber().intValue()
      ));

    makeNotificationsTransparentCheckBox.setSelected(initialThemeSettingsModel.isSeeThroughNotifications());
    makeNotificationsTransparentCheckBox.addActionListener(e -> {
      notificationOpacitySlider.setEnabled(makeNotificationsTransparentCheckBox.isSelected());
      themeSettingsModel.setSeeThroughNotifications(makeNotificationsTransparentCheckBox.isSelected());
    });
    notificationOpacitySlider.setForeground(UIUtil.getContextHelpForeground());
    notificationOpacitySlider.setEnabled(initialThemeSettingsModel.isSeeThroughNotifications());
    notificationOpacitySlider.setValue(initialThemeSettingsModel.getNotificationOpacity());
    notificationOpacitySlider.addChangeListener(change ->
      themeSettingsModel.setNotificationOpacity(notificationOpacitySlider.getValue())
    );

    themeChangeAnimationCheckBox.setSelected(initialThemeSettingsModel.isLafAnimation());
    themeChangeAnimationCheckBox.addActionListener(e ->
      themeSettingsModel.setLafAnimation(themeChangeAnimationCheckBox.isSelected()));
  }

  @Override
  public boolean isModified() {
    return !initialThemeSettingsModel.equals(themeSettingsModel);
  }

  @Override
  public void apply() {
    ThemeSettings.INSTANCE.apply(themeSettingsModel);
    initialThemeSettingsModel = themeSettingsModel.duplicate();
  }

  private void createUIComponents() {
    generalLinks = new JTextPane();
    String accentHex = ColorUtil.toHex(
      JBColor.namedColor("Link.activeForeground", JBColor.namedColor("link.foreground", 0x589DF6))
    );
    generalLinks.setEditable(false);
    generalLinks.setContentType("text/html" );
    generalLinks.setBackground(UIUtil.getPanelBackground());
    generalLinks.setText(
      "<html>\n" +
        "<head>\n" +
        "    <style type='text/css'>\n" +
        "        body {\n" +
        "            font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n" +
        "        }\n" +
        "\n" +
        "        a {\n" +
        "            color: #" + accentHex + ";\n" +
        "            font-weight: bold;\n" +
        "        }\n" +
        "\n" +
        "        p {\n" +
        "            color: #" + ColorUtil.toHex(UIUtil.getLabelForeground()) + ";\n" +
        "        }\n" +
        "        .meme {\n" +
        "            margin-top: 5;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<a href='https://github.com/doki-theme/doki-theme-jetbrains#documentation'>View Documentation</a><br/><br/>\n" +
        "<a href='https://github.com/doki-theme/doki-theme-jetbrains/blob/master/changelog/CHANGELOG.md'>See Changelog</a><br/><br/>\n" +
        "<a href='https://github.com/doki-theme/doki-theme-jetbrains/issues'>Report Issue</a><br/><br/>\n" +
        "</div>\n" +
        "</html>"
    );
    generalLinks.addHyperlinkListener(h -> {
      if (HyperlinkEvent.EventType.ACTIVATED.equals(h.getEventType())) {
        BrowserUtil.browse(h.getURL());
      }
    });

    currentThemeWomboComboBox = ThemeSettings.INSTANCE.createThemeComboBoxModel(
      () -> this.themeSettingsModel == null ?
        ThemeSettings.createThemeSettingsModel() :
        themeSettingsModel
    );

    try {
      materialIconPane = ThemeSettings.INSTANCE.createMaterialIconsPane(
        () -> this.themeSettingsModel == null ?
          ThemeSettings.createThemeSettingsModel() :
          themeSettingsModel
      );
    } catch (RuntimeException ignored) {
      materialIconPane = new DialogPanel();
    }
  }
}
