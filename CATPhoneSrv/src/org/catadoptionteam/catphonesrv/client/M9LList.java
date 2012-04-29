/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import org.catadoptionteam.catphonesrv.shared.Cat;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author bessermt
 *
 */
public class M9LList extends Composite implements HasText
{
	private static final String EDIT = "Edit";

	private static M9LListUiBinder uiBinder = GWT.create(M9LListUiBinder.class);

	private CatServiceAsync m9lSvc = GWT.create(CatService.class);

	interface M9LListUiBinder extends UiBinder<Widget, M9LList>
	{
	}

	private Cat[] data_ = new Cat[Cat.Felineality.values().length*2];

	@UiField 
	Grid table;

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
	public M9LList()
	{
		initWidget(uiBinder.createAndBindUi(this));

	    table.getRowFormatter().addStyleName(0, "listHeader");

		load();

		final int nRows = Cat.Felineality.values().length;

		table.resize(nRows + 1, 3);

		for (Cat.Felineality felineality: Cat.Felineality.values())
		{
			final int row = felineality.ordinal();

			final Button editLeft = new Button(EDIT);
			editLeft.addClickHandler
			(
				new ClickHandler()
				{
					public void onClick(ClickEvent clickEvent)
					{
						// TODO: How is it that I can use row here?  The thread is asynchronous and the value is out of scope by the time the thread gets here.  
						edit(row, 0);
					}
				}
			);
			table.setWidget(row + 1, 0, editLeft);

			final String felinealityStr = felineality.toString();
			table.setText(row + 1, 1, felinealityStr);

			final Button editRight = new Button(EDIT);
			editRight.addClickHandler
			(
				new ClickHandler()
				{
					public void onClick(ClickEvent clickEvent)
					{
						// TODO: How is it that I can use row here?  The thread is asynchronous and the value is out of scope by the time the thread gets here.  
						edit(row, 1);
					}
				}
			);
			table.setWidget(row + 1, 2, editRight);
		}
	}

//	public M9LList(String firstName)
//	{
//		this();
//
//		// Can access @UiField after calling createAndBindUi
//		button.setText(firstName);
//	}

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
				Window.alert("Error saving M9L Cat.");
			}

			@Override
			public void onSuccess(Long result)
			{
				cat.setID(result);
				Window.alert("M9L cat saved.");
			}
		};

		// Save the cat data to the server.
		m9lSvc.setM9LCat(cat, callback);
	}

	private void edit(final int row, final int col)
	{
		final int index = 2*row + col;
		final Cat cat = data_[index];
		

		// User edits the M9L cat.

		final CatDialog catDialog = new CatDialog("Edit M9L Cat", cat);

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
					cat.setCategory(Cat.CATEGORY_M9L);
					final Cat.Felineality felineality = Cat.Felineality.create(row);
					cat.setASPCAality(felineality);

					save(cat);
	
					data_[index] = cat;
	
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

	private void add(final Cat cat, final int side)
	{
		final int row = cat.getFelineality().ordinal();

		data_[2*row + side] = cat;
	}

	private void update(final Cat[] cats)
	{
		int i = data_.length;
		while (i != 0)
		{
			--i;
			data_[i] = new Cat();
		}

		int[] index = new int[Cat.Felineality.values().length];

		if (cats != null)
		{
			for (Cat cat: cats)
			{
				i = cat.getFelineality().ordinal();
				final int side = index[i] % 2;
				++index[i];
				add(cat, side);
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
				Window.alert("Error loading M9L.");
			}

			@Override
			public void onSuccess(Cat[] result)
			{
				update(result);
			}
		};

		// Read the cat data from the server.
		m9lSvc.getM9LCats(callback);
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
