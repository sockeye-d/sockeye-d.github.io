```yaml
meta:
  title: coho
  description: Static site generator written in Kotlin
```
# coho

Coho is a static site generator written in Kotlin. It supports
* live reload
* simple Kotlin-based configuration

[link](/projects/godl.md)

```css
@import "font.css";
@import "color.css";

:root {
    --h1-font-size: 2rem;
    --h2-font-size: 1.5rem;
    --h3-font-size: 1rem;
    --font-size: 16px;
}

:root {
    --slow-animation: 0.5s cubic-bezier(0, 0, .2, 1);
    --fast-animation: 0.2s cubic-bezier(0, 0, .2, 1);
}

page-header>.main-header {
    flex-direction: row;
}

.links {
    flex-direction: column;
}

.link-popup {
    flex: 1 1 auto;
    display: grid;
    grid-template-rows: repeat(2, 1fr);
    grid-auto-flow: column;
    grid-auto-columns: 1fr;
}

.link-container {
    display: none;
}

.link-menu {
    flex: 1 1 auto;
    display: flex;
    align-items: center;
    justify-content: right;
}

@media (width >=40rem) {
    .link-container {
        display: grid;
    }

    .link-menu {
        display: none;
    }

    page-header>.main-header {
        flex-direction: row;
    }
}

:root {
    font-size: var(--font-size);
}

h1 {
    font-size: var(--h1-font-size);
}

h2 {
    font-size: var(--h2-font-size);
}

h3 {
    font-size: var(--h3-font-size);
}

h1 {
    font-family: "LiberationSerif";
    font-weight: bold;
    margin-top: 0.5rem;
    margin-bottom: 0.5rem;
}

body {
    background-color: var(--mantle);
    color: var(--text);
    font-family: "LiberationSans";
    display: flex;
    flex-direction: column;
    margin: 0;
    min-height: 100dvh;
}

content {
    padding: 0.5rem;
    background-color: var(--base);
    border-radius: 0.5rem;
    flex: 1 1 auto;
}

pre.codeblock {
    white-space: pre-wrap;
    overflow-wrap: break-word;
    background-color: var(--mantle);
    margin: 1rem;
    margin-left: 0;
    margin-right: 0;
    padding: 0.8rem;
    box-shadow: 0px 0.3rem 0.3rem color-mix(in srgb, var(--mantle) 70%, #7f000000);
    border-radius: 4px;
}

code {
    font-family: "Iosevka";
}

a {
    color: var(--blue);
    text-decoration: underline;
    text-decoration-color: transparent;
}

a:hover {
    text-decoration: underline;
}

a:visited {
    color: var(--lavender);
}

page-header>.main-header {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    display: flex;
    background-color: var(--mantle);
}

page-header>.link-popup {
    display: grid;
    margin-top: -40px;
    z-index: -1;
    transition: margin-top var(--fast-animation);
}

page-header>.link-popup.open {
    margin-top: 0;
    display: grid;
}

page-header {
    display: flex;
    flex-direction: column;
    position: sticky;
    top: 0;
    margin: 0px;
    background-color: var(--mantle);
    box-shadow: 0px 0px 0.5rem var(--crust);
}

page-header:has(.link-popup)>.main-header>.links>.link-menu>svg {
    rotate: 0deg;
    transition: rotate var(--fast-animation), color var(--fast-animation);
}

page-header:has(.link-popup.open)>.main-header>.links>.link-menu>svg {
    rotate: 90deg;
    color: var(--text);
}

.link-menu {
    cursor: pointer;
}

page-footer {
    background-color: var(--mantle);
}

gap {
    flex: 1 1 auto;
}

footer {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 0.5rem 0px 0px;
}

footer>* {
    padding-top: 4px;
    padding-bottom: 4px;
}

.link-container {
    flex: 1 1 auto;
    grid-template-rows: repeat(2, 1fr);
    grid-auto-flow: column;
    grid-auto-columns: 1fr;
}

.links {
    display: flex;
    flex-direction: column;
    flex: 1 1 auto;
}

.links:nth-child(0) {
    flex: 1 1 auto;
}

.links>* {
    margin-left: 0.25rem;
    margin-right: 0.25rem;
}

.links>a {
    font-size: 150%;
}

.project-link {
    align-content: center;
    text-align: center;
    filter: drop-shadow(0px 0px 0px var(--text));
    font-family: "Iosevka";
    transition: transform var(--slow-animation), font-size var(--slow-animation);
    font-style: italic;
}

.project-link:hover {
    transform: translateY(-2px);
    font-style: normal;
    font-weight: bold;
}

.footer-text {
    text-align: center;
}

.icon {
    width: 2rem;
    height: 2rem;
    stroke: currentColor;
}

.icon-small {
    width: 1rem;
    height: 1rem;
    stroke: currentColor;
}

.icon-medium {
    width: 1.5rem;
    height: 1.5rem;
    stroke: currentColor;
    stroke-linecap: round;
    stroke-linejoin: round;
    fill: none;
}

.socials>a {
    padding: 0.125rem;
}

.socials>a>svg {
    transition: transform var(--slow-animation);
}

.socials>a:hover>svg {
    transform: translateY(-2px);
}

a:hover:has(svg) {
    text-decoration: none;
}

#themeToggle {
    cursor: pointer;
}

code > span {
    color: #ff00ff;
}

.code-keyword {
    color: var(--mauve);
}

.code-function {
    color: var(--blue);
    font-style: italic;
}

.code-number {
    color: var(--peach);
}

.code-operator {
    color: var(--sky);
}

.code-comment {
    color: var(--overlay2);
}

.code-text {
    color: var(--text);
}

.code-annotation {
    color: var(--yellow);
}

.code-punctuation {
    color: var(--overlay2);
}

.code-string {
    color: var(--green);
}

.code-raw-string {
    color: var(--green);
}

.code-label {
    color: var(--text);
}

.code-class-name {
    color: var(--yellow)
}

.code-directive {
    color: var(--teal)
}

.code-text:has(+ .code-directive) {
    color: var(--teal)
}

.code-boolean {
    color: var(--mauve);
}

.code-interpolation {
    color: var(--pink);
}

.code-delimiter {
    color: var(--pink);
}

.code-attr-name {
    color: var(--yellow);
}

.code-cdata {
    color: var(--lavender);
}

.code-entity {
    color: var(--peach);
    font-style: italic;
}

.code-prolog {
    color: var(--yellow);
}

```

