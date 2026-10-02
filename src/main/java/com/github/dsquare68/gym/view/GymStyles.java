package com.github.dsquare68.gym.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderColor;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;

/**
 * Lumo utility class groups shared by the gym views.
 *
 * <p>Replaces the demo's theme CSS files (details-exercise.css,
 * gym-title-items.css, training-calendary-summary.css, training-view.css,
 * football-page.css): a PF4J plugin can't contribute to the host's frontend
 * bundle, so styling goes through Lumo utilities (loaded by the host via
 * {@code Lumo.UTILITY_STYLESHEET}), theme variants and Lumo tokens instead.
 * The demo's hardcoded colours map to theme colours so the plugin follows
 * HUB's light/dark theme:
 * <ul>
 *   <li>{@code #adedff} (light blue panels) - {@link Background#PRIMARY_10}</li>
 *   <li>{@code #99d299} (light green panels) - {@link Background#SUCCESS_10}</li>
 *   <li>{@code #0000DD} (blue text) - {@link TextColor#PRIMARY}</li>
 *   <li>{@code #00DD00} / {@code #FF0000} - {@link TextColor#SUCCESS} / {@link TextColor#ERROR}</li>
 * </ul>
 * Font sizes above Lumo's largest step (48px, 38px, 36px, 34px) map to
 * {@link FontSize#XXXLARGE} (2.5rem, about 40px).
 */
public final class GymStyles {

	/** Header bars: title, exercise details settings, calendar settings. */
	public static final String[] HEADER_BAR = { Background.PRIMARY_10 };

	/** Large blue heading text. */
	public static final String[] HEADING = { TextColor.PRIMARY, FontSize.XXXLARGE };

	/** Green panels with a primary border: month and day summaries. */
	public static final String[] CALENDAR_PANEL = {
			Background.SUCCESS_10, Border.ALL, BorderColor.PRIMARY, BorderRadius.MEDIUM };

	/** A "label: value" row inside a calendar panel. */
	public static final String[] CALENDAR_DATA = { TextColor.PRIMARY, FontSize.XXLARGE };

	/** Highlighted number in exercise details (was exercise-details-span-blue). */
	public static final String[] DETAILS_VALUE = { TextColor.PRIMARY, Whitespace.PRE_LINE, FontSize.XXXLARGE };

	/** Describing text in exercise details (was exercise-details-span-white). */
	public static final String[] DETAILS_TEXT = { TextColor.BODY, Whitespace.PRE_LINE, FontSize.XXXLARGE };

	/** Round value compared to the previous training. */
	public static final String[] BETTER = { FontSize.LARGE, FontWeight.MEDIUM, TextColor.SUCCESS };
	public static final String[] WORSE = { FontSize.LARGE, FontWeight.MEDIUM, TextColor.ERROR };
	public static final String[] EQUAL = { FontSize.LARGE, FontWeight.MEDIUM, TextColor.PRIMARY };

	private GymStyles() {
	}

	/**
	 * Thick primary-colour outline for the selected calendar day. Lumo's
	 * border utilities are 1px only, so this uses an inline style built on a
	 * Lumo token (the demo used a 5px border).
	 */
	public static void setSelected(Component component, boolean selected) {
		if (selected)
			component.getElement().getStyle().set("outline", "4px solid var(--lumo-primary-color)");
		else
			component.getElement().getStyle().remove("outline");
	}
}
