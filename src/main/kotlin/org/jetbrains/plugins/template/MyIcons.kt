package org.jetbrains.plugins.template

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object MyIcons {
    @JvmField
    val Group: Icon = IconLoader.getIcon("/icons/bitcoin.svg", MyIcons::class.java)
    @JvmField
    val Action: Icon = IconLoader.getIcon("/icons/add_folder.svg", MyIcons::class.java)
}