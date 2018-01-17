package org.spring.tools.boot.java.ls;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Alex Boyko
 */
public class SpringBootGutterIconRenderer extends GutterIconRenderer {

    public static SpringBootGutterIconRenderer INSTANCE = new SpringBootGutterIconRenderer();

    private Supplier<Icon> icon = Suppliers.memoize(() -> IconLoader.getIcon("/icons/boot-icon.png"));

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return icon.get();
    }
}
