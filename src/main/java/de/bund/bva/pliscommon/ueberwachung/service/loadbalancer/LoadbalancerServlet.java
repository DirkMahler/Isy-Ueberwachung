package de.bund.bva.pliscommon.ueberwachung.service.loadbalancer;

/*
 * #%L
 * Ueberwachungsschnittstelle fuer Services der PLIS.
 * %%
 * 
 * %%
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The Federal Office of Administration (Bundesverwaltungsamt, BVA)
 * licenses this file to you under the Apache License, Version 2.0 (the
 * License). You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet zur Steuerung des Loadbalancings einer Webanwendungen. Der
 * Loadbalancer kann die URL des Servlets regelmäßig abfragen. Das Servlet
 * liefert HTTP OK, falls die IsAlive-Datei gefunden wurde. Falls nicht wird
 * HTTP FORBIDDEN an den aufrufenden Loadbalancer gemeldet. Der Loadbalancer
 * verteilt dann keine Anfragen an die Webanwendung mehr. Der Pfad zur
 * Loadbalancer-Datei kann bei Bedarf über den Init-Parameter
 * {@link #PARAM_IS_ALIVE_FILE_LOCATION} angegeben werden. Ist der Parameter
 * nicht gesetzt, wird der Default-Wert {@link #DEFAULT_IS_ALIVE_FILE_LOCATION}
 * verwendet.
 * 
 * 
 */
public class LoadbalancerServlet extends HttpServlet {
    /** UID der Klasse. */
    private static final long serialVersionUID = 7248576003928677600L;

    /** Logger der Klasse. */
    private static final Logger LOG = Logger.getLogger(LoadbalancerServlet.class);

    /** Parametername fuer den Pfad zur IsAlive-Datei. */
    private static final String PARAM_IS_ALIVE_FILE_LOCATION = "isAliveFileLocation";

    /** Standard Ablageort der IsAlive-Datei. */
    private static final String DEFAULT_IS_ALIVE_FILE_LOCATION = "/WEB-INF/classes/config/isAlive";

    /**
     * File-Referenz für IsAlive-Datei.
     */
    private File isAliveFile;

    /**
     * Initialisiert das Servlet.
     */
    @Override
    public void init() {
        LOG.info("Initialisiere Loadbalancer-Servlet.");
        String isAliveFileLocation = getInitParameter(PARAM_IS_ALIVE_FILE_LOCATION);
        if (isAliveFileLocation == null) {
            LOG.debug("Position der IsAliveDatei nicht konfiguriert. Verwende Standard-Einstellung: "
                    + DEFAULT_IS_ALIVE_FILE_LOCATION);
            isAliveFileLocation = DEFAULT_IS_ALIVE_FILE_LOCATION;
        }
        String realIsAliveFilePath = getServletContext().getRealPath(isAliveFileLocation);
        isAliveFile = new File(realIsAliveFilePath);
        LOG.info("IsAlive-Datei " + isAliveFile.getAbsolutePath() + " konfiguriert.");
    }

    /**
     * @param req Der HttpServletRequest an das Loadbalancer-Servlet.
     * @param resp Die Antwort des Loadbalancer-Servlets.
     * 
     * @throws ServletException 
     * @throws IOException Wenn die Antwort nicht geschrieben werden kann.
     * GET-Request bearbeiten. Prüft, ob die IsAlive-Datei aus
     * {@link #PARAM_IS_ALIVE_FILE_LOCATION} vorhanden ist und liefert dann HTTP
     * OK zurück. Andernfalls wird HTTP FORBIDDEN zurückgeliefert.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isAliveFile.exists()) {
            LOG.debug("IsAlive-Datei gefunden, sende HTTP OK.");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("<html><body><center>IS ALIVE!</center></body></html>");
        } else {
            LOG.info("IsAlive-Datei " + isAliveFile.getAbsolutePath()
                    + " existiert nicht, sende HTTP FORBIDDEN.");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

}
