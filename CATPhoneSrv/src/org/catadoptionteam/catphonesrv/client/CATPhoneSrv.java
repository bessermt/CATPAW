package org.catadoptionteam.catphonesrv.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CATPhoneSrv implements EntryPoint
{
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
//	private static final String SERVER_ERROR = "An error occurred while "
//			+ "attempting to contact the server. Please check your network "
//			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
//	private final GreetingServiceAsync greetingService = GWT
//			.create(GreetingService.class);

	private COTDList cotdList_;
	private M9LList m9lList_;

	private LoginInfo loginInfo_;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("Please sign in to your Google Account to access the application.");
	private Anchor signInLink = new Anchor("Sign In");
//	private Anchor signOutLink = new Anchor("Sign Out");

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad()
	{
		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		final String returnCallbackURL = GWT.getHostPageBaseURL();
		loginService.login
		(
			returnCallbackURL, 
			new AsyncCallback<LoginInfo>()
			{
				public void onFailure(Throwable error)
				{
					Window.alert("Error loading Cat of the Day.");
				}

				public void onSuccess(LoginInfo result)
				{
					loginInfo_ = result;
					final boolean loggedIn = loginInfo_.isLoggedIn();
					if (loggedIn)
					{
						loadModule();
					}
					else
					{
						loadLogin();
					}
				}
			}
		);
	}

	private void loadLogin()
	{
		// Assemble login panel.
		signInLink.setHref(loginInfo_.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get("loginButtonContainer").add(loginPanel);
	}

	private void loadModule()
	{
		String logButtonText;
		ClickHandler logClickHandler;

		logButtonText = "Logout";
		logClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				final String logoutURL = loginInfo_.getLogoutUrl();
				Window.Location.replace(logoutURL);
			}
		};

		final Button logButton = new Button(logButtonText);
		logButton.addClickHandler(logClickHandler);

		// We can add style names to widgets
		logButton.addStyleName("loginButton");

		final Label errorLabel = new Label();

		// Use RootPanel.get() to get the entire body element
		
		RootPanel.get("loginButtonContainer").add(logButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Add the COTD List
		cotdList_ = new COTDList();
		RootPanel.get("cotdListContainer").add(cotdList_);

		// Add the M9L List
		m9lList_ = new M9LList();
		RootPanel.get("m9lListContainer").add(m9lList_);

		// Focus the cursor on the name field when the app loads
		logButton.setFocus(true);
	}
}
