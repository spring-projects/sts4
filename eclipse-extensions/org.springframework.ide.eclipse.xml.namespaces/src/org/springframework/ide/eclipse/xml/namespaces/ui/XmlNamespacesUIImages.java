/*******************************************************************************
 * Copyright (c) 2004, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;

/**
 * The images provided by the Spring Beans UI plugin.
 * Initialize the image registry by declaring all of the required
 * graphics. This involves creating JFace image descriptors describing
 * how to create/find the image should it be needed.
 * The image is not actually allocated until requested.
 * Prefix conventions
 * Wizard Banners			WIZBAN_
 * Preference Banners		PREF_BAN_
 * Property Page Banners	PROPBAN_
 * Color toolbar			CTOOL_
 * Enable toolbar			ETOOL_
 * Disable toolbar			DTOOL_
 * Local enabled toolbar	ELCL_
 * Local Disable toolbar	DLCL_
 * Object large			OBJL_
 * Object small			OBJS_
 * View 					VIEW_
 * Product images			PROD_
 * Misc images				MISC_
 * Where are the images?
 * The images (typically gifs) are found in the same location as this
 * plugin class. This may mean the same package directory as the
 * package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via
 * this plugin class.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class XmlNamespacesUIImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";
	private static final String NAME_PREFIX = SpringXmlNamespacesPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(SpringXmlNamespacesPlugin.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			SpringXmlNamespacesPlugin.log(e);
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/*
	 * Available cached Images in the Spring Beans UI plugin image registry.
	 */
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "project_obj.gif";
	public static final String IMG_OBJS_ANNOTATION = NAME_PREFIX + "annotation_obj.gif";
	public static final String IMG_OBJS_CONFIG = NAME_PREFIX + "config_obj.gif";
	public static final String IMG_OBJS_CONFIG_SET = NAME_PREFIX + "configset_obj.gif";
	public static final String IMG_OBJS_IMPORT = NAME_PREFIX + "import_obj.gif";
	public static final String IMG_OBJS_ALIAS = NAME_PREFIX + "alias_obj.gif";
	public static final String IMG_OBJS_DESCRIPTION = NAME_PREFIX + "description_obj.gif";
	public static final String IMG_OBJS_BEAN = NAME_PREFIX + "bean_obj.gif";
	public static final String IMG_OBJS_VIRTUAL_FOLDER = NAME_PREFIX + "virtual_folder_obj.gif";
	public static final String IMG_OBJS_BEAN_REF = NAME_PREFIX + "beanref_obj.gif";
	public static final String IMG_OBJS_CONSTRUCTOR = NAME_PREFIX + "constructor_obj.gif";
	public static final String IMG_OBJS_PROPERTY = NAME_PREFIX + "property_obj.gif";
	public static final String IMG_OBJS_VALUE = NAME_PREFIX + "value_obj.gif";
	public static final String IMG_OBJS_COLLECTION = NAME_PREFIX + "collection_obj.gif";
    public static final String IMG_OBJS_LIST = NAME_PREFIX + "list_obj.gif";
    public static final String IMG_OBJS_MAP = NAME_PREFIX + "map_obj.gif";
    public static final String IMG_OBJS_METHOD_OVERRIDE = NAME_PREFIX + "method_override_obj.gif";
    public static final String IMG_OBJS_PROPERTIES = NAME_PREFIX + "properties_obj.gif";
    public static final String IMG_OBJS_REQUEST_MAPPING = NAME_PREFIX + "request_mapping_obj.gif";
    public static final String IMG_OBJS_KEY = NAME_PREFIX + "key_obj.gif";
    public static final String IMG_OBJS_CONSTANT = NAME_PREFIX + "constant_obj.gif";
    public static final String IMG_OBJS_CONTENT_ASSIST = NAME_PREFIX + "content_assist_separator_obj.gif";
    public static final String IMG_OBJS_PROPERTY_PATH = NAME_PREFIX + "property_path_obj.gif";
	public static final String IMG_OBJS_SPRING = NAME_PREFIX + "spring_obj.gif";
	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif";
	public static final String IMG_OBJS_REFERENCE = NAME_PREFIX + "arrow_obj.gif";
	public static final String IMG_OBJS_NAMESPACE_BEAN = NAME_PREFIX + "namespace_bean_obj.gif";
	public static final String IMG_OBJS_NAMESPACE_COMPONENT = NAME_PREFIX + "namespace_component_obj.gif";
	public static final String IMG_OBJS_AOP_CONFIG = NAME_PREFIX + "aop_config_obj.gif";
    public static final String IMG_OBJS_ASPECT = NAME_PREFIX + "aspect_obj.gif";
    public static final String IMG_OBJS_ADVICE = NAME_PREFIX + "advice_obj.gif";
    public static final String IMG_OBJS_AROUND_ADVICE = NAME_PREFIX + "around_advice_obj.gif";
    public static final String IMG_OBJS_BEFORE_ADVICE = NAME_PREFIX + "before_advice_obj.gif";
    public static final String IMG_OBJS_AFTER_ADVICE = NAME_PREFIX + "after_advice_obj.gif";
    public static final String IMG_OBJS_INTRODUCTION = NAME_PREFIX + "introduction_obj.gif";
    public static final String IMG_OBJS_POINTCUT = NAME_PREFIX + "pointcut_obj.gif";
    public static final String IMG_OBJS_SCRIPT = NAME_PREFIX + "script_obj.gif";
    public static final String IMG_OBJS_TX = NAME_PREFIX + "tx_obj.gif";
    public static final String IMG_OBJS_EJB = NAME_PREFIX + "ejb_obj.gif";
    public static final String IMG_OBJS_XSD = NAME_PREFIX + "xsd_obj.gif";
    public static final String IMG_OBJS_CONTEXT = NAME_PREFIX + "context_obj.gif";
    public static final String IMG_OBJS_JMS = NAME_PREFIX + "jms_obj.gif";
    public static final String IMG_OBJS_WARNING = NAME_PREFIX + "warning.png";

	public static final String IMG_WIZ_PROJECT = NAME_PREFIX + "project_wiz.png";
	public static final String IMG_WIZ_CONFIG = NAME_PREFIX + "config_wiz.png";

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16
	private final static String WIZBAN = "wizban/"; //basic colors - size 16x16
	private final static String OVR = "ovr16/"; //basic colors - size 7x8

	public static final ImageDescriptor DESC_OBJS_PROJECT = createManaged(OBJECT, IMG_OBJS_PROJECT);
	public static final ImageDescriptor DESC_OBJS_ANNOTATION = createManaged(OBJECT, IMG_OBJS_ANNOTATION);
	public static final ImageDescriptor DESC_OBJS_CONFIG = createManaged(OBJECT, IMG_OBJS_CONFIG);
	public static final ImageDescriptor DESC_OBJS_CONFIG_SET = createManaged(OBJECT, IMG_OBJS_CONFIG_SET);
	public static final ImageDescriptor DESC_OBJS_IMPORT = createManaged(OBJECT, IMG_OBJS_IMPORT);
	public static final ImageDescriptor DESC_OBJS_ALIAS = createManaged(OBJECT, IMG_OBJS_ALIAS);
	public static final ImageDescriptor DESC_OBJS_DESCRIPTION = createManaged(OBJECT, IMG_OBJS_DESCRIPTION);
	public static final ImageDescriptor DESC_OBJS_BEAN = createManaged(OBJECT, IMG_OBJS_BEAN);
	public static final ImageDescriptor DESC_OBJS_VIRTUAL_FOLDER = createManaged(OBJECT, IMG_OBJS_VIRTUAL_FOLDER);
	public static final ImageDescriptor DESC_OBJS_BEAN_REF = createManaged(OBJECT, IMG_OBJS_BEAN_REF);
	public static final ImageDescriptor DESC_OBJS_CONSTRUCTOR = createManaged(OBJECT, IMG_OBJS_CONSTRUCTOR);
	public static final ImageDescriptor DESC_OBJS_CONTENT_ASSIST = createManaged(OBJECT, IMG_OBJS_CONTENT_ASSIST);
	public static final ImageDescriptor DESC_OBJS_PROPERTY = createManaged(OBJECT, IMG_OBJS_PROPERTY);
	public static final ImageDescriptor DESC_OBJS_VALUE = createManaged(OBJECT, IMG_OBJS_VALUE);
	public static final ImageDescriptor DESC_OBJS_COLLECTION = createManaged(OBJECT, IMG_OBJS_COLLECTION);
	public static final ImageDescriptor DESC_OBJS_LIST = createManaged(OBJECT, IMG_OBJS_LIST);
	public static final ImageDescriptor DESC_OBJS_MAP = createManaged(OBJECT, IMG_OBJS_MAP);
	public static final ImageDescriptor DESC_OBJS_METHOD_OVERRIDE = createManaged(OBJECT, IMG_OBJS_METHOD_OVERRIDE);
	public static final ImageDescriptor DESC_OBJS_PROPERTIES = createManaged(OBJECT, IMG_OBJS_PROPERTIES);
	public static final ImageDescriptor DESC_OBJS_REQUEST_MAPPING = createManaged(OBJECT, IMG_OBJS_REQUEST_MAPPING);
	public static final ImageDescriptor DESC_OBJS_KEY = createManaged(OBJECT, IMG_OBJS_KEY);
	public static final ImageDescriptor DESC_OBJS_CONSTANT = createManaged(OBJECT, IMG_OBJS_CONSTANT);
	public static final ImageDescriptor DESC_OBJS_PROPERTY_PATH = createManaged(OBJECT, IMG_OBJS_PROPERTY_PATH);
	public static final ImageDescriptor DESC_OBJS_SPRING = createManaged(OBJECT, IMG_OBJS_SPRING);
	public static final ImageDescriptor DESC_OBJS_ERROR = createManaged(OBJECT, IMG_OBJS_ERROR);
	public static final ImageDescriptor DESC_OBJS_REFERENCE = createManaged(OBJECT, IMG_OBJS_REFERENCE);
	public static final ImageDescriptor DESC_OBJS_NAMESPACE_BEAN = createManaged(OBJECT, IMG_OBJS_NAMESPACE_BEAN);
	public static final ImageDescriptor DESC_OBJS_NAMESPACE_COMPONENT = createManaged(OBJECT, IMG_OBJS_NAMESPACE_COMPONENT);
	public static final ImageDescriptor DESC_OBJS_AOP_CONFIG = createManaged(OBJECT, IMG_OBJS_AOP_CONFIG);
	public static final ImageDescriptor DESC_OBJS_ASPECT = createManaged(OBJECT, IMG_OBJS_ASPECT);
	public static final ImageDescriptor DESC_OBJS_ADVICE = createManaged(OBJECT, IMG_OBJS_ADVICE);
	public static final ImageDescriptor DESC_OBJS_AROUND_ADVICE = createManaged(OBJECT, IMG_OBJS_AROUND_ADVICE);
	public static final ImageDescriptor DESC_OBJS_BEFORE_ADVICE = createManaged(OBJECT, IMG_OBJS_BEFORE_ADVICE);
	public static final ImageDescriptor DESC_OBJS_AFTER_ADVICE = createManaged(OBJECT, IMG_OBJS_AFTER_ADVICE);
	public static final ImageDescriptor DESC_OBJS_INTRODUCTION = createManaged(OBJECT, IMG_OBJS_INTRODUCTION);
	public static final ImageDescriptor DESC_OBJS_POINTCUT = createManaged(OBJECT, IMG_OBJS_POINTCUT);
	public static final ImageDescriptor DESC_OBJS_SCRIPT = createManaged(OBJECT, IMG_OBJS_SCRIPT);
	public static final ImageDescriptor DESC_OBJS_TX = createManaged(OBJECT, IMG_OBJS_TX);
	public static final ImageDescriptor DESC_OBJS_EJB = createManaged(OBJECT, IMG_OBJS_EJB);
	public static final ImageDescriptor DESC_OBJS_XSD = createManaged(OBJECT, IMG_OBJS_XSD);
	public static final ImageDescriptor DESC_OBJS_CONTEXT = createManaged(OBJECT, IMG_OBJS_CONTEXT);
	public static final ImageDescriptor DESC_OBJS_JMS = createManaged(OBJECT, IMG_OBJS_JMS);
	public static final ImageDescriptor DESC_OBJS_WARNING = createManaged(OBJECT, IMG_OBJS_WARNING);

	public static final ImageDescriptor DESC_OVR_SPRING = create(OVR, "spring_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ERROR = create(OVR, "error_ovr.gif");
	public static final ImageDescriptor DESC_OVR_WARNING = create(OVR, "warning_ovr.gif");
	public static final ImageDescriptor DESC_OVR_EXTERNAL = create(OVR, "external_ovr.gif");
	public static final ImageDescriptor DESC_OVR_CHILD = create(OVR, "child_ovr.gif");
	public static final ImageDescriptor DESC_OVR_FACTORY = create(OVR, "factory_ovr.gif");
	public static final ImageDescriptor DESC_OVR_PROTOTYPE = create(OVR, "prototype_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ABSTRACT = create(OVR, "abstract_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ANNOTATION = create(OVR, "annotation_ovr.gif");

	public static final ImageDescriptor DESC_WIZ_PROJECT = createManaged(WIZBAN, IMG_WIZ_PROJECT);
	public static final ImageDescriptor DESC_WIZ_CONFIG = createManaged(WIZBAN, IMG_WIZ_CONFIG);

	/*
	 * Helper method to initialize the image registry from the SpringXmlNamespacesPlugin
	 * class.
	 */
	public static void initializeImageRegistry(ImageRegistry registry) {
		for (String key : imageDescriptors.keySet()) {
			registry.put(key, imageDescriptors.get(key));
		}
	}

	/**
	 * Returns the {@link Image} identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return SpringXmlNamespacesPlugin.getDefault().getImageRegistry().get(key);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	//---- Helper methods to access icons on the file system -------------------

	private static void setImageDescriptors(IAction action, String type,
			String relPath) {
		action.setImageDescriptor(create("e" + type, relPath));
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
					"d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			SpringXmlNamespacesPlugin.log(e);
		}
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(
				   makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(name, result);
			return result;
		} catch (MalformedURLException e) {
			SpringXmlNamespacesPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix,
					name));
		} catch (MalformedURLException e) {
			SpringXmlNamespacesPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name)
			throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/').append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}
}
