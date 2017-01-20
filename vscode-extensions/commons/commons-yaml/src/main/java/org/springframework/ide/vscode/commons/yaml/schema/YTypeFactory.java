/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.EnumValueParser;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.ValueParser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * Static utility method for creating YType objects representing either
 * 'array-like', 'map-like' or 'object-like' types which can be used
 * to build up a 'Yaml Schema'.
 *
 * @author Kris De Volder
 */
public class YTypeFactory {

	public YType contextAware(String name, SchemaContextAware<YType> guessType) {
		return new YContextSensitive(name, guessType);
	}

	public YType yany(String name) {
		return new YAny(name);
	}

	public YType yseq(YType el) {
		return new YSeqType(el);
	}

	public YType ymap(YType key, YType val) {
		return new YMapType(key, val);
	}

	public YBeanType ybean(String name, YTypedProperty... properties) {
		return new YBeanType(name, properties);
	}

	public YBeanUnionType yunion(String name, YBeanType... types) {
		Assert.isLegal(types.length>1);
		return new YBeanUnionType(name, types);
	}

	/**
	 * YTypeUtil instances capable of 'interpreting' the YType objects created by this
	 * YTypeFactory
	 */
	public final YTypeUtil TYPE_UTIL = new YTypeUtil() {

		@Override
		public boolean isSequencable(YType type) {
			return ((AbstractType)type).isSequenceable();
		}

		@Override
		public boolean isMap(YType type) {
			return ((AbstractType)type).isMap();
		}

		@Override
		public boolean isAtomic(YType type) {
			return ((AbstractType)type).isAtomic();
		}

		@Override
		public Map<String, YTypedProperty> getPropertiesMap(YType type) {
			return ((AbstractType)type).getPropertiesMap();
		}

		@Override
		public List<YTypedProperty> getProperties(YType type) {
			return ((AbstractType)type).getProperties();
		}

		@Override
		public YValueHint[] getHintValues(YType type, DynamicSchemaContext dc) throws Exception {
			return ((AbstractType)type).getHintValues(dc);
		}

		@Override
		public YType getDomainType(YType type) {
			return ((AbstractType)type).getDomainType();
		}

		@Override
		public String niceTypeName(YType type) {
			return type.toString();
		}

		@Override
		public YType getKeyType(YType type) {
			return ((AbstractType)type).getKeyType();
		}

		@Override
		public boolean isBean(YType type) {
			return ((AbstractType)type).isBean();
		}

		@Override
		public ValueParser getValueParser(YType type, DynamicSchemaContext dc) {
			return ((AbstractType)type).getParser(dc);
		}

		@Override
		public YType inferMoreSpecificType(YType type, DynamicSchemaContext schemaContext) {
			return ((AbstractType)type).inferMoreSpecificType(schemaContext);
		}
	};

	/////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Provides default implementations for all YType methods.
	 */
	public static abstract class AbstractType implements YType {

		private SchemaContextAware<ValueParser> parser;
		private List<YTypedProperty> propertyList = new ArrayList<>();
		private final List<YValueHint> hints = new ArrayList<>();
		private Map<String, YTypedProperty> cachedPropertyMap;
		private SchemaContextAware<Callable<Collection<YValueHint>>> hintProvider;

		public boolean isSequenceable() {
			return false;
		}

		public YType inferMoreSpecificType(DynamicSchemaContext dc) {
			return this;
		}

		public boolean isBean() {
			return false;
		}

		public YType getKeyType() {
			return null;
		}

		public YType getDomainType() {
			return null;
		}

		public void addHintProvider(Callable<Collection<YValueHint>> hintProvider) {
			addHintProvider((DynamicSchemaContext dc) -> hintProvider);
		}

		public void addHintProvider(SchemaContextAware<Callable<Collection<YValueHint>>> hintProvider) {
			this.hintProvider = hintProvider;
		}

		public YValueHint[] getHintValues(DynamicSchemaContext dc) throws Exception {
			Collection<YValueHint> providerHints=getProviderHints(dc);

			if (providerHints == null || providerHints.isEmpty()) {
				return hints.toArray(new YValueHint[hints.size()]);
			} else {
				// Only merge if there are provider hints to merge
				Set<YValueHint> mergedHints = new LinkedHashSet<>();

				// Add type hints first
				for (YValueHint val : hints) {
					mergedHints.add(val);
				}

				// merge the provider hints
				for (YValueHint val : providerHints) {
					mergedHints.add(val);
				}
				return mergedHints.toArray(new YValueHint[mergedHints.size()]);
			}
		}

		private Collection<YValueHint> getProviderHints(DynamicSchemaContext dc) throws Exception {
			if (hintProvider != null) {
				Callable<Collection<YValueHint>> withContext = hintProvider.withContext(dc);
				if (withContext != null) {
					 return withContext.call();
				}
			}
			return ImmutableList.of();
		}

