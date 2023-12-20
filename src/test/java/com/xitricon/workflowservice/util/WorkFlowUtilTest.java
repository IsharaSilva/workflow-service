package com.xitricon.workflowservice.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkFlowUtilTest {

	@Test
	void testPrivateConstructorInvocation() throws Exception {
		final Constructor<WorkflowUtil> constructor = WorkflowUtil.class.getDeclaredConstructor();

		// check that all constructors are 'private':
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));

		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			Assertions.fail("Constructor is not a privtae one");
		} catch (InvocationTargetException e) {
			assertTrue(e.getTargetException() instanceof IllegalStateException);
		}
	}

}
