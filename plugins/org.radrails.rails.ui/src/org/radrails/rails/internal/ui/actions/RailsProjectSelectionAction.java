/**
 * This file Copyright (c) 2005-2007 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.actions;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.radrails.rails.internal.core.RailsPlugin;
import org.radrails.rails.internal.ui.wizards.NewRailsProjectWizardAction;
import org.radrails.rails.ui.RailsUIPlugin;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class RailsProjectSelectionAction extends Action implements IMenuCreator
{

	private Menu fMenu;
	private IRubyProjectListener listener;

	/**
	 * RailsProjectSelectionAction
	 */
	public RailsProjectSelectionAction()
	{
		this("Select Rails Project");
	}

	/**
	 * RailsProjectSelectionAction
	 * 
	 * @param tooltip
	 */
	public RailsProjectSelectionAction(String tooltip)
	{
		setEnabled(!RailsPlugin.getRailsProjects().isEmpty());
		setToolTipText(tooltip);
		setImageDescriptor(RailsUIPlugin.getImageDescriptor("icons/rails_project.png"));
		setMenuCreator(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener()
		{

			public void resourceChanged(IResourceChangeEvent event)
			{
				IResource source = event.getResource();
				if (source != null)
					return;
				IResourceDelta[] deltas = event.getDelta().getAffectedChildren(
						IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED, IResource.PROJECT);
				if (deltas != null && deltas.length > 0)
				{
					// project changed
					Display.getDefault().asyncExec(new Runnable()
					{

						public void run()
						{
							if (fMenu != null && !fMenu.isDisposed())
							{
								fMenu.dispose();
							}
							fMenu = null;
							setEnabled(!RailsPlugin.getRailsProjects().isEmpty());
						}
					});
				}
			}

		}, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent)
	{
		if (fMenu != null && !fMenu.isDisposed())
		{
			fMenu.dispose();
		}

		fMenu = new Menu(parent);
		int accel = 1;
		Set<IProject> projects = RailsPlugin.getRailsProjects();
		for (IProject project : projects)
		{
			String label = project.getName();
			ImageDescriptor image = null;
			addActionToMenu(fMenu, new RailsProjectAction(label, image, project), accel);
			accel++;
		}
		MenuItem addProjectItem = new MenuItem(fMenu, SWT.PUSH);
		addProjectItem.setText("Create new Rails project");
		addProjectItem.setImage(RailsUIPlugin.getImage("icons/newproj.gif"));
		addProjectItem.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				new NewRailsProjectWizardAction().run();
			}

		});
		return fMenu;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator)
	{
		if (accelerator < 10)
		{
			StringBuffer label = new StringBuffer();
			// add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}

		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent)
	{
		return null;
	}

	private class RailsProjectAction extends Action
	{
		private IProject project;

		/**
		 * RailsProjectAction
		 * 
		 * @param label
		 * @param image
		 * @param project
		 */
		public RailsProjectAction(String label, ImageDescriptor image, IProject project)
		{
			setText(label);
			if (image != null)
			{
				setImageDescriptor(image);
			}
			this.project = project;
		}

		/**
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run()
		{
			if (listener != null)
			{
				listener.projectSelected(project);
			}
		}

		/**
		 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
		 */
		public void runWithEvent(Event event)
		{
			run();
		}
	}

	/**
	 * @return the listener
	 */
	public IRubyProjectListener getListener()
	{
		return listener;
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(IRubyProjectListener listener)
	{
		this.listener = listener;
	}
}
