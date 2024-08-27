package org.test;

public class PointcutExamples {
	
	@Pointcut("cflow(execution(* com.example..*.*(..)))")
	public void myPointcutFlow() {}
	
	@Pointcut("target(com.example.service.MyService)")
	public void targetService() {}
	
	@Before("myPointcutFlow()")
	public void beforeAdviceFlow() {
	    System.out.println("Before advice triggered by control flow.");
	}
	
	@AfterReturning(pointcut="targetService()", returning="result")
	public void afterReturningAdvice(Object result) {
	    System.out.println("After returning advice: " + result);
	}

	@Around("targetService()")
	public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
	    System.out.println("Around advice: before method execution");
	    Object result = joinPoint.proceed();
	    System.out.println("Around advice: after method execution");
	    return result;
	}

}
