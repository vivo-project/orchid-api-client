/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.orcidclient.testwebapp;

import static edu.cornell.mannlib.orcidclient.actions.ApiAction.READ_PROFILE;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.orcidclient.actions.ActionManager;
import edu.cornell.mannlib.orcidclient.auth.AuthorizationManager;
import edu.cornell.mannlib.orcidclient.auth.AuthorizationStatus;
import edu.cornell.mannlib.orcidclient.context.OrcidClientContext;

/**
 * TODO
 */
public abstract class OrcidActor {
	protected final HttpServletResponse resp;
	protected final PrintWriter out;
	protected final OrcidClientContext occ;
	protected final AuthorizationManager authManager;
	protected final ActionManager actionManager;

	protected OrcidActor(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		this.resp = resp;
		this.out = resp.getWriter();
		resp.setContentType("text/html");

		this.occ = OrcidClientContext.getInstance();
		this.authManager = occ.getAuthorizationManager(req);
		this.actionManager = occ.getActionManager(req);
	}

	public void exec() throws IOException {
		AuthorizationStatus auth = authManager
				.getAuthorizationStatus(READ_PROFILE);
		if (auth.isSeekingAuthorization()) {
			showAuthorizationPending(auth);
			authManager.clearStatus(auth.getAction());
		} else if (auth.isSeekingAccessToken()) {
			showAuthorizationPending(auth);
			authManager.clearStatus(auth.getAction());
		} else if (auth.isDenied()) {
			showAuthorizationDeclined();
			authManager.clearStatus(auth.getAction());
		} else if (auth.isFailure()) {
			showAuthorizationFailure(auth);
			authManager.clearStatus(auth.getAction());
		} else if (auth.isSuccess()) {
			performAction(auth);
		} else { // NONE
			seekAuthorization();
		}
	}

	protected abstract void performAction(AuthorizationStatus authStatus);

	protected abstract void seekAuthorization() throws IOException;

	protected void showAuthorizationPending(AuthorizationStatus authStatus) {
		out.println("<html><head></head><body>");
		out.println("<h1>Authorization Pending?</h1>");
		out.println("<pre>" + authStatus + "</pre>");
		out.println("</body></html>");
	}

	protected void showAuthorizationDeclined() {
		out.println("<html><head></head><body>");
		out.println("<h1>Authorization Declined</h1>");
		out.println("</body></html>");
	}

	protected void showAuthorizationFailure(AuthorizationStatus authStatus) {
		out.println("<html><head></head><body>");
		out.println("<h1>Authorization Failure</h1>");
		out.println(authStatus);
		out.println("</body></html>");
	}

	protected void showInternalError(Exception e) {
		out.println("<html><head></head><body>");
		out.println("<h1>Internal Error</h1>");
		out.println("<pre>");
		e.printStackTrace(out);
		out.println("</pre>");
		out.println("</body></html>");
	}
}
