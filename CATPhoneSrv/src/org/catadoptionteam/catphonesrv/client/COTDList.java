/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import java.util.ArrayList;
import java.util.Date;

import org.catadoptionteam.catphonesrv.shared.Cat;
import org.catadoptionteam.catphonesrv.shared.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author bessermt
 *
 */
public class COTDList extends Composite implements HasText
{
	private ArrayList<Cat> data_ = new ArrayList<Cat>();

	private static COTDListUiBinder uiBinder = GWT.create(COTDListUiBinder.class);

	private CatServiceAsync cotdSvc = GWT.create(CatService.class);

	interface COTDListUiBinder extends UiBinder<Widget, COTDList>
	{
	}

	/**
	 * Because this class has a default constructor, it can
	 * be used as a binder template. In other words, it can be used in other
	 * *.ui.xml files as follows:
	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 *   xmlns:g="urn:import:**user's package**">
	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
	 * </ui:UiBinder>
	 * Note that depending on the widget that is used, it may be necessary to
	 * implement HasHTML instead of HasText.
	 */
	public COTDList()
	{
		initWidget(uiBinder.createAndBindUi(this));
	    table.getRowFormatter().addStyleName(0, "listHeader");
		load();
	}

	@UiField 
	FlexTable table;

	@UiField 
	Button add;

//	public COTDList(String firstName)
//	{
//		this();
//
//		// Can access @UiField after calling createAndBindUi
//		button.setText(firstName);
//	}

	private void clear()
	{
		data_.clear();
		table.clear();
		table.setText(0, 0, "Date");
		table.setText(0, 1, "Name");
		table.setText(0, 2, "Edit");
	}

	private void save(final Cat cat)
	{
		// Initialize the service proxy.
		// if (catSvc == null)
		// {
		//	catSvc = GWT.create(CatService.class);
		// }

		// Set up the callback object.
		AsyncCallback<Long> callback = new AsyncCallback<Long>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				Window.alert("Error saving Cat of the Day.");
			}

			@Override
			public void onSuccess(Long result)
			{
				cat.setID(result);
				Window.alert("Cat of the Day saved.");
			}
		};

		// Save the cat data to the server.
		cotdSvc.setCOTDCat(cat, callback);
	}

	private void edit(final int index)
	{
		final Cat cat = data_.get(index);

		// User edits the Cat of the Day.

		final CatDialog catDialog = new CatDialog("Edit Cat of the Day", cat);

		final ClickHandler okClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				final Cat cat = catDialog.getCat();
				if (cat == null)
				{
					Window.alert("Invalid record, data not saved.");
				}
				else
				{
					cat.setCategory(Cat.CATEGORY_ANIMAL_OF_THE_DAY);

					save(cat);

					data_.set(index, cat);

					final String name = cat.getName();
					table.setText(index + 1, 1, name);

					catDialog.hide();
				}
			}
		};

		final ClickHandler cancelClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				catDialog.hide();
			}
		};

		final Button okButton = catDialog.getOKButton();
		okButton.addClickHandler(okClickHandler);

		final Button cancelButton = catDialog.getCancelButton();
		cancelButton.addClickHandler(cancelClickHandler);

		catDialog.show();
		catDialog.center();
	}

	private void add(final Cat cat)
	{
		final String expDate = cat.getExpDate();

		final String name = cat.getName();

		final int count = table.getRowCount();

		table.setText(count, 0, expDate);
		table.setText(count, 1, name);

		// Add a button to Edit this Animal in the table.
		final Button editButton = new Button("Edit");
		editButton.addClickHandler
		(
			new ClickHandler()
			{
				public void onClick(ClickEvent clickEvent)
				{
					// TODO: How is it that I can use count here?  The thread is asynchronous and the value is out of scope by the time the thread gets here.  
					final int index = count - 1;
					edit(index);
				}
			}
		);
		table.setWidget(count, 2, editButton);

		data_.add(cat);
	}

	private void add()
	{
		final Cat cat = new Cat();

		// Get the new cat data from the user.

		final CatDialog catDialog = new CatDialog("Add New Cat of the Day", cat); // TODO: Do I really need an empty Cat?

		final ClickHandler okClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				final Cat cat = catDialog.getCat();

				if (cat == null)
				{
					Window.alert("Invalid record, data not saved.");
				}
				else
				{
					Date nextDate;
					final int lastIndex = data_.size() - 1;
					if (lastIndex < 0)
					{
						final Date now = new Date();
						nextDate = new Date(now.getYear(), now.getMonth(), now.getDate()); // Deprecated, but I don't see a viable alternative.  
					}
					else
					{
						final Cat lastCat = data_.get(lastIndex);
						final String lastDateStr = lastCat.getExpDate();
						final Date lastDate = Util.parseDate(lastDateStr);
						final long nextDayMilsecs = lastDate.getTime() + Util.MILSEC_PER_DAY;
						nextDate = new Date(nextDayMilsecs);
					}
					final String nextDateStr = Util.toString(nextDate);
					cat.setExpDate(nextDateStr);
	
					save(cat);
	
					add(cat);

					catDialog.hide();
				}
			}
		};

		final ClickHandler cancelClickHandler = new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				catDialog.hide();
			}
		};

		final Button okButton = catDialog.getOKButton();
		okButton.addClickHandler(okClickHandler);

		final Button cancelButton = catDialog.getCancelButton();
		cancelButton.addClickHandler(cancelClickHandler);

		catDialog.show();
		catDialog.center();
	}

	private void update(final Cat[] cats)
	{
		clear();
		if (cats != null)
		{
			for (Cat cat: cats)
			{
				add(cat);
			}
		}
	}

	private void load()
	{
		// Initialize the service proxy.
		// if (catSvc == null)
		// {
		//	catSvc = GWT.create(CatService.class);
		// }

		// Set up the callback object.
		AsyncCallback<Cat[]> callback = new AsyncCallback<Cat[]>()
		{
			@Override
			public void onFailure(Throwable caught)
			{
				Window.alert("Error loading Cat of the Day.");
			}

			@Override
			public void onSuccess(Cat[] result)
			{
				update(result);
			}
		};

		// Read the cat data from the server.
		cotdSvc.getCOTDCats(callback);
	}

	@UiHandler("add")
	void onAddClick(ClickEvent e)
	{
		add();
	}

	@Override
	public void setText(String text)
	{
		// TODO Delete this and don't implement HasText
		// button.setText(text);
	}

	@Override
	public String getText()
	{
		// TODO Delete this and don't implement HasText
		return null;
//		return button.getText();
	}
}
