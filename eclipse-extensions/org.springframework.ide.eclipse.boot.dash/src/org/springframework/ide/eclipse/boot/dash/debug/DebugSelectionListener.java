//package org.springframework.ide.eclipse.boot.dash.debug;
//
//import org.eclipse.jface.text.ITextSelection;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.ui.ISelectionListener;
//import org.eclipse.ui.ISelectionService;
//import org.eclipse.ui.IWorkbenchPart;
//import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
//
//public class DebugSelectionListener implements ISelectionListener, Disposable {
//
//	private ISelectionService selectionService;
//
//	public DebugSelectionListener(ISelectionService selectionService) {
//		this.selectionService = selectionService;
//		selectionService.addSelectionListener(this);
//	}
//
//	@Override
//	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
//		showSelection(selection);
//	}
//
//	private void showSelection(ISelection selection) {
//		if (selection instanceof IStructuredSelection) {
//			showSelection((IStructuredSelection) selection);
//		} else if (selection instanceof ITextSelection) {
//			showSelection((ITextSelection) selection);
//		}
//	}
//
//	private void showSelection(ITextSelection selection) {
//		System.out.println(">>> TextSelection:");
//		System.out.println(selection.getText());
//		System.out.println("<<< TextSelection");
//	}
//
//	private void showSelection(IStructuredSelection selection) {
//		System.out.println(">>> StructuredSelection:");
//		for (Object e : selection.toArray()) {
//			System.out.println(e);
//		}
//		System.out.println("<<< StructuredSelection");
//	}
//
//	@Override
//	public void dispose() {
//		selectionService.removeSelectionListener(this);
//	}
//
//}
