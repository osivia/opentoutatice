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
// *
// */
// package fr.toutatice.ecm.platform.web.restapi.server.jaxrs;
//
// import static org.mockito.Matchers.anyString;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import javax.servlet.RequestDispatcher;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// import org.junit.Before;
// import org.junit.Test;
// import org.nuxeo.ecm.restapi.server.APIServlet;
//
// public class ToutaticeAPIRootTest {
//
// HttpServletRequest req = mock(HttpServletRequest.class);
// HttpServletResponse resp = mock(HttpServletResponse.class);
//
// @Before
// public void doBefore() {
// when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
// }
//
// @Test
// public void spaceAreEncodedInUrls() throws Exception {
//
// when(req.getPathInfo()).thenReturn("/toutatice/v1/web/note-sur-la-journee-du-12-fevrier-2016");
//
// APIServlet servlet = new APIServlet();
// servlet.service(req, resp);
//
// verify(req).getRequestDispatcher("/site/api/toutatice/v1/web/note-sur-la-journee-du-12-fevrier-2016");
//
// }
//
// // TODO: Use Nuxeo Test Features to unit test the ToutaticeAPIRoot class implementation.
//
// }