```qml
import QtQuick
import QtQuick.Layouts
import Qt.labs.platform as Platform
import QtQuick.Controls
import org.kde.kirigami as Kirigami
import org.kde.kirigamiaddons.formcard as FormCard
import org.kde.kirigamiaddons.settings as KirigamiSettings
import org.kde.kirigamiaddons.statefulapp as StatefulApp
import org.kde.desktop as KdeDesktop

import org.fishy.godl

import "config" as Configuration

StatefulApp.StatefulWindow {
    id: root

    property string text

    height: Kirigami.Units.gridUnit * 30
    minimumHeight: Kirigami.Units.gridUnit * 10
    minimumWidth: Kirigami.Units.gridUnit * 30
    width: Kirigami.Units.gridUnit * 45
    windowName: i18nc("@title:window", "godl")

    application: GodlApp {
        id: app

        configurationView: KirigamiSettings.ConfigurationView {
            modules: [
                KirigamiSettings.ConfigurationModule {
                    category: "general"
                    // @disable-check M17
                    icon.name: "configure"
                    moduleId: "general"
                    page: () => generalConfigPage
                    text: i18n("General")
                },
                KirigamiSettings.ConfigurationModule {
                    category: "downloads"
                    // @disable-check M17
                    icon.name: "download"
                    moduleId: "downloads"
                    page: () => downloadsConfigPage
                    text: i18n("Downloads")
                },
                KirigamiSettings.ConfigurationModule {
                    category: "project"
                    // @disable-check M17
                    icon.name: "project-development"
                    moduleId: "project"
                    page: () => projectsConfigPage
                    text: i18n("Projects")
                }
            ]

            // hey so this is cursed
            onConfigViewItemChanged: {
                if (!configViewItem)
                    return;
                configViewItem.flags = Qt.Dialog;
                configViewItem.transientParent = root;
                configViewItem.modality = Qt.ApplicationModal;
                // hey so this is more cursed
                // never do this
                configViewItem.visible = false;
                configViewItem.visible = true;
            }
        }

        onNextTabTriggered: if (mainPage.activePageIndex !== 2)
            mainPage.activePageIndex++
        onPrevTabTriggered: if (mainPage.activePageIndex !== 0)
            mainPage.activePageIndex--
    }
    footer: RowLayout {
        Layout.fillWidth: true

        Item {
            width: Kirigami.Units.largeSpacing
        }

        Label {
            color: palette.placeholderText
            text: `${VersionInfo.tag} (${VersionInfo.commitHash})`
        }

        Item {
            Layout.fillWidth: true
        }

        ToolButton {
            id: notificationPopupToggle

            checkable: true
            checked: notificationPopup.visible
            icon.name: "download"

            onCheckedChanged: if (checked) {
                notificationPopup.open();
            } else {
                notificationPopup.close();
            }

            Popup {
                id: notificationPopup

                property bool visible2

                closePolicy: Popup.CloseOnEscape

                // this is so cursed... but it works
                // height: visible2 ? notificationCardsScroll.height + padding * 2.0 : 0
                height: notificationCardsScroll.height + padding * 2.0
                padding: Kirigami.Units.smallSpacing
                rightPadding: 0
                topPadding: 0
                width: Math.min(Kirigami.Units.gridUnit * 20.0, Math.round(root.width / 2))
                x: parent.width - width
                y: -height
                z: 10000

                enter: Transition {
                    NumberAnimation {
                        duration: Kirigami.Units.longDuration
                        easing.type: Easing.OutExpo
                        from: 0.0
                        property: "height"
                        to: notificationCardsScroll.height + notificationPopup.padding * 2.0
                    }
                }
                exit: Transition {
                    NumberAnimation {
                        duration: Kirigami.Units.longDuration
                        easing.type: Easing.OutExpo
                        from: notificationCardsScroll.height + notificationPopup.padding * 2.0
                        property: "height"
                        to: 0.0
                    }
                }

                ScrollView {
                    id: notificationCardsScroll

                    ScrollBar.horizontal.policy: ScrollBar.AlwaysOff
                    clip: true
                    height: Kirigami.Units.gridUnit * 15
                    width: notificationPopup.availableWidth

                    Kirigami.CardsListView {
                        id: repeater

                        Layout.fillHeight: true
                        Layout.fillWidth: true
                        model: dl.model
                        reuseItems: false

                        delegate: Kirigami.Card {
                            id: card

                            property var backgroundColor: if (card.modelData.error !== "") {
                                return Kirigami.Theme.negativeBackgroundColor;
                            } else if (card.modelData.stage === DownloadInfo.Finished) {
                                return Kirigami.Theme.positiveBackgroundColor;
                            } else {
                                return null;
                            }
                            required property DownloadInfo modelData

                            Layout.fillWidth: true

                            actions: [
                                Kirigami.Action {
                                    enabled: card.modelData.stage === DownloadInfo.Downloading
                                    icon.name: "process-stop"
                                    visible: !closeAction.visible

                                    onTriggered: dl.cancel(card.modelData.id)
                                },
                                Kirigami.Action {
                                    id: closeAction

                                    icon.name: "dialog-close"
                                    visible: card.modelData.stage === DownloadInfo.Finished || card.modelData.error !== ""

                                    onTriggered: dl.remove(card.modelData.id)
                                }
                            ]
                            contentItem: ColumnLayout {
                                Layout.fillWidth: true

                                Label {
                                    Layout.fillWidth: true
                                    text: card.modelData.error
                                    visible: card.modelData.error !== ""
                                }

                                Label {
                                    Layout.fillWidth: true
                                    text: i18n("Installation complete")
                                    visible: card.modelData.stage === DownloadInfo.Finished
                                }

                                RowLayout {
                                    Layout.fillWidth: true
                                    visible: card.modelData.stage !== DownloadInfo.Finished && card.modelData.error === ""

                                    Label {
                                        Layout.preferredWidth: Kirigami.Units.gridUnit * 6
                                        elide: Text.ElideRight
                                        text: card.modelData.stage === DownloadInfo.Downloading ? `${SizeConverter.formatSize(card.modelData.downloadSpeed)}/s` : i18n("Extracting")
                                    }

                                    Kirigami.Separator {
                                        Layout.fillHeight: true
                                    }

                                    ProgressBar {
                                        Layout.fillWidth: true
                                        indeterminate: card.modelData.progress < 0.0
                                        value: card.modelData.progress
                                        width: Kirigami.Units.gridUnit * 2.0
                                    }
                                }
                            }
                            header: Kirigami.Heading {
                                elide: Text.ElideRight
                                level: 2
                                text: card.modelData.assetName
                            }

                            onBackgroundColorChanged: if (backgroundColor !== null)
                                background.color = backgroundColor
                        }
                    }
                }

                Label {
                    anchors.centerIn: parent
                    text: i18n("No active downloads")
                    visible: repeater.count === 0
                }
            }
        }
    }
    globalDrawer: Kirigami.GlobalDrawer {
        isMenu: true

        actions: [
            Kirigami.Action {
                fromQAction: root.application.action("open_about_page")
            },
            Kirigami.Action {
                fromQAction: root.application.action("options_configure_keybinding")
            },
            Kirigami.Action {
                fromQAction: root.application.action("options_configure")
            },
            Kirigami.Action {
                fromQAction: root.application.action("godl_next_page")
                visible: false
            },
            Kirigami.Action {
                fromQAction: root.application.action("godl_prev_page")
                visible: false
            }
        ]
    }
    pageStack.initialPage: Kirigami.Page {
        id: mainPage

        property int activePageIndex: 0
        property list<Kirigami.Action> baseActions: [
            Kirigami.Action {
                separator: true
            },
            Kirigami.Action {
                ActionGroup.group: actionGroup
                checkable: true
                checked: mainPage.activePageIndex === 0
                displayHint: Kirigami.DisplayHint.KeepVisible
                icon.name: "document-edit"
                text: "Projects"

                onTriggered: mainPage.activePageIndex = 0
            },
            Kirigami.Action {
                ActionGroup.group: actionGroup
                checkable: true
                checked: mainPage.activePageIndex === 1
                displayHint: Kirigami.DisplayHint.KeepVisible
                icon.name: "drive"
                text: "Local versions"

                onTriggered: mainPage.activePageIndex = 1
            },
            Kirigami.Action {
                ActionGroup.group: actionGroup
                checkable: true
                checked: mainPage.activePageIndex === 2
                displayHint: Kirigami.DisplayHint.KeepVisible
                icon.name: "globe"
                text: "Remote versions"

                onTriggered: mainPage.activePageIndex = 2
            }
        ]

        actions: swipeView.children[swipeView.currentIndex].actions.concat(baseActions)
        bottomPadding: 0

        ActionGroup {
            id: actionGroup

        }

        StackLayout {
            id: swipeView

            anchors.fill: parent
            currentIndex: mainPage.activePageIndex

            onCurrentIndexChanged: mainPage.activePageIndex = currentIndex

            ProjectsPage {
                title: "Projects"
            }

            LocalVersionsPage {
                title: "Local versions"
            }

            RemoteVersionsPage {
                id: dlPage

                title: "Remote versions"

                Component.onCompleted: {
                    dlPage.dl = dl;
                }
                StackLayout.onIsCurrentItemChanged: if (!hasContent && StackLayout.isCurrentItem) {
                    refresh();
                }
            }
        }
    }

    Kirigami.ApplicationWindow {
        id: aboutPage

        height: Kirigami.Units.gridUnit * 25
        modality: Qt.WindowModal
        visible: false
        width: Kirigami.Units.gridUnit * 40

        pageStack.initialPage: FormCard.AboutPage {
        }
    }

    Component {
        id: generalConfigPage

        Configuration.GeneralConfigPage {
        }
    }

    Component {
        id: downloadsConfigPage

        Configuration.DownloadsConfigPage {
        }
    }

    Component {
        id: projectsConfigPage

        Configuration.ProjectsConfigPage {
        }
    }

    DownloadManager {
        id: dl

        onDownloadStarted: notificationPopup.open()
    }
}
```
