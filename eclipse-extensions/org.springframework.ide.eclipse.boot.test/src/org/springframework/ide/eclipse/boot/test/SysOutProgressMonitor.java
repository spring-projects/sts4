package org.springframework.ide.eclipse.boot.test;

import org.eclipse.core.runtime.IProgressMonitor;

public class SysOutProgressMonitor implements IProgressMonitor {

	private String task;

	@Override
	public void beginTask(String task, int arg1) {
		System.out.println("beginTask: "+task);
		this.task = task;
	}

	@Override
	public void done() {
		System.out.println("beginTask"+task);
		task = null;
	}

	@Override
	public void internalWorked(double arg0) {
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void setCanceled(boolean arg0) {
	}

	@Override
	public void setTaskName(String task) {
		System.out.println("taskName = "+task);
	}

	@Override
	public void subTask(String task) {
		System.out.println("subTask = "+task);
	}

	@Override
	public void worked(int arg0) {
		System.err.print(".");
	}

}
