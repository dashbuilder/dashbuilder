// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.xml;

import org.hibernate.validator.metadata.AnnotationIgnores;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MetaConstraint;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.*;

/**
 * @author Hardy Ferentschik
 */
public class XmlMappingParser {

	private static final String VALIDATION_MAPPING_XSD = "META-INF/validation-mapping-1.0.xsd";
	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";
	private static final String PAYLOAD_PARAM = "payload";
	private static final String PACKAGE_SEPARATOR = ".";

	private final Set<Class<?>> processedClasses = new HashSet<Class<?>>();
	private final ConstraintHelper constraintHelper;
	private final AnnotationIgnores annotationIgnores;
	private final Map<Class<?>, List<MetaConstraint<?, ? extends Annotation>>> constraintMap;
	private final Map<Class<?>, List<Member>> cascadedMembers;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	public XmlMappingParser(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
		this.annotationIgnores = new AnnotationIgnores();
		this.constraintMap = new HashMap<Class<?>, List<MetaConstraint<?, ? extends Annotation>>>();
		this.cascadedMembers = new HashMap<Class<?>, List<Member>>();
		this.defaultSequences = new HashMap<Class<?>, List<Class<?>>>();
	}

	public void parse(Set<InputStream> mappingStreams) {
		
	}

	public Set<Class<?>> getXmlConfiguredClasses() {
		return processedClasses;
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return annotationIgnores;
	}

	public <T> List<MetaConstraint<T, ? extends Annotation>> getConstraintsForClass(Class<T> beanClass) {
		List<MetaConstraint<T, ? extends Annotation>> list = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
		if ( constraintMap.containsKey( beanClass ) ) {
			for ( MetaConstraint<?, ? extends Annotation> metaConstraint : constraintMap.get( beanClass ) ) {
				@SuppressWarnings("unchecked") // safe cast since the list of meta constraints is always specific to the bean type
						MetaConstraint<T, ? extends Annotation> boundMetaConstraint = ( MetaConstraint<T, ? extends Annotation> ) metaConstraint;
				list.add( boundMetaConstraint );
			}
			return list;
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<Member> getCascadedMembersForClass(Class<?> beanClass) {
		if ( cascadedMembers.containsKey( beanClass ) ) {
			return cascadedMembers.get( beanClass );
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<Class<?>> getDefaultSequenceForClass(Class<?> beanClass) {
		if ( defaultSequences.containsKey( beanClass ) ) {
			return defaultSequences.get( beanClass );
		}
		else {
			return Collections.emptyList();
		}
	}

	
}
