package com.googlecode.asyn4j.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * get class method tool
 * 
 * @author panxiuyan
 * 
 */
public class MethodUtil {

	/**
	 * 
	 * get target method
	 * 
	 * @param clazz
	 * @param pararm
	 * @param methodName
	 * @return
	 */
	public static Method getTargetMethod(Class clazz, Object[] pararm,
			String methodName) {
		List<Method> mList = new ArrayList<Method>();

		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				mList.add(method);
			}
		}

		if (mList.size() == 0)
			return null;

		if (mList.size() == 1)
			return mList.get(0);

		for (Method m : mList) {
			Class[] classes = m.getParameterTypes();
			if (classes.length == 0 && (pararm == null || pararm.length == 0))
				return m;
			if (pararm == null || pararm.length == 0) {
				return null;
			}
			if (classes.length != pararm.length) {
				continue;
			}
			for (int i = 0; i < classes.length; i++) {
				Class clzss = classes[i];
				Class paramClzss = pararm[i].getClass();
				if (!clzss.toString().equals(paramClzss.toString())) {
					break;
				}

			}
			return m;

		}

		return null;

	}

	/**
	 *get method key
	 * 
	 * @param clazz
	 * @param pararm
	 * @param methodName
	 * @return
	 */
	public static String getClassMethodKey(Class clazz, Object[] pararm,
			String methodName) {

		StringBuilder sb = new StringBuilder();
		sb.append(clazz.toString());
		sb.append(".").append(methodName);
		if (pararm != null && pararm.length > 0) {
			for (Object obj : pararm) {
				sb.append("-").append(obj.getClass().toString());
			}
		}
		return sb.toString();

	}

}
