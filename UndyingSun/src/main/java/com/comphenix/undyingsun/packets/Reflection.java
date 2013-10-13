/*
 *  UndyingSum - Bukkit server plugin that allows for decoupling the server and client clock.
 *  Copyright (C) 2013 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.undyingsun.packets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utility methods for retrieving method and fields.
 * @author Kristian
 */
class Reflection {
	/**
	 * Search for the first publically and privately defined method of the given name and parameter count.
	 * @param requireMod - modifiers that are required.
	 * @param bannedMod - modifiers that are banned.
	 * @param clazz - a class to start with.
	 * @param methodName - the method name, or NULL to skip.
	 * @param paramCount - the expected parameter count.
	 * @return The first method by this name.
	 * @throws IllegalStateException If we cannot find this method.
	 */
	public static Method getMethod(int requireMod, int bannedMod, Class<?> clazz, String methodName, Class<?>... params) {
		for (Method method : clazz.getDeclaredMethods()) {
			// Limitation: Doesn't handle overloads
			if ((method.getModifiers() & requireMod) == requireMod &&
				(method.getModifiers() & bannedMod) == 0 &&
				(methodName == null || method.getName().equals(methodName)) && 
				 Arrays.equals(method.getParameterTypes(), params)) {
				
				method.setAccessible(true);
				return method;
			}
		}
		// Search in every superclass
		if (clazz.getSuperclass() != null)
			return getMethod(requireMod, bannedMod, clazz.getSuperclass(), methodName, params);
		throw new IllegalStateException(String.format(
			"Unable to find method %s (%s).", methodName, Arrays.asList(params)));
	}
	
	/**
	 * Search for the first publically and privately defined field of the given name. 
	 * @param instance - an instance of the class with the field.
	 * @param clazz - an optional class to start with, or NULL to deduce it from instance.
	 * @param fieldName - the field name.
	 * @return The first field by this name.
	 * @throws IllegalStateException If we cannot find this field.
	 */
	public static Field getField(Object instance, Class<?> clazz, String fieldName) {
		if (clazz == null) 
			clazz = instance.getClass();
		// Ignore access rules
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				field.setAccessible(true);
				return field;
			}
		}
		// Recursively fild the correct field
		if (clazz.getSuperclass() != null)
			return getField(instance, clazz.getSuperclass(), fieldName);
		throw new IllegalStateException("Unable to find field " + fieldName + " in " + instance);
	}
}
