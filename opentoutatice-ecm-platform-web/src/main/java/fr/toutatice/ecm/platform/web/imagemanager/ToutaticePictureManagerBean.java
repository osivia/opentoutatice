/// *
// * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Lesser General Public License
// * (LGPL) version 2.1 which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/lgpl-2.1.html
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// *
// * Contributors:
// * mberhaut1
// * dchevrier
// * lbillon
// *
// */
// package fr.toutatice.ecm.platform.web.imagemanager;
//
// import static org.jboss.seam.ScopeType.CONVERSATION;
//
// import javax.faces.context.FacesContext;
//
// import org.apache.commons.collections.CollectionUtils;
// import org.jboss.seam.annotations.Install;
// import org.jboss.seam.annotations.Name;
// import org.jboss.seam.annotations.Scope;
// import org.nuxeo.ecm.core.api.Blob;
// import org.nuxeo.ecm.core.api.NuxeoException;
// import org.nuxeo.ecm.core.api.DocumentException;
// import org.nuxeo.ecm.core.api.DocumentLocation;
// import org.nuxeo.ecm.core.api.DocumentModel;
// import org.nuxeo.ecm.core.api.DocumentModelList;
// import org.nuxeo.ecm.core.api.model.Property;
// import org.nuxeo.ecm.core.api.model.PropertyException;
// import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
// import org.nuxeo.ecm.platform.url.api.DocumentView;
// import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;
// import org.nuxeo.ecm.platform.util.RepositoryLocation;
//
// import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
// import fr.toutatice.ecm.platform.service.url.ToutaticeDocumentLocation;
// import fr.toutatice.ecm.platform.service.url.ToutaticeDocumentResolver;
// import fr.toutatice.ecm.platform.service.url.WebIdRef;
//
/// **
// * @author David Chevrier
// *
// */
// @Name("pictureManager")
// @Scope(CONVERSATION)
// @Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
// public class ToutaticePictureManagerBean extends PictureManagerBean {
//
// private static final long serialVersionUID = 7735094004834523711L;
//
// @Override
// public void download(DocumentView docView) throws NuxeoException {
// if (docView != null) {
// DocumentLocation docLoc = docView.getDocumentLocation();
//
// if(docLoc instanceof ToutaticeDocumentLocation){
// ToutaticeDocumentLocation ttcDocLoc = (ToutaticeDocumentLocation) docLoc;
// WebIdRef webIdRef = ttcDocLoc.getWebIdRef();
// if(webIdRef != null){
// try {
// downloadWebIdFile(docView, ttcDocLoc);
// } catch (DocumentException e) {
// throw new NuxeoException(e);
// }
// }
// }else{
// super.download(docView);
// }
// }
// }
//
// /* FIXME: fork */
// protected void downloadWebIdFile(DocumentView docView, ToutaticeDocumentLocation docLoc) throws PropertyException, NuxeoException, DocumentException {
// // fix for NXP-1799
// if (documentManager == null) {
// RepositoryLocation loc = new RepositoryLocation(docLoc.getServerName());
// navigationContext.setCurrentServerLocation(loc);
// documentManager = navigationContext.getOrCreateDocumentManager();
// }
// DocumentModelList docs = ToutaticeDocumentResolver.resolveReference(documentManager, docLoc.getWebIdRef());
// if (CollectionUtils.isNotEmpty(docs) && docs.size() > 0) {
// // FIXME: As we just want binary, we take arbitrary the first one
// DocumentModel doc = docs.get(0);
//
// String[] propertyPath = docView.getParameter(DocumentFileCodec.FILE_PROPERTY_PATH_KEY).split(":");
// String title = null;
// String field = null;
// Property datamodel = null;
// if (propertyPath.length == 2) {
// title = propertyPath[0];
// field = propertyPath[1];
// datamodel = doc.getProperty("picture:views");
// } else if (propertyPath.length == 3) {
// String schema = propertyPath[0];
// title = propertyPath[1];
// field = propertyPath[2];
// datamodel = doc.getProperty(schema + ":" + "views");
// }
// Property view = null;
// for (Property property : datamodel) {
// if (property.get("title").getValue().equals(title)) {
// view = property;
// }
// }
//
// if (view == null) {
// for (Property property : datamodel) {
// if (property.get("title").getValue().equals("Thumbnail")) {
// view = property;
// }
// }
// }
// if (view == null) {
// return;
// }
// Blob blob = (Blob) view.getValue(field);
// String filename = (String) view.getValue("filename");
// // download
// FacesContext context = FacesContext.getCurrentInstance();
//
// ComponentUtils.download(context, blob, filename);
// }
//
// }
// }
