/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *							Bug 405066 - [1.8][compiler][codegen] Implement code generation infrastructure for JSR335             
 *							Bug 406982 - [1.8][compiler] Generation of MethodParameters Attribute in classfile
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

public interface ClassFileConstants {

	int AccDefault = 0;
	/*
	 * Modifiers
	 */
	int AccPublic       = 0x0001;
	int AccPrivate      = 0x0002;
	int AccProtected    = 0x0004;
	int AccStatic       = 0x0008;
	int AccFinal        = 0x0010;
	int AccSynchronized = 0x0020;
	int AccVolatile     = 0x0040;
	int AccBridge       = 0x0040;
	int AccTransient    = 0x0080;
	int AccVarargs      = 0x0080;
	int AccNative       = 0x0100;
	int AccInterface    = 0x0200;
	int AccAbstract     = 0x0400;
	int AccStrictfp     = 0x0800;
	int AccSynthetic    = 0x1000;
	int AccAnnotation   = 0x2000;
	int AccEnum         = 0x4000;

	/**
	 * From classfile version 52 (compliance 1.8 up), meaning that a formal parameter is mandated
	 * by a language specification, so all compilers for the language must emit it.
	 */
	int AccMandated     = 0x8000;

	
	/**
	 * Other VM flags.
	 */
	int AccSuper = 0x0020;

//	/**
//	 * Extra flags for types and members attributes (not from the JVMS, should have been defined in ExtraCompilerModifiers).
//	 */
//	int AccAnnotationDefault = ASTNode.Bit18; // indicate presence of an attribute  "DefaultValue" (annotation method)
//	int AccDeprecated = ASTNode.Bit21; // indicate presence of an attribute "Deprecated"

	int Utf8Tag = 1;
	int IntegerTag = 3;
	int FloatTag = 4;
	int LongTag = 5;
	int DoubleTag = 6;
	int ClassTag = 7;
	int StringTag = 8;
	int FieldRefTag = 9;
	int MethodRefTag = 10;
	int InterfaceMethodRefTag = 11;
	int NameAndTypeTag = 12;
	int MethodHandleTag = 15;
	int MethodTypeTag = 16;
	int InvokeDynamicTag = 18;

	int ConstantMethodRefFixedSize = 5;
	int ConstantClassFixedSize = 3;
	int ConstantDoubleFixedSize = 9;
	int ConstantFieldRefFixedSize = 5;
	int ConstantFloatFixedSize = 5;
	int ConstantIntegerFixedSize = 5;
	int ConstantInterfaceMethodRefFixedSize = 5;
	int ConstantLongFixedSize = 9;
	int ConstantStringFixedSize = 3;
	int ConstantUtf8FixedSize = 3;
	int ConstantNameAndTypeFixedSize = 5;
	int ConstantMethodHandleFixedSize = 4;
	int ConstantMethodTypeFixedSize = 3;
	int ConstantInvokeDynamicFixedSize = 5;

	// JVMS 4.4.8
	int MethodHandleRefKindGetField = 1;
	int MethodHandleRefKindGetStatic = 2;
	int MethodHandleRefKindPutField = 3;
	int MethodHandleRefKindPutStatic = 4;
	int MethodHandleRefKindInvokeVirtual = 5;
	int MethodHandleRefKindInvokeStatic = 6;
	int MethodHandleRefKindInvokeSpecial = 7;
	int MethodHandleRefKindNewInvokeSpecial = 8;
	int MethodHandleRefKindInvokeInterface = 9;

	int MAJOR_VERSION_1_1 = 45;
	int MAJOR_VERSION_1_2 = 46;
	int MAJOR_VERSION_1_3 = 47;
	int MAJOR_VERSION_1_4 = 48;
	int MAJOR_VERSION_1_5 = 49;
	int MAJOR_VERSION_1_6 = 50;
	int MAJOR_VERSION_1_7 = 51;
	int MAJOR_VERSION_1_8 = 52;
	int MAJOR_VERSION_1_9 = 53; // This might change

	int MINOR_VERSION_0 = 0;
	int MINOR_VERSION_1 = 1;
	int MINOR_VERSION_2 = 2;
	int MINOR_VERSION_3 = 3;
	int MINOR_VERSION_4 = 4;

	// JDK 1.1 -> 1.9, comparable value allowing to check both major/minor version at once 1.4.1 > 1.4.0
	// 16 unsigned bits for major, then 16 bits for minor
	long JDK1_1 = ((long)ClassFileConstants.MAJOR_VERSION_1_1 << 16) + ClassFileConstants.MINOR_VERSION_3; // 1.1. is 45.3
	long JDK1_2 =  ((long)ClassFileConstants.MAJOR_VERSION_1_2 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_3 =  ((long)ClassFileConstants.MAJOR_VERSION_1_3 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_4 = ((long)ClassFileConstants.MAJOR_VERSION_1_4 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_5 = ((long)ClassFileConstants.MAJOR_VERSION_1_5 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_6 = ((long)ClassFileConstants.MAJOR_VERSION_1_6 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_7 = ((long)ClassFileConstants.MAJOR_VERSION_1_7 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_8 = ((long)ClassFileConstants.MAJOR_VERSION_1_8 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_9 = ((long)ClassFileConstants.MAJOR_VERSION_1_9 << 16) + ClassFileConstants.MINOR_VERSION_0;

	/*
	 * cldc1.1 is 45.3, but we modify it to be different from JDK1_1.
	 * In the code gen, we will generate the same target value as JDK1_1
	 */
	long CLDC_1_1 = ((long)ClassFileConstants.MAJOR_VERSION_1_1 << 16) + ClassFileConstants.MINOR_VERSION_4;

	// jdk level used to denote future releases: optional behavior is not enabled for now, but may become so. In order to enable these,
	// search for references to this constant, and change it to one of the official JDT constants above.
	long JDK_DEFERRED = Long.MAX_VALUE;

	int INT_ARRAY = 10;
	int BYTE_ARRAY = 8;
	int BOOLEAN_ARRAY = 4;
	int SHORT_ARRAY = 9;
	int CHAR_ARRAY = 5;
	int LONG_ARRAY = 11;
	int FLOAT_ARRAY = 6;
	int DOUBLE_ARRAY = 7;

	// Debug attributes
	int ATTR_SOURCE = 0x1; // SourceFileAttribute
	int ATTR_LINES = 0x2; // LineNumberAttribute
	int ATTR_VARS = 0x4; // LocalVariableTableAttribute
	int ATTR_STACK_MAP_TABLE = 0x8; // Stack map table attribute
	int ATTR_STACK_MAP = 0x10; // Stack map attribute: cldc
	int ATTR_TYPE_ANNOTATION = 0x20; // type annotation attribute (jsr 308)
	int ATTR_METHOD_PARAMETERS = 0x40; // method parameters attribute (jep 118)

	// See java.lang.invoke.LambdaMetafactory constants - option bitflags when calling altMetaFactory()
	int FLAG_SERIALIZABLE = 0x01;
	int FLAG_MARKERS = 0x02;
	int FLAG_BRIDGES = 0x04;
}