		public List<YTypedProperty> getProperties() {
			return Collections.unmodifiableList(propertyList);
		}

		public Map<String, YTypedProperty> getPropertiesMap() {
			if (cachedPropertyMap==null) {
				cachedPropertyMap = new LinkedHashMap<>();
				for (YTypedProperty p : propertyList) {
					cachedPropertyMap.put(p.getName(), p);
				}
			}
			return Collections.unmodifiableMap(cachedPropertyMap);
		}

		public boolean isAtomic() {
			return false;
		}

		public boolean isMap() {
			return false;
		}

		@Override
		public abstract String toString(); // force each sublcass to implement a (nice) toString method.

		public void addProperty(YTypedProperty p) {
			cachedPropertyMap = null;
			propertyList.add(p);
		}

		public void addProperty(String name, YType type, Renderable description) {
			YTypedPropertyImpl prop;
			addProperty(prop = new YTypedPropertyImpl(name, type));
			prop.setDescriptionProvider(description);
		}

		public void addProperty(String name, YType type) {
			addProperty(new YTypedPropertyImpl(name, type));
		}
		public void addHints(String... strings) {
			if (strings != null) {
				for (String value : strings) {
					BasicYValueHint hint = new BasicYValueHint(value);
					if (!hints.contains(hint)) {
						hints.add(hint);
					}
				}
			}
		}

		public void addHints(YValueHint... extraHints) {
			for (YValueHint h : extraHints) {
				if (!hints.contains(h)) {
					hints.add(h);
				}
			}
		}

		public void parseWith(SchemaContextAware<ValueParser> parser) {
			this.parser = parser;
		}

		public void parseWith(ValueParser parser) {
			parseWith((DynamicSchemaContext dc) -> parser);
		}
		private ValueParser getParser(DynamicSchemaContext dc) {
			return parser == null ? null : parser.withContext(dc);
		}

	}

	/**
	 * Represents a type that depends on the DynamicSchemaContext
	 */
	public static class YContextSensitive extends YAny {

		private final SchemaContextAware<YType> typeGuesser;

		public YContextSensitive(String name, SchemaContextAware<YType> typeGuesser) {
			super(name);
			this.typeGuesser = typeGuesser;
		}

		@Override
		public YType inferMoreSpecificType(DynamicSchemaContext dc) {
			if (dc!=null) {
				YType inferred = typeGuesser.withContext(dc);
				if (inferred!=null) {
					return inferred;
				}
			}
			return this;
		}

	}


	/**
	 * Represents a type that is completely unconstrained. Anything goes: A map, a sequence or some
	 * atomic value.
	 */
	public static class YAny extends AbstractType {
		private final String name;

		public YAny(String name) {
			this.name = name;
		}

		@Override
		public boolean isAtomic() {
			return true;
		}

		@Override
		public boolean isSequenceable() {
			return true;
		}

