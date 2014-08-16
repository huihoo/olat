/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.connectors.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.support.vo.AuthenticationVO;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the authentication management per user
 * <P>
 * Initial Date: 15 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class UserAuthenticationMgmtITCase extends OlatJerseyTestCase {

    @Autowired
    private BaseSecurity baseSecurity;

    @Test
    public void testGetAuthentications() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("/users/administrator/auth", MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        final List<AuthenticationVO> vos = parseAuthenticationArray(body);
        assertNotNull(vos);
        assertFalse(vos.isEmpty());
        method.releaseConnection();
    }

    @Test
    public void testCreateAuthentications() throws IOException {
        final Identity adminIdent = baseSecurity.findIdentityByName("administrator");
        try {
            final Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-API");
            if (refAuth != null) {
                baseSecurity.deleteAuthentication(refAuth);
            }
        } catch (final Exception e) {
            //
        }
        DBFactory.getInstance().commitAndCloseSession();

        final HttpClient c = loginWithCookie("administrator", "olat");

        final AuthenticationVO vo = new AuthenticationVO();
        vo.setAuthUsername("administrator");
        vo.setIdentityKey(adminIdent.getKey());
        vo.setProvider("REST-API");
        vo.setCredential("credentials");

        final String stringuifiedAuth = stringuified(vo);
        final PutMethod method = createPut("/users/administrator/auth", MediaType.APPLICATION_JSON, true);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(entity);

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);
        final String body = method.getResponseBodyAsString();
        final AuthenticationVO savedAuth = parse(body, AuthenticationVO.class);
        final Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-API");
        method.releaseConnection();

        assertNotNull(refAuth);
        assertNotNull(refAuth.getKey());
        assertTrue(refAuth.getKey().longValue() > 0);
        assertNotNull(savedAuth);
        assertNotNull(savedAuth.getKey());
        assertTrue(savedAuth.getKey().longValue() > 0);
        assertEquals(refAuth.getKey(), savedAuth.getKey());
        assertEquals(refAuth.getAuthusername(), savedAuth.getAuthUsername());
        assertEquals(refAuth.getIdentity().getKey(), savedAuth.getIdentityKey());
        assertEquals(refAuth.getProvider(), savedAuth.getProvider());
        assertEquals(refAuth.getCredential(), savedAuth.getCredential());
    }

    @Test
    public void testDeleteAuthentications() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an authentication token
        final Identity adminIdent = baseSecurity.findIdentityByName("administrator");
        final Authentication authentication = baseSecurity.createAndPersistAuthentication(adminIdent, "REST-A-2", "administrator", "credentials");
        assertTrue(authentication != null && authentication.getKey() != null && authentication.getKey().longValue() > 0);
        DBFactory.getInstance().intermediateCommit();

        // delete an authentication token
        final String request = "/users/administrator/auth/" + authentication.getKey().toString();
        final DeleteMethod method = createDelete(request, MediaType.APPLICATION_XML, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        method.releaseConnection();

        final Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-A-2");
        assertNull(refAuth);
    }

    private List<AuthenticationVO> parseAuthenticationArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<AuthenticationVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
