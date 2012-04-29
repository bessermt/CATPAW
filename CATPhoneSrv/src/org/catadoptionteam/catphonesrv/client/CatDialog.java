/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import org.catadoptionteam.catphonesrv.shared.Cat;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author bessermt
 *
 */
public final class CatDialog extends DialogBox implements ClickHandler
{
	private CatEditor catEditor_;

	/**
	 * 
	 */
//	private CatDialog()
//	{
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param autoHide
	 */
//	private CatDialog(boolean autoHide)
//	{
//		super(autoHide);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param captionWidget
	 */
//	private CatDialog(Caption captionWidget)
//	{
//		super(captionWidget);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @param autoHide
	 * @param modal
	 */
	private CatDialog(boolean autoHide, boolean modal)
	{
		super(autoHide, modal);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param autoHide
	 * @param modal
	 * @param captionWidget
	 */
//	private CatDialog(boolean autoHide, boolean modal, Caption captionWidget)
//	{
//		super(autoHide, modal, captionWidget);
//		// TODO Auto-generated constructor stub
//	}

	public CatDialog(final String title, final Cat cat)
	{
		this(true, true);

		// setAnimationEnabled(true);

		setText(title);

		final VerticalPanel dialogDataPanel = new VerticalPanel();

		catEditor_ = new CatEditor(cat);

		dialogDataPanel.add(catEditor_);

		// DialogBox is a SimplePanel, so you have to set it's widget property to
		// whatever you want its contents to be.
		setWidget(dialogDataPanel);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	@Override
	public void onClick(ClickEvent event)
	{
		hide();
		// TODO Auto-generated method stub
	}

	public Cat getCat()
	{
		final Cat result = catEditor_.getCat();
		return result;
	}

	public Button getOKButton()
	{
		final Button result = catEditor_.getOKButton();
		return result;
	}

	public Button getCancelButton()
	{
		final Button result = catEditor_.getCancelButton();
		return result;
	}

}
