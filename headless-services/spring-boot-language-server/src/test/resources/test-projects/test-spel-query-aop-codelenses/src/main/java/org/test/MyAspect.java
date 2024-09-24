package org.test;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class MyAspect {

	@Pointcut("execution(* com.example.aopdemo..*(..))")
	public void myPointcut() {
		// Pointcut definition
	}

	@Before("myPointcut()")
	public void beforeAdvice() {
		System.out.println("Before advice executed");
	}

	@Around("execution(* com.example.aopdemo..*(..))")
	public Object aroundAdvice(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("Around advice: before method execution");
		Object result = joinPoint.proceed();
		System.out.println("Around advice: after method execution");
		return result;
	}

	@After("execution(* com.example.aopdemo..*(..))")
	public void afterAdvice() {
		System.out.println("After advice executed");
	}

	@AfterReturning(pointcut = "execution(* com.example.aopdemo..*(..))", returning = "result")
	public void afterReturningAdvice(Object result) {
		System.out.println("After returning advice executed, returned: " + result);
	}

	@AfterThrowing(pointcut = "execution(* com.example.aopdemo..*(..))", throwing = "ex")
	public void afterThrowingAdvice(Exception ex) {
		System.out.println("After throwing advice executed, exception: " + ex.getMessage());
	}

	@DeclareParents(value = "org.test..*", defaultImpl = Test.class)
	public static MyInterface mixin;
}

interface MyInterface {
	void someMethod();
}

class Test implements MyInterface {
	@Override
	public void someMethod() {
		System.out.println("Default implementation");
	}
}
