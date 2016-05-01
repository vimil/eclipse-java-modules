package com.cwctravel.eclipse.plugins.javamodule;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.PlatformUI;

public class TeamModuleDecorator implements ILightweightLabelDecorator {
	private static final String REPOSITORY_ID_GIT = "org.eclipse.egit.core.GitProvider";
	private static final String REPOSITORY_ID_TFS = "com.microsoft.tfs.client.eclipse.TFSRepositoryProvider";

	private static class OverlayInfo {
		private ImageDescriptor overlay;
		private int quadrant;

		private OverlayInfo() {
			quadrant = -1;
		}

		public String toString() {
			return overlay.toString() + "-" + quadrant;
		}
	}

	private static class DecorationInterceptor implements IDecoration {
		private Map<String, OverlayInfo> overlayInfoMap;
		private Set<String> prefixes;
		private Set<String> suffixes;

		private DecorationInterceptor() {
			overlayInfoMap = new HashMap<String, OverlayInfo>();
			prefixes = new HashSet<String>();
			suffixes = new HashSet<String>();
		}

		@Override
		public void addPrefix(String prefix) {
			prefixes.add(prefix);
		}

		@Override
		public void addSuffix(String suffix) {
			suffixes.add(suffix);
		}

		@Override
		public void addOverlay(ImageDescriptor overlay) {
			OverlayInfo overlayInfo = new OverlayInfo();
			overlayInfo.overlay = overlay;
			overlayInfoMap.put(overlayInfo.toString(), overlayInfo);
		}

		@Override
		public void addOverlay(ImageDescriptor overlay, int quadrant) {
			OverlayInfo overlayInfo = new OverlayInfo();
			overlayInfo.overlay = overlay;
			overlayInfo.quadrant = quadrant;
			overlayInfoMap.put(overlayInfo.toString(), overlayInfo);
		}

		@Override
		public void setForegroundColor(Color color) {
		}

		@Override
		public void setBackgroundColor(Color color) {

		}

		@Override
		public void setFont(Font font) {

		}

		@Override
		public IDecorationContext getDecorationContext() {
			return null;
		}

	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof Module) {
			Module module = (Module) element;
			RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(module.getProject());
			if (repositoryProvider != null) {
				IBaseLabelProvider baseLabelProvider = null;

				String repositoryId = repositoryProvider.getID();
				if (repositoryId.equals(REPOSITORY_ID_TFS)) {
					baseLabelProvider = PlatformUI.getWorkbench().getDecoratorManager()
							.getBaseLabelProvider("com.microsoft.tfs.client.eclipse.ui.decorators.LabelDecorator");
				} else if (repositoryId.equals(REPOSITORY_ID_GIT)) {
					baseLabelProvider = PlatformUI.getWorkbench().getDecoratorManager()
							.getBaseLabelProvider("org.eclipse.egit.ui.internal.decorators.GitLightweightDecorator");
				}
				if (baseLabelProvider instanceof ILightweightLabelDecorator) {
					ILightweightLabelDecorator lightWeightLabelDecorator = (ILightweightLabelDecorator) baseLabelProvider;

					Object[] children = module.getChildren();
					if (children != null) {
						DecorationInterceptor decorationInterceptor = new DecorationInterceptor();
						for (Object child : children) {
							if (child instanceof IAdaptable) {
								IResource childResource = ((IAdaptable) child).getAdapter(IResource.class);
								lightWeightLabelDecorator.decorate(childResource, decorationInterceptor);
							}
						}
						List<OverlayInfo> overlayInfos = new ArrayList<OverlayInfo>(
								decorationInterceptor.overlayInfoMap.values());
						List<OverlayInfo> prioritizedOverlayInfos = prioritizeOverlayInfos(repositoryId, overlayInfos);
						if (!prioritizedOverlayInfos.isEmpty()) {
							for (OverlayInfo overlayInfo : prioritizedOverlayInfos) {
								if (overlayInfo.quadrant < 0) {
									decoration.addOverlay(overlayInfo.overlay);
								} else {
									decoration.addOverlay(overlayInfo.overlay, overlayInfo.quadrant);
								}
							}
						} else {
							ImageDescriptor versionedImageDescriptor = TeamImages
									.getImageDescriptor("ovr/version_controlled.gif");
							decoration.addOverlay(versionedImageDescriptor);
						}

						for (String prefix : decorationInterceptor.prefixes) {
							decoration.addPrefix(prefix);
						}

						for (String suffix : decorationInterceptor.suffixes) {
							decoration.addSuffix(suffix);
						}
					}
				}
			}
		}
	}

	private List<OverlayInfo> prioritizeOverlayInfos(String repositoryId, List<OverlayInfo> overlayInfos) {
		List<OverlayInfo> result = new ArrayList<OverlayInfo>();
		for (int i = 0; i < overlayInfos.size(); i++) {
			OverlayInfo overlayInfo = overlayInfos.get(i);
			boolean replaced = false;
			for (int j = 0; j < result.size(); j++) {
				OverlayInfo currentOverlayInfo = result.get(j);
				if (currentOverlayInfo.quadrant == overlayInfo.quadrant) {
					OverlayInfo replacedOverlayInfo = prioritizeOverlayInfo(repositoryId, overlayInfo,
							currentOverlayInfo);
					result.set(j, replacedOverlayInfo);
					replaced = true;
					break;
				}
			}
			if (!replaced) {
				result.add(overlayInfo);
			}
		}

		return result;
	}

	private OverlayInfo prioritizeOverlayInfo(String repositoryId, OverlayInfo overlayInfo1, OverlayInfo overlayInfo2) {
		ImageDescriptor id1 = overlayInfo1.overlay;
		ImageDescriptor id2 = overlayInfo2.overlay;
		String img1path = getImagePath(id1);
		String img2path = getImagePath(id2);
		if (REPOSITORY_ID_TFS.equals(repositoryId)) {

			if (img1path.endsWith("overlay_tfs.gif") && img2path.endsWith("overlay_edit.gif")) {
				return overlayInfo2;
			}
			if (img1path.endsWith("overlay_edit.gif") && img2path.endsWith("overlay_tfs.gif")) {
				return overlayInfo1;
			}
		} else if (REPOSITORY_ID_GIT.equals(repositoryId)) {
			if (img1path.endsWith("version_controlled.gif") && img2path.endsWith("staged.gif")) {
				return overlayInfo2;
			}
			if (img1path.endsWith("staged.gif") && img2path.endsWith("version_controlled.gif")) {
				return overlayInfo1;
			}
		}
		return overlayInfo2;
	}

	private String getImagePath(ImageDescriptor id) {
		String result = null;
		Field[] fields = id.getClass().getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				if (field.getName().equals("descriptor")) {
					field.setAccessible(true);
					try {
						ImageDescriptor nestedDescriptor = (ImageDescriptor) field.get(id);
						result = getImagePath(nestedDescriptor);
						if (result != null) {
							break;
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						JavaModulePlugin.log(IStatus.WARNING, e.getMessage(), e);
					}
				} else if (field.getName().equals("url")) {
					field.setAccessible(true);
					try {
						URL url = (URL) field.get(id);
						result = url.toString();
						break;
					} catch (IllegalArgumentException | IllegalAccessException e) {
						JavaModulePlugin.log(IStatus.WARNING, e.getMessage(), e);
					}
				}
			}
		}
		return result;
	}

}