		@Override
		public boolean isMap() {
			return true;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public static class YMapType extends AbstractType {

		private final YType key;
		private final YType val;

		private YMapType(YType key, YType val) {
			this.key = key;
			this.val = val;
		}

		@Override
		public String toString() {
			return "Map<"+key.toString()+", "+val.toString()+">";
		}

		@Override
		public boolean isMap() {
			return true;
		}

		@Override
		public YType getKeyType() {
			return key;
		}

		@Override
		public YType getDomainType() {
			return val;
		}
	}

	public static class YSeqType extends AbstractType {

		private YType el;

		private YSeqType(YType el) {
			this.el = el;
		}

		@Override
		public String toString() {
			return el.toString()+"[]";
		}

		@Override
		public boolean isSequenceable() {
			return true;
		}

		@Override
		public YType getDomainType() {
			return el;
		}
	}

	public static class YBeanType extends AbstractType {
		private final String name;

		public YBeanType(String name, YTypedProperty[] properties) {
			this.name = name;
			for (YTypedProperty p : properties) {
				addProperty(p);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean isBean() {
			return true;
		}

	}

	public static class YAtomicType extends AbstractType {
		private final String name;
		private YAtomicType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		@Override
		public boolean isAtomic() {
			return true;
		}
	}

	/**
	 * Represents a union of several bean types. It is assumed one primary property
	 * exists in each of the the sub-bean types that can be used to identify the
	 * type. In other words the primary property has a unique name so that when
	 * this property is being assigned a value we can infer from that which
	 * specific bean-type we are dealing with.
	 */
	public static class YBeanUnionType extends AbstractType {
		private final String name;
		private List<YBeanType> types;

		private Map<String, AbstractType> typesByPrimary;
		private ImmutableList<YTypedProperty> primaryProps;

		public YBeanUnionType(String name, YBeanType... types) {
			this.name = name;
			this.types = new ArrayList<>();
			for (YBeanType t : types) {
				addUnionMember(t);
			}
		}

		private void addUnionMember(YBeanType type) {
			types.add(type);
		}

		private String findPrimary(AbstractType t, List<YBeanType> types) {
			//Note: passing null dynamic context below is okay, assuming the properties in YBeanType
			// do not care about dynamic context.
			for (YTypedProperty p : t.getProperties()) {
				String name = p.getName();
				if (isUniqueFor(name, t, types)) {
					return name;
				}
			}
			Assert.isLegal(false, "Couldn't find a unique property key for "+t);
			return null; //unreachable, but compiler doesn't know.
		}
		private boolean isUniqueFor(String name, AbstractType t, List<YBeanType> types) {
			for (YBeanType other : types) {
				if (other!=t) {
					//Note: passing null dynamic context below is okay, assuming the properties in YBeanType
					// do not care about dynamic context.
					if (other.getPropertiesMap().containsKey(name)) {
						return false;
					}
				}
			}
			return true;
		}
		@Override
		public String toString() {
			return name;
		}
		@Override
		public boolean isBean() {
			return true;
		}

		@Override
		public List<YTypedProperty> getProperties() {
			//Reaching here means we couldn't guess the type from existing props.
			//We'll just return the primary properties, these are good to give as hints
			//then, since at least one of them should be added.
			return getPrimaryProps();
		}

		private synchronized Map<String, AbstractType> typesByPrimary() {
			if (typesByPrimary==null) {
				//To ensure that the map of 'typesByPrimary' is never stale, make the list of
				// types immutable at this point. The assumption here is that union can be
				// built up flexibly using mutation ops during initialization, but once it
				// starts being used it becomes immutable.
				types = ImmutableList.copyOf(types);
				ImmutableMap.Builder<String, AbstractType> builder = ImmutableMap.builder();
				for (YType _t : types) {
					AbstractType t = (AbstractType)_t;
					builder.put(findPrimary(t, types), t);
				}
				typesByPrimary = builder.build();
			}
			return typesByPrimary;
		}

		private List<YTypedProperty> getPrimaryProps() {
			if (primaryProps==null) {
				Builder<YTypedProperty> builder = ImmutableList.builder();
				for (Entry<String, AbstractType> entry : typesByPrimary().entrySet()) {
					builder.add(entry.getValue().getPropertiesMap().get(entry.getKey()));
				}
				primaryProps = builder.build();
			}
			return primaryProps;
		}

		@Override
		public YType inferMoreSpecificType(DynamicSchemaContext dc) {
			Set<String> existingProps = dc.getDefinedProperties();
			if (!existingProps.isEmpty()) {
				for (Entry<String, AbstractType> entry : typesByPrimary().entrySet()) {
					String primaryName = entry.getKey();
					if (existingProps.contains(primaryName)) {
						return entry.getValue();
					}
				}
			}
			return super.inferMoreSpecificType(dc);
		}
	}


	public static class YTypedPropertyImpl implements YTypedProperty {

		final private String name;
		final private YType type;
		private Renderable description = Renderables.NO_DESCRIPTION;
		private boolean isRequired;

		private YTypedPropertyImpl(String name, YType type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public YType getType() {
			return this.type;
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}

		@Override
		public Renderable getDescription() {
			return description;
		}

		public YTypedPropertyImpl setDescriptionProvider(Renderable description) {
			this.description = description;
			return this;
		}

		public YTypedPropertyImpl isRequired(boolean b) {
			this.isRequired = b;
			return this;
		}

		@Override
		public boolean isRequired() {
			return isRequired;
		}
	}

	public YAtomicType yatomic(String name) {
		return new YAtomicType(name);
	}

	public YTypedPropertyImpl yprop(String name, YType type) {
		return new YTypedPropertyImpl(name, type);
	}

	public YAtomicType yenum(String name, BiFunction<String, Collection<String>, String> errorMessageFormatter, SchemaContextAware<Collection<String>> values) {
		YAtomicType t = yatomic(name);
		t.addHintProvider((dc) -> {
			Collection<String> strings = values.withContext(dc);
			return strings==null
					? null
					: () -> strings.stream()
						.map((s) -> new BasicYValueHint(s))
						.collect(Collectors.toSet());
		});
		t.parseWith((DynamicSchemaContext dc) -> {
			EnumValueParser enumParser = new EnumValueParser(name, values.withContext(dc)) {
				@Override
				protected String createErrorMessage(String parseString, Collection<String> values) {
					return errorMessageFormatter.apply(parseString, values);
				}
			};
			return enumParser;
		});
		return t;
	}

	public YAtomicType yenum(String name, String... values) {
		YAtomicType t = yatomic(name);
		t.addHints(values);
		t.parseWith(new EnumValueParser(name, values));
		return t;
	}

	public YValueHint hint(String value, String label) {
		return new BasicYValueHint(value, label);
	}

}
