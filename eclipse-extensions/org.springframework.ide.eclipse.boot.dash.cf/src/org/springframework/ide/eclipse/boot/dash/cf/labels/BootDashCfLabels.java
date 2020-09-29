package org.springframework.ide.eclipse.boot.dash.cf.labels;

import static org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.BootDashCfColumns.JMX_SSH_TUNNEL;
import static org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels.MUTED_TEXT_DECORATION_COLOR_THEME;
import static org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels.TEXT_DECORATION_COLOR_THEME;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.JmxSshTunnelStatus;
import org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

import com.google.common.collect.ImmutableSet;

public class BootDashCfLabels {

	public static final BootDashLabels.Contribution jmxDecoration = new BootDashLabels.Contribution(ImmutableSet.of(JMX_SSH_TUNNEL)) {
		@Override public StyledString getStyledText(Stylers stylers, BootDashElement element, BootDashColumn col) {
			if (element instanceof CloudAppDashElement) {
				CloudAppDashElement cfApp = (CloudAppDashElement) element;
				JmxSshTunnelStatus tunnelState = cfApp.getJmxSshTunnelStatus().getValue();
				if (tunnelState!=JmxSshTunnelStatus.DISABLED) {
					Color activeColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(TEXT_DECORATION_COLOR_THEME);
					Color notActiveColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(MUTED_TEXT_DECORATION_COLOR_THEME);
					return new StyledString("jmx", tunnelState==JmxSshTunnelStatus.ACTIVE ? stylers.color(activeColor) : stylers.color(notActiveColor));
				}
				return new StyledString();
			}
			return null;
		}

	};

}
